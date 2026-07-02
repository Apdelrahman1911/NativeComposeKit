package io.github.apdelrahman1911.nativecomposekit.components.feedback

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

/**
 * Mounts the feedback system once, near the app root (inside `NativeKitTheme`). Provides [controller] to all
 * descendants via [LocalNativeFeedbackController] and renders the platform-appropriate surfaces over
 * [content]:
 * - **Android** draws Compose overlays (Material `SnackbarHost`/`AlertDialog`/`ModalBottomSheet` +
 *   themed banner/HUD).
 * - **iOS** presents real `UIAlertController`s and adds key-window overlays imperatively; it draws no
 *   Compose overlay itself (the host just emits [content] + side effects).
 *
 * Post messages from anywhere below via `LocalNativeFeedbackController.current` (or the [controller] you
 * passed in).
 *
 * Mount **exactly one** host per controller: each mounted host renders the controller's state, so a second
 * one would present every message twice. Constructing the controller yourself (ViewModel/DI) is supported —
 * see [NativeFeedbackController]'s constructor contract.
 */
@Composable
public fun NativeFeedbackHost(
    controller: NativeFeedbackController = rememberNativeFeedbackController(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalNativeFeedbackController provides controller) {
        PlatformFeedbackHost(controller, content)
    }
}

/** Platform renderer for [NativeFeedbackHost]. Android = Compose overlays; iOS = native presentation + effects. */
@Composable
internal expect fun PlatformFeedbackHost(
    controller: NativeFeedbackController,
    content: @Composable () -> Unit,
)
