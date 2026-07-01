package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerScope
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A horizontally swipeable pager for carousels, onboarding, and image galleries. **Compose-drawn on both
 * platforms** — no UIKit control hosts arbitrary Compose pages, and Compose Foundation's pager supplies the
 * swipe/fling physics, so this is a thin, customizable wrapper over `HorizontalPager` in the kit's style. (This is
 * a real gap, unlike the retired `NativeTabBar`, which had a native alternative in `NativeSegmentedControl`.)
 *
 * State-driven: it uses a Compose [PagerState] (remembered by default from [pageCount]). Pass your own [state] to
 * drive it and pair it with [NativePageControl] for the dots. [pageContent] is the page slot; [contentPadding] /
 * [pageSpacing] / [userScrollEnabled] / [key] are overridable. Defaults show one full-width page, no peeking.
 *
 * `val state = rememberPagerState { items.size }`
 * `NativePager(items.size, state = state) { page -> PageCard(items[page]) }`
 * `NativePageControl(items.size, state.currentPage, onCurrentPageChange = { scope.launch { state.animateScrollToPage(it) } })`
 */
@Composable
public fun NativePager(
    pageCount: Int,
    modifier: Modifier = Modifier,
    state: PagerState = rememberPagerState { pageCount },
    contentPadding: PaddingValues = PaddingValues(0.dp),
    pageSpacing: Dp = 0.dp,
    userScrollEnabled: Boolean = true,
    key: ((page: Int) -> Any)? = null,
    pageContent: @Composable PagerScope.(page: Int) -> Unit,
) {
    HorizontalPager(
        state = state,
        modifier = modifier,
        contentPadding = contentPadding,
        pageSpacing = pageSpacing,
        userScrollEnabled = userScrollEnabled,
        key = key,
        pageContent = pageContent,
    )
}
