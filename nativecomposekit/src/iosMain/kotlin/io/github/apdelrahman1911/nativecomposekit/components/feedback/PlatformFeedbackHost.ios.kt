package io.github.apdelrahman1911.nativecomposekit.components.feedback

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import io.github.apdelrahman1911.nativecomposekit.components.toUIColor
import io.github.apdelrahman1911.nativecomposekit.theme.LocalNativeStrings

/**
 * iOS feedback host. Emits [content] and drives native presentation via side effects — it draws no
 * Compose overlay itself. Per-status styles and theme colors are resolved here, *in composition*, then
 * captured by the effects (so the imperative UIKit code reads only theme-resolved values).
 *
 * Each effect is keyed on the active record's monotonic id: it presents on first appearance, survives
 * recomposition, and tears down on dispose (when the controller clears/advances that lane).
 */
@Composable
internal actual fun PlatformFeedbackHost(
    controller: NativeFeedbackController,
    content: @Composable () -> Unit,
) {
    content()

    // Transient lane (toast HUD / snackbar / banner) — key-window overlay.
    val transient = controller.activeTransient
    if (transient != null) {
        val style = resolveFeedbackStyle(transient.status, filled = true)
        DisposableEffect(transient.id) {
            val handle = presentTransient(transient, style, controller)
            onDispose { handle?.dismiss() }
        }
    }

    // Modal lane (alert / sheet) — native UIAlertController by default, branded overlay on opt-in.
    val modal = controller.activeModal
    if (modal != null) {
        val cardStyle = resolveFeedbackStyle(NativeFeedbackStatus.Info, filled = false) // neutral surface card
        val primary = MaterialTheme.colorScheme.primary.toUIColor()
        val error = MaterialTheme.colorScheme.error.toUIColor()
        val cancelColor = MaterialTheme.colorScheme.onSurfaceVariant.toUIColor()
        val strings = LocalNativeStrings.current // localized fallback action titles ("OK"/"Cancel")
        DisposableEffect(modal.id) {
            val dismiss = presentModal(modal, cardStyle, primary, error, cancelColor, strings, controller)
            onDispose { dismiss() }
        }
    }
}
