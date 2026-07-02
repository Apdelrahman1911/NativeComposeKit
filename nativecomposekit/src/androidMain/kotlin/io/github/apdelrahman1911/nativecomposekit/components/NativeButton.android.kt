package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
internal actual fun PlatformNativeButton(
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
    touch: NativeInteropTouch, // no-op on Android: Compose Material isn't embedded via UIKit interop
    contentDescription: String?,
    testTag: String?,
) {
    val c = style.colors
    val shape = RoundedCornerShape(style.cornerRadius)
    val pad = PaddingValues(
        start = style.insets.start,
        top = style.insets.top,
        end = style.insets.end,
        bottom = style.insets.bottom,
    )
    var expanded by remember { mutableStateOf(false) }

    var m = modifier.heightIn(min = style.height)
    if (fullWidth) m = m.fillMaxWidth()
    if (testTag != null) m = m.testTag(testTag)
    // While loading, the label composable is replaced by a spinner, so without an explicit
    // contentDescription the node would lose its accessible name mid-operation — keep announcing the
    // button by its text (iOS keeps its label the same way).
    val cd = contentDescription ?: if (loading) text.takeIf { it.isNotEmpty() } else null
    if (cd != null) m = m.semantics { this.contentDescription = cd }

    // Trailing icon, or the auto chevron when this is a menu button.
    val trailingVector = trailingIcon?.androidImageVector
        ?: if (menu != null) Icons.Filled.KeyboardArrowDown else null

    val content: @Composable RowScope.() -> Unit = {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = c.content,
            )
        } else {
            val li = leadingIcon?.androidImageVector
            if (li != null) {
                Icon(li, contentDescription = leadingIcon.contentDescription, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(style.iconSpacing))
            }
            if (text.isNotEmpty()) Text(text = text, style = style.textStyle)
            if (trailingVector != null) {
                Spacer(Modifier.width(style.iconSpacing))
                Icon(trailingVector, contentDescription = trailingIcon?.contentDescription, modifier = Modifier.size(18.dp))
            }
        }
    }

    val isEnabled = enabled && !loading
    // A menu button's tap only presents the menu — onClick is reserved for menu-less buttons (matches
    // iOS, where showsMenuAsPrimaryAction suppresses the tap action while presenting).
    // An empty menu is treated as NO menu (parity with iOS, where a nil UIMenu installs no
    // menu interaction): the tap stays a plain onClick instead of opening an empty surface.
    val hasMenu = menu != null && menu.items.isNotEmpty()
    val click: () -> Unit = if (hasMenu) ({ expanded = true }) else onClick

    Box {
        when (style.variant) {
            NativeButtonVariant.Primary, NativeButtonVariant.Destructive ->
                Button(
                    onClick = click,
                    modifier = m,
                    enabled = isEnabled,
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
                    onClick = click,
                    modifier = m,
                    enabled = isEnabled,
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
                    onClick = click,
                    modifier = m,
                    enabled = isEnabled,
                    shape = shape,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = c.content,
                        disabledContentColor = c.content,
                    ),
                    contentPadding = pad,
                    content = content,
                )

            NativeButtonVariant.Outline ->
                OutlinedButton(
                    onClick = click,
                    modifier = m,
                    enabled = isEnabled,
                    shape = shape,
                    border = BorderStroke(1.dp, if (c.border.isSpecified) c.border else c.content),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = c.content,
                        disabledContentColor = c.content,
                    ),
                    contentPadding = pad,
                    content = content,
                )
        }

        if (menu != null) {
            NativeMenuDropdown(expanded = expanded, onDismiss = { expanded = false }, menu = menu)
        }
    }
}
