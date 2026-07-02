package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerScope
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A horizontally swipeable pager for carousels, onboarding, and image galleries. **Compose-drawn on both
 * platforms** — no UIKit control hosts arbitrary Compose pages, and Compose Foundation's pager supplies the
 * swipe/fling physics, so this is a thin, customizable wrapper over `HorizontalPager` in the kit's style. (This is
 * a real gap, unlike the retired `NativeTabBar`, which had a native alternative in `NativeSegmentedControl`.)
 *
 * This overload **owns its state**: [pageCount] feeds an internal `PagerState`. To drive the pager from
 * outside (bind [NativePageControl] to the dots, `animateScrollToPage`…), use the [PagerState] overload —
 * the state's `pageCount` lambda is then the only source of the count, so the two can never disagree.
 * [pageContent] is the page slot; [contentPadding]/[pageSpacing]/[pageSize]/[beyondViewportPageCount]/
 * [userScrollEnabled]/[key] are overridable. Defaults show one full-width page, no peeking.
 *
 * `NativePager(items.size) { page -> PageCard(items[page]) }`
 */
@Composable
public fun NativePager(
    pageCount: Int,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    pageSize: PageSize = PageSize.Fill,
    pageSpacing: Dp = 0.dp,
    beyondViewportPageCount: Int = 0,
    userScrollEnabled: Boolean = true,
    key: ((page: Int) -> Any)? = null,
    testTag: String? = null,
    pageContent: @Composable PagerScope.(page: Int) -> Unit,
) {
    NativePager(
        state = rememberPagerState { pageCount },
        modifier = modifier,
        contentPadding = contentPadding,
        pageSize = pageSize,
        pageSpacing = pageSpacing,
        beyondViewportPageCount = beyondViewportPageCount,
        userScrollEnabled = userScrollEnabled,
        key = key,
        testTag = testTag,
        pageContent = pageContent,
    )
}

/**
 * [NativePager] driven by a caller-owned [state] — pair it with [NativePageControl] for the dots. The page
 * count lives in the state's `pageCount` lambda (there is deliberately no count parameter here: a separate
 * count was silently ignored when a state was passed — one source of truth). See the [pageCount] overload
 * for a self-contained pager and the shared parameter docs.
 *
 * `val state = rememberPagerState { items.size }`
 * `NativePager(state) { page -> PageCard(items[page]) }`
 * `NativePageControl(items.size, state.currentPage, onCurrentPageChange = { scope.launch { state.animateScrollToPage(it) } })`
 */
@Composable
public fun NativePager(
    state: PagerState,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    pageSize: PageSize = PageSize.Fill,
    pageSpacing: Dp = 0.dp,
    beyondViewportPageCount: Int = 0,
    userScrollEnabled: Boolean = true,
    key: ((page: Int) -> Any)? = null,
    testTag: String? = null,
    pageContent: @Composable PagerScope.(page: Int) -> Unit,
) {
    var m = modifier
    testTag?.let { m = m.testTag(it) }
    HorizontalPager(
        state = state,
        modifier = m,
        contentPadding = contentPadding,
        pageSize = pageSize,
        beyondViewportPageCount = beyondViewportPageCount,
        pageSpacing = pageSpacing,
        userScrollEnabled = userScrollEnabled,
        key = key,
        pageContent = pageContent,
    )
}
