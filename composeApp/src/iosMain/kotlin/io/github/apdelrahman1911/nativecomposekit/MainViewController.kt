package io.github.apdelrahman1911.nativecomposekit

import androidx.compose.ui.window.ComposeUIViewController
import io.github.apdelrahman1911.nativecomposekit.app.configureCoilImageLoader
import io.github.apdelrahman1911.nativecomposekit.components.NativeImeLog
import io.github.apdelrahman1911.nativecomposekit.navigation.NativeNavLog
import platform.UIKit.UIViewController

/**
 * The single iOS entry point, exposed to Swift as `MainViewControllerKt.MainViewController()` and hosted by
 * `ContentView`. Renders the shared Compose navigation shell ([App] = `NativeNavigator` + `NativeNavHost`) inside
 * ONE `ComposeUIViewController`. Compose owns the entire navigation stack; Swift only hosts this view controller
 * and owns no navigation state — there is no SwiftUI/UIKit stack to fight the Kotlin source of truth.
 */
fun MainViewController(): UIViewController {
    NativeNavLog.enabled = true // demo: trace navigation (Xcode console tag "NCK-Nav")
    NativeImeLog.enabled = true // demo: trace keyboard-frame inset (Xcode console tag "NCK-Kbd")
    configureCoilImageLoader() // app-level image loader (Coil + Ktor/Darwin); the kit stays dependency-free
    return ComposeUIViewController { App() }
}
