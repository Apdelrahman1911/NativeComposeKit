import SwiftUI
import ComposeApp

/// Hosts a Kotlin-provided `ComposeUIViewController` (resolved from a route id by `NativeNavBridge`) inside
/// SwiftUI. The successor to the spike's `ComposeContainer`. [make] is invoked once per SwiftUI identity.
struct NativeComposeContainer: UIViewControllerRepresentable {
    let make: () -> UIViewController

    func makeUIViewController(context: Context) -> UIViewController { make() }
    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
