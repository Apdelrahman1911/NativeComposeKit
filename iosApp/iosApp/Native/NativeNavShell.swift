import SwiftUI
import UIKit
import ComposeApp

/// Native navigation chrome as a **ratified projection** of the Kotlin `NativeNavigator` (the single source of
/// truth): a real `UITabBarController` + one `UINavigationController` per tab + one view controller per stack
/// entry. Kotlin state is APPLIED to this mirror by `sync()` — the only writer of the native stacks. UIKit is
/// allowed to run a user pop SPECULATIVELY on the mirror (its interactive edge swipe / back button — that is
/// what makes the bar title, back button, and content all track the finger), and a **committed** pop is then
/// ratified into Kotlin as one idempotent, tab-scoped intent (`backCommitted(tabId, entryId)` → the
/// navigator's `popTo`). A cancelled gesture restores the mirror and Kotlin never hears about it.
///
/// The mirror never writes stack STATE into Kotlin (the old `reconcileStack` failure mode): pops are the only
/// UIKit-initiated change and they arrive as intents naming the entry the user landed on; any other
/// divergence is resolved by re-asserting the projection. Tab selection is projection-first — `shouldSelect`
/// reports the intent and returns false, so UIKit never self-switches.
final class NativeShellViewController: UITabBarController, UINavigationControllerDelegate, UITabBarControllerDelegate {
    private let root: NativeNavRoot
    private var cancellable: NativeChromeCancellable?

    // Mirror bookkeeping. `expectedEntries` is what sync() last APPLIED per tab — updated in lockstep with the
    // mutation calls, never from delegate callbacks. A didShow that matches it is our own operation settling
    // (or a cancelled gesture); a strict-prefix mismatch is a user-committed pop; anything else re-asserts.
    private var navsByTab: [String: UINavigationController] = [:]
    private var tabOrder: [String] = []
    private var renderedTabs: [NativeChromeTab] = []
    private var hostsByEntry: [String: RouteHostController] = [:]
    private var expectedEntries: [String: [String]] = [:]
    private var isApplying = false
    private var syncScheduled = false
    private var actionIdByTag: [Int: String] = [:]

    private var presentedSheet: UIViewController?
    private var presentedSheetId: String?

    init(root: NativeNavRoot) {
        self.root = root
        super.init(nibName: nil, bundle: nil)
    }
    required init?(coder: NSCoder) { fatalError("init(coder:) not used") }

    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = UIColor { traits in
            NativeShellChromeKt.nativeBackgroundUIColor(dark: traits.userInterfaceStyle == .dark)
        }
        delegate = self
        configureTabBarAppearance()

