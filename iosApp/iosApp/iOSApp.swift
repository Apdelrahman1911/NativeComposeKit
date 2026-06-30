import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    // The shared navigation source of truth (BrandNavigator) + app graph, built once.
    private let bridge = MainViewControllerKt.createBrandNavBridge()

    var body: some Scene {
        WindowGroup {
            // Production shell: native SwiftUI TabView + NavigationStack projecting the BrandNavigator SoT.
            // iOS 15 falls back to the plain Compose catalog (ContentView → MainViewController()).
            if #available(iOS 16.0, *) {
                BrandShell(bridge: bridge)
            } else {
                ContentView()
            }
        }
    }
}
