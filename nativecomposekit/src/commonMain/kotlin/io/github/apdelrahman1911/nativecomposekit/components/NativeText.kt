package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeTextStyle

/**
 * Native text. Defaults come from AppTheme's type scale (via [style]); every other parameter is an
 * optional override so it is not locked to a single use case. Renders the most native primitive on
 * each platform — Compose `Text` on Android, a real `UILabel` on iOS.
 */
@Composable
public fun NativeText(
    text: String,
    modifier: Modifier = Modifier,
    style: NativeTextStyle = NativeTextStyle.Body,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = null,
    align: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    textStyleOverride: TextStyle? = null,
    testTag: String? = null,
) {
    val resolved = resolveNativeTextStyle(style, color, fontWeight, align, textStyleOverride)
    PlatformNativeText(
        text = text,
        modifier = modifier,
        textStyle = resolved,
        maxLines = maxLines,
        overflow = overflow,
        testTag = testTag,
    )
}

@Composable
private fun resolveNativeTextStyle(
    style: NativeTextStyle,
    color: Color,
    fontWeight: FontWeight?,
    align: TextAlign?,
    override: TextStyle?,
): TextStyle {
    val base = when (style) {
        NativeTextStyle.Display -> MaterialTheme.typography.displaySmall
        NativeTextStyle.Title -> MaterialTheme.typography.titleLarge
        NativeTextStyle.Body -> MaterialTheme.typography.bodyLarge
        NativeTextStyle.Label -> MaterialTheme.typography.labelLarge
    }
    var ts = base.copy(color = if (color.isSpecified) color else MaterialTheme.colorScheme.onSurface)
    if (fontWeight != null) ts = ts.copy(fontWeight = fontWeight)
    if (align != null) ts = ts.copy(textAlign = align)
    if (override != null) ts = ts.merge(override)
    return ts
}

/** Native text renderer. Android → `Text`; iOS → `UILabel` via `UIKitView`. */
@Composable
internal expect fun PlatformNativeText(
    text: String,
    modifier: Modifier,
    textStyle: TextStyle,
    maxLines: Int,
    overflow: TextOverflow,
    testTag: String?,
)
