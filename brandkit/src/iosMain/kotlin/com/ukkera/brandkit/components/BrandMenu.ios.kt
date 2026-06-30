package com.ukkera.brandkit.components

import com.ukkera.brandkit.components.model.BrandMenu
import com.ukkera.brandkit.components.model.BrandMenuItemRole
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIAction
import platform.UIKit.UIImage
import platform.UIKit.UIMenu
import platform.UIKit.UIMenuElementAttributesDestructive
import platform.UIKit.UIMenuElementAttributesDisabled
import platform.UIKit.UIMenuElementState

/**
 * Build a native `UIMenu` from a [BrandMenu]. Each item becomes a `UIAction` whose handler invokes
 * [com.ukkera.brandkit.components.model.BrandMenuItem.onSelect]; destructive items get the native
 * `.destructive` attribute (red) and disabled items the `.disabled` attribute. Attached to a button via
 * `UIButton.menu` + `showsMenuAsPrimaryAction`.
 */
@OptIn(BetaInteropApi::class, ExperimentalForeignApi::class)
internal fun buildUIMenu(menu: BrandMenu): UIMenu {
    val actions = menu.items.map { item ->
        val image = item.icon?.sfSymbolName?.let { UIImage.systemImageNamed(it) }
        val action = UIAction.actionWithTitle(
            title = item.title,
            image = image,
            identifier = null,
            handler = { _ -> item.onSelect() },
        )
        var attrs: ULong = 0u
        if (item.role == BrandMenuItemRole.Destructive) attrs = attrs or UIMenuElementAttributesDestructive
        if (!item.enabled) attrs = attrs or UIMenuElementAttributesDisabled
        action.attributes = attrs
        if (item.selected) action.state = UIMenuElementState.UIMenuElementStateOn // native checkmark
        action
    }
    return UIMenu.menuWithTitle(menu.title ?: "", actions)
}
