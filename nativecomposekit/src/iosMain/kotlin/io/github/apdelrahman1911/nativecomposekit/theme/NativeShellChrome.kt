package io.github.apdelrahman1911.nativecomposekit.theme

import io.github.apdelrahman1911.nativecomposekit.components.toUIColor
import platform.UIKit.UIApplication
import platform.UIKit.UIColor
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowLevelNormal

/**
 * The brand background as a plain `UIColor` for [dark] — the single source of truth (Compose `AppTheme`
 * background) for a native host that must paint a background itself. The UIKit chrome shell builds a
 * dynamic `UIColor { nativeBackgroundUIColor(dark: $0.userInterfaceStyle == .dark) }` for its root view
 * so the area behind its bars adapts to light/dark automatically.
 */
public fun nativeBackgroundUIColor(dark: Boolean): UIColor =
    (if (dark) nativeDarkBackground else nativeLightBackground).toUIColor()

/**
 * iOS: paint the app's own content windows with the brand background for [dark] so pixels behind the
 * Compose canvas (rotation gaps, keyboard dismissal, transition edges) match the theme instead of the
 * system black/white. Driven by [NativeAppearanceScope] (a `LaunchedEffect` on the resolved dark state),
 * so it runs at launch and on every light↔dark flip — system or in-app ([NativeAppearance.setDark]).
 *
 * Deliberately narrow: only NORMAL-level windows with a root view controller are touched.
 * `UIApplication.windows` also lists full-screen system overlay windows (the keyboard's input-assistant
 * `UITextEffectsWindow`, for one) that sit ABOVE the app and pass touches through — painting one of those
 * opaque blanks the entire UI while taps keep landing on the app underneath ("blank page after switching
 * theme"). Bar appearance is likewise NOT touched here: the chrome shell owns its `UINavigationBar` /
 * `UITabBar` styling, and global appearance proxies would leak brand-opaque bars into system controllers
 * (the share sheet, the color picker).
 */
internal actual fun applyNativeShellChrome(dark: Boolean) {
    val bg = nativeBackgroundUIColor(dark)
    UIApplication.sharedApplication.windows.forEach { w ->
        val window = w as? UIWindow ?: return@forEach
        if (window.windowLevel != UIWindowLevelNormal || window.rootViewController == null) return@forEach
        window.backgroundColor = bg
    }
}
