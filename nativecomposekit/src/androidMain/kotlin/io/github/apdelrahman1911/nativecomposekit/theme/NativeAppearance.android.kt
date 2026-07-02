package io.github.apdelrahman1911.nativecomposekit.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.view.View
import android.view.WindowInsetsController
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView

/** No-op on Android: there's no native shell here, and the Compose side already follows
 * [NativeAppearance.darkOverride] via [NativeAppearanceScope]. */
internal actual fun applyPlatformColorScheme(dark: Boolean?) {
    // intentionally empty
}

/** No-op on Android: Compose draws the whole surface (no native window/bars to theme). */
internal actual fun applyNativeShellChrome(dark: Boolean, lightBackground: Color, darkBackground: Color) {
    // intentionally empty
}

/**
 * Keeps the status/navigation-bar icon luminance in step with the resolved dark state. Edge-to-edge
 * Android keys bar-icon appearance to the SYSTEM night mode, so an in-app `NativeAppearance.setDark(true)`
 * on a light-mode device would otherwise paint the app dark while the clock/battery icons stay dark too —
 * unreadable chrome. Idempotent per composition; no activity recreation involved.
 */
@Composable
internal actual fun PlatformAppearanceSync(dark: Boolean) {
    val view = LocalView.current
    if (view.isInEditMode) return
    SideEffect {
        val window = view.context.findActivity()?.window ?: return@SideEffect
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val lightBars =
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS or
                    WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
            window.insetsController?.setSystemBarsAppearance(if (dark) 0 else lightBars, lightBars)
        } else {
            @Suppress("DEPRECATION")
            val lightFlags =
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility =
                if (dark) window.decorView.systemUiVisibility and lightFlags.inv()
                else window.decorView.systemUiVisibility or lightFlags
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
