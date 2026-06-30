package com.ukkera.brandkit

import androidx.compose.ui.window.ComposeUIViewController
import com.ukkera.brandkit.app.AppRoute
import com.ukkera.brandkit.app.AppTab
import com.ukkera.brandkit.app.appNavGraph
import com.ukkera.brandkit.app.appRootRoute
import com.ukkera.brandkit.app.appRouteTitle
import com.ukkera.brandkit.app.configureCoilImageLoader
import com.ukkera.brandkit.navigation.BrandNavBridge
import com.ukkera.brandkit.navigation.createBrandNavigator
import platform.UIKit.UIViewController

/**
 * Exposed to Swift as `MainViewControllerKt.MainViewController()` — the **iOS-15 fallback** that renders the
 * shared Compose/Material shell ([App]) inside a single `ComposeUIViewController` (used by `ContentView`).
 */
fun MainViewController(): UIViewController {
    configureCoilImageLoader() // app-level image loader (Coil + Ktor/Darwin); the kit stays dependency-free
    return ComposeUIViewController { App() } // native shell chrome is themed by BrandAppearanceScope (iOS)
}

/**
 * Build the navigation bridge the SwiftUI shell (`BrandShell`) drives — exposed to Swift as
 * `MainViewControllerKt.createBrandNavBridge()`. Creates one shared [com.ukkera.brandkit.navigation.BrandNavigator]
 * (the source of truth) + the app graph; the shell holds the returned bridge for the app's lifetime.
 */
fun createBrandNavBridge(): BrandNavBridge {
    configureCoilImageLoader() // app-level image loader (Coil + Ktor/Darwin); the kit stays dependency-free
    val navigator = createBrandNavigator(
        tabs = AppTab.entries.toList(),
        initialTab = AppTab.Library,
        rootRoutes = ::appRootRoute,
    )
    return BrandNavBridge(
        navigator = navigator,
        graph = appNavGraph(navigator),
        routeForId = { id -> if (id == AppRoute.GlassInteropTest.id) AppRoute.GlassInteropTest else null },
        titleForRoute = ::appRouteTitle,
    )
}
