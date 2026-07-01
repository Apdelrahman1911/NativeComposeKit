package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow

/**
 * An **in-content** tab strip for switching views within a screen (e.g. Overview / Chapters / Comments) —
 * distinct from the app's bottom tab bar (that's the native chrome shell). **Compose-drawn
 * on both platforms** (a Material `PrimaryTabRow` with a moving indicator, themed by AppTheme). Single
 * selection across [tabs]; [onSelectedIndexChange] reports the tapped index. Labels are single-line/ellipsized.
 *
 * `NativeTabBar(listOf("Overview","Chapters"), selectedIndex = tab, onSelectedIndexChange = { tab = it })`
 *
 * **Deprecated (kit thesis — native-per-platform).** This is a Material-only, Compose-on-both wrapper with no
 * native iOS renderer, so it doesn't serve the kit's "most-native-per-platform" goal. In-content selection is
 * already covered natively by [NativeSegmentedControl] (a real `UISegmentedControl` on iOS), which is the
 * idiomatic iOS in-content switcher. Migrate to [NativeSegmentedControl]; this will be removed in a later release.
 */
@Deprecated(
    message = "Material-only wrapper with no native iOS renderer. Use NativeSegmentedControl (native " +
        "UISegmentedControl on iOS) for in-content selection.",
    replaceWith = ReplaceWith(
        "NativeSegmentedControl(options = tabs, selectedIndex = selectedIndex, " +
            "onSelectedIndexChange = onSelectedIndexChange, modifier = modifier, testTag = testTag)",
    ),
    level = DeprecationLevel.WARNING,
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun NativeTabBar(
    tabs: List<String>,
    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    testTag: String? = null,
) {
    var m = modifier
    testTag?.let { m = m.testTag(it) }
    PrimaryTabRow(
        selectedTabIndex = selectedIndex.coerceIn(0, (tabs.size - 1).coerceAtLeast(0)),
        modifier = m,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary,
    ) {
        tabs.forEachIndexed { index, label ->
            Tab(
                selected = index == selectedIndex,
                onClick = { onSelectedIndexChange(index) },
                text = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
            )
        }
    }
}
