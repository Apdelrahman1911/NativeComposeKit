package com.ukkera.brandkit.components.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp

/** Content padding as four edge values — becomes PaddingValues on Android / insets on iOS. */
@Immutable
internal data class BrandInsets(val start: Dp, val top: Dp, val end: Dp, val bottom: Dp)

@Immutable
public data class BrandButtonColors(
    val container: Color,
    val content: Color,
    val border: Color = Color.Unspecified, // Unspecified == no border
)

/**
 * Fully resolved button styling handed to the platform renderer. The renderer reads ONLY these
 * values (already pulled from AppTheme) — it hardcodes nothing.
 */
@Immutable
internal data class ResolvedButtonStyle(
    val variant: BrandButtonVariant,
    val colors: BrandButtonColors,
    val height: Dp,
    val insets: BrandInsets,
    val cornerRadius: Dp,
    /** Gap between a leading/trailing icon and the label (from BrandTokens.spacingSm). */
    val iconSpacing: Dp,
    val textStyle: TextStyle,
)

@Immutable
public data class BrandFieldColors(
    val text: Color,
    val placeholder: Color,
    val container: Color,
    val border: Color,
    val focusedBorder: Color,
    val errorBorder: Color,
    val label: Color,
    val helper: Color,
    val error: Color,
    val cursor: Color,
)

/** Fully resolved text-field styling handed to the platform renderer. */
@Immutable
internal data class ResolvedFieldStyle(
    val colors: BrandFieldColors,
    val cornerRadius: Dp,
    val minHeight: Dp,
    val textStyle: TextStyle,
)

/**
 * Each [surface] below is the page color the control sits on (from the theme). iOS paints it behind
 * the native control so the rounded/transparent native widget doesn't reveal the interop host
 * backdrop (a white/black box in dark mode). Android ignores [surface].
 */

/** Resolved toggle/switch styling. */
@Immutable
internal data class ResolvedToggleStyle(
    val trackOnColor: Color, // track when on (UISwitch onTintColor / Material checked track)
    val thumbColor: Color,
    val surface: Color,
)

/** Resolved segmented-control styling. */
@Immutable
internal data class ResolvedSegmentedStyle(
    val selectedColor: Color,     // selected segment background
    val textColor: Color,         // unselected label color
    val selectedTextColor: Color, // selected label color
    val surface: Color,
    val textStyle: TextStyle,
)

/** Resolved slider styling. */
@Immutable
internal data class ResolvedSliderStyle(
    val activeTrackColor: Color,   // filled (minimum) track
    val inactiveTrackColor: Color, // unfilled (maximum) track
    val thumbColor: Color,
    val surface: Color,
)

/** Resolved stepper styling. */
@Immutable
internal data class ResolvedStepperStyle(
    val tint: Color, // -/+ control tint
    val surface: Color,
)

/**
 * Fully resolved styling for a feedback surface (toast / snackbar / banner / inline status, and the
 * branded iOS alert/sheet overlay). Resolved from a [BrandFeedbackStatus] against
 * [com.ukkera.brandkit.theme.BrandStatusColors] + the theme — the renderer reads ONLY these values.
 *
 * - [background]/[content] are the filled-surface pair (e.g. successContainer / onSuccessContainer).
 * - [iconTint]/[border] use the bolder status color so a leading glyph and outline read clearly,
 *   including the non-filled (outlined) inline variant where [background] is the page [surface].
 */
@Immutable
internal data class ResolvedFeedbackStyle(
    val background: Color,
    val content: Color,
    val border: Color,
    val iconTint: Color,
    val cornerRadius: Dp,
    val insets: BrandInsets,
    val textStyle: TextStyle,
    val titleTextStyle: TextStyle,
)

/**
 * Resolved progress-indicator styling. iOS `UIActivityIndicatorView`/`UIProgressView`; Android Material.
 * No `surface` backing: the spinner is transparent and the bar is opaque, so neither reveals the interop
 * host backdrop (unlike the rounded Toggle/Slider, which do need a backing).
 */
@Immutable
internal data class ResolvedProgressStyle(
    val indicator: Color, // the moving/filled part (UIProgressView progressTint / spinner color)
    val track: Color,     // the unfilled track (linear only on iOS)
)

/** Resolved search-field styling (iOS `UISearchBar`, Android Material search-styled field). */
@Immutable
internal data class ResolvedSearchStyle(
    val text: Color,
    val placeholder: Color,
    val container: Color, // Android field container (iOS uses the native search-field appearance)
    val tint: Color, // magnifier / clear / cancel / cursor tint
    val cornerRadius: Dp,
    val textStyle: TextStyle,
    /** Page color painted behind the rounded native `UISearchBar` on iOS (so its corners don't reveal the
     * interop host backdrop in dark mode); Android ignores it. */
    val surface: Color,
)

/** Resolved page-control (dots) styling (iOS `UIPageControl`, Android branded dots — no backing needed). */
@Immutable
internal data class ResolvedPageControlStyle(
    val current: Color,
    val inactive: Color,
)
