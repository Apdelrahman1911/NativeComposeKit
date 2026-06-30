package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeMenu
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeMenuItemRole

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
                text = { Text(item.title, color = tint) },
                onClick = {
                    onDismiss()
                    item.onSelect()
                },
                enabled = item.enabled,
                leadingIcon = if (iconVector != null) {
                    { Icon(iconVector, contentDescription = iconCd, tint = tint) }
                } else {
                    null
                },
                trailingIcon = if (item.selected) {
                    { Icon(Icons.Default.Check, contentDescription = "Selected", tint = tint) }
                } else {
                    null
                },
            )
        }
    }
}
