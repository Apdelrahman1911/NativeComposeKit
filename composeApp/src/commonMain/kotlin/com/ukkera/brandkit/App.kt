package com.ukkera.brandkit

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.ukkera.brandkit.app.AppRoute
import com.ukkera.brandkit.app.AppTab
import com.ukkera.brandkit.app.appNavGraph
import com.ukkera.brandkit.app.appRootRoute
import com.ukkera.brandkit.app.appRouteTitle
import com.ukkera.brandkit.components.feedback.BrandFeedbackHost
import com.ukkera.brandkit.navigation.BrandNavBarItem
import com.ukkera.brandkit.navigation.BrandNavHost
import com.ukkera.brandkit.navigation.rememberBrandNavigator
import com.ukkera.brandkit.theme.BrandAppearance
import com.ukkera.brandkit.theme.BrandAppearanceScope

/**
 * Shared Compose entry: the **Compose/Material navigation shell** (Tier-1 chrome on Android, and the iOS-15
 * fallback). It builds the [com.ukkera.brandkit.navigation.BrandNavigator] source of truth and renders it via
 * `BrandNavHost`. On iOS 16+ the production shell is native SwiftUI (`BrandShell`) driving the same navigator
 * through `BrandNavBridge` — `App()` is not used there.
 *
 * Appearance (dark/light + RTL) is process-wide via [BrandAppearance] / [BrandAppearanceScope], so toggling it
 * (from the Catalog tab) applies to the whole app and the native chrome.
 */
@Composable
fun App() {
    BrandAppearanceScope {
        BrandFeedbackHost {
            val navigator = rememberBrandNavigator(
                tabs = AppTab.entries.toList(),
                initialTab = AppTab.Library,
                rootRoutes = ::appRootRoute,
            )
            val graph = remember(navigator) { appNavGraph(navigator) }
            BrandNavHost(
                navigator = navigator,
                graph = graph,
                tabs = listOf(
                    BrandNavBarItem(AppTab.Library, "Library", Icons.AutoMirrored.Filled.List),
                    BrandNavBarItem(AppTab.Settings, "Settings", Icons.Filled.Settings),
                    BrandNavBarItem(AppTab.Catalog, "Catalog", Icons.Filled.GridView),
                ),
                title = ::appRouteTitle,
                actions = {
                    // The "+" on the Library tab presents the debug glass-interop stress test as a sheet.
                    if (navigator.state.selectedTab.id == AppTab.Library.id) {
                        IconButton(onClick = { navigator.presentSheet(AppRoute.GlassInteropTest) }) {
                            Icon(Icons.Filled.Add, contentDescription = "Glass interop test")
                        }
                    }
                },
            )
        }
    }
}
