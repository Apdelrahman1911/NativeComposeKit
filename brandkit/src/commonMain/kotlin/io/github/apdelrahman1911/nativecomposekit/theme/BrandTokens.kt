package io.github.apdelrahman1911.nativecomposekit.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Brand design tokens that sit ALONGSIDE MaterialTheme — the things M3 doesn't model directly
 * (per-size control heights, corner radii, the spacing scale). Part of [AppTheme]; read via
 * [BrandTheme.tokens]. Swap by providing a different instance to [LocalBrandTokens].
 */
@Immutable
public data class BrandTokens(
    val buttonHeightSmall: Dp = 36.dp,
    val buttonHeightMedium: Dp = 44.dp,
    val buttonHeightLarge: Dp = 52.dp,
    val buttonPadHSmall: Dp = 12.dp,
    val buttonPadHMedium: Dp = 16.dp,
    val buttonPadHLarge: Dp = 22.dp,
    val buttonPadVSmall: Dp = 6.dp,
    val buttonPadVMedium: Dp = 10.dp,
    val buttonPadVLarge: Dp = 14.dp,
    val cornerSmall: Dp = 8.dp,
    val cornerMedium: Dp = 12.dp,
    val fieldMinHeight: Dp = 52.dp,
    val spacingXs: Dp = 4.dp,
    val spacingSm: Dp = 8.dp,
    val spacingMd: Dp = 16.dp,
    val spacingLg: Dp = 24.dp,
)

public val LocalBrandTokens: ProvidableCompositionLocal<BrandTokens> = staticCompositionLocalOf { BrandTokens() }

/** Ergonomic accessor: `BrandTheme.tokens.spacingMd`, `BrandTheme.statusColors.success`. */
public object BrandTheme {
    public val tokens: BrandTokens
        @Composable
        @ReadOnlyComposable
        get() = LocalBrandTokens.current

    /** Semantic status colors (success/warning/info) for feedback surfaces; Error reuses scheme.error. */
    public val statusColors: BrandStatusColors
        @Composable
        @ReadOnlyComposable
        get() = LocalBrandStatusColors.current
}
