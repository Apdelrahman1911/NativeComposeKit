package com.ukkera.brandkit.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.ukkera.brandkit.components.model.BrandButtonColors
import com.ukkera.brandkit.components.model.BrandButtonSize
import com.ukkera.brandkit.components.model.BrandButtonVariant
import com.ukkera.brandkit.components.model.BrandIcon
import com.ukkera.brandkit.components.model.BrandInsets
import com.ukkera.brandkit.components.model.BrandInteropTouch
import com.ukkera.brandkit.components.model.BrandMenu
import com.ukkera.brandkit.components.model.ResolvedButtonStyle
import com.ukkera.brandkit.theme.BrandTheme

/**
 * Icon-only brand button — a square/circular tap target with no label. Defaults to the [variant]'s theme
 * colors keyed by [size]; circular by default (override with [cornerRadius]). Renders the most native
 * control per platform — a Material 3 icon button on Android, a `UIButton` on iOS. Attach a [menu] to
 * present a native pull-down on tap (e.g. an overflow "…" button).
 *
 * [contentDescription] is **required** — an icon-only control has no visible label, so an accessible name
 * is mandatory (VoiceOver/TalkBack would otherwise announce an unnamed button). It maps to the button's
 * `accessibilityLabel` (iOS) / merged `contentDescription` (Android).
 */
@Composable
public fun BrandIconButton(
    icon: BrandIcon,
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    variant: BrandButtonVariant = BrandButtonVariant.Tertiary,
    size: BrandButtonSize = BrandButtonSize.Medium,
    enabled: Boolean = true,
    loading: Boolean = false,
    menu: BrandMenu? = null,
    cornerRadius: Dp? = null,
    colorsOverride: BrandButtonColors? = null,
    touch: BrandInteropTouch = BrandInteropTouch.Cooperative,
    testTag: String? = null,
) {
    val resolved = resolveIconButtonStyle(variant, size, enabled, cornerRadius, colorsOverride)
    PlatformBrandIconButton(
        icon = icon,
        onClick = onClick,
        modifier = modifier,
        style = resolved,
        enabled = enabled,
        loading = loading,
        menu = menu,
        touch = touch,
        contentDescription = contentDescription,
        testTag = testTag,
    )
}

@Composable
private fun resolveIconButtonStyle(
    variant: BrandButtonVariant,
    size: BrandButtonSize,
    enabled: Boolean,
    cornerRadius: Dp?,
    colorsOverride: BrandButtonColors?,
): ResolvedButtonStyle {
    val tokens = BrandTheme.tokens
    val scheme = MaterialTheme.colorScheme
    val colors = brandButtonColors(variant, enabled, colorsOverride, scheme)
    val side = when (size) {
        BrandButtonSize.Small -> tokens.buttonHeightSmall
        BrandButtonSize.Medium -> tokens.buttonHeightMedium
        BrandButtonSize.Large -> tokens.buttonHeightLarge
    }
    val pad = tokens.spacingSm
    return ResolvedButtonStyle(
        variant = variant,
        colors = colors,
        height = side,
        insets = BrandInsets(pad, pad, pad, pad),
        cornerRadius = cornerRadius ?: side / 2f, // circular by default
        iconSpacing = tokens.spacingSm,
        textStyle = MaterialTheme.typography.labelLarge.copy(color = colors.content), // unused (no label)
    )
}

/** Native icon-button renderer. Android → Material 3 icon button; iOS → square `UIButton` via `UIKitView`. */
@Composable
internal expect fun PlatformBrandIconButton(
    icon: BrandIcon,
    onClick: () -> Unit,
    modifier: Modifier,
    style: ResolvedButtonStyle,
    enabled: Boolean,
    loading: Boolean,
    menu: BrandMenu?,
    touch: BrandInteropTouch,
    contentDescription: String?,
    testTag: String?,
)
