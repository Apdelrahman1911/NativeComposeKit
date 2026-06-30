package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeButtonVariant
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeIcon
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeInteropTouch
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeMenu
import io.github.apdelrahman1911.nativecomposekit.components.model.ResolvedButtonStyle

@Composable
internal actual fun PlatformNativeSplitButton(
    text: String,
    onPrimaryClick: () -> Unit,
    menu: NativeMenu,
    modifier: Modifier,
    style: ResolvedButtonStyle,
    enabled: Boolean,
    loading: Boolean,
    leadingIcon: NativeIcon?,
    touch: NativeInteropTouch, // no-op on Android
    contentDescription: String?,
    testTag: String?,
) {
    val c = style.colors
    val r = style.cornerRadius
    val startShape = RoundedCornerShape(topStart = r, bottomStart = r, topEnd = 0.dp, bottomEnd = 0.dp)
    val endShape = RoundedCornerShape(topStart = 0.dp, bottomStart = 0.dp, topEnd = r, bottomEnd = r)
    var expanded by remember { mutableStateOf(false) }
    val isEnabled = enabled && !loading

    var m = modifier.heightIn(min = style.height)
    if (testTag != null) m = m.testTag(testTag)
    val cd = contentDescription
    if (cd != null) m = m.semantics { this.contentDescription = cd }

    val primaryPad = PaddingValues(
        start = style.insets.start,
        top = style.insets.top,
        end = style.insets.end,
        bottom = style.insets.bottom,
    )
    val chevronPad = PaddingValues(horizontal = style.insets.top, vertical = style.insets.top)
    val segMod = Modifier.heightIn(min = style.height)

    Row(modifier = m, verticalAlignment = Alignment.CenterVertically) {
        SplitSegment(style, startShape, onPrimaryClick, isEnabled, primaryPad, segMod) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = c.content)
            } else {
                val li = leadingIcon?.androidImageVector
                if (li != null) {
                    Icon(li, contentDescription = leadingIcon.contentDescription, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(style.iconSpacing))
                }
                Text(text = text, style = style.textStyle)
            }
        }
        Box(
            Modifier
                .width(1.dp)
                .height(style.height)
                .background(c.content.copy(alpha = 0.3f)),
        )
        Box {
            SplitSegment(style, endShape, { expanded = true }, isEnabled, chevronPad, segMod) {
                Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "More", modifier = Modifier.size(20.dp))
            }
            NativeMenuDropdown(expanded = expanded, onDismiss = { expanded = false }, menu = menu)
        }
    }
}

/** One segment of the split button — the same Material button type the [style] variant maps to. */
@Composable
private fun SplitSegment(
    style: ResolvedButtonStyle,
    shape: RoundedCornerShape,
    onClick: () -> Unit,
    enabled: Boolean,
    pad: PaddingValues,
    modifier: Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    val c = style.colors
    when (style.variant) {
        NativeButtonVariant.Primary, NativeButtonVariant.Destructive ->
            Button(
                onClick = onClick,
                modifier = modifier,
                enabled = enabled,
                shape = shape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = c.container,
                    contentColor = c.content,
                    disabledContainerColor = c.container,
                    disabledContentColor = c.content,
                ),
                contentPadding = pad,
                content = content,
            )

        NativeButtonVariant.Secondary ->
            FilledTonalButton(
                onClick = onClick,
                modifier = modifier,
                enabled = enabled,
                shape = shape,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = c.container,
                    contentColor = c.content,
                    disabledContainerColor = c.container,
                    disabledContentColor = c.content,
                ),
                contentPadding = pad,
                content = content,
            )

        NativeButtonVariant.Tertiary ->
            TextButton(
                onClick = onClick,
                modifier = modifier,
                enabled = enabled,
                shape = shape,
                colors = ButtonDefaults.textButtonColors(contentColor = c.content, disabledContentColor = c.content),
                contentPadding = pad,
                content = content,
            )

        NativeButtonVariant.Outline ->
            OutlinedButton(
                onClick = onClick,
                modifier = modifier,
                enabled = enabled,
                shape = shape,
                border = BorderStroke(1.dp, if (c.border.isSpecified) c.border else c.content),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = c.content, disabledContentColor = c.content),
                contentPadding = pad,
                content = content,
            )
    }
}
