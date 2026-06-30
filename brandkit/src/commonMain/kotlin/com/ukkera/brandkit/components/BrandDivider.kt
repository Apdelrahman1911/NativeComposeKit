package com.ukkera.brandkit.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Divider direction. [Horizontal] separates stacked rows; [Vertical] separates side-by-side content. */
public enum class BrandDividerOrientation { Horizontal, Vertical }

/**
 * A hairline separator (Compose-drawn on both platforms). Defaults to a 1dp `outlineVariant` line. For a
 * [BrandDividerOrientation.Horizontal] divider the [startIndent]/[endIndent] insets are **layout-direction
 * aware** (an iOS-style inset separator that begins after a list item's text aligns correctly in RTL); for
 * a [BrandDividerOrientation.Vertical] divider they map to top/bottom insets (a vertical axis — so they are
 * direction-neutral).
 *
 * `BrandDivider()` — full-width hairline. `BrandDivider(startIndent = 56.dp)` — inset under a leading icon.
 */
@Composable
public fun BrandDivider(
    modifier: Modifier = Modifier,
    orientation: BrandDividerOrientation = BrandDividerOrientation.Horizontal,
    thickness: Dp = 1.dp,
    color: Color? = null,
    startIndent: Dp = 0.dp,
    endIndent: Dp = 0.dp,
    testTag: String? = null,
) {
    val lineColor = color ?: MaterialTheme.colorScheme.outlineVariant
    var m = modifier
    testTag?.let { m = m.testTag(it) }
    when (orientation) {
        BrandDividerOrientation.Horizontal -> HorizontalDivider(
            modifier = m.padding(start = startIndent, end = endIndent),
            thickness = thickness,
            color = lineColor,
        )
        BrandDividerOrientation.Vertical -> VerticalDivider(
            modifier = m.padding(top = startIndent, bottom = endIndent),
            thickness = thickness,
            color = lineColor,
        )
    }
}