        sync() // build the initial mirror synchronously — no empty first frame
        cancellable = root.chrome.observe { [weak self] _ in
            // Schedule-only: this may fire re-entrantly inside an intent (the navigator notifies inline).
            // All UIKit reads/writes happen level-triggered in sync(), against fresh state.
            DispatchQueue.main.async { self?.scheduleSync() }
        }
    }

    deinit { cancellable?.cancel() }

    private func scheduleSync() {
        guard !syncScheduled else { return }
        syncScheduled = true
        DispatchQueue.main.async { [weak self] in
            guard let self else { return }
            self.syncScheduled = false
            self.sync()
        }
    }

    /// Level-triggered reconciler: reads the CURRENT projection and converges the mirror to it. Every call is
    /// a fixpoint step — running it twice is a no-op — so ordering races between emissions, delegate
    /// callbacks, and the main queue cannot apply stale snapshots.
    private func sync() {
        let state = root.chrome.currentState()
        syncTabs(state)
        for tabId in tabOrder { syncStack(tabId: tabId, state: state) }
        syncSelection(state)
        syncSheet(state.sheetId)
        assertConverged(state)
    }

    /// Build/reconcile the per-tab `UINavigationController`s and their `UITabBarItem`s (value-compared, so
    /// localized titles can change at runtime without the bar drifting from the projection).
    private func syncTabs(_ state: NativeChromeState) {
        let tabs = state.tabs
        let unchanged = renderedTabs.count == tabs.count
            && zip(renderedTabs, tabs).allSatisfy { $0.isEqual($1) }
        if unchanged { return }

        var navs: [UINavigationController] = []
        var order: [String] = []
        for (i, tab) in tabs.enumerated() {
            let nav = navsByTab[tab.id] ?? {
                let nav = UINavigationController()
                nav.delegate = self
                nav.navigationBar.prefersLargeTitles = false
                configureNavBarAppearance(nav.navigationBar)
                // The transition container backdrop: visible in the gaps while views move. Themed so no
                // system default (black/grey) can peek through or be sampled mid-transition.
                nav.view.backgroundColor = UIColor { traits in
                    NativeShellChromeKt.nativeBackgroundUIColor(dark: traits.userInterfaceStyle == .dark)
                }
                return nav
            }()
            nav.tabBarItem = UITabBarItem(title: tab.title, image: UIImage(systemName: tab.sfSymbol), tag: i)
            navsByTab[tab.id] = nav
            navs.append(nav)
            order.append(tab.id)
        }
        tabOrder = order
        renderedTabs = tabs
        isApplying = true
        setViewControllers(navs, animated: false)
        isApplying = false
    }

    /// Converge one tab's native stack to the projected entries. Never touches a stack mid-transition —
    /// UIKit mutates `viewControllers` at interactive-gesture START (the popping entry is already absent),
    /// so a mid-transition diff is wrong and a mid-transition write wedges the controller. Defer + re-sync.
    private func syncStack(tabId: String, state: NativeChromeState) {
        guard let nav = navsByTab[tabId] else { return }
        if let coordinator = nav.transitionCoordinator {
            coordinator.animate(alongsideTransition: nil) { [weak self] _ in self?.scheduleSync() }
            return
        }

        let entries = (state.backStacksByTab[tabId] as? [NativeChromeEntry]) ?? []
        guard !entries.isEmpty else { return } // navigator invariant: every tab has a root

        var target: [RouteHostController] = []
        for entry in entries {
            // Entry ids are unique per STACK, not per app — the same route id may live on two tabs, and each
            // needs its own controller (one UIViewController can never sit in two navigation stacks).
            let hostKey = "\(tabId)|\(entry.id)"
            if let existing = hostsByEntry[hostKey] {
                existing.navigationItem.title = entry.title
                target.append(existing)
            } else {
                guard let content = root.chrome.contentViewController(entryId: entry.id) else {
                    NSLog("NCK-Shell: no content for entry '%@' (stale projection) — deferring tab '%@'", entry.id, tabId)
                    return
                }
                let host = RouteHostController(entryId: entry.id, title: entry.title, content: content)
                hostsByEntry[hostKey] = host
                target.append(host)
            }
        }

        let current = nav.viewControllers.compactMap { $0 as? RouteHostController }
        let currentIds = current.map(\.entryId)
        let targetIds = target.map(\.entryId)
        let visible = (selectedViewController === nav) && view.window != nil

        isApplying = true
        if targetIds != currentIds {
            if targetIds.count == currentIds.count + 1, Array(targetIds.prefix(currentIds.count)) == currentIds, visible {
                nav.pushViewController(target[target.count - 1], animated: true)
            } else if targetIds.count < currentIds.count, Array(currentIds.prefix(targetIds.count)) == targetIds {
                // One animated multi-pop covers back button echoes, popToRoot on a tab re-tap, and the
                // back-button long-press menu alike.
                nav.popToViewController(target[target.count - 1], animated: visible)
            } else {
                nav.setViewControllers(target, animated: false)
            }
        }
        expectedEntries[tabId] = targetIds // in lockstep with the mutation, never from didShow
        isApplying = false

        applyActions(state, to: target, tabId: tabId)
        purgeHosts(state)
    }

    /// Right-bar actions apply to EVERY entry of the selected tab (each `navigationItem` needs its own
    /// item instances): during an interactive pop UIKit cross-fades between the two entries' bar items, so
    /// the incoming entry's actions must already be correct mid-gesture. Items are rebuilt only when the
    /// actions VALUE changes — replacing a live item resets its glass backing, which then re-renders through
    /// its dark "not sampled yet" fallback for a frame (the plus-button flash).
    private func applyActions(_ state: NativeChromeState, to hosts: [RouteHostController], tabId: String) {
        guard tabId == state.selectedTabId else { return }
        actionIdByTag = Dictionary(uniqueKeysWithValues: state.actions.enumerated().map { ($0, $1.id) })
        for host in hosts {
            let unchanged = host.appliedActions.count == state.actions.count
                && zip(host.appliedActions, state.actions).allSatisfy { $0.isEqual($1) }
            if unchanged { continue }
            host.navigationItem.rightBarButtonItems = state.actions.enumerated().map { (i, action) in
                let b = UIBarButtonItem(
                    image: UIImage(systemName: action.sfSymbol),
                    style: .plain, target: self, action: #selector(actionTapped(_:)))
                b.tag = i
                return b
            }
            host.appliedActions = state.actions
        }
    }

    /// Drop hosts for entries no longer in their tab's projected stack (sync is deferred during transitions,
    /// so a host can never be purged while UIKit is still animating it).
    private func purgeHosts(_ state: NativeChromeState) {
        var live = Set<String>()
        for tabId in tabOrder {
            for entry in (state.backStacksByTab[tabId] as? [NativeChromeEntry]) ?? [] { live.insert("\(tabId)|\(entry.id)") }
        }
        for key in hostsByEntry.keys where !live.contains(key) { hostsByEntry.removeValue(forKey: key) }
    }

    private func syncSelection(_ state: NativeChromeState) {
        guard let nav = navsByTab[state.selectedTabId] else { return }
        if selectedViewController !== nav {
            isApplying = true
            selectedViewController = nav // programmatic selection fires no delegate callbacks
            isApplying = false
        }
    }

    /// Debug tripwire for the historical bug class: after every sync the mirror must equal the projection
    /// (unless a transition is mid-flight, in which case a re-sync is already scheduled).
    private func assertConverged(_ state: NativeChromeState) {
        #if DEBUG
        for tabId in tabOrder {
            guard let nav = navsByTab[tabId], nav.transitionCoordinator == nil else { continue }
            let actual = nav.viewControllers.compactMap { ($0 as? RouteHostController)?.entryId }
            let projected = ((state.backStacksByTab[tabId] as? [NativeChromeEntry]) ?? []).map(\.id)
            if actual != projected {
                NSLog("NCK-Shell: DESYNC tab '%@' — projected %@, native %@", tabId, projected.description, actual.description)
            }
        }
        #endif
    }

    // MARK: - UIKit → Kotlin (intents only, never stack writes)

    /// The single ratification point. `didShow` fires when a navigation transition SETTLES: after our own
    /// animated apply (mirror == expected → no-op), after a CANCELLED interactive pop (UIKit restored the
    /// stack → mirror == expected → no-op — Kotlin never learns the gesture existed), and after a user-
    /// committed pop (strict prefix → ratify the entry the user landed on). Anything else re-asserts.
    func navigationController(_ navigationController: UINavigationController, didShow viewController: UIViewController, animated: Bool) {
        guard !isApplying else { return }
        guard let tabId = navsByTab.first(where: { $0.value === navigationController })?.key,
              let expected = expectedEntries[tabId] else { return }
        let actual = navigationController.viewControllers.compactMap { ($0 as? RouteHostController)?.entryId }

        if actual == expected { return }
        if actual.count < expected.count, Array(expected.prefix(actual.count)) == actual, let top = actual.last {
            expectedEntries[tabId] = actual // the mirror already moved; remember it so a duplicate didShow no-ops
            root.chrome.backCommitted(tabId: tabId, entryId: top)
        } else {
            NSLog("NCK-Shell: unexpected native stack on tab '%@' (expected %@, actual %@) — re-asserting the projection",
                  tabId, expected.description, actual.description)
            scheduleSync()
        }
    }

    /// Tab taps are intents; UIKit never switches itself (returning false leaves the bar visually unchanged —
    /// selection is applied only by `syncSelection` from Kotlin state). A re-tap of the current tab lands here
    /// too and becomes the navigator's pop-to-root.
    func tabBarController(_ tabBarController: UITabBarController, shouldSelect viewController: UIViewController) -> Bool {
        if let tabId = navsByTab.first(where: { $0.value === viewController })?.key {
            root.chrome.tabSelected(tabId: tabId)
        }
        return false
    }

    @objc private func actionTapped(_ sender: UIBarButtonItem) {
        if let id = actionIdByTag[sender.tag] { root.chrome.actionTapped(actionId: id) }
    }

    // MARK: - Appearance

    /// OPAQUE, theme-colored bars — deliberately not the translucent default material. The bar's material
    /// samples whatever is composited behind it, and during a navigation transition that is UIKit's dimmed
    /// transition container: a translucent bar visibly darkens for the duration of every push/pop/interactive
    /// swipe (full-width grey band over the status+bar strip) and snaps back on settle. Content never scrolls
    /// under the top bar in this shell (screens are laid out below it), so translucency buys nothing here —
    /// opaque in the same background color renders identically at rest and stays rock-stable mid-gesture.
    private func configureNavBarAppearance(_ navBar: UINavigationBar) {
        let nav = UINavigationBarAppearance()
        nav.configureWithOpaqueBackground()
        nav.backgroundColor = UIColor { traits in
            NativeShellChromeKt.nativeBackgroundUIColor(dark: traits.userInterfaceStyle == .dark)
        }
        nav.shadowColor = .clear // the themed background is seamless with content; no hairline at rest today
        nav.titleTextAttributes = [.foregroundColor: UIColor.label]
        nav.largeTitleTextAttributes = [.foregroundColor: UIColor.label]
        navBar.standardAppearance = nav
        navBar.compactAppearance = nav
        navBar.scrollEdgeAppearance = nav
        if #available(iOS 15.0, *) { navBar.compactScrollEdgeAppearance = nav }
    }

    private func configureTabBarAppearance() {
        let tab = UITabBarAppearance()
        tab.configureWithDefaultBackground()
        tabBar.standardAppearance = tab
        if #available(iOS 15.0, *) { tabBar.scrollEdgeAppearance = tab }
    }

    // MARK: - Sheet

    /// Present / dismiss the sheet natively, mirroring the navigator's sheet STATE only. The content is Compose
    /// (supplied by the bridge); presentation + the interactive swipe-to-dismiss are native; the navigator owns
    /// the state. No stack is involved.
    ///
    /// UIKit silently DROPS a `present` issued while another presentation/dismissal is in flight (e.g. tap "+",
    /// swipe the sheet away, tap "+" again within the dismiss animation). Presenting optimistically there used
    /// to strand `presentedSheet` pointing at nothing and wedge the feature until relaunch — so this defers and
    /// re-syncs against the CURRENT chrome state once the transition (or attach-to-window) settles.
    private func syncSheet(_ sheetId: String?) {
        if let sheetId {
            if let current = presentedSheet {
                // Same sheet already up → nothing to do. A DIFFERENT id means the projection replaced the
                // sheet: dismiss the old one, then re-sync from the completion so UIKit never sees a
                // present-during-dismiss (tracking the id is what makes A→B switches possible at all).
                guard presentedSheetId != sheetId else { return }
                presentedSheet = nil
                presentedSheetId = nil
                current.dismiss(animated: true) { [weak self] in
                    guard let self else { return }
                    self.syncSheet(self.root.chrome.currentState().sheetId)
                }
                return
            }
            if let inFlight = presentedViewController {
                // Our own dismissal in flight: re-sync when its transition settles.
                if inFlight.isBeingDismissed {
                    if let coordinator = inFlight.transitionCoordinator {
                        coordinator.animate(alongsideTransition: nil) { [weak self] _ in
                            guard let self else { return }
                            self.syncSheet(self.root.chrome.currentState().sheetId)
                        }
                    } else {
                        DispatchQueue.main.async { [weak self] in
                            guard let self else { return }
                            self.syncSheet(self.root.chrome.currentState().sheetId)
                        }
                    }
                } else {
                    // A FOREIGN modal (e.g. a feedback alert) is up — not ours to fight, but without a
                    // retry the sheet would only appear after the next unrelated chrome emission. Poll
                    // shortly; each blocked attempt schedules exactly one retry, so this self-limits.
                    DispatchQueue.main.asyncAfter(deadline: .now() + 0.4) { [weak self] in
                        guard let self else { return }
                        self.syncSheet(self.root.chrome.currentState().sheetId)
                    }
                }
                return
            }
            // A present before the shell is in a window (launch-state sheet) is also dropped by UIKit.
            guard viewIfLoaded?.window != nil else {
                DispatchQueue.main.async { [weak self] in
                    guard let self else { return }
                    self.syncSheet(self.root.chrome.currentState().sheetId)
                }
                return
            }
            guard let sheet = root.chrome.sheetViewController() else { return }
            sheet.modalPresentationStyle = .pageSheet
            sheet.view.backgroundColor = .clear
            if let spc = sheet.sheetPresentationController {
                spc.detents = [.medium(), .large()]
                spc.prefersGrabberVisible = true
                spc.delegate = self
            }
            presentedSheet = sheet
            presentedSheetId = sheetId
            present(sheet, animated: true)
        } else if let sheet = presentedSheet {
            presentedSheet = nil
            presentedSheetId = nil
            sheet.dismiss(animated: true)
        }
    }
}

