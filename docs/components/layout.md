# Layout

Containers, screen chrome, and list scaffolding. Every component here is Compose-drawn on both platforms; none of them wrap a native leaf control.

### BrandCard

A themed container surface for covers, grid cells, and grouped content.

**Android:** Compose-drawn rounded surface with theme colors, shape, and optional shadow.
**iOS:** the same Compose surface (a card is styled view layout, not a native leaf control).

**Use it when**
- You need a grouped content container or a tappable cover/grid cell.

**Avoid it when**
- You want a row inside a settings list; use BrandListItem inside BrandListSection.

**Parameters**

| Parameter | Type | Default | Description |
|---|---|---|---|
| modifier | Modifier | Modifier | Layout modifier applied to the card. |
| variant | BrandCardVariant | BrandCardVariant.Filled | `Filled` (tonal `surfaceVariant`), `Elevated` (`surface` + 2dp shadow), or `Outlined` (`surface` + hairline outline). |
| onClick | (() -> Unit)? | null | Makes the whole card a Material-ripple button clipped to the shape. |
| onClickLabel | String? | null | Accessibility label for the click action. |
| enabled | Boolean | true | Gates a tappable card; dims content and drops elevation when false. No-op for a card with no `onClick`. |
| cornerRadius | Dp? | null | Corner radius; falls back to `BrandTheme.tokens.cornerMedium`. |
| contentPadding | PaddingValues? | null | Inner padding; falls back to `PaddingValues(BrandTheme.tokens.spacingMd)`. |
| containerColor | Color? | null | Overrides the variant's container color. |
| contentColor | Color? | null | Overrides the resolved content color provided via `LocalContentColor`. |
| contentDescription | String? | null | Accessibility description for the card. |
| testTag | String? | null | Test tag for UI tests. |
| content | @Composable ColumnScope.() -> Unit | — | Card content, laid out in a `Column`. |

**Example**

```kotlin
BrandCard(variant = BrandCardVariant.Elevated, onClick = { open(cover) }) {
    BrandText(cover.title)
}
```

**Notes** — `enabled` only affects a card that has an `onClick`; for a non-clickable card it does nothing. Content inherits the resolved content color through `LocalContentColor`, and the card publishes its container color via `LocalBrandSurface` so descendant surface-relative fills and native-control probes adapt to it. The shadow is applied before the clip so it renders outside the rounded bounds.

### BrandScaffold

A themed screen scaffold (top bar, bottom bar, FAB, content) decoupled from navigation.

**Android:** Compose `Scaffold`.
**iOS:** the same Compose `Scaffold`.

**Use it when**
- You build a standalone screen and want top-bar/bottom-bar/FAB slots without going through the nav host.

**Avoid it when**
- The screen already gets its chrome from `BrandNavHost`.

**Parameters**

| Parameter | Type | Default | Description |
|---|---|---|---|
| modifier | Modifier | Modifier | Layout modifier applied to the scaffold. |
| topBar | @Composable () -> Unit | {} | Top bar slot; pair it with `BrandTopBar`. |
| bottomBar | @Composable () -> Unit | {} | Bottom bar slot. |
| floatingActionButton | @Composable () -> Unit | {} | FAB slot. |
| containerColor | Color? | null | Page surface; falls back to `background`. Published via `LocalBrandSurface`. |
| contentColor | Color? | null | Content color; falls back to `onBackground`. |
| contentWindowInsets | WindowInsets | ScaffoldDefaults.contentWindowInsets | Window insets applied to the content. |
| content | @Composable (PaddingValues) -> Unit | — | Screen content; receives the inner padding to apply. |

**Example**

```kotlin
BrandScaffold(topBar = { BrandTopBar("Settings") }) { inner ->
    Column(Modifier.padding(inner)) { /* … */ }
}
```

**Notes** — `containerColor` (default `background`) is published via `LocalBrandSurface` for the content, so surface-relative fills and the native-control light/dark probe adapt to the page the content sits on. Apply the `PaddingValues` passed to `content` so your content clears the bars and insets.

### BrandTopBar

A themed top app bar, decoupled from navigation, for any screen or a `BrandScaffold` top-bar slot.

