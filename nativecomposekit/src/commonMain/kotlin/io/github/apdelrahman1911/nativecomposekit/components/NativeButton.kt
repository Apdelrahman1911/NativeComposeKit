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
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeButtonColors
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeButtonShape
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeButtonSize
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeButtonVariant
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeIcon
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeInsets
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeInteropTouch
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeMenu
import io.github.apdelrahman1911.nativecomposekit.components.model.ResolvedButtonStyle
import io.github.apdelrahman1911.nativecomposekit.theme.NativeTheme

/**
 * Native button. Defaults (colors, height, padding, radius, text style) come from AppTheme keyed by
 * [variant], [size], and [shape]; the override parameters keep it flexible enough for the whole app.
 * Renders the most native control per platform — a Material 3 button on Android, a `UIButton` on iOS.
 *
 * Supply [leadingIcon] and/or [trailingIcon] together — both render in their true positions. Attach a
 * [menu] to turn the button into a pull-down menu trigger: tapping opens the menu (native `UIMenu` /
 * Material `DropdownMenu`) and a chevron is appended automatically. For a button that performs an action
 * *and* offers a menu, use `NativeSplitButton`.
 */
@Composable
public fun NativeButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: NativeButtonVariant = NativeButtonVariant.Primary,
    size: NativeButtonSize = NativeButtonSize.Medium,
    shape: NativeButtonShape = NativeButtonShape.Rounded,
    enabled: Boolean = true,
    loading: Boolean = false,
    fullWidth: Boolean = false,
    leadingIcon: NativeIcon? = null,
    trailingIcon: NativeIcon? = null,
    menu: NativeMenu? = null,
    contentPadding: PaddingValues? = null,
    cornerRadius: Dp? = null,
    colorsOverride: NativeButtonColors? = null,
    textStyleOverride: TextStyle? = null,
    touch: NativeInteropTouch = NativeInteropTouch.Cooperative,
    contentDescription: String? = null,
    testTag: String? = null,
) {
    val resolved = resolveButtonStyle(
        variant, size, shape, enabled, contentPadding, cornerRadius, colorsOverride, textStyleOverride,
    )
    PlatformNativeButton(
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
    variant: NativeButtonVariant,
    size: NativeButtonSize,
    shape: NativeButtonShape,
    enabled: Boolean,
    contentPadding: PaddingValues?,
    cornerRadius: Dp?,
    colorsOverride: NativeButtonColors?,
    textStyleOverride: TextStyle?,
): ResolvedButtonStyle {
    val tokens = NativeTheme.tokens
    val scheme = MaterialTheme.colorScheme

    val colors = nativeButtonColors(variant, enabled, colorsOverride, scheme)

    val height = when (size) {
        NativeButtonSize.Small -> tokens.buttonHeightSmall
        NativeButtonSize.Medium -> tokens.buttonHeightMedium
        NativeButtonSize.Large -> tokens.buttonHeightLarge
    }

    val insets = if (contentPadding != null) {
        val ld = LocalLayoutDirection.current
        NativeInsets(
            start = contentPadding.calculateStartPadding(ld),
            top = contentPadding.calculateTopPadding(),
            end = contentPadding.calculateEndPadding(ld),
            bottom = contentPadding.calculateBottomPadding(),
        )
    } else {
        val padH = when (size) {
            NativeButtonSize.Small -> tokens.buttonPadHSmall
            NativeButtonSize.Medium -> tokens.buttonPadHMedium
            NativeButtonSize.Large -> tokens.buttonPadHLarge
        }
        val padV = when (size) {
            NativeButtonSize.Small -> tokens.buttonPadVSmall
            NativeButtonSize.Medium -> tokens.buttonPadVMedium
            NativeButtonSize.Large -> tokens.buttonPadVLarge
        }
        NativeInsets(padH, padV, padH, padV)
    }

    val baseText = when (size) {
        NativeButtonSize.Small -> MaterialTheme.typography.labelLarge
        NativeButtonSize.Medium -> MaterialTheme.typography.titleSmall
        NativeButtonSize.Large -> MaterialTheme.typography.titleMedium
    }.copy(fontWeight = FontWeight.Medium, color = colors.content)
    val textStyle = if (textStyleOverride != null) baseText.merge(textStyleOverride) else baseText

    // An explicit cornerRadius always wins; otherwise shape decides (Pill = capsule).
    val resolvedCorner = cornerRadius ?: when (shape) {
        NativeButtonShape.Rounded -> tokens.cornerMedium
        NativeButtonShape.Pill -> height / 2f
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

/** The theme colors for a [variant]. Shared by NativeButton, NativeIconButton, and NativeSplitButton. */
internal fun nativeVariantColors(variant: NativeButtonVariant, scheme: ColorScheme): NativeButtonColors =
    when (variant) {
        NativeButtonVariant.Primary -> NativeButtonColors(scheme.primary, scheme.onPrimary)
        NativeButtonVariant.Secondary -> NativeButtonColors(scheme.secondaryContainer, scheme.onSecondaryContainer)
        NativeButtonVariant.Tertiary -> NativeButtonColors(Color.Transparent, scheme.primary)
        NativeButtonVariant.Outline -> NativeButtonColors(Color.Transparent, scheme.primary, scheme.outline)
        NativeButtonVariant.Destructive -> NativeButtonColors(scheme.error, scheme.onError)
    }

/** Variant colors with the disabled tones applied when [enabled] is false. The single color entry point. */
internal fun nativeButtonColors(
    variant: NativeButtonVariant,
    enabled: Boolean,
    override: NativeButtonColors?,
    scheme: ColorScheme,
): NativeButtonColors {
    val base = override ?: nativeVariantColors(variant, scheme)
    return if (enabled) base else disabledColors(base, scheme)
}

/** Material-style disabled tones, derived from the theme (never hardcoded in the renderers). */
internal fun disabledColors(base: NativeButtonColors, scheme: ColorScheme): NativeButtonColors {
    val container = if (base.container == Color.Transparent) Color.Transparent
    else scheme.onSurface.copy(alpha = 0.12f)
    val border = if (base.border.isSpecified) scheme.onSurface.copy(alpha = 0.12f) else base.border
    return NativeButtonColors(container, scheme.onSurface.copy(alpha = 0.38f), border)
}

/** Native button renderer. Android → Material 3 button; iOS → `UIButton` via `UIKitView`. */
@Composable
internal expect fun PlatformNativeButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier,
    style: ResolvedButtonStyle,
    enabled: Boolean,
    loading: Boolean,
    fullWidth: Boolean,
    leadingIcon: NativeIcon?,
    trailingIcon: NativeIcon?,
    menu: NativeMenu?,
    touch: NativeInteropTouch,
    contentDescription: String?,
    testTag: String?,
)
