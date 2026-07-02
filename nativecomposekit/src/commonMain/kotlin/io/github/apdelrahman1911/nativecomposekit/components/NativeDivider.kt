package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.apdelrahman1911.nativecomposekit.components.internal.resolveSurfaceFill
import io.github.apdelrahman1911.nativecomposekit.theme.LocalNativeSurface

/** Divider direction. [Horizontal] separates stacked rows; [Vertical] separates side-by-side content. */
public enum class NativeDividerOrientation { Horizontal, Vertical }

/**
 * A hairline separator (Compose-drawn on both platforms). Defaults to a 1dp `outlineVariant` line. For a
 * [NativeDividerOrientation.Horizontal] divider the [startIndent]/[endIndent] insets are **layout-direction
 * aware** (an iOS-style inset separator that begins after a list item's text aligns correctly in RTL); for
 * a [NativeDividerOrientation.Vertical] divider they map to top/bottom insets (a vertical axis — so they are
 * direction-neutral).
 *
 * `NativeDivider()` — full-width hairline. `NativeDivider(startIndent = 56.dp)` — inset under a leading icon.
 */
@Composable
public fun NativeDivider(
    modifier: Modifier = Modifier,
    orientation: NativeDividerOrientation = NativeDividerOrientation.Horizontal,
    thickness: Dp = 1.dp,
    color: Color? = null,
    startIndent: Dp = 0.dp,
    endIndent: Dp = 0.dp,
    testTag: String? = null,
) {
    // Surface-adaptive default (rule 2, docs/design-system-rules.md): the M3 `outlineVariant` hairline equals
    // `surfaceVariant` in the baseline DARK scheme, so it vanishes inside a Filled card. Blending the published
    // surface toward `onSurface` reads the same on the page and stays visible on any container in both modes.
    val lineColor = color ?: run {
        val surface = resolveSurfaceFill(LocalNativeSurface.current, MaterialTheme.colorScheme.surface)
        lerp(surface, MaterialTheme.colorScheme.onSurface, 0.12f)
    }
    var m = modifier
    testTag?.let { m = m.testTag(it) }
    when (orientation) {
        NativeDividerOrientation.Horizontal -> HorizontalDivider(
            modifier = m.padding(start = startIndent, end = endIndent),
            thickness = thickness,
            color = lineColor,
        )
        NativeDividerOrientation.Vertical -> VerticalDivider(
            modifier = m.padding(top = startIndent, bottom = endIndent),
            thickness = thickness,
            color = lineColor,
        )
    }
}
