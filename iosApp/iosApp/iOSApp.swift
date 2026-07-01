import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    // One navigator (the Kotlin source of truth) feeds both the Compose content and the native chrome bridge.
    private let root = MainViewControllerKt.createNativeNavRoot()

    var body: some Scene {
        WindowGroup {
            // Real native chrome (UINavigationBar + UITabBar) as a DUMB projection of the Compose-owned stack:
            // it renders state and sends intents; it never owns or reconciles the navigation stack.
            // Edge-to-edge: the shell fills under the status bar + home indicator so Compose content renders
            // BEHIND the native nav/tab bars and scrolls under their Liquid Glass. The bars still sit at the
            // safe-area edges (via the shell's safeAreaLayoutGuide); content is inset below them in Compose.
            NativeNavShell(root: root).ignoresSafeArea()
        }
    }
}
