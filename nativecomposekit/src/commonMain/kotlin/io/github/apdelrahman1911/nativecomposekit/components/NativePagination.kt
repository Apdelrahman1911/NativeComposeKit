package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeButtonVariant
import io.github.apdelrahman1911.nativecomposekit.theme.LocalNativeStrings
import io.github.apdelrahman1911.nativecomposekit.theme.NativeTheme
import kotlinx.coroutines.flow.filterNotNull

/** The loading state of a paginated (load-more) list. Drives [nativePaginationFooter]; you own the transitions. */
public enum class NativePageLoadState { Idle, Loading, Error, EndReached }

/**
 * Pure rule for "the list has scrolled within [buffer] items of the end" — extracted so it's unit-testable.
 * False for an empty list. [buffer] is coerced to be non-negative.
 */
internal fun shouldLoadMore(lastVisibleIndex: Int, totalItems: Int, buffer: Int): Boolean =
    totalItems > 0 && lastVisibleIndex >= totalItems - 1 - buffer.coerceAtLeast(0)

/**
 * Pure firing rule for [NativeLoadMoreEffect]: fire when the end is within reach ([shouldLoadMore]) AND this
 * [totalItems] hasn't already fired ([lastFiredTotal]). Re-keying on the item count is what re-arms the
 * trigger when a loaded page doesn't fill the viewport (the end stays visible, so an edge-triggered boolean
 * would never flip back and the list stalls), while still firing at most once per count (scroll jitter near
 * the end doesn't spam the loader, and a failed load — no growth — doesn't loop).
 */
internal fun loadMoreShouldFire(
    lastVisibleIndex: Int,
    totalItems: Int,
    buffer: Int,
    lastFiredTotal: Int?,
): Boolean = shouldLoadMore(lastVisibleIndex, totalItems, buffer) && totalItems != lastFiredTotal

/**
 * Fire [onLoadMore] each time the list scrolls within [buffer] items of the end (infinite scroll) — at most
 * once per item count, re-armed when the count changes, so a page that doesn't fill the viewport still
 * chains into the next load (see [loadMoreShouldFire]). State-driven — it holds no paging state; you decide
 * what "load more" does and track your own [NativePageLoadState]. Guard [onLoadMore] against concurrent
 * loads (e.g. ignore it while your state is already `Loading`). Set [enabled] to false to pause (e.g. at
 * `EndReached` or while loading).
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
    LoadMoreEffectImpl(stateKey = listState, buffer = buffer, enabled = enabled, onLoadMore = onLoadMore) {
        val info = listState.layoutInfo
        val last = info.visibleItemsInfo.lastOrNull()?.index ?: return@LoadMoreEffectImpl null
        last to info.totalItemsCount
    }
}

/**
 * [NativeLoadMoreEffect] for a lazy **grid** — same trigger and re-arm rules, reading [gridState]'s layout
 * info. Pair it with [LazyGridScope.nativePaginationFooter] for the full-width footer row.
 */
@Composable
public fun NativeLoadMoreEffect(
    gridState: LazyGridState,
    buffer: Int = 3,
    enabled: Boolean = true,
    onLoadMore: () -> Unit,
) {
    LoadMoreEffectImpl(stateKey = gridState, buffer = buffer, enabled = enabled, onLoadMore = onLoadMore) {
        val info = gridState.layoutInfo
        val last = info.visibleItemsInfo.lastOrNull()?.index ?: return@LoadMoreEffectImpl null
        last to info.totalItemsCount
    }
}

/**
 * Shared load-more engine. [visibleEnd] reads (last visible index, total item count) — inside
 * `snapshotFlow`, so scroll and dataset changes both re-evaluate it. The last-fired count lives in the
 * effect (reset when [stateKey]/[buffer]/[enabled] change): re-enabling after a load deliberately re-arms,
 * since "still near the end with fresh data" is exactly when the next page should chain.
 */
