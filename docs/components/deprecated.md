# Deprecated

These components are deprecated and kept only for source compatibility; both are Material-only, Compose-on-both wrappers with no native iOS renderer, and will be removed in a later release.

### BrandTabBar

An in-content tab strip for switching views within a screen (for example Overview / Chapters / Comments), distinct from the app's bottom tab bar.

**Android:** Material 3 `PrimaryTabRow` with a moving indicator, themed by AppTheme.
**iOS:** the same Compose-drawn `PrimaryTabRow` (no native iOS renderer).

**Use it when**
- You are maintaining existing code that already calls it and cannot migrate yet.

**Avoid it when**
- Writing new code — use `BrandSegmentedControl`, which renders a native `UISegmentedControl` on iOS for in-content selection.

**Deprecated —** use `BrandSegmentedControl` (native `UISegmentedControl` on iOS) for in-content selection.

**Parameters**

| Parameter | Type | Default | Description |
|---|---|---|---|
| `tabs` | `List<String>` | — | Tab labels. Single-line and ellipsized. |
| `selectedIndex` | `Int` | — | Index of the selected tab. Coerced into the valid range. |
| `onSelectedIndexChange` | `(Int) -> Unit` | — | Called with the tapped tab's index. |
| `modifier` | `Modifier` | `Modifier` | Layout modifier for the tab row. |
| `testTag` | `String?` | `null` | Optional test tag applied to the row. |

**Example**

```kotlin
var tab by remember { mutableStateOf(0) }
BrandTabBar(
    tabs = listOf("Overview", "Chapters"),
    selectedIndex = tab,
    onSelectedIndexChange = { tab = it },
)
```

**Notes** — `selectedIndex` is coerced into `0..(tabs.size - 1)` before layout. Labels are forced to a single line with ellipsis overflow. The container uses the theme surface color and the primary color for content.

### BrandTooltip

A transient label that floats above an anchor on long-press (touch) — for feature hints and icon-button affordance names.

**Android:** Material 3 `TooltipBox` with a `PlainTooltip`, themed by AppTheme.
**iOS:** the same Compose-drawn `TooltipBox` (no native iOS renderer).

**Use it when**
- You are maintaining existing code that already calls it and cannot migrate yet.

**Avoid it when**
- Writing new code — iOS has no hover-tooltip idiom (long-press maps to a context menu), so prefer inline helper text, an accessible `contentDescription`, or `BrandPopover` for caller-controlled contextual content.

**Deprecated —** no native iOS tooltip idiom (long-press = context menu on iOS). Prefer inline helper text / `contentDescription`, or `BrandPopover` for caller-controlled contextual content.

**Parameters**

| Parameter | Type | Default | Description |
|---|---|---|---|
| `text` | `String` | — | The tip text shown in the tooltip. |
| `modifier` | `Modifier` | `Modifier` | Layout modifier for the tooltip box. |
| `content` | `@Composable () -> Unit` | — | The anchor the tooltip attaches to. |

**Example**

```kotlin
BrandTooltip("Add to library") {
    BrandIconButton(addIcon, ::add, contentDescription = "Add")
}
```

**Notes** — The tooltip is short and self-dismissing, unlike `BrandPopover`, which is a richer, caller-controlled surface. It uses `TooltipDefaults.rememberPlainTooltipPositionProvider()` for placement and a fresh `rememberTooltipState()` for each instance.
