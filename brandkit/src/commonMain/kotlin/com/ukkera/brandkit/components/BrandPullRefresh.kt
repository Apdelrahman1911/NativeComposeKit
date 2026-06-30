package com.ukkera.brandkit.components

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Pull-to-refresh container: wrap a vertically-scrollable [content]; a downward overscroll at the top fires
 * [onRefresh], and the themed spinner shows while [isRefreshing] is true (the caller owns that flag and flips
 * it back when the load completes).
 *
 * **Compose-drawn on both platforms** (one shared `PullToRefreshBox`, themed by `MaterialTheme`). On iOS this
 * is the Compose pull-refresh, **not** a native `UIRefreshControl` — that would require hosting the scroll in
 * a `UIScrollView` via interop, which is out of scope for a content-level control. A documented, intentional
 * platform divergence (kit rule 5).
 *
 * `BrandPullRefresh(isRefreshing = loading, onRefresh = ::reload) { LazyColumn { … } }`
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun BrandPullRefresh(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier,
        content = content,
    )
}
