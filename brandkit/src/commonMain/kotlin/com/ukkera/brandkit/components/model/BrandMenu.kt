package com.ukkera.brandkit.components.model

import androidx.compose.runtime.Immutable

/**
 * A pull-down menu attached to a button. Renders the most native control per platform — a `UIMenu`
 * shown via `UIButton.showsMenuAsPrimaryAction` on iOS, an anchored Material 3 `DropdownMenu` on
 * Android. Used by [com.ukkera.brandkit.components.BrandButton] (tap opens the menu),
 * `BrandIconButton`, and `BrandSplitButton` (chevron opens the menu).
 */
@Immutable
public data class BrandMenu(
    val items: List<BrandMenuItem>,
    /** Optional header shown at the top of the menu (iOS `UIMenu.title`; ignored by Android Material). */
    val title: String? = null,
)

/**
 * One row in a [BrandMenu]. Every field but [title]/[onSelect] has a safe default.
 *
 * @param icon optional leading glyph (Android uses [BrandIcon.androidImageVector]; iOS uses
 *   [BrandIcon.sfSymbolName]).
 * @param role [BrandMenuItemRole.Destructive] renders the row red and uses the native destructive style.
 * @param enabled when false the row is shown dimmed and is not selectable.
 * @param selected marks the row as the current choice — a native **checkmark** (iOS `UIMenuElementState.on`,
 *   Android a trailing check). Use for single/multi-select menus (e.g. a sort-order picker).
 */
@Immutable
public data class BrandMenuItem(
    val title: String,
    val onSelect: () -> Unit,
    val icon: BrandIcon? = null,
    val role: BrandMenuItemRole = BrandMenuItemRole.Normal,
    val enabled: Boolean = true,
    val selected: Boolean = false,
)
