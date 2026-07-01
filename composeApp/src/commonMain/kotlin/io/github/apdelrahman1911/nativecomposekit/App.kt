package io.github.apdelrahman1911.nativecomposekit

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.apdelrahman1911.nativecomposekit.app.AppRoute
import io.github.apdelrahman1911.nativecomposekit.app.AppTab
import io.github.apdelrahman1911.nativecomposekit.app.appNavGraph
import io.github.apdelrahman1911.nativecomposekit.app.appRootRoute
import io.github.apdelrahman1911.nativecomposekit.app.appRouteTitle
import io.github.apdelrahman1911.nativecomposekit.components.feedback.NativeFeedbackHost
import io.github.apdelrahman1911.nativecomposekit.navigation.NativeNavBarItem
import io.github.apdelrahman1911.nativecomposekit.navigation.NativeNavHost
import io.github.apdelrahman1911.nativecomposekit.navigation.rememberNativeNavigator
import io.github.apdelrahman1911.nativecomposekit.theme.NativeAppearance
import io.github.apdelrahman1911.nativecomposekit.theme.NativeAppearanceScope

/**
 * Shared Compose entry: the **Compose navigation shell** for BOTH platforms. It builds the
 * [io.github.apdelrahman1911.nativecomposekit.navigation.NativeNavigator] source of truth and renders it via
 * `NativeNavHost` inside a single host — an Android Activity, or one `ComposeUIViewController` on iOS
 * (`MainViewController()`). Compose owns the navigation stack outright: there is no native SwiftUI/UIKit
 * navigation container mirroring or reconciling it, so the stack has exactly one owner.
 *
 * Appearance (dark/light + RTL) is process-wide via [NativeAppearance] / [NativeAppearanceScope], so toggling it
 * (from the Catalog tab) applies to the whole app and its chrome.
 */
@Composable
fun App() {
    NativeAppearanceScope {
        NativeFeedbackHost {
            val navigator = rememberNativeNavigator(
                tabs = AppTab.entries.toList(),
                initialTab = AppTab.Catalog,
                rootRoutes = ::appRootRoute,
            )
            val graph = remember(navigator) { appNavGraph(navigator) }
            NativeNavHost(
                navigator = navigator,
                graph = graph,
                tabs = listOf(
                    NativeNavBarItem(AppTab.Catalog, "Components", Icons.Filled.GridView),
                    NativeNavBarItem(AppTab.Library, "Library", Icons.AutoMirrored.Filled.List),
                    NativeNavBarItem(AppTab.Settings, "Settings", Icons.Filled.Settings),
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