**Android:** Compose `TopAppBar`, or `CenterAlignedTopAppBar` when `centerTitle` is set.
**iOS:** the same Compose bar (the native iOS nav bar belongs to the SwiftUI shell).

**Use it when**
- You need in-content or Android screen chrome with a title, optional nav icon, and trailing actions.

**Avoid it when**
- You want the native iOS navigation bar; that lives in the SwiftUI shell, not here.

**Parameters**

| Parameter | Type | Default | Description |
|---|---|---|---|
| title | String | — | Bar title; single-line, ellipsized. |
| modifier | Modifier | Modifier | Layout modifier applied to the bar. |
| subtitle | String? | null | Optional second line in muted secondary text. |
| navigationIcon | ImageVector? | null | Leading icon (a plain Compose `ImageVector`, no SF-Symbol slot). |
| onNavigationClick | (() -> Unit)? | null | Click handler for the navigation icon. The icon shows only when both this and `navigationIcon` are set. |
| navigationContentDescription | String? | null | Accessible name for the icon-only navigation control. |
| centerTitle | Boolean | false | Centers the title (iOS convention); leave off for the Android leading-aligned look. |
| testTag | String? | null | Test tag for UI tests. |
| actions | @Composable RowScope.() -> Unit | {} | Trailing slot; put `BrandIconButton`s here. |

**Example**

```kotlin
BrandTopBar("Library", actions = {
    BrandIconButton(addIcon, onAdd, contentDescription = "Add")
})
```

**Notes** — Colors come from the theme: `surface` container, `onSurface` title. The navigation icon renders only when both `navigationIcon` and `onNavigationClick` are non-null; give it a `navigationContentDescription` since an icon-only control needs an accessible name. Collapse-on-scroll is deferred: Material's `TopAppBarScrollBehavior` is still experimental and is kept out of the public signature.

### BrandListSection

Groups rows under an optional header and footer; the standard settings or details section.

**Android:** Compose-drawn column of rows with separators between them.
**iOS:** the same Compose section.

**Use it when**
- You build a settings or details screen and want grouped rows with shared separators.

**Avoid it when**
- You need a single standalone row; use BrandListItem directly with `showDivider` if needed.

**Parameters**

| Parameter | Type | Default | Description |
|---|---|---|---|
| rows | List<@Composable () -> Unit> | — | The rows; the section draws separators between them, never after the last. |
| modifier | Modifier | Modifier | Layout modifier applied to the section. |
| header | String? | null | Optional header in muted secondary text. |
| footer | String? | null | Optional footer in muted secondary text. |
| style | BrandListSectionStyle | BrandListSectionStyle.Grouped | `Grouped` wraps rows in a rounded inset card; `Plain` leaves them flat edge-to-edge. |
| showDividers | Boolean | true | Draws hairline separators between rows. |
| dividerInset | Dp | BrandTheme.tokens.spacingMd | Start/end inset for the separators. |
| contentDescription | String? | null | Accessibility description for the section. |
| testTag | String? | null | Test tag for UI tests. |

**Example**

```kotlin
BrandListSection(header = "Reader", rows = listOf(
    { BrandListItem("Direction", trailingText = "L → R", onClick = { /* … */ }) },
    { BrandListItem("Theme", trailingText = "Dark", onClick = { /* … */ }) },
))
```

**Notes** — Separators are drawn between rows only, so there is no per-row book-keeping; leave `showDivider` off on the rows themselves. `Grouped` (a Filled `BrandCard`) assumes the section sits on the page `surface`/`background`, where the tonal card reads as raised. Pass an already-uppercased `header` for the classic iOS grouped-header casing; the kit does not force it, for i18n.

### BrandListItem

A single list row: the backbone of settings screens, chapter lists, and navigation menus.

**Android:** Compose-drawn row with optional text column, leading slot, and trailing affordance.
**iOS:** the same Compose row (a row is layout, not a native leaf control).

**Use it when**
- You need a rich, optional-slot row for settings, lists, or navigation.

**Avoid it when**
- You need a free-form container; use BrandCard instead.

**Parameters**

