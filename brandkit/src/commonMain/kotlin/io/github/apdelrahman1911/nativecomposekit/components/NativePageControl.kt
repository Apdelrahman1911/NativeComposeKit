package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.github.apdelrahman1911.nativecomposekit.components.model.ResolvedPageControlStyle

/**
 * A page indicator (the row of dots) for carousels, featured-manga banners, and onboarding. Renders the
 * most native control per platform: on **iOS** a real `UIPageControl` (which also supports tap-to-page);
 * on **Android** a branded row of Compose dots. Pass [onCurrentPageChange] to allow changing the page by
 * tapping the indicator (matches `UIPageControl`); leave it null for a display-only indicator.
 *
 * [currentPage] is clamped to `0..pageCount-1`. Typically paired with a pager that owns the actual paging.
 *
 * `NativePageControl(pageCount = covers.size, currentPage = page)`
 */
@Composable
public fun NativePageControl(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
    onCurrentPageChange: ((Int) -> Unit)? = null,
    color: Color? = null,
    inactiveColor: Color? = null,
    contentDescription: String? = null,
    testTag: String? = null,
) {
    val scheme = MaterialTheme.colorScheme
    val style = ResolvedPageControlStyle(
        current = color ?: scheme.primary,
        inactive = inactiveColor ?: scheme.outlineVariant,
    )
    val safeCount = pageCount.coerceAtLeast(0)
    val safeCurrent = if (safeCount == 0) 0 else currentPage.coerceIn(0, safeCount - 1)
    PlatformNativePageControl(
        pageCount = safeCount,
        currentPage = safeCurrent,
        modifier = modifier,
        onCurrentPageChange = onCurrentPageChange,
        style = style,
        contentDescription = contentDescription,
        testTag = testTag,
    )
}

/** Native page-control renderer. Android → branded Compose dots; iOS → `UIPageControl`. */
@Composable
internal expect fun PlatformNativePageControl(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier,
    onCurrentPageChange: ((Int) -> Unit)?,
    style: ResolvedPageControlStyle,
    contentDescription: String?,
    testTag: String?,
)
