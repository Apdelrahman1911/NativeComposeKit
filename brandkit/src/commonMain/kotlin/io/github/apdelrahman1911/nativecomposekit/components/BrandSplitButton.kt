package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import io.github.apdelrahman1911.nativecomposekit.components.model.BrandButtonColors
import io.github.apdelrahman1911.nativecomposekit.components.model.BrandButtonSize
import io.github.apdelrahman1911.nativecomposekit.components.model.BrandButtonVariant
import io.github.apdelrahman1911.nativecomposekit.components.model.BrandIcon
import io.github.apdelrahman1911.nativecomposekit.components.model.BrandInsets
import io.github.apdelrahman1911.nativecomposekit.components.model.BrandInteropTouch
import io.github.apdelrahman1911.nativecomposekit.components.model.BrandMenu
import io.github.apdelrahman1911.nativecomposekit.components.model.ResolvedButtonStyle
import io.github.apdelrahman1911.nativecomposekit.theme.BrandTheme

/**
 * Split button — a primary action segment plus a chevron segment that opens a [menu]. Tapping the label
 * fires [onPrimaryClick]; tapping the chevron presents the menu (native `UIMenu` on iOS, anchored
 * `DropdownMenu` on Android). The two segments share the [variant] colors with a hairline divider and a
 * single rounded outer outline. Use it when a button has one main action and related secondary actions.
 */
@Composable
public fun BrandSplitButton(
    text: String,
    onPrimaryClick: () -> Unit,
    menu: BrandMenu,
    modifier: Modifier = Modifier,
    variant: BrandButtonVariant = BrandButtonVariant.Primary,
    size: BrandButtonSize = BrandButtonSize.Medium,
    enabled: Boolean = true,
    loading: Boolean = false,
    leadingIcon: BrandIcon? = null,
    cornerRadius: Dp? = null,
    colorsOverride: BrandButtonColors? = null,
    textStyleOverride: TextStyle? = null,
    touch: BrandInteropTouch = BrandInteropTouch.Cooperative,
    contentDescription: String? = null,
    testTag: String? = null,
) {
    val resolved = resolveSplitButtonStyle(variant, size, enabled, cornerRadius, colorsOverride, textStyleOverride)
    PlatformBrandSplitButton(
        text = text,
        onPrimaryClick = onPrimaryClick,
        menu = menu,
        modifier = modifier,
        style = resolved,
        enabled = enabled,
        loading = loading,
        leadingIcon = leadingIcon,
        touch = touch,
        contentDescription = contentDescription,
        testTag = testTag,
    )
}

@Composable
private fun resolveSplitButtonStyle(
    variant: BrandButtonVariant,
    size: BrandButtonSize,
    enabled: Boolean,
    cornerRadius: Dp?,
    colorsOverride: BrandButtonColors?,
    textStyleOverride: TextStyle?,
): ResolvedButtonStyle {
    val tokens = BrandTheme.tokens
    val scheme = MaterialTheme.colorScheme
    val colors = brandButtonColors(variant, enabled, colorsOverride, scheme)
    val height = when (size) {
        BrandButtonSize.Small -> tokens.buttonHeightSmall
        BrandButtonSize.Medium -> tokens.buttonHeightMedium
        BrandButtonSize.Large -> tokens.buttonHeightLarge
    }
    val padH = when (size) {
        BrandButtonSize.Small -> tokens.buttonPadHSmall
        BrandButtonSize.Medium -> tokens.buttonPadHMedium
        BrandButtonSize.Large -> tokens.buttonPadHLarge
    }
    val padV = when (size) {
        BrandButtonSize.Small -> tokens.buttonPadVSmall
        BrandButtonSize.Medium -> tokens.buttonPadVMedium
        BrandButtonSize.Large -> tokens.buttonPadVLarge
    }
    val baseText = when (size) {
        BrandButtonSize.Small -> MaterialTheme.typography.labelLarge
        BrandButtonSize.Medium -> MaterialTheme.typography.titleSmall
        BrandButtonSize.Large -> MaterialTheme.typography.titleMedium
    }.copy(fontWeight = FontWeight.Medium, color = colors.content)
    val textStyle = if (textStyleOverride != null) baseText.merge(textStyleOverride) else baseText
    return ResolvedButtonStyle(
        variant = variant,
        colors = colors,
        height = height,
        insets = BrandInsets(padH, padV, padH, padV),
        cornerRadius = cornerRadius ?: tokens.cornerMedium,
        iconSpacing = tokens.spacingSm,
        textStyle = textStyle,
    )
}

/** Native split-button renderer. Android → two Material buttons + DropdownMenu; iOS → two `UIButton`s. */
@Composable
internal expect fun PlatformBrandSplitButton(
    text: String,
    onPrimaryClick: () -> Unit,
    menu: BrandMenu,
    modifier: Modifier,
    style: ResolvedButtonStyle,
    enabled: Boolean,
    loading: Boolean,
    leadingIcon: BrandIcon?,
    touch: BrandInteropTouch,
    contentDescription: String?,
    testTag: String?,
)