| Parameter | Type | Default | Description |
|---|---|---|---|
| headline | String | — | Primary row text; up to 2 lines, ellipsized. |
| modifier | Modifier | Modifier | Layout modifier applied to the row. |
| overline | String? | null | Small label above the headline. |
| supporting | String? | null | Secondary text below the headline. |
| leading | (@Composable () -> Unit)? | null | Leading slot (icon, `BrandAvatar`, cover). |
| trailing | (@Composable () -> Unit)? | null | Trailing slot for a custom control such as a `BrandToggle`. |
| trailingText | String? | null | Trailing value text; single-line, ellipsized, width-capped at 140dp. |
| onClick | (() -> Unit)? | null | Makes the row tappable. |
| onClickLabel | String? | null | Accessibility label for the click action. |
| onLongClick | (() -> Unit)? | null | Long-press handler. |
| onLongClickLabel | String? | null | Accessible name for the long-press action; required for it to surface as a custom accessibility action. |
| enabled | Boolean | true | Disables the row and dims its text when false. |
| showChevron | Boolean | onClick != null | Adds a layout-direction-aware disclosure chevron. Defaults on for tappable rows. |
| showDivider | Boolean | false | Draws a bottom hairline; for standalone rows (leave off inside a section). |
| swipeAction | BrandSwipeAction? | null | Trailing right-to-left swipe action; the row snaps back after firing. |
| minHeight | Dp | 56.dp | Minimum row height. |
| contentPadding | PaddingValues? | null | Inner padding; falls back to horizontal `spacingMd` and vertical `spacingSm + spacingXs`. |
| contentDescription | String? | null | Accessibility description for the row. |
| testTag | String? | null | Test tag for UI tests. |

`BrandSwipeAction` (passed to `swipeAction`):

| Parameter | Type | Default | Description |
|---|---|---|---|
| label | String | — | Action label; also exposed as a custom accessibility action. |
| onAction | () -> Unit | — | Runs when the row is swiped past the threshold. Remove the item from your list here if the action deletes it. |
| icon | ImageVector? | null | Decorative reveal glyph (Compose-drawn). |
| destructive | Boolean | false | Tints the reveal red. |

**Example**

```kotlin
BrandListItem(
    headline = "Notifications",
    supporting = "Push, email, in-app",
    trailing = { BrandToggle(checked = on, onCheckedChange = { on = it }) },
)
```

**Notes** — A tappable row with no interactive `trailing` is merged into one focus target (at least `minHeight` tall) with the auto-mirrored chevron. When `trailing` holds its own control (a toggle), the row is left unmerged so that control stays focusable, so do not also make such a row `onClick`-able; that creates two competing tap targets. `trailingText` is single-line, ellipsized, and capped at 140dp so it cannot crush the headline. Swipe and long-press are gesture-only and invisible to screen readers, so they are exposed as explicit custom accessibility actions; a long-press surfaces one only when you pass `onLongClickLabel`. A swiped row fills its background with the published `LocalBrandSurface` (page background or card container) so the reveal does not show through at rest.

### BrandDivider

A hairline separator for stacked rows or side-by-side content.

**Android:** Compose `HorizontalDivider` or `VerticalDivider`.
**iOS:** the same Compose divider.

**Use it when**
- You need a standalone separator between rows or columns.

**Avoid it when**
- Rows already live in a BrandListSection; the section draws separators between them for you.

**Parameters**

| Parameter | Type | Default | Description |
|---|---|---|---|
| modifier | Modifier | Modifier | Layout modifier applied to the divider. |
| orientation | BrandDividerOrientation | BrandDividerOrientation.Horizontal | `Horizontal` separates stacked rows; `Vertical` separates side-by-side content. |
| thickness | Dp | 1.dp | Line thickness. |
| color | Color? | null | Line color; falls back to `outlineVariant`. |
| startIndent | Dp | 0.dp | Leading inset (top inset when `Vertical`). |
| endIndent | Dp | 0.dp | Trailing inset (bottom inset when `Vertical`). |
| testTag | String? | null | Test tag for UI tests. |

**Example**

```kotlin
BrandDivider(startIndent = 56.dp) // inset under a leading icon
```

**Notes** — Defaults to a 1dp `outlineVariant` line. For a `Horizontal` divider the `startIndent`/`endIndent` insets are layout-direction aware, so an inset separator that begins after a list item's text aligns correctly in RTL. For a `Vertical` divider they map to top/bottom insets, which is a direction-neutral axis.
