package com.ukkera.brandkit.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/** iOS-only [BrandColorWell] knobs. Each is a documented no-op on Android (its presets are opaque). */
@Immutable
public data class BrandColorWellIosOptions(
    /** Lets the native `UIColorWell` picker edit opacity. Ignored on Android (preset swatches are opaque). */
    val supportsAlpha: Boolean = true,
)

/**
 * A color picker swatch. On **iOS** this is a real `UIColorWell` (the system color picker — full spectrum,
 * eyedropper, opacity). On **Android** there is no system color picker, so this is a swatch that opens a
 * dialog of preset colors (a documented platform difference; a full HSV picker can be added later).
 * iOS-only knobs (alpha support) live in [ios] = [BrandColorWellIosOptions]; the Android presets are opaque.
 *
 * `BrandColorWell(color = highlight, onColorChange = { highlight = it })`
 */
@Composable
public fun BrandColorWell(
    color: Color,
    onColorChange: (Color) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentDescription: String? = null,
    testTag: String? = null,
    ios: BrandColorWellIosOptions = BrandColorWellIosOptions(),
) {
    PlatformBrandColorWell(
        color = color,
        onColorChange = onColorChange,
        modifier = modifier,
        enabled = enabled,
        supportsAlpha = ios.supportsAlpha,
        contentDescription = contentDescription,
        testTag = testTag,
    )
}

@Composable
internal expect fun PlatformBrandColorWell(
    color: Color,
    onColorChange: (Color) -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    supportsAlpha: Boolean,
    contentDescription: String?,
    testTag: String?,
)
