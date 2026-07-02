package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import io.github.apdelrahman1911.nativecomposekit.theme.LocalNativeStrings

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
 * Accessibility: the pull gesture is invisible to screen readers, so the container exposes a localized
 * **Refresh** custom action, and announces the in-flight state while [isRefreshing].
 *
 * [indicatorColor]/[indicatorContainerColor] restyle the spinner (theme defaults otherwise).
 *
 * `NativePullRefresh(isRefreshing = loading, onRefresh = ::reload) { LazyColumn { … } }`
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun NativePullRefresh(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    indicatorColor: Color = Color.Unspecified,
    indicatorContainerColor: Color = Color.Unspecified,
    testTag: String? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val strings = LocalNativeStrings.current
    var m = modifier.semantics {
        customActions = listOf(CustomAccessibilityAction(strings.refresh) { onRefresh(); true })
        if (isRefreshing) stateDescription = strings.refreshing
    }
    testTag?.let { m = m.testTag(it) }

    val state = rememberPullToRefreshState()
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = m,
        state = state,
        indicator = {
            PullToRefreshDefaults.Indicator(
                state = state,
                isRefreshing = isRefreshing,
                modifier = Modifier.align(Alignment.TopCenter),
                color = indicatorColor.takeOrElse { MaterialTheme.colorScheme.primary },
                containerColor = indicatorContainerColor.takeOrElse { MaterialTheme.colorScheme.surfaceContainerHigh },
            )
        },
    ) {
        content()
    }
}
