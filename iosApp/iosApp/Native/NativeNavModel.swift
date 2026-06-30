import SwiftUI
import ComposeApp

/// The SwiftUI-side projection of the Kotlin `NativeNavigator` (the source of truth), driven through
/// `NativeNavBridge`. It mirrors the SoT into `@Published` state (SoT → SwiftUI via `observe`) and reports user
/// gestures back as intents (SwiftUI → SoT), guarded by `applyingFromKotlin` so a SoT-driven path update isn't
/// echoed back as a user change. State is mutated only on the main thread (the observe callback hops via
/// `DispatchQueue.main`).
final class NativeNavModel: ObservableObject {
    @Published var selectedTab: String
    @Published var pathByTab: [String: [String]]   // tabId -> tail route ids (root excluded; it's the stack's own root view)
    @Published var sheetId: String?

    let tabIds: [String]
    private let bridge: NativeNavBridge
    private var cancellable: NativeNavCancellable?
    private var applyingFromKotlin = false
    // When each tab's path last grew from a Kotlin-driven push. SwiftUI fires a stale `set` (its pre-push path)
    // during the push transition; a "pop" landing within this window of a push is that write-back, not a user back.
    private var lastPushAt: [String: Date] = [:]

    init(bridge: NativeNavBridge) {
        self.bridge = bridge
        let tabs = (bridge.tabIds() as? [String]) ?? []
        self.tabIds = tabs
        self.selectedTab = bridge.selectedTabId()
        self.sheetId = bridge.sheetId()
        var initial: [String: [String]] = [:]
        for t in tabs {
            initial[t] = Array((((bridge.stackIds(tabId: t)) as? [String]) ?? []).dropFirst())
        }
        self.pathByTab = initial
        self.cancellable = bridge.observe(onChange: { [weak self] _ in
            DispatchQueue.main.async { self?.refresh() }
        })
    }

    deinit { cancellable?.cancel() }

    /// SoT → SwiftUI: re-read the navigator and mirror into the published state (avoids bridging the Kotlin Map).
    private func refresh() {
        applyingFromKotlin = true
        // Only publish values that actually changed — replacing selectedTab / the whole pathByTab map on every
        // notification churns every tab's NavigationStack and can drop a freshly pushed destination.
        let newSelected = bridge.selectedTabId()
        if selectedTab != newSelected { selectedTab = newSelected }
        let newSheet = bridge.sheetId()
        if sheetId != newSheet { sheetId = newSheet }
        var next: [String: [String]] = [:]
        for t in tabIds {
            next[t] = Array((((bridge.stackIds(tabId: t)) as? [String]) ?? []).dropFirst())
        }
        if next != pathByTab {
            // Stamp tabs whose path just grew — that's a push driven by Kotlin. SwiftUI replays the pre-push
            // path back to us as a `set` during the push transition; the stamp lets userChangedPath tell that
            // stale write-back (lands within a beat) from a real user back (lands after they've seen the screen).
            for t in tabIds where (next[t] ?? []).count > (pathByTab[t] ?? []).count { lastPushAt[t] = Date() }
            print("NCK-Nav-iOS: refresh pathByTab \(pathByTab) -> \(next)"); pathByTab = next
        }
        // Hold the guard until SwiftUI has applied this path on the next runloop. SwiftUI emits transitional
        // `set` callbacks on the path binding during the push/pop animation; releasing the guard synchronously
        // here lets those stale callbacks be mistaken for user navigation and reconcile the stack backward —
        // popping the just-pushed screen and landing on an empty/wrong destination.
        DispatchQueue.main.async { self.applyingFromKotlin = false }
    }

    // SwiftUI → SoT (guarded so Kotlin-driven updates don't loop back as user changes).
    func userChangedPath(tab: String, to tail: [String]) {
        // Keep our published path consistent with SwiftUI's actual NavigationStack *immediately*. Otherwise it
        // lags a back-swipe until the async SoT refresh, and a quick pop-then-repush of the SAME route leaves the
        // binding holding the stale (still-pushed) value: the refresh then sees the SoT already equal to that
        // stale path and skips updating it, so the stack (popped, then re-pushed) and the binding stay desynced —
        // the blank/stuck screen with broken back.
        let current = pathByTab[tab] ?? []
        // A user can only POP the stack (back-swipe/button) — every push is Kotlin-driven. So a legitimate `set`
        // is `current` itself (no-op) or a strict prefix of it (popped from the top). Any other shape is a
        // spurious callback SwiftUI emits during keyboard/layout transitions; honoring it would reconcile the
        // stack to a wrong state (the unexpected pop while typing). Drop it.
        let isValidPop = tail.count <= current.count && Array(current.prefix(tail.count)) == tail
        if !isValidPop {
            print("NCK-Nav-iOS: ignore spurious set tab=\(tab) tail=\(tail) current=\(current) applyingFromKotlin=\(applyingFromKotlin)")
            return
        }
        // A pop landing within a beat of a Kotlin-driven push is SwiftUI replaying its pre-push path during the
        // push transition, not a user back — honoring it would reconcile the SoT backward and pop the screen we
        // just opened (the empty/stuck destination). A real back arrives well after the user has seen the screen.
        if tail.count < current.count, let pushedAt = lastPushAt[tab], Date().timeIntervalSince(pushedAt) < 0.6 {
            print("NCK-Nav-iOS: ignore stale pop tab=\(tab) tail=\(tail) current=\(current) age=\(Date().timeIntervalSince(pushedAt))s")
            return
        }
        print("NCK-Nav-iOS: userPath tab=\(tab) tail=\(tail) current=\(current) applyingFromKotlin=\(applyingFromKotlin)")
        if pathByTab[tab] != tail { pathByTab[tab] = tail }
        guard !applyingFromKotlin else { return }
        bridge.reconcileStack(tabId: tab, routeIds: [bridge.rootRouteId(tabId: tab)] + tail)
    }

    func userSelectedTab(_ tab: String) {
        guard !applyingFromKotlin else { return }
        bridge.selectTab(tabId: tab)
    }

    func userDismissedSheet() {
        guard !applyingFromKotlin else { return }
        bridge.dismissSheet()
    }

    func presentGlassTest() { bridge.presentSheet(routeId: "debug/glass-interop") }

    func makeViewController(_ routeId: String) -> UIViewController { bridge.viewController(forRouteId: routeId) }
    func rootRouteId(_ tab: String) -> String { bridge.rootRouteId(tabId: tab) }

    /// Chrome title for a pushed route, from the shared `appRouteTitle` (no Swift-side id parsing).
    func title(_ routeId: String) -> String { bridge.title(forRouteId: routeId) }
}
