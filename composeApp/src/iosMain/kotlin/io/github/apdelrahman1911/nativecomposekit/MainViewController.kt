package io.github.apdelrahman1911.nativecomposekit

import androidx.compose.ui.window.ComposeUIViewController
import io.github.apdelrahman1911.nativecomposekit.app.AppRoute
import io.github.apdelrahman1911.nativecomposekit.app.AppTab
import io.github.apdelrahman1911.nativecomposekit.app.CHROME_DEMO_ACTION_ID
import io.github.apdelrahman1911.nativecomposekit.app.appBarConfig
import io.github.apdelrahman1911.nativecomposekit.app.appNavGraph
import io.github.apdelrahman1911.nativecomposekit.app.appRootRoute
import io.github.apdelrahman1911.nativecomposekit.app.appRouteTitle
import io.github.apdelrahman1911.nativecomposekit.app.configureCoilImageLoader
import io.github.apdelrahman1911.nativecomposekit.app.navigation.NativeNavChrome
import io.github.apdelrahman1911.nativecomposekit.app.navigation.NativeNavLog
import io.github.apdelrahman1911.nativecomposekit.app.navigation.createNativeNavigator
import io.github.apdelrahman1911.nativecomposekit.chrome.NativeChromeAction
import io.github.apdelrahman1911.nativecomposekit.chrome.NativeChromeSource
import io.github.apdelrahman1911.nativecomposekit.chrome.NativeChromeTab
import io.github.apdelrahman1911.nativecomposekit.components.NativeImeLog
import kotlin.native.Platform
import kotlinx.coroutines.launch
import platform.UIKit.UIViewController

/**
 * The pure-Compose iOS entry, exposed as `MainViewControllerKt.MainViewController()`. Renders the shared shell
 * ([App] = the sample's `NativeNavigator` + `NativeNavHost`, Material chrome) in ONE `ComposeUIViewController`.
 * Kept as a self-contained fallback; the production shell is [createNativeNavRoot] (native chrome over the same
 * renderer).
 */
@OptIn(kotlin.experimental.ExperimentalNativeApi::class)
fun MainViewController(): UIViewController {
    // Demo diagnostics — debug binaries only, never in release.
    NativeNavLog.enabled = Platform.isDebugBinary
    NativeImeLog.enabled = Platform.isDebugBinary
    io.github.apdelrahman1911.nativecomposekit.app.AppDevTools.enabled = Platform.isDebugBinary
    configureCoilImageLoader()
    return ComposeUIViewController { App() }
}

/**
 * Everything the Swift native-chrome shell needs: the [chrome] source, one navigator behind it (the Kotlin
 * source of truth). The shell renders navigation natively — a `UITabBarController` + per-tab
 * `UINavigationController`s, one Compose screen per stack entry via [NativeChromeSource.contentViewController]
 * — as a **ratified projection**: state and per-entry content flow out, user actions come back as intents,
 * and no native container ever writes a stack into Kotlin.
 */
class NativeNavRoot(
    val chrome: NativeChromeSource,
)

/**
 * Build the iOS native-chrome shell's pieces — exposed to Swift as `MainViewControllerKt.createNativeNavRoot()`.
 * One navigator (source of truth) feeds the chrome source; a real consumer would swap this reference navigator
 * for its own and adapt it into the same [NativeChromeSource] contract.
 */
@OptIn(kotlin.experimental.ExperimentalNativeApi::class)
fun createNativeNavRoot(): NativeNavRoot {
    // Demo diagnostics (nav tracing "NCK-Nav", keyboard-inset tracing "NCK-Kbd") — debug binaries only.
    NativeNavLog.enabled = Platform.isDebugBinary
    NativeImeLog.enabled = Platform.isDebugBinary
    io.github.apdelrahman1911.nativecomposekit.app.AppDevTools.enabled = Platform.isDebugBinary
    configureCoilImageLoader() // app-level image loader (Coil + Ktor/Darwin); the kit stays dependency-free

    val navigator = createNativeNavigator(
        tabs = AppTab.entries.toList(),
        initialTab = AppTab.Catalog,
        rootRoutes = ::appRootRoute,
    )
    val graph = appNavGraph(navigator)

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
            // Debug builds only: the Library "+" presents the glass-interop stress test as a native sheet.
            if (tabId == AppTab.Library.id && io.github.apdelrahman1911.nativecomposekit.app.AppDevTools.enabled) {
                listOf(NativeChromeAction("glass-interop", "plus"))
            } else {
                emptyList()
            }
        },
        onAction = { id ->
            when (id) {
                "glass-interop" -> navigator.presentSheet(AppRoute.GlassInteropTest)
                CHROME_DEMO_ACTION_ID -> presentChromeDemoAlert()
            }
        },
        barConfigForRoute = ::appBarConfig, // per-screen chrome behavior, shared with the Material host
    )

    // TEMP-VERIFY (removed before commit): chrome-customization battery — styled shell (tint + selected
    // tab color + large titles enabled) and a scripted ChromeDemo round trip.
    io.github.apdelrahman1911.nativecomposekit.theme.applyNativeShellStyle(
        io.github.apdelrahman1911.nativecomposekit.theme.NativeShellStyle(
            tint = io.github.apdelrahman1911.nativecomposekit.theme.NativeShellColor(
                light = androidx.compose.ui.graphics.Color(0.75f, 0.20f, 0.30f),
                dark = androidx.compose.ui.graphics.Color(0.95f, 0.45f, 0.55f),
            ),
            tabItemSelected = io.github.apdelrahman1911.nativecomposekit.theme.NativeShellColor(
                light = androidx.compose.ui.graphics.Color(0.75f, 0.20f, 0.30f),
                dark = androidx.compose.ui.graphics.Color(0.95f, 0.45f, 0.55f),
            ),
        ),
    )
    kotlinx.coroutines.MainScope().launch {
        kotlinx.coroutines.delay(3000)
        navigator.selectTab(AppTab.Settings); println("NCK-VERIFY settings selected")
        kotlinx.coroutines.delay(1600)
        navigator.push(AppRoute.ChromeDemo); println("NCK-VERIFY chrome demo pushed")
        kotlinx.coroutines.delay(2600)
        navigator.pop(); println("NCK-VERIFY popped back")
        kotlinx.coroutines.delay(1600)
        println("NCK-VERIFY battery done")
    }

    return NativeNavRoot(chrome = chrome)
}

/**
 * The chrome demo's per-screen action: action handlers are plain Kotlin, so the sample does the simplest
 * native thing — a real `UIAlertController` presented on the topmost controller.
 */
private fun presentChromeDemoAlert() {
    val alert = platform.UIKit.UIAlertController.alertControllerWithTitle(
        title = "Per-screen action",
        message = "This bar button belongs to the Chrome demo screen only — it came from its NativeBarConfig, not the tab.",
        preferredStyle = platform.UIKit.UIAlertControllerStyleAlert,
    )
    alert.addAction(
        platform.UIKit.UIAlertAction.actionWithTitle("Nice", platform.UIKit.UIAlertActionStyleDefault, handler = null),
    )
    var vc = platform.UIKit.UIApplication.sharedApplication.keyWindow?.rootViewController
    while (vc?.presentedViewController != null) vc = vc?.presentedViewController
    vc?.presentViewController(alert, animated = true, completion = null)
}
