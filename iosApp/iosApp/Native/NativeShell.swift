import SwiftUI
import ComposeApp

/// The native background as a light/dark **dynamic** color, sourced from the Compose `AppTheme` (single source
/// of truth — `NativeShellChromeKt.nativeBackgroundUIColor`). Used to fill the one region the kit's window/bar
/// theming can't reach: the area around the iOS **floating tab bar** (a `TabView` layout region). The window
/// and the nav/tab bars themselves are themed automatically by the kit (`applyNativeShellChrome`).
let nativeShellBackground = Color(UIColor { traits in
    NativeShellChromeKt.nativeBackgroundUIColor(dark: traits.userInterfaceStyle == .dark)
})

/// The production iOS shell (successor to the throwaway `SpikeShell`): a native `TabView` + per-tab
/// `NavigationStack`, all **projecting** the Kotlin `NativeNavigator` source of truth via `NativeNavModel` /
/// `NativeNavBridge`. Tab selection, pushes (from Compose `navigator.push`), back-swipe/back-button, and the
/// sheet all flow through the shared SoT. iOS 16+ (NavigationStack); iOS 15 uses the `ContentView` fallback.
@available(iOS 16.0, *)
struct NativeShell: View {
    @StateObject private var model: NativeNavModel

    init(bridge: NativeNavBridge) {
        _model = StateObject(wrappedValue: NativeNavModel(bridge: bridge))
    }

    var body: some View {
        TabView(selection: Binding(
            get: { model.selectedTab },
            set: { model.userSelectedTab($0) }
        )) {
            ForEach(model.tabIds, id: \.self) { tabId in
                NativeTabStack(model: model, tabId: tabId)
                    .tabItem { Label(tabTitle(tabId), systemImage: tabIcon(tabId)) }
                    .tag(tabId)
            }
        }
        // Fill the area around the floating tab bar (and any bottom safe-area gap) with the native background
        // so it matches the content — the kit themes the window/bars, but this TabView region is SwiftUI's.
        .background(nativeShellBackground.ignoresSafeArea())
        // NOTE: the sheet (e.g. the Library "+" glass-interop test) is presented **natively by NativeNavBridge**
        // as a real UISheetPresentationController (the proven NativeSheet path), not a SwiftUI `.sheet` here —
        // a `.sheet` on a TabView whose trigger lives in a child NavigationStack is unreliable.
    }
}

@available(iOS 16.0, *)
private struct NativeTabStack: View {
    @ObservedObject var model: NativeNavModel
    let tabId: String

    var body: some View {
        NavigationStack(path: Binding(
            get: { model.pathByTab[tabId] ?? [] },
            set: { newTail in model.userChangedPath(tab: tabId, to: newTail) }
        )) {
            NativeComposeContainer { model.makeViewController(model.rootRouteId(tabId)) }
                .ignoresSafeArea(.keyboard)
                .ignoresSafeArea(.container, edges: .bottom) // native content fills behind the floating tab bar
                .navigationTitle(tabTitle(tabId))
                .navigationBarTitleDisplayMode(.inline)
                .navigationDestination(for: String.self) { routeId in
                    NativeComposeContainer { model.makeViewController(routeId) }
                        .ignoresSafeArea(.keyboard)
                        .ignoresSafeArea(.container, edges: .bottom) // native content fills behind the floating tab bar
                        .navigationTitle(model.title(routeId))
                        .navigationBarTitleDisplayMode(.inline)
                }
                .toolbar {
                    if tabId == "library" {
                        ToolbarItem(placement: .navigationBarTrailing) {
                            Button(action: { model.presentGlassTest() }) {
                                // Full 44pt HIG touch target with the ENTIRE frame tappable (a bare
                                // `Image` label only fires on the glyph, while iOS shows the press
                                // highlight over a larger area — so edge taps highlighted but didn't fire).
                                Image(systemName: "plus")
                                    .frame(minWidth: 44, minHeight: 44)
                                    .contentShape(Rectangle())
                            }
                        }
                    }
                }
        }
    }
}

// MARK: - Chrome titles/icons (app-specific, native side)

private func tabTitle(_ id: String) -> String {
    switch id {
    case "library": return "Library"
    case "settings": return "Settings"
    case "catalog": return "Catalog"
    default: return ""
    }
}

private func tabIcon(_ id: String) -> String {
    switch id {
    case "library": return "books.vertical"
    case "settings": return "gearshape"
    case "catalog": return "square.grid.2x2"
    default: return "circle"
    }
}
