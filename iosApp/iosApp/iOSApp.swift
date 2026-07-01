import SwiftUI

@main
struct iOSApp: App {
    var body: some Scene {
        WindowGroup {
            // Navigation is owned entirely by Compose (NativeNavigator + NativeNavHost) inside one
            // ComposeUIViewController. Swift hosts that view controller and owns no navigation state — no
            // NavigationStack, no UINavigationController, nothing that could own or reconcile the stack.
            ContentView()
        }
    }
}
