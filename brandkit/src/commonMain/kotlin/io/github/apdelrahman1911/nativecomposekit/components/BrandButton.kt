package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import io.github.apdelrahman1911.nativecomposekit.components.model.BrandButtonColors
import io.github.apdelrahman1911.nativecomposekit.components.model.BrandButtonShape
import io.github.apdelrahman1911.nativecomposekit.components.model.BrandButtonSize
import io.github.apdelrahman1911.nativecomposekit.components.model.BrandButtonVariant
import io.github.apdelrahman1911.nativecomposekit.components.model.BrandIcon
import io.github.apdelrahman1911.nativecomposekit.components.model.BrandInsets
import io.github.apdelrahman1911.nativecomposekit.components.model.BrandInteropTouch
import io.github.apdelrahman1911.nativecomposekit.components.model.BrandMenu
import io.github.apdelrahman1911.nativecomposekit.components.model.ResolvedButtonStyle
import io.github.apdelrahman1911.nativecomposekit.theme.BrandTheme

/**
 * Brand button. Defaults (colors, height, padding, radius, text style) come from AppTheme keyed by
 * [variant], [size], and [shape]; the override parameters keep it flexible enough for the whole app.
 * Renders the most native control per platform — a Material 3 button on Android, a `UIButton` on iOS.
 *
 * Supply [leadingIcon] and/or [trailingIcon] together — both render in their true positions. Attach a
 * [menu] to turn the button into a pull-down menu trigger: tapping opens the menu (native `UIMenu` /
 * Material `DropdownMenu`) and a chevron is appended automatically. For a button that performs an action
 * *and* offers a menu, use `BrandSplitButton`.
 */
@Composable
public fun BrandButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: BrandButtonVariant = BrandButtonVariant.Primary,
    size: BrandButtonSize = BrandButtonSize.Medium,
    shape: BrandButtonShape = BrandButtonShape.Rounded,
    enabled: Boolean = true,
    loading: Boolean = false,
    fullWidth: Boolean = false,
    leadingIcon: BrandIcon? = null,
    trailingIcon: BrandIcon? = null,
    menu: BrandMenu? = null,
    contentPadding: PaddingValues? = null,
    cornerRadius: Dp? = null,
    colorsOverride: BrandButtonColors? = null,
    textStyleOverride: TextStyle? = null,
    touch: BrandInteropTouch = BrandInteropTouch.Cooperative,
    contentDescription: String? = null,
    testTag: String? = null,
) {
    val resolved = resolveButtonStyle(
        variant, size, shape, enabled, contentPadding, cornerRadius, colorsOverride, textStyleOverride,
    )
    PlatformBrandButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        style = resolved,
        enabled = enabled,
        loading = loading,
        fullWidth = fullWidth,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        menu = menu,
        touch = touch,
        contentDescription = contentDescription,
        testTag = testTag,
    )
}

@Composable
private fun resolveButtonStyle(
    variant: BrandButtonVariant,
    size: BrandButtonSize,
    shape: BrandButtonShape,
    enabled: Boolean,
    contentPadding: PaddingValues?,
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

    val insets = if (contentPadding != null) {
        val ld = LocalLayoutDirection.current
        BrandInsets(
            start = contentPadding.calculateStartPadding(ld),
            top = contentPadding.calculateTopPadding(),
            end = contentPadding.calculateEndPadding(ld),
            bottom = contentPadding.calculateBottomPadding(),
        )
    } else {
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
        BrandInsets(padH, padV, padH, padV)
    }

    val baseText = when (size) {
        BrandButtonSize.Small -> MaterialTheme.typography.labelLarge
        BrandButtonSize.Medium -> MaterialTheme.typography.titleSmall
        BrandButtonSize.Large -> MaterialTheme.typography.titleMedium
    }.copy(fontWeight = FontWeight.Medium, color = colors.content)
    val textStyle = if (textStyleOverride != null) baseText.merge(textStyleOverride) else baseText

    // An explicit cornerRadius always wins; otherwise shape decides (Pill = capsule).
    val resolvedCorner = cornerRadius ?: when (shape) {
        BrandButtonShape.Rounded -> tokens.cornerMedium
        BrandButtonShape.Pill -> height / 2f
    }

    return ResolvedButtonStyle(
        variant = variant,
        colors = colors,
        height = height,
        insets = insets,
        cornerRadius = resolvedCorner,
        iconSpacing = tokens.spacingSm,
        textStyle = textStyle,
    )
}

/** The theme colors for a [variant]. Shared by BrandButton, BrandIconButton, and BrandSplitButton. */
internal fun brandVariantColors(variant: BrandButtonVariant, scheme: ColorScheme): BrandButtonColors =
    when (variant) {
        BrandButtonVariant.Primary -> BrandButtonColors(scheme.primary, scheme.onPrimary)
        BrandButtonVariant.Secondary -> BrandButtonColors(scheme.secondaryContainer, scheme.onSecondaryContainer)
        BrandButtonVariant.Tertiary -> BrandButtonColors(Color.Transparent, scheme.primary)
        BrandButtonVariant.Outline -> BrandButtonColors(Color.Transparent, scheme.primary, scheme.outline)
        BrandButtonVariant.Destructive -> BrandButtonColors(scheme.error, scheme.onError)
    }

/** Variant colors with the disabled tones applied when [enabled] is false. The single color entry point. */
internal fun brandButtonColors(
    variant: BrandButtonVariant,
    enabled: Boolean,
    override: BrandButtonColors?,
    scheme: ColorScheme,
): BrandButtonColors {
    val base = override ?: brandVariantColors(variant, scheme)
    return if (enabled) base else disabledColors(base, scheme)
}

/** Material-style disabled tones, derived from the theme (never hardcoded in the renderers). */
internal fun disabledColors(base: BrandButtonColors, scheme: ColorScheme): BrandButtonColors {
    val container = if (base.container == Color.Transparent) Color.Transparent
    else scheme.onSurface.copy(alpha = 0.12f)
    val border = if (base.border.isSpecified) scheme.onSurface.copy(alpha = 0.12f) else base.border
    return BrandButtonColors(container, scheme.onSurface.copy(alpha = 0.38f), border)
}

/** Native button renderer. Android → Material 3 button; iOS → `UIButton` via `UIKitView`. */
@Composable
internal expect fun PlatformBrandButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier,
    style: ResolvedButtonStyle,
    enabled: Boolean,
    loading: Boolean,
    fullWidth: Boolean,
    leadingIcon: BrandIcon?,
    trailingIcon: BrandIcon?,
    menu: BrandMenu?,
    touch: BrandInteropTouch,
    contentDescription: String?,
    testTag: String?,
)
