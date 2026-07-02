import SwiftUI
import UIKit
import ComposeApp

/// A **dumb native chrome shell**: a real `UINavigationBar` (top) + the Compose content view controller (middle,
/// which renders the nav stack) + a real `UITabBar` (bottom). It renders `NativeNavChrome` state one-way and
/// forwards taps as intents (`backRequested`/`tabSelected`/`actionTapped`). It owns NO navigation stack — the
/// Kotlin `NativeNavigator` is the sole owner; nothing here reads, mirrors, or reconciles the stack.
final class NativeShellViewController: UIViewController, UITabBarDelegate, UINavigationBarDelegate {
    private let root: NativeNavRoot
    private let content: UIViewController
    private let navBar = UINavigationBar()
    private let tabBar = UITabBar()
    private var cancellable: NativeChromeCancellable?
    private var tabItemsById: [String: UITabBarItem] = [:]
    private var renderedTabs: [NativeChromeTab] = []
    private var actionIdByTag: [Int: String] = [:]
    private var presentedSheet: UIViewController?
    private var presentedSheetId: String?

    init(root: NativeNavRoot) {
        self.root = root
        self.content = root.contentViewController
        super.init(nibName: nil, bundle: nil)
    }
    required init?(coder: NSCoder) { fatalError("init(coder:) not used") }

    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = UIColor { traits in
            NativeShellChromeKt.nativeBackgroundUIColor(dark: traits.userInterfaceStyle == .dark)
        }

        addChild(content)
        content.view.translatesAutoresizingMaskIntoConstraints = false
        navBar.translatesAutoresizingMaskIntoConstraints = false
        tabBar.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(content.view)
        view.addSubview(navBar)
        view.addSubview(tabBar)
        content.didMove(toParent: self)
        tabBar.delegate = self
        navBar.delegate = self
        configureBarAppearance()

        let safe = view.safeAreaLayoutGuide
        NSLayoutConstraint.activate([
            navBar.topAnchor.constraint(equalTo: safe.topAnchor),
            navBar.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            navBar.trailingAnchor.constraint(equalTo: view.trailingAnchor),

            tabBar.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            tabBar.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            tabBar.bottomAnchor.constraint(equalTo: view.bottomAnchor),

            // Content starts BELOW the nav bar (a normal top bar — no scroll-under there) but fills to the very
            // bottom, BEHIND the tab bar (added above it in the z-order). The bottom safe-area inset (set in
            // viewDidLayoutSubviews) makes content end clear of the tab bar while the scroll still renders behind
            // it → content scrolls under the tab bar's Liquid Glass.
            content.view.topAnchor.constraint(equalTo: navBar.bottomAnchor),
            content.view.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            content.view.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            content.view.bottomAnchor.constraint(equalTo: view.bottomAnchor),
        ])

