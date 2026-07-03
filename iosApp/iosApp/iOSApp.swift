import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    // One navigator (the Kotlin source of truth) feeds the native chrome bridge.
    private let root = MainViewControllerKt.createNativeNavRoot()

    var body: some Scene {
        WindowGroup {
            // Real native navigation chrome (UITabBarController + per-tab UINavigationControllers, one
            // Compose screen per stack entry) as a RATIFIED PROJECTION of the Compose-owned stack: Kotlin
            // state is applied to the native containers, user pops that UIKit commits (the interactive
            // edge swipe, the back button) are reported back as idempotent intents, and the native side
            // never writes a stack into Kotlin. Edge-to-edge so content scrolls under the Liquid Glass
            // tab bar; the real containers provide every safe-area inset.
            NativeNavShell(root: root).ignoresSafeArea()
        }
    }
}
