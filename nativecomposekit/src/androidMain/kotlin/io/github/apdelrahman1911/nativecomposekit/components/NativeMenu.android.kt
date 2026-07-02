package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeMenu
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeMenuItemRole
import io.github.apdelrahman1911.nativecomposekit.theme.LocalNativeStrings

/**
 * Android pull-down menu = a Material 3 [DropdownMenu] anchored to the button. Destructive items use the
 * theme error color. Anchor it inside the same `Box` as the button so it opens beneath the trigger.
 */
@Composable
internal fun NativeMenuDropdown(expanded: Boolean, onDismiss: () -> Unit, menu: NativeMenu) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        menu.items.forEach { item ->
            val tint = if (item.role == NativeMenuItemRole.Destructive) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurface
            }
            val iconVector = item.icon?.androidImageVector
            val iconCd = item.icon?.contentDescription
            DropdownMenuItem(
                // Colors flow through the item's content color (NOT per-slot overrides) so DISABLED rows get
                // Material's dimmed treatment — a hardcoded tint rendered disabled items at full color.
                text = { Text(item.title) },
                colors = MenuDefaults.itemColors(
                    textColor = tint,
                    leadingIconColor = tint,
                    trailingIconColor = tint,
                ),
                onClick = {
                    onDismiss()
                    item.onSelect()
                },
                enabled = item.enabled,
                leadingIcon = if (iconVector != null) {
                    { Icon(iconVector, contentDescription = iconCd) }
                } else {
                    null
                },
                trailingIcon = if (item.selected) {
                    { Icon(Icons.Default.Check, contentDescription = LocalNativeStrings.current.menuSelected) }
                } else {
                    null
                },
            )
        }
    }
}
