package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeButtonColors
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeButtonIosBackground
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeButtonIosOptions
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeButtonSize
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeButtonVariant
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeIcon
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeInsets
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeInteropTouch
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeMenu
import io.github.apdelrahman1911.nativecomposekit.components.model.ResolvedButtonStyle
import io.github.apdelrahman1911.nativecomposekit.theme.NativeTheme

/**
 * Icon-only brand button — a square/circular tap target with no label. Defaults to the [variant]'s theme
 * colors keyed by [size]; circular by default (override with [cornerRadius]). Renders the most native
 * control per platform — a Material 3 icon button on Android, a `UIButton` on iOS. Attach a [menu] to
 * present a native pull-down on tap (e.g. an overflow "…" button) — the tap then **only** opens the
 * menu; [onClick] is not called (the same rule on both platforms).
 *
 * [contentDescription] is **required** — an icon-only control has no visible label, so an accessible name
 * is mandatory (VoiceOver/TalkBack would otherwise announce an unnamed button). It maps to the button's
 * `accessibilityLabel` (iOS) / merged `contentDescription` (Android).
 */
@Composable
public fun NativeIconButton(
    icon: NativeIcon,
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    variant: NativeButtonVariant = NativeButtonVariant.Tertiary,
    size: NativeButtonSize = NativeButtonSize.Medium,
    enabled: Boolean = true,
    loading: Boolean = false,
    menu: NativeMenu? = null,
    cornerRadius: Dp? = null,
    colorsOverride: NativeButtonColors? = null,
    touch: NativeInteropTouch = NativeInteropTouch.Cooperative,
    ios: NativeButtonIosOptions = NativeButtonIosOptions(),
    testTag: String? = null,
) {
    val resolved = resolveIconButtonStyle(variant, size, enabled, cornerRadius, colorsOverride, iosBackground = ios.background)
    PlatformNativeIconButton(
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
    variant: NativeButtonVariant,
    size: NativeButtonSize,
    enabled: Boolean,
    cornerRadius: Dp?,
    colorsOverride: NativeButtonColors?,
    iosBackground: NativeButtonIosBackground = NativeButtonIosBackground.Automatic,
): ResolvedButtonStyle {
    val tokens = NativeTheme.tokens
    val scheme = MaterialTheme.colorScheme
    val colors = nativeButtonColors(variant, enabled, colorsOverride, scheme)
    val side = when (size) {
        NativeButtonSize.Small -> tokens.buttonHeightSmall
        NativeButtonSize.Medium -> tokens.buttonHeightMedium
        NativeButtonSize.Large -> tokens.buttonHeightLarge
    }
    val pad = tokens.spacingSm
    return ResolvedButtonStyle(
        variant = variant,
        colors = colors,
        height = side,
        insets = NativeInsets(pad, pad, pad, pad),
        cornerRadius = cornerRadius ?: side / 2f, // circular by default
        iconSpacing = tokens.spacingSm,
        textStyle = MaterialTheme.typography.labelLarge.copy(color = colors.content), // unused (no label)
        iosBackground = iosBackground,
    )
}

/** Native icon-button renderer. Android → Material 3 icon button; iOS → square `UIButton` via `UIKitView`. */
@Composable
internal expect fun PlatformNativeIconButton(
    icon: NativeIcon,
    onClick: () -> Unit,
    modifier: Modifier,
    style: ResolvedButtonStyle,
    enabled: Boolean,
    loading: Boolean,
    menu: NativeMenu?,
    touch: NativeInteropTouch,
    contentDescription: String?,
    testTag: String?,
)
