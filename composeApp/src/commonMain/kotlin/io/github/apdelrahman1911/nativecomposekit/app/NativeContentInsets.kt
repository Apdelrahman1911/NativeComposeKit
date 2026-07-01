package io.github.apdelrahman1911.nativecomposekit.app

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.dp

/**
 * Top inset a hosting shell asks scrollable content to begin below, when the shell draws a **translucent overlay
 * bar on top of the content** (rather than reserving space above it). The content still fills the whole area —
 * including BEHIND the bar — so it scrolls under the bar and the bar's material (iOS 26 Liquid Glass) refracts it.
 *
 * The iOS native-chrome shell provides the real value: its `UINavigationBar` overlays the Compose content, and
 * the value is the bar's height (surfaced through `WindowInsets.safeDrawing` via the content VC's safe-area
 * inset). Android leaves it at 0 — the Material `NativeNavHost` insets content below its top bar via Scaffold
 * padding, so content sits below the bar, not behind it.
 */
val LocalNativeContentTopInset = compositionLocalOf { 0.dp }
