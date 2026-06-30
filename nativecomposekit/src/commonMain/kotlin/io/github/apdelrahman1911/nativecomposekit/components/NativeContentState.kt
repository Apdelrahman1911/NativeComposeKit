package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

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

    /** The load failed; [message] is a user-facing reason. Pair with `onRetry` for a retry affordance. */
    @Immutable
    public data class Error(val message: String? = null) : NativeLoadState<Nothing>

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
 * `NativeContentState(state, onRetry = ::reload) { items -> LazyColumn { items(items) { … } } }`
 */
@Composable
public fun <T> NativeContentState(
    state: NativeLoadState<T>,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
    emptyTitle: String = "Nothing here yet",
    emptyMessage: String? = null,
    emptyIcon: ImageVector? = null,
    errorTitle: String = "Something went wrong",
    retryLabel: String = "Retry",
    loadingContent: (@Composable () -> Unit)? = null,
    emptyContent: (@Composable () -> Unit)? = null,
    errorContent: (@Composable (NativeLoadState.Error) -> Unit)? = null,
    content: @Composable (T) -> Unit,
) {
    when (state) {
        is NativeLoadState.Content -> Box(modifier) { content(state.value) }
        NativeLoadState.Loading -> Centered(modifier) {
            if (loadingContent != null) loadingContent() else NativeProgressIndicator(contentDescription = "Loading")
        }
        NativeLoadState.Empty -> Centered(modifier) {
            if (emptyContent != null) emptyContent() else NativeEmptyState(title = emptyTitle, message = emptyMessage, icon = emptyIcon)
        }
        is NativeLoadState.Error -> Centered(modifier) {
            if (errorContent != null) {
                errorContent(state)
            } else {
                NativeEmptyState(
                    title = errorTitle,
                    message = state.message,
                    icon = emptyIcon,
                    actionLabel = if (onRetry != null) retryLabel else null,
                    onAction = onRetry,
                )
            }
        }
    }
}

@Composable
private fun Centered(modifier: Modifier, content: @Composable () -> Unit) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) { content() }
}