extension NativeShellViewController: UISheetPresentationControllerDelegate {
    // A native interactive dismissal (swipe / tap-outside) is reported back to the source of truth — the shell
    // never decides navigation, it only tells Kotlin the user closed the sheet. Idempotent.
    func presentationControllerDidDismiss(_ presentationController: UIPresentationController) {
        presentedSheet = nil
        presentedSheetId = nil
        root.chrome.dismissSheet()
    }
}

/// One stack entry: a thin UIKit identity — a stable `entryId`, its own `navigationItem`, and the extended-
/// layout policy — around the entry's own Compose host. Distinct instance per entry, never shared or
/// re-parented, which is what keeps every native stack state legal by construction (the old single-shared-
/// content-controller design is how `UINavigationController` corrupted itself).
final class RouteHostController: UIViewController {
    let entryId: String
    /// Last actions VALUE applied to this entry's `navigationItem` (see `applyActions` — items are only
    /// rebuilt on change so their glass backings are never reset mid-transition).
    var appliedActions: [NativeChromeAction] = []
    private let content: UIViewController

    init(entryId: String, title: String, content: UIViewController) {
        self.entryId = entryId
        self.content = content
        super.init(nibName: nil, bundle: nil)
        navigationItem.title = title
        // Below the top bar (a normal bar — content is not designed to scroll under it), but extended BEHIND
        // the Liquid Glass tab bar: UIKit then reports the overlap as the bottom safe-area inset, which flows
        // into Compose's WindowInsets.safeDrawing (the source of the app's LocalNativeContentBottomInset) —
        // scrollable screens end clear of the bar yet still render and scroll under its glass.
        edgesForExtendedLayout = [.bottom]
    }
    required init?(coder: NSCoder) { fatalError("init(coder:) not used") }

    override func viewDidLoad() {
        super.viewDidLoad()
        // Self-opaque: during a native push/pop this screen is all there is behind the finger.
        view.backgroundColor = UIColor { traits in
            NativeShellChromeKt.nativeBackgroundUIColor(dark: traits.userInterfaceStyle == .dark)
        }
        addChild(content)
        content.view.frame = view.bounds
        content.view.autoresizingMask = [.flexibleWidth, .flexibleHeight]
        view.addSubview(content.view)
        content.didMove(toParent: self)
    }

    #if DEBUG
    deinit { NSLog("NCK-Shell: released entry '%@'", entryId) }
    #endif
}

/// Hosts the native-chrome shell inside the SwiftUI `App`.
struct NativeNavShell: UIViewControllerRepresentable {
    let root: NativeNavRoot
    func makeUIViewController(context: Context) -> UIViewController { NativeShellViewController(root: root) }
    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
