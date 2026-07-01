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
            NativeNavShell(root: root).ignoresSafeArea(.keyboard)
        }
    }
}
