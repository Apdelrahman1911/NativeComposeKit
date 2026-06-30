package com.ukkera.brandkit.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.ukkera.brandkit.components.model.BrandButtonVariant
import com.ukkera.brandkit.components.model.BrandIcon
import com.ukkera.brandkit.components.model.BrandInteropTouch
import com.ukkera.brandkit.components.model.BrandMenu
import com.ukkera.brandkit.components.model.ResolvedButtonStyle

@Composable
internal actual fun PlatformBrandIconButton(
    icon: BrandIcon,
    onClick: () -> Unit,
    modifier: Modifier,
    style: ResolvedButtonStyle,
    enabled: Boolean,
    loading: Boolean,
    menu: BrandMenu?,
    touch: BrandInteropTouch, // no-op on Android
    contentDescription: String?,
    testTag: String?,
) {
    val c = style.colors
    val shape = RoundedCornerShape(style.cornerRadius)
    var expanded by remember { mutableStateOf(false) }

    var m = modifier.size(style.height)
    if (testTag != null) m = m.testTag(testTag)

    val isEnabled = enabled && !loading
    // onClick fires on tap; if a menu is attached it also opens.
    val click: () -> Unit = { onClick(); if (menu != null) expanded = true }

    val content: @Composable () -> Unit = {
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = c.content)
        } else {
            val iv = icon.androidImageVector
            if (iv != null) {
                Icon(iv, contentDescription = contentDescription ?: icon.contentDescription, modifier = Modifier.size(20.dp))
            }
        }
    }

    Box {
        when (style.variant) {
            BrandButtonVariant.Primary, BrandButtonVariant.Destructive ->
                FilledIconButton(
                    onClick = click,
                    modifier = m,
                    enabled = isEnabled,
                    shape = shape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = c.container,
                        contentColor = c.content,
                        disabledContainerColor = c.container,
                        disabledContentColor = c.content,
                    ),
                    content = content,
                )

            BrandButtonVariant.Secondary ->
                FilledTonalIconButton(
                    onClick = click,
                    modifier = m,
                    enabled = isEnabled,
                    shape = shape,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = c.container,
                        contentColor = c.content,
                        disabledContainerColor = c.container,
                        disabledContentColor = c.content,
                    ),
                    content = content,
                )

            BrandButtonVariant.Tertiary ->
                IconButton(
                    onClick = click,
                    modifier = m,
                    enabled = isEnabled,
                    colors = IconButtonDefaults.iconButtonColors(contentColor = c.content, disabledContentColor = c.content),
                    content = content,
                )

            BrandButtonVariant.Outline ->
                OutlinedIconButton(
                    onClick = click,
                    modifier = m,
                    enabled = isEnabled,
                    shape = shape,
                    border = BorderStroke(1.dp, if (c.border.isSpecified) c.border else c.content),
                    colors = IconButtonDefaults.outlinedIconButtonColors(contentColor = c.content),
                    content = content,
                )
        }

        if (menu != null) {
            BrandMenuDropdown(expanded = expanded, onDismiss = { expanded = false }, menu = menu)
        }
    }
}
