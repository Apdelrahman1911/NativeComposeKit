package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeButtonVariant
import io.github.apdelrahman1911.nativecomposekit.theme.NativeTheme

/** The loading state of a paginated (load-more) list. Drives [nativePaginationFooter]; you own the transitions. */
public enum class NativePageLoadState { Idle, Loading, Error, EndReached }

/**
 * Pure rule for "the list has scrolled within [buffer] items of the end" — extracted so it's unit-testable.
 * False for an empty list. [buffer] is coerced to be non-negative.
 */
internal fun shouldLoadMore(lastVisibleIndex: Int, totalItems: Int, buffer: Int): Boolean =
    totalItems > 0 && lastVisibleIndex >= totalItems - 1 - buffer.coerceAtLeast(0)

/**
 * Fire [onLoadMore] once each time [listState] scrolls within [buffer] items of the end (infinite scroll). State-
 * driven — it holds no paging state; you decide what "load more" does and track your own [NativePageLoadState].
 * Guard [onLoadMore] against concurrent loads (e.g. ignore it while your state is already `Loading`). Set
 * [enabled] to false to pause (e.g. at `EndReached` or while loading).
 *
 * `NativeLoadMoreEffect(listState, enabled = state != Loading && state != EndReached) { viewModel.loadNext() }`
 */
@Composable
public fun NativeLoadMoreEffect(
    listState: LazyListState,
    buffer: Int = 3,
    enabled: Boolean = true,
    onLoadMore: () -> Unit,
) {
    val reached by remember(listState, enabled, buffer) {
        derivedStateOf {
            if (!enabled) return@derivedStateOf false
            val info = listState.layoutInfo
            val last = info.visibleItemsInfo.lastOrNull()?.index ?: return@derivedStateOf false
            shouldLoadMore(last, info.totalItemsCount, buffer)
        }
    }
    LaunchedEffect(reached) {
        if (reached) onLoadMore()
    }
}

/**
 * A load-more footer row for a `LazyColumn`, shown according to [state]. Renders nothing for `Idle`/`EndReached`,
 * a centered [loading] slot for `Loading`, and an [error] slot (given a retry lambda) for `Error`. Both slots have
 * good defaults (a spinner / a Retry button) and are fully overridable to fit your design.
 *
 * `LazyColumn { items(rows) { Row(it) }; nativePaginationFooter(loadState, onRetry = ::retry) }`
 */
public fun LazyListScope.nativePaginationFooter(
    state: NativePageLoadState,
    onRetry: () -> Unit = {},
    loading: @Composable () -> Unit = { NativeProgressIndicator() },
    error: @Composable (retry: () -> Unit) -> Unit = { retry ->
        NativeButton(text = "Retry", onClick = retry, variant = NativeButtonVariant.Tertiary)
    },
) {
    when (state) {
        NativePageLoadState.Loading -> item(key = "native-pagination-footer") {
            Box(Modifier.fillMaxWidth().padding(NativeTheme.tokens.spacingMd), contentAlignment = Alignment.Center) {
                loading()
            }
        }
        NativePageLoadState.Error -> item(key = "native-pagination-footer") {
            Box(Modifier.fillMaxWidth().padding(NativeTheme.tokens.spacingMd), contentAlignment = Alignment.Center) {
                error(onRetry)
            }
        }
        NativePageLoadState.Idle, NativePageLoadState.EndReached -> Unit
    }
}
