package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.minimumInteractiveComponentSize
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
internal actual fun PlatformNativeIconButton(
    icon: NativeIcon,
    onClick: () -> Unit,
    modifier: Modifier,
    style: ResolvedButtonStyle,
    enabled: Boolean,
    loading: Boolean,
    menu: NativeMenu?,
    touch: NativeInteropTouch, // no-op on Android
    contentDescription: String?,
    testTag: String?,
) {
    val c = style.colors
    val shape = RoundedCornerShape(style.cornerRadius)
    var expanded by remember { mutableStateOf(false) }

    // The a11y touch minimum must wrap the hard visual size: applied BEFORE .size so a Small/Medium
    // button keeps a ≥48dp interactive area centered around its compact visual.
    var m = modifier.minimumInteractiveComponentSize().size(style.height)
    if (testTag != null) m = m.testTag(testTag)
    // The accessible name lives on the button node, not the inner Icon — the icon (and its description)
    // is replaced by a spinner while loading, and the name must survive that.
    val cd = contentDescription ?: icon.contentDescription
    if (cd != null) m = m.semantics { this.contentDescription = cd }

    val isEnabled = enabled && !loading
    // A menu button's tap only presents the menu — onClick is reserved for menu-less buttons (matches
    // iOS, where showsMenuAsPrimaryAction suppresses the tap action while presenting).
    // An empty menu is treated as NO menu (parity with iOS, where a nil UIMenu installs no
    // menu interaction): the tap stays a plain onClick instead of opening an empty surface.
    val hasMenu = menu != null && menu.items.isNotEmpty()
    val click: () -> Unit = if (hasMenu) ({ expanded = true }) else onClick

    val content: @Composable () -> Unit = {
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = c.content)
        } else {
            val iv = icon.androidImageVector
            if (iv != null) {
                Icon(iv, contentDescription = null, modifier = Modifier.size(20.dp))
            }
        }
    }

    Box {
        when (style.variant) {
            NativeButtonVariant.Primary, NativeButtonVariant.Destructive ->
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

            NativeButtonVariant.Secondary ->
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

            NativeButtonVariant.Tertiary ->
                // Rendered through FilledIconButton with a transparent container (visually identical to
                // the plain IconButton): plain IconButton exposes no shape parameter in this Material 3
                // version, so an explicit cornerRadius would be silently ignored and its ripple would
                // stay circular. The Surface-backed variant takes the shape and bounds the ripple to it.
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

            NativeButtonVariant.Outline ->
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
            NativeMenuDropdown(expanded = expanded, onDismiss = { expanded = false }, menu = menu)
        }
    }
}
