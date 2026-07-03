package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import io.github.apdelrahman1911.nativecomposekit.theme.LocalNativeCapabilities
import io.github.apdelrahman1911.nativecomposekit.theme.LocalNativeStrings

/**
 * The async lifecycle of a piece of screen content: [Loading] → [Content] / [Empty] / [Error]. A small,
 * pure (Compose-free) sealed model so view-models can drive [NativeContentState] without depending on UI.
 * [T] is the loaded payload (a list, a detail object, …).
 */
@Immutable
public sealed interface NativeLoadState<out T> {
    /** The first load (or a hard reload) is in flight; nothing to show yet. */
    public data object Loading : NativeLoadState<Nothing>

    /** Loaded successfully but there is nothing to display (empty list / no results). */
    public data object Empty : NativeLoadState<Nothing>

    /**
     * The load failed; [message] is a user-facing reason and [cause] the underlying exception (for
     * logging/diagnostics — never rendered). Pair with `onRetry` for a retry affordance. Compares by
     * value over [message]/[cause]; not a `data class` so fields stay addable binary-compatibly.
     */
    @Immutable
    public class Error(
        public val message: String? = null,
        public val cause: Throwable? = null,
    ) : NativeLoadState<Nothing> {
        override fun equals(other: Any?): Boolean =
            this === other || (other is Error && message == other.message && cause == other.cause)

        override fun hashCode(): Int = (message?.hashCode() ?: 0) * 31 + (cause?.hashCode() ?: 0)
    }

    /** Loaded; [value] is the payload to render. */
    @Immutable
    public data class Content<out T>(val value: T) : NativeLoadState<T>
}

/**
 * Renders the right UI for a [NativeLoadState]: a centered spinner while [NativeLoadState.Loading], a
 * [NativeEmptyState] for [NativeLoadState.Empty], a [NativeEmptyState] with an optional **Retry** action for
 * [NativeLoadState.Error] (shown only when [onRetry] is provided), and [content] for
 * [NativeLoadState.Content]. **Compose-drawn on both platforms.** This is the kit's standard
 * loading→empty→error→content pattern (with the previously-missing error/retry variant).
 *
 * The loading/empty/error visuals have sensible themed defaults; override any of them with
 * [loadingContent]/[emptyContent]/[errorContent] when a screen needs something bespoke (e.g. skeleton rows
 * instead of a spinner). [content] is rendered directly (it owns its own layout); the placeholder states are
 * centered in the available space.
 *
 * State changes cross-fade by default ([animate]; disabled automatically under the OS reduce-motion
 * setting). [errorIcon] gives failures their own glyph (falls back to [emptyIcon]). Note: iOS
 * interop-backed controls inside [content] snap rather than fade (native views don't animate with
 * Compose transitions — see docs/interop-notes.md).
 *
 * `NativeContentState(state, onRetry = ::reload) { items -> LazyColumn { items(items) { … } } }`
 */
@Composable
public fun <T> NativeContentState(
    state: NativeLoadState<T>,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
    emptyTitle: String = LocalNativeStrings.current.emptyStateTitle,
    emptyMessage: String? = null,
    emptyIcon: ImageVector? = null,
    errorIcon: ImageVector? = null,
    errorTitle: String = LocalNativeStrings.current.errorStateTitle,
    retryLabel: String = LocalNativeStrings.current.retry,
    animate: Boolean = true,
    loadingContent: (@Composable () -> Unit)? = null,
    emptyContent: (@Composable () -> Unit)? = null,
    errorContent: (@Composable (NativeLoadState.Error) -> Unit)? = null,
    content: @Composable (T) -> Unit,
) {
    // Cross-fade BETWEEN branches only (keyed on the state's class, not its value) so a content update
    // doesn't re-fade; hard-cut under the OS reduce-motion preference or animate = false.
    val reduceMotion = LocalNativeCapabilities.current.isReduceMotionEnabled
    AnimatedContent(
        targetState = state,
        modifier = modifier,
        transitionSpec = {
            if (animate && !reduceMotion) fadeIn() togetherWith fadeOut()
            else EnterTransition.None togetherWith ExitTransition.None
        },
        contentKey = { it::class },
    ) { target ->
        when (target) {
            is NativeLoadState.Content -> Box { content(target.value) }
            NativeLoadState.Loading -> Centered {
                if (loadingContent != null) loadingContent() else NativeProgressIndicator(contentDescription = LocalNativeStrings.current.loading)
            }
            NativeLoadState.Empty -> Centered {
                if (emptyContent != null) emptyContent() else NativeEmptyState(title = emptyTitle, message = emptyMessage, icon = emptyIcon)
            }
            is NativeLoadState.Error -> Centered {
                if (errorContent != null) {
                    errorContent(target)
                } else {
                    NativeEmptyState(
                        title = errorTitle,
                        message = target.message,
                        // A failure glyph should not silently reuse the "no results" glyph — [errorIcon]
                        // wins, falling back to [emptyIcon] to preserve existing callers.
                        icon = errorIcon ?: emptyIcon,
                        actionLabel = if (onRetry != null) retryLabel else null,
                        onAction = onRetry,
                    )
                }
            }
        }
    }
}

@Composable
private fun Centered(content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { content() }
}
