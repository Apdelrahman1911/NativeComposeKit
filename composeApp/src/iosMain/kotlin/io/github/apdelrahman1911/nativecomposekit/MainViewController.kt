package io.github.apdelrahman1911.nativecomposekit

import androidx.compose.ui.window.ComposeUIViewController
import io.github.apdelrahman1911.nativecomposekit.app.AppRoute
import io.github.apdelrahman1911.nativecomposekit.app.AppTab
import io.github.apdelrahman1911.nativecomposekit.app.appNavGraph
import io.github.apdelrahman1911.nativecomposekit.app.appRootRoute
import io.github.apdelrahman1911.nativecomposekit.app.appRouteTitle
import io.github.apdelrahman1911.nativecomposekit.app.configureCoilImageLoader
import io.github.apdelrahman1911.nativecomposekit.navigation.NativeNavBridge
import io.github.apdelrahman1911.nativecomposekit.navigation.createNativeNavigator
import platform.UIKit.UIViewController

/**
 * Exposed to Swift as `MainViewControllerKt.MainViewController()` — the **iOS-15 fallback** that renders the
 * shared Compose/Material shell ([App]) inside a single `ComposeUIViewController` (used by `ContentView`).
 */
fun MainViewController(): UIViewController {
    configureCoilImageLoader() // app-level image loader (Coil + Ktor/Darwin); the kit stays dependency-free
    return ComposeUIViewController { App() } // native shell chrome is themed by NativeAppearanceScope (iOS)
}

/**
 * Build the navigation bridge the SwiftUI shell (`NativeShell`) drives — exposed to Swift as
 * `MainViewControllerKt.createNativeNavBridge()`. Creates one shared [io.github.apdelrahman1911.nativecomposekit.navigation.NativeNavigator]
 * (the source of truth) + the app graph; the shell holds the returned bridge for the app's lifetime.
 */
fun createNativeNavBridge(): NativeNavBridge {
    configureCoilImageLoader() // app-level image loader (Coil + Ktor/Darwin); the kit stays dependency-free
    val navigator = createNativeNavigator(
        tabs = AppTab.entries.toList(),
        initialTab = AppTab.Library,
        rootRoutes = ::appRootRoute,
    )
    return NativeNavBridge(
        navigator = navigator,
        graph = appNavGraph(navigator),
        routeForId = { id -> if (id == AppRoute.GlassInteropTest.id) AppRoute.GlassInteropTest else null },
        titleForRoute = ::appRouteTitle,
    )
}
