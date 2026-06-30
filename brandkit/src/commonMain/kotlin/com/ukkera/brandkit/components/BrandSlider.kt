package com.ukkera.brandkit.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ukkera.brandkit.components.internal.resolveSurfaceFill
import com.ukkera.brandkit.components.model.ResolvedSliderStyle
import com.ukkera.brandkit.theme.LocalBrandSurface

/**
 * Brand slider — a continuous value in [valueRange]. Colors come from AppTheme; renders the most
 * native control per platform — a Material 3 `Slider` on Android, a real `UISlider` on iOS.
 */
@Composable
public fun BrandSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    enabled: Boolean = true,
    contentDescription: String? = null,
    testTag: String? = null,
) {
    val scheme = MaterialTheme.colorScheme
    val style = ResolvedSliderStyle(
        activeTrackColor = scheme.primary,
        inactiveTrackColor = scheme.surfaceVariant,
        thumbColor = scheme.primary,
        // Drive the iOS control's light/dark from the surface it actually sits on (card/page/glass), not the page.
        surface = resolveSurfaceFill(LocalBrandSurface.current, scheme.background),
    )
    PlatformBrandSlider(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        min = valueRange.start,
        max = valueRange.endInclusive,
        enabled = enabled,
        style = style,
        contentDescription = contentDescription,
        testTag = testTag,
    )
}

/** Native slider renderer. Android → Material 3 `Slider`; iOS → `UISlider` via `UIKitView`. */
@Composable
internal expect fun PlatformBrandSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier,
    min: Float,
    max: Float,
    enabled: Boolean,
    style: ResolvedSliderStyle,
    contentDescription: String?,
    testTag: String?,
)
