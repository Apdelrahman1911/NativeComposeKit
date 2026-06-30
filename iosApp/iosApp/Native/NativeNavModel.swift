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
        selectedTab = bridge.selectedTabId()
        sheetId = bridge.sheetId()
        var next: [String: [String]] = [:]
        for t in tabIds {
            next[t] = Array((((bridge.stackIds(tabId: t)) as? [String]) ?? []).dropFirst())
        }
        pathByTab = next
        applyingFromKotlin = false
    }

    // SwiftUI → SoT (guarded so Kotlin-driven updates don't loop back as user changes).
    func userChangedPath(tab: String, to tail: [String]) {
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