@Composable
private fun LoadMoreEffectImpl(
    stateKey: Any,
    buffer: Int,
    enabled: Boolean,
    onLoadMore: () -> Unit,
    visibleEnd: () -> Pair<Int, Int>?,
) {
    val currentOnLoadMore by rememberUpdatedState(onLoadMore)
    LaunchedEffect(stateKey, buffer, enabled) {
        if (!enabled) return@LaunchedEffect
        var lastFiredTotal: Int? = null
        snapshotFlow { visibleEnd() }
            .filterNotNull()
            .collect { (lastVisible, total) ->
                if (loadMoreShouldFire(lastVisible, total, buffer, lastFiredTotal)) {
                    lastFiredTotal = total
                    currentOnLoadMore()
                }
            }
    }
}

private const val PaginationFooterKey = "native-pagination-footer"

/** The centered, padded row every footer state renders in — one look across list and grid hosts. */
@Composable
private fun PaginationFooterRow(content: @Composable () -> Unit) {
    Box(
        Modifier.fillMaxWidth().padding(NativeTheme.tokens.spacingMd),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

/** Default `loading` slot: the kit spinner, named for screen readers (a silent spinner announces nothing). */
@Composable
private fun DefaultPaginationLoading() {
    NativeProgressIndicator(contentDescription = LocalNativeStrings.current.loading)
}

/** Default `error` slot: a Retry button — hidden when there is no [retry] callback (no dead button). */
@Composable
private fun DefaultPaginationError(retry: (() -> Unit)?) {
    if (retry != null) {
        NativeButton(text = LocalNativeStrings.current.retry, onClick = retry, variant = NativeButtonVariant.Tertiary)
    }
}

/**
 * A load-more footer row for a `LazyColumn`, shown according to [state]. Renders nothing for `Idle`/`EndReached`,
 * a centered [loading] slot for `Loading`, and an [error] slot for `Error`. Both slots have good defaults (a
 * spinner announced as loading / a Retry button) and are fully overridable to fit your design. The slot receives
 * [onRetry] as passed — `null` means "no retry affordance", and the default slot then renders nothing.
 *
 * `LazyColumn { items(rows) { Row(it) }; nativePaginationFooter(loadState, onRetry = ::retry) }`
 */
public fun LazyListScope.nativePaginationFooter(
    state: NativePageLoadState,
    onRetry: (() -> Unit)? = null,
    loading: @Composable () -> Unit = { DefaultPaginationLoading() },
    error: @Composable (retry: (() -> Unit)?) -> Unit = { retry -> DefaultPaginationError(retry) },
) {
    when (state) {
        NativePageLoadState.Loading -> item(key = PaginationFooterKey) { PaginationFooterRow { loading() } }
        NativePageLoadState.Error -> item(key = PaginationFooterKey) { PaginationFooterRow { error(onRetry) } }
        NativePageLoadState.Idle, NativePageLoadState.EndReached -> Unit
    }
}

/**
 * [nativePaginationFooter] for a lazy **grid**: the footer spans the full line (`GridItemSpan(maxLineSpan)`),
 * so the spinner/retry row sits centered under the grid instead of inside one cell. Same states, slots, and
 * defaults as the list variant.
 *
 * `LazyVerticalGrid(columns) { items(covers) { Cover(it) }; nativePaginationFooter(loadState, onRetry = ::retry) }`
 */
public fun LazyGridScope.nativePaginationFooter(
    state: NativePageLoadState,
    onRetry: (() -> Unit)? = null,
    loading: @Composable () -> Unit = { DefaultPaginationLoading() },
    error: @Composable (retry: (() -> Unit)?) -> Unit = { retry -> DefaultPaginationError(retry) },
) {
    when (state) {
        NativePageLoadState.Loading -> item(key = PaginationFooterKey, span = { GridItemSpan(maxLineSpan) }) {
            PaginationFooterRow { loading() }
        }
        NativePageLoadState.Error -> item(key = PaginationFooterKey, span = { GridItemSpan(maxLineSpan) }) {
            PaginationFooterRow { error(onRetry) }
        }
        NativePageLoadState.Idle, NativePageLoadState.EndReached -> Unit
    }
}
