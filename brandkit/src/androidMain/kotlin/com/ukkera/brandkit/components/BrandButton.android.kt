package com.ukkera.brandkit.components

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
import com.ukkera.brandkit.components.model.BrandButtonVariant
import com.ukkera.brandkit.components.model.BrandIcon
import com.ukkera.brandkit.components.model.BrandInteropTouch
import com.ukkera.brandkit.components.model.BrandMenu
import com.ukkera.brandkit.components.model.ResolvedButtonStyle

@Composable
internal actual fun PlatformBrandButton(
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
    touch: BrandInteropTouch, // no-op on Android: Compose Material isn't embedded via UIKit interop
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
    val cd = contentDescription
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
    // onClick fires on tap; if a menu is attached it also opens.
    val click: () -> Unit = { onClick(); if (menu != null) expanded = true }

    Box {
        when (style.variant) {
            BrandButtonVariant.Primary, BrandButtonVariant.Destructive ->
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

            BrandButtonVariant.Secondary ->
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

            BrandButtonVariant.Tertiary ->
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

            BrandButtonVariant.Outline ->
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
            BrandMenuDropdown(expanded = expanded, onDismiss = { expanded = false }, menu = menu)
        }
    }
}
