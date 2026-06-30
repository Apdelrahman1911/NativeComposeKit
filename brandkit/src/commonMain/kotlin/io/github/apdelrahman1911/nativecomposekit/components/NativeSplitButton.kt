package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeButtonColors
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeButtonSize
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeButtonVariant
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeIcon
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeInsets
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeInteropTouch
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeMenu
import io.github.apdelrahman1911.nativecomposekit.components.model.ResolvedButtonStyle
import io.github.apdelrahman1911.nativecomposekit.theme.NativeTheme

/**
 * Split button — a primary action segment plus a chevron segment that opens a [menu]. Tapping the label
 * fires [onPrimaryClick]; tapping the chevron presents the menu (native `UIMenu` on iOS, anchored
 * `DropdownMenu` on Android). The two segments share the [variant] colors with a hairline divider and a
 * single rounded outer outline. Use it when a button has one main action and related secondary actions.
 */
@Composable
public fun NativeSplitButton(
    text: String,
    onPrimaryClick: () -> Unit,
    menu: NativeMenu,
    modifier: Modifier = Modifier,
    variant: NativeButtonVariant = NativeButtonVariant.Primary,
    size: NativeButtonSize = NativeButtonSize.Medium,
    enabled: Boolean = true,
    loading: Boolean = false,
    leadingIcon: NativeIcon? = null,
    cornerRadius: Dp? = null,
    colorsOverride: NativeButtonColors? = null,
    textStyleOverride: TextStyle? = null,
    touch: NativeInteropTouch = NativeInteropTouch.Cooperative,
    contentDescription: String? = null,
    testTag: String? = null,
) {
    val resolved = resolveSplitButtonStyle(variant, size, enabled, cornerRadius, colorsOverride, textStyleOverride)
    PlatformNativeSplitButton(
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
    variant: NativeButtonVariant,
    size: NativeButtonSize,
    enabled: Boolean,
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
    val baseText = when (size) {
        NativeButtonSize.Small -> MaterialTheme.typography.labelLarge
        NativeButtonSize.Medium -> MaterialTheme.typography.titleSmall
        NativeButtonSize.Large -> MaterialTheme.typography.titleMedium
    }.copy(fontWeight = FontWeight.Medium, color = colors.content)
    val textStyle = if (textStyleOverride != null) baseText.merge(textStyleOverride) else baseText
    return ResolvedButtonStyle(
        variant = variant,
        colors = colors,
        height = height,
        insets = NativeInsets(padH, padV, padH, padV),
        cornerRadius = cornerRadius ?: tokens.cornerMedium,
        iconSpacing = tokens.spacingSm,
        textStyle = textStyle,
    )
}

/** Native split-button renderer. Android → two Material buttons + DropdownMenu; iOS → two `UIButton`s. */
@Composable
internal expect fun PlatformNativeSplitButton(
    text: String,
    onPrimaryClick: () -> Unit,
    menu: NativeMenu,
    modifier: Modifier,
    style: ResolvedButtonStyle,
    enabled: Boolean,
    loading: Boolean,
    leadingIcon: NativeIcon?,
    touch: NativeInteropTouch,
    contentDescription: String?,
    testTag: String?,
)
