package io.github.apdelrahman1911.nativecomposekit.theme

import io.github.apdelrahman1911.nativecomposekit.components.toUIColor
import platform.UIKit.UIApplication
import platform.UIKit.UIColor
import platform.UIKit.UINavigationBar
import platform.UIKit.UINavigationBarAppearance
import platform.UIKit.UITabBar
import platform.UIKit.UITabBarAppearance
import platform.UIKit.UIView
import platform.UIKit.UIWindow

/**
 * The brand background as a plain `UIColor` for [dark] — the single source of truth (Compose `AppTheme`
 * background) for a native host that must paint a background SwiftUI-side. [applyNativeShellChrome] already
 * themes the window + nav/tab bars automatically; this is needed only for the **area around the iOS floating
 * tab bar**, which is a `TabView` layout region the appearance proxies can't reach — the shell sets it via
 * `.background(...)`. Build a dynamic `UIColor { nativeBackgroundUIColor(dark: $0.userInterfaceStyle == .dark) }`
 * in Swift so it adapts to light/dark.
 */
public fun nativeBackgroundUIColor(dark: Boolean): UIColor =
    (if (dark) nativeDarkBackground else nativeLightBackground).toUIColor()

/**
 * iOS: theme the native SwiftUI shell chrome — the `UIWindow`, the `NavigationStack` nav bar, the `TabView`
 * tab bar, and therefore the status-bar / home-indicator safe-area regions — with the brand background for
 * the given [dark] mode, so the shell matches the hosted Compose content instead of defaulting to system
 * black (dark) / white (light).
 *
 * This is the ROOT fix and it lives in the kit: it's driven automatically by [NativeAppearanceScope] (a
 * `LaunchedEffect` on the resolved dark state), so it runs at launch AND on every light↔dark flip — system
 * or in-app ([NativeAppearance.setDark]) — because Compose recomposes either way. A host app gets seamless
 * chrome for free with **no Swift code** (except the floating-tab-bar surround; see [nativeBackgroundUIColor]).
 */
internal actual fun applyNativeShellChrome(dark: Boolean) {
    val bg = nativeBackgroundUIColor(dark)

    val nav = UINavigationBarAppearance().apply {
        configureWithOpaqueBackground()
        backgroundColor = bg
        shadowColor = null // drop the hairline so the bar reads as one surface with the content
    }
    val tab = UITabBarAppearance().apply {
        configureWithOpaqueBackground()
        backgroundColor = bg
        shadowColor = null
    }
    // Proxies theme bars built later (e.g. a pushed detail screen's nav bar).
    UINavigationBar.appearance().apply {
        standardAppearance = nav
        scrollEdgeAppearance = nav
        compactAppearance = nav
    }
    UITabBar.appearance().apply {
        standardAppearance = tab
        scrollEdgeAppearance = tab
    }
    // ...and walk the live hierarchy so already-built bars + the window itself update on a light↔dark flip
    // (proxies only affect bars created after this call).
    UIApplication.sharedApplication.windows.forEach { w ->
        (w as? UIWindow)?.let { window ->
            window.backgroundColor = bg
            window.themeBars(nav, tab)
        }
    }
}

private fun UIView.themeBars(nav: UINavigationBarAppearance, tab: UITabBarAppearance) {
    (this as? UINavigationBar)?.apply {
        standardAppearance = nav
        scrollEdgeAppearance = nav
        compactAppearance = nav
    }
    (this as? UITabBar)?.apply {
        standardAppearance = tab
        scrollEdgeAppearance = tab
    }
    subviews.forEach { (it as? UIView)?.themeBars(nav, tab) }
}