        render(root.chrome.currentState())
        cancellable = root.chrome.observe { [weak self] state in
            DispatchQueue.main.async { self?.render(state) }
        }
    }

    // The tab bar overlays the content (which fills to the bottom of the view). Inset the hosted Compose content
    // by how far the tab bar rises above the home-indicator safe area, so its scrollable content ends clear of
    // the bar yet still renders BEHIND it and scrolls under the Liquid Glass. additionalSafeAreaInsets flows into
    // Compose's WindowInsets.safeDrawing. (The nav bar needs no inset — content is constrained below it.)
    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        let bottom = max(0, tabBar.frame.height - view.safeAreaInsets.bottom)
        let insets = UIEdgeInsets(top: 0, left: 0, bottom: bottom, right: 0)
        if content.additionalSafeAreaInsets != insets {
            content.additionalSafeAreaInsets = insets
        }
    }

    deinit { cancellable?.cancel() }

    /// Native, dark-mode-adaptive bar backgrounds + label colors. Applied to every appearance slot so the bars
    /// look right in light and dark (the app drives dark mode; the bars follow via the trait environment).
    private func configureBarAppearance() {
        let nav = UINavigationBarAppearance()
        nav.configureWithDefaultBackground()
        nav.titleTextAttributes = [.foregroundColor: UIColor.label]
        nav.largeTitleTextAttributes = [.foregroundColor: UIColor.label]
        navBar.standardAppearance = nav
        navBar.compactAppearance = nav
        navBar.scrollEdgeAppearance = nav
        if #available(iOS 15.0, *) { navBar.compactScrollEdgeAppearance = nav }

        let tab = UITabBarAppearance()
        tab.configureWithDefaultBackground()
        tabBar.standardAppearance = tab
        if #available(iOS 15.0, *) { tabBar.scrollEdgeAppearance = tab }
    }

    /// One-way: render the chrome projection onto the native bars. Never reads native state back.
    private func render(_ state: NativeChromeState) {
        // Top bar: a large title at a tab root, an inline title + native back button (labelled with the previous
        // screen) once pushed. The back button is derived by UIKit from a second item placed below the current
        // one; tapping it routes to `backRequested()` via the UINavigationBarDelegate. Actions map to right items.
        let item = UINavigationItem(title: state.title)
        actionIdByTag.removeAll()
        item.rightBarButtonItems = state.actions.enumerated().map { (i, action) in
            let b = UIBarButtonItem(
                image: UIImage(systemName: action.sfSymbol),
                style: .plain, target: self, action: #selector(actionTapped(_:)))
            b.tag = i
            actionIdByTag[i] = action.id
            return b
        }
        if state.canGoBack {
            // A second item below the current one gives UIKit the real native back button — on iOS 26 the Liquid
            // Glass chevron, with the previous title (from backTitle) where the system shows a label.
            let previous = UINavigationItem(title: state.backTitle ?? "Back")
            navBar.setItems([previous, item], animated: false)
        } else {
            navBar.setItems([item], animated: false)
        }

        // Bottom bar: RECONCILE the items against every emission — tabs carry localized titles and can be
        // conditional, so building them once would let the native bar drift from the Kotlin projection
        // (the exact "shell-side stale state" this contract forbids). NativeChromeTab compares by value.
        let tabsChanged = tabBar.items == nil
            || renderedTabs.count != state.tabs.count
            || zip(renderedTabs, state.tabs).contains(where: { !$0.isEqual($1) })
        if tabsChanged {
            tabItemsById.removeAll()
            var built: [UITabBarItem] = []
            for (i, t) in state.tabs.enumerated() {
                let ti = UITabBarItem(title: t.title, image: UIImage(systemName: t.sfSymbol), tag: i)
                tabItemsById[t.id] = ti
                built.append(ti)
            }
            renderedTabs = state.tabs
            tabBar.setItems(built, animated: false)
        }
        tabBar.selectedItem = tabItemsById[state.selectedTabId]

        syncSheet(state.sheetId)
    }

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

    // The native back button (rendered by UIKit from the item below the top one) reports back to Kotlin, which
    // owns the stack. Return false: the bar never pops its own items — the next state projection re-renders them.
    func navigationBar(_ navigationBar: UINavigationBar, shouldPop item: UINavigationItem) -> Bool {
        root.chrome.backRequested()
        return false
    }

    @objc private func actionTapped(_ sender: UIBarButtonItem) {
        if let id = actionIdByTag[sender.tag] { root.chrome.actionTapped(actionId: id) }
    }

    // A tab tap is an INTENT, not a stack mutation — the navigator decides.
    func tabBar(_ tabBar: UITabBar, didSelect item: UITabBarItem) {
        if let id = tabItemsById.first(where: { $0.value === item })?.key {
            root.chrome.tabSelected(tabId: id)
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

/// Hosts the native-chrome shell inside the SwiftUI `App`.
struct NativeNavShell: UIViewControllerRepresentable {
    let root: NativeNavRoot
    func makeUIViewController(context: Context) -> UIViewController { NativeShellViewController(root: root) }
    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
