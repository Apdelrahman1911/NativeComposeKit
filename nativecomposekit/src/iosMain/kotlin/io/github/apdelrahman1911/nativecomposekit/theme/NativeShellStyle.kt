package io.github.apdelrahman1911.nativecomposekit.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import io.github.apdelrahman1911.nativecomposekit.components.toUIColor
import platform.UIKit.UIColor
import platform.UIKit.UIFont

/**
 * A light/dark color pair for the native shell — the shell resolves the right one from the trait
 * environment, so styled chrome adapts to appearance changes automatically.
 */
@Immutable
public class NativeShellColor(
    public val light: Color,
    public val dark: Color,
)

/** How the shell's navigation bars paint their background. */
public enum class NativeShellBarBackground {
    /**
     * The default: OPAQUE, in the theme background color ([nativeBackgroundUIColor] — including a
     * consumer-injected palette). Opaque is deliberate: a translucent bar samples whatever is composited
     * behind it, and during a navigation transition that is UIKit's dimmed transition container — the bar
     * visibly darkens mid-swipe (see docs/native-chrome.md).
     */
    Themed,

    /**
     * The system bar material (translucent blur / Liquid Glass). Native-looking when content scrolls
     * under a bar — but this shell lays content BELOW the top bar, and the material re-exposes the
     * documented mid-transition darkening. Opt in only if that trade-off is acceptable.
     */
    SystemMaterial,

    /** Opaque, in [NativeShellStyle.customBarBackground] (falls back to [Themed] when that is null). */
    Custom,
}

/**
 * Appearance styling for the iOS native chrome shell — the UIKit-side counterpart of restyling the
 * Material host's bars with Compose parameters. Register once with [applyNativeShellStyle] **before the
 * shell is created** (next to `createNativeNavRoot()`); the shell reads it while configuring its
 * `UINavigationBar`/`UITabBar` appearances. Every field defaults to today's look, so an unstyled app is
 * byte-identical. Per-SCREEN behavior (hiding bars, large-title opt-in, per-screen actions) travels
 * separately, through `NativeBarConfig` on the chrome contract; this object is appearance only.
 *
 * - [barBackground]/[customBarBackground] — bar background treatment (see [NativeShellBarBackground]).
 * - [tint] — bar control tint: the back chevron, bar button items, and the selected tab item (unless
 *   overridden by [tabItemSelected]). Null keeps the system tint.
 * - [tabItemSelected]/[tabItemUnselected] — tab bar item icon+label colors per state. Null keeps system.
 * - [titleFont] — the navigation bar's INLINE title font. Null keeps the system font. (Large titles keep
 *   the system large-title font.)
 * - [showsHairline] — restore the bar's bottom hairline (today's bars draw none).
 * - [largeTitles] — master switch for large navigation titles; which screens actually SHOW one is chosen
 *   per entry via `NativeBarConfig.prefersLargeTitle`. NOTE: with Compose content there is no
 *   `UIScrollView` under the bar, so large titles do NOT collapse on scroll — they stay large while such
 *   an entry is on top.
 */
@Immutable
public class NativeShellStyle(
    public val barBackground: NativeShellBarBackground = NativeShellBarBackground.Themed,
    public val customBarBackground: NativeShellColor? = null,
    public val tint: NativeShellColor? = null,
    public val tabItemSelected: NativeShellColor? = null,
    public val tabItemUnselected: NativeShellColor? = null,
    public val titleFont: UIFont? = null,
    public val showsHairline: Boolean = false,
    public val largeTitles: Boolean = false,
) {
    public companion object {
        /** Today's exact chrome: opaque themed bars, no hairline, system fonts/tints, compact titles. */
        public val Default: NativeShellStyle = NativeShellStyle()
    }
}

private var registeredStyle: NativeShellStyle = NativeShellStyle.Default

/**
 * Register [style] as the shell chrome appearance. Call from Kotlin startup code BEFORE building the
 * shell (the shell configures its bar appearances at creation and does not re-read the style afterwards).
 * Main-thread only, like all UIKit interaction here.
 */
public fun applyNativeShellStyle(style: NativeShellStyle) {
    registeredStyle = style
}

/** The registered shell style ([NativeShellStyle.Default] until [applyNativeShellStyle] runs). */
public fun nativeShellStyle(): NativeShellStyle = registeredStyle

// ---- Swift-facing resolvers (the shell builds dynamic UIColors over these, one per trait style) ----

/**
 * The bar background for [dark] under the registered style: the theme background for [Themed] (and as the
 * fallback for a [Custom] style missing its colors), the custom pair for [Custom]. Meaningless for
 * [SystemMaterial] (the shell uses the system material instead of a color).
 */
public fun nativeShellBarBackgroundUIColor(dark: Boolean): UIColor {
    val style = registeredStyle
    val custom = style.customBarBackground
    return if (style.barBackground == NativeShellBarBackground.Custom && custom != null) {
        (if (dark) custom.dark else custom.light).toUIColor()
    } else {
        nativeBackgroundUIColor(dark)
    }
}

/** The registered control tint for [dark], or null to keep the system tint. */
public fun nativeShellTintUIColor(dark: Boolean): UIColor? =
    registeredStyle.tint?.let { (if (dark) it.dark else it.light).toUIColor() }

/** The registered selected tab-item color for [dark], or null to keep the system appearance. */
public fun nativeShellTabItemSelectedUIColor(dark: Boolean): UIColor? =
    registeredStyle.tabItemSelected?.let { (if (dark) it.dark else it.light).toUIColor() }

/** The registered unselected tab-item color for [dark], or null to keep the system appearance. */
public fun nativeShellTabItemUnselectedUIColor(dark: Boolean): UIColor? =
    registeredStyle.tabItemUnselected?.let { (if (dark) it.dark else it.light).toUIColor() }
