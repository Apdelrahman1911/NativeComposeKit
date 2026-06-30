package com.ukkera.brandkit.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * The async lifecycle of a piece of screen content: [Loading] → [Content] / [Empty] / [Error]. A small,
 * pure (Compose-free) sealed model so view-models can drive [BrandContentState] without depending on UI.
 * [T] is the loaded payload (a list, a detail object, …).
 */
@Immutable
public sealed interface BrandLoadState<out T> {
    /** The first load (or a hard reload) is in flight; nothing to show yet. */
    public data object Loading : BrandLoadState<Nothing>

    /** Loaded successfully but there is nothing to display (empty list / no results). */
    public data object Empty : BrandLoadState<Nothing>

    /** The load failed; [message] is a user-facing reason. Pair with `onRetry` for a retry affordance. */
    @Immutable
    public data class Error(val message: String? = null) : BrandLoadState<Nothing>

    /** Loaded; [value] is the payload to render. */
    @Immutable
    public data class Content<out T>(val value: T) : BrandLoadState<T>
}

/**
 * Renders the right UI for a [BrandLoadState]: a centered spinner while [BrandLoadState.Loading], a
 * [BrandEmptyState] for [BrandLoadState.Empty], a [BrandEmptyState] with an optional **Retry** action for
 * [BrandLoadState.Error] (shown only when [onRetry] is provided), and [content] for
 * [BrandLoadState.Content]. **Compose-drawn on both platforms.** This is the kit's standard
 * loading→empty→error→content pattern (with the previously-missing error/retry variant).
 *
 * The loading/empty/error visuals have sensible themed defaults; override any of them with
 * [loadingContent]/[emptyContent]/[errorContent] when a screen needs something bespoke (e.g. skeleton rows
 * instead of a spinner). [content] is rendered directly (it owns its own layout); the placeholder states are
 * centered in the available space.
 *
 * `BrandContentState(state, onRetry = ::reload) { items -> LazyColumn { items(items) { … } } }`
 */
@Composable
public fun <T> BrandContentState(
    state: BrandLoadState<T>,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
    emptyTitle: String = "Nothing here yet",
    emptyMessage: String? = null,
    emptyIcon: ImageVector? = null,
    errorTitle: String = "Something went wrong",
    retryLabel: String = "Retry",
    loadingContent: (@Composable () -> Unit)? = null,
    emptyContent: (@Composable () -> Unit)? = null,
    errorContent: (@Composable (BrandLoadState.Error) -> Unit)? = null,
    content: @Composable (T) -> Unit,
) {
    when (state) {
        is BrandLoadState.Content -> Box(modifier) { content(state.value) }
        BrandLoadState.Loading -> Centered(modifier) {
            if (loadingContent != null) loadingContent() else BrandProgressIndicator(contentDescription = "Loading")
        }
        BrandLoadState.Empty -> Centered(modifier) {
            if (emptyContent != null) emptyContent() else BrandEmptyState(title = emptyTitle, message = emptyMessage, icon = emptyIcon)
        }
        is BrandLoadState.Error -> Centered(modifier) {
            if (errorContent != null) {
                errorContent(state)
            } else {
                BrandEmptyState(
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
