package io.github.apdelrahman1911.nativecomposekit

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.window.ComposeUIViewController
import io.github.apdelrahman1911.nativecomposekit.app.AppRoute
import io.github.apdelrahman1911.nativecomposekit.app.AppTab
import io.github.apdelrahman1911.nativecomposekit.app.appNavGraph
import io.github.apdelrahman1911.nativecomposekit.app.appRootRoute
import io.github.apdelrahman1911.nativecomposekit.app.appRouteTitle
import io.github.apdelrahman1911.nativecomposekit.app.LocalNativeContentBottomInset
import io.github.apdelrahman1911.nativecomposekit.app.configureCoilImageLoader
import io.github.apdelrahman1911.nativecomposekit.app.navigation.NativeNavChrome
import io.github.apdelrahman1911.nativecomposekit.app.navigation.NativeNavContent
import io.github.apdelrahman1911.nativecomposekit.app.navigation.NativeNavLog
import io.github.apdelrahman1911.nativecomposekit.app.navigation.createNativeNavigator
import io.github.apdelrahman1911.nativecomposekit.chrome.NativeChromeAction
import io.github.apdelrahman1911.nativecomposekit.chrome.NativeChromeSource
import io.github.apdelrahman1911.nativecomposekit.chrome.NativeChromeTab
import io.github.apdelrahman1911.nativecomposekit.components.NativeImeLog
import io.github.apdelrahman1911.nativecomposekit.components.feedback.NativeFeedbackHost
import io.github.apdelrahman1911.nativecomposekit.theme.NativeAppearanceScope
import platform.UIKit.UIViewController

/**
 * The pure-Compose iOS entry, exposed as `MainViewControllerKt.MainViewController()`. Renders the shared shell
 * ([App] = the sample's `NativeNavigator` + `NativeNavHost`, Material chrome) in ONE `ComposeUIViewController`.
 * Kept as a self-contained fallback; the production shell is [createNativeNavRoot] (native chrome over the same
 * renderer).
 */
fun MainViewController(): UIViewController {
    NativeNavLog.enabled = true
    NativeImeLog.enabled = true
    configureCoilImageLoader()
    return ComposeUIViewController { App() }
}

/**
 * Everything the Swift native-chrome shell needs, sharing ONE navigator: the [contentViewController] (a
 * `ComposeUIViewController` that renders ONLY the nav stack via `NativeNavContent`) and the [chrome] source that
 * drives the native `UINavigationBar` + `UITabBar`. Compose/`NativeNavigator` remains the sole stack owner; the
 * shell sees only the kit's nav-agnostic [NativeChromeSource] contract.
 */
class NativeNavRoot(
    val contentViewController: UIViewController,
    val chrome: NativeChromeSource,
)

/**
 * Build the iOS native-chrome shell's pieces — exposed to Swift as `MainViewControllerKt.createNativeNavRoot()`.
 * One navigator (source of truth) feeds both the content view controller and the chrome source; a real consumer
 * would swap this reference navigator for its own and adapt it into the same [NativeChromeSource] contract.
 */
fun createNativeNavRoot(): NativeNavRoot {
    NativeNavLog.enabled = true // demo: trace navigation (Xcode console tag "NCK-Nav")
    NativeImeLog.enabled = true // demo: trace keyboard-frame inset (Xcode console tag "NCK-Kbd")
    configureCoilImageLoader() // app-level image loader (Coil + Ktor/Darwin); the kit stays dependency-free

    val navigator = createNativeNavigator(
        tabs = AppTab.entries.toList(),
        initialTab = AppTab.Catalog,
        rootRoutes = ::appRootRoute,
    )
    val graph = appNavGraph(navigator)

    // iOS renders content only (renderSheet = false); the shell presents the sheet as a real native sheet.
    val contentViewController = ComposeUIViewController {
        NativeAppearanceScope {
            NativeFeedbackHost {
                // The native shell's UITabBar overlays the content; publish its height (surfaced as the content
                // VC's bottom safe-area inset) so scrollable screens end clear of the bar yet still render behind
                // it and scroll under the Liquid Glass. (The nav bar needs no inset — content is placed below it.)
                val bottomInset = WindowInsets.safeDrawing.asPaddingValues().calculateBottomPadding()
                CompositionLocalProvider(LocalNativeContentBottomInset provides bottomInset) {
                    NativeNavContent(navigator, graph, renderSheet = false)
                }
            }
        }
    }

    val chrome = NativeNavChrome(
        navigator = navigator,
        graph = graph,
        titleForRoute = ::appRouteTitle,
        tabs = listOf(
            NativeChromeTab(AppTab.Catalog.id, "Components", "square.grid.2x2"),
            NativeChromeTab(AppTab.Library.id, "Library", "books.vertical"),
            NativeChromeTab(AppTab.Settings.id, "Settings", "gearshape"),
        ),
        actionsForTab = { tabId ->
            // The Library tab's "+" presents the debug glass-interop stress test as a native sheet.
            if (tabId == AppTab.Library.id) listOf(NativeChromeAction("glass-interop", "plus")) else emptyList()
        },
        onAction = { id -> if (id == "glass-interop") navigator.presentSheet(AppRoute.GlassInteropTest) },
    )

    return NativeNavRoot(contentViewController = contentViewController, chrome = chrome)
}
