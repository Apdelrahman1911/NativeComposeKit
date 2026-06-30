package io.github.apdelrahman1911.nativecomposekit.components.model

import androidx.compose.runtime.Immutable

/**
 * A pull-down menu attached to a button. Renders the most native control per platform — a `UIMenu`
 * shown via `UIButton.showsMenuAsPrimaryAction` on iOS, an anchored Material 3 `DropdownMenu` on
 * Android. Used by [io.github.apdelrahman1911.nativecomposekit.components.NativeButton] (tap opens the menu),
 * `NativeIconButton`, and `NativeSplitButton` (chevron opens the menu).
 */
@Immutable
public data class NativeMenu(
    val items: List<NativeMenuItem>,
    /** Optional header shown at the top of the menu (iOS `UIMenu.title`; ignored by Android Material). */
    val title: String? = null,
)

/**
 * One row in a [NativeMenu]. Every field but [title]/[onSelect] has a safe default.
 *
 * @param icon optional leading glyph (Android uses [NativeIcon.androidImageVector]; iOS uses
 *   [NativeIcon.sfSymbolName]).
 * @param role [NativeMenuItemRole.Destructive] renders the row red and uses the native destructive style.
 * @param enabled when false the row is shown dimmed and is not selectable.
 * @param selected marks the row as the current choice — a native **checkmark** (iOS `UIMenuElementState.on`,
 *   Android a trailing check). Use for single/multi-select menus (e.g. a sort-order picker).
 */
@Immutable
public data class NativeMenuItem(
    val title: String,
    val onSelect: () -> Unit,
    val icon: NativeIcon? = null,
    val role: NativeMenuItemRole = NativeMenuItemRole.Normal,
    val enabled: Boolean = true,
    val selected: Boolean = false,
)
