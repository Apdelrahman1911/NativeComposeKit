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
 * Build a native `UIMenu` from a [NativeMenu]. Each item becomes a `UIAction`; destructive items get the
 * native `.destructive` attribute (red) and disabled items the `.disabled` attribute. Attached to a button
 * via `UIButton.menu` + `showsMenuAsPrimaryAction`.
 *
 * Returns **null for an itemless model**: attaching an empty `UIMenu` as a primary action swallows the tap
 * to pop an empty menu container, while a nil `UIButton.menu` installs no menu interaction at all — the tap
 * falls through to the button's normal action (matching Android, which renders no dropdown surface).
 *
 * Handlers dispatch **by index through [onSelectAt]** rather than capturing each item's `onSelect` lambda:
 * the caller caches the built menu across recompositions (rebuilding `UIMenu` per update dismissed an open
 * menu under the user's finger), and index dispatch lets the cached actions always reach the CURRENT model's
 * callbacks instead of stale captures.
 */
@OptIn(BetaInteropApi::class, ExperimentalForeignApi::class)
internal fun buildUIMenu(menu: NativeMenu, onSelectAt: (Int) -> Unit): UIMenu? {
    if (menu.items.isEmpty()) return null
    val actions = menu.items.mapIndexed { index, item ->
        val image = item.icon?.sfSymbolName?.let { UIImage.systemImageNamed(it) }
        val action = UIAction.actionWithTitle(
            title = item.title,
            image = image,
            identifier = null,
            handler = { _ -> onSelectAt(index) },
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

/**
 * The structural (lambda-free) identity of a menu: what the rendered `UIMenu` actually shows. Two models
 * with equal fingerprints render identically, so the cached `UIMenu` can be reused even though the models'
 * `onSelect` lambdas are fresh on every recomposition (index dispatch keeps them live — see [buildUIMenu]).
 */
internal fun NativeMenu.structuralFingerprint(): List<Any?> =
    listOf(title) + items.map { listOf(it.title, it.icon?.sfSymbolName, it.role, it.enabled, it.selected) }
