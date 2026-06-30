package io.github.apdelrahman1911.nativecomposekit.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

/**
 * Process-wide appearance source of truth, shared across **every** Compose composition — the catalog and
 * each screen the native SwiftUI shell hosts in its own `ComposeUIViewController`. Compose snapshot state is
 * global to the process, so flipping [darkOverride]/[rtl] recomposes **all** of those compositions at once;
 * that's how dark/light + RTL apply app-wide even though each hosted screen is a separate composition.
 *
 * Wrap every composition root in [NativeAppearanceScope] (instead of `AppTheme` directly) so they all follow
 * this state. Use [setDark] to also flip the **native** chrome (iOS window interface style) so the tab/nav
 * bars match the Compose content.
 */
public object NativeAppearance {
    /** null = follow the system; true/false = an explicit app-wide override. */
    public var darkOverride: Boolean? by mutableStateOf<Boolean?>(null)
        private set

    public var rtl: Boolean by mutableStateOf(false)

    /** Set the app-wide dark mode: updates every Compose composition AND the native chrome. */
    public fun setDark(dark: Boolean) {
        darkOverride = dark
        applyPlatformColorScheme(dark)
    }
}

/**
 * The single composition root wrapper: applies [AppTheme] + the layout direction from [NativeAppearance].
 * Use at the top of the catalog `App()` and inside each `ComposeUIViewController` the shell hosts, so they
 * all share one appearance.
 */
@Composable
public fun NativeAppearanceScope(
    drawBackground: Boolean = true,
    content: @Composable () -> Unit,
) {
    val dark = NativeAppearance.darkOverride ?: isSystemInDarkTheme()
    // Theme the native host chrome (iOS window + nav/tab bars) to the brand background so it matches this
    // content. Re-runs on every dark flip (system or in-app) since `dark` changes; no-op on Android.
    LaunchedEffect(dark) { applyNativeShellChrome(dark) }
    AppTheme(darkTheme = dark) {
        val layoutDir = if (NativeAppearance.rtl) LayoutDirection.Rtl else LayoutDirection.Ltr
        // Publish runtime capabilities (reduce-motion) alongside layout direction for every hosted composition.
        val capabilities = NativeCapabilities(isReduceMotionEnabled = rememberReduceMotion())
        CompositionLocalProvider(
            LocalLayoutDirection provides layoutDir,
            LocalNativeCapabilities provides capabilities,
        ) {
            if (drawBackground) {
                // Paint the theme background across the whole host AND publish it as the current surface.
                // On iOS a `NativeText` is a `UILabel` in a `UIKitView` whose transparent pixels reveal the
                // ComposeUIViewController's *system* (white/black) backdrop, not the Compose surface — so on
                // an off-white light background every loose label showed a box. Filling the labels with the
                // real background (via [LocalNativeSurface]) and painting that same background here makes the
                // whole screen one uniform color in both light and dark. Containers like `NativeCard` override
                // [LocalNativeSurface] with their own color so their text fills to match them instead.
                Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                    CompositionLocalProvider(LocalNativeSurface provides MaterialTheme.colorScheme.background) {
                        content()
                    }
                }
            } else {
                // Transparent host (a Liquid Glass sheet): no opaque fill, and labels stay transparent so the
                // native material shows through. `NativeCard` still publishes its own surface locally.
                CompositionLocalProvider(LocalNativeSurface provides Color.Unspecified) {
                    content()
                }
            }
        }
    }
}

/** Flips native app chrome to match (iOS: every window's `overrideUserInterfaceStyle`). Android: no-op — the
 * Compose side already follows [NativeAppearance.darkOverride]. */
internal expect fun applyPlatformColorScheme(dark: Boolean)

/** Themes the native host shell chrome (window, nav bar, tab bar, safe areas) to the brand background for
 * [dark] so it matches the Compose content. iOS implements it; Android is a no-op (Compose owns the chrome). */
internal expect fun applyNativeShellChrome(dark: Boolean)
