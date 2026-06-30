package io.github.apdelrahman1911.nativecomposekit.components

import io.github.apdelrahman1911.nativecomposekit.components.model.NativeMenu
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeMenuItemRole
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIAction
import platform.UIKit.UIImage
import platform.UIKit.UIMenu
import platform.UIKit.UIMenuElementAttributesDestructive
import platform.UIKit.UIMenuElementAttributesDisabled
import platform.UIKit.UIMenuElementState

/**
 * Build a native `UIMenu` from a [NativeMenu]. Each item becomes a `UIAction` whose handler invokes
 * [io.github.apdelrahman1911.nativecomposekit.components.model.NativeMenuItem.onSelect]; destructive items get the native
 * `.destructive` attribute (red) and disabled items the `.disabled` attribute. Attached to a button via
 * `UIButton.menu` + `showsMenuAsPrimaryAction`.
 */
@OptIn(BetaInteropApi::class, ExperimentalForeignApi::class)
internal fun buildUIMenu(menu: NativeMenu): UIMenu {
    val actions = menu.items.map { item ->
        val image = item.icon?.sfSymbolName?.let { UIImage.systemImageNamed(it) }
        val action = UIAction.actionWithTitle(
            title = item.title,
            image = image,
            identifier = null,
            handler = { _ -> item.onSelect() },
        )
        var attrs: ULong = 0u
        if (item.role == NativeMenuItemRole.Destructive) attrs = attrs or UIMenuElementAttributesDestructive
        if (!item.enabled) attrs = attrs or UIMenuElementAttributesDisabled
        action.attributes = attrs
        if (item.selected) action.state = UIMenuElementState.UIMenuElementStateOn // native checkmark
        action
    }
    return UIMenu.menuWithTitle(menu.title ?: "", actions)
}
