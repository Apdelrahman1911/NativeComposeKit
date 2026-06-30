package com.ukkera.brandkit.components.feedback

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

/**
 * Mounts the feedback system once, near the app root (inside `AppTheme`). Provides [controller] to all
 * descendants via [LocalBrandFeedbackController] and renders the platform-appropriate surfaces over
 * [content]:
 * - **Android** draws Compose overlays (Material `SnackbarHost`/`AlertDialog`/`ModalBottomSheet` +
 *   themed banner/HUD).
 * - **iOS** presents real `UIAlertController`s and adds key-window overlays imperatively; it draws no
 *   Compose overlay itself (the host just emits [content] + side effects).
 *
 * Post messages from anywhere below via `LocalBrandFeedbackController.current` (or the [controller] you
 * passed in).
 */
@Composable
public fun BrandFeedbackHost(
    controller: BrandFeedbackController = rememberBrandFeedbackController(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalBrandFeedbackController provides controller) {
        PlatformFeedbackHost(controller, content)
    }
}

/** Platform renderer for [BrandFeedbackHost]. Android = Compose overlays; iOS = native presentation + effects. */
@Composable
internal expect fun PlatformFeedbackHost(
    controller: BrandFeedbackController,
    content: @Composable () -> Unit,
)
