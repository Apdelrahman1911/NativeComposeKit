# Selection & sliders

Controls for picking a value: toggles, checkboxes, single-select groups, segmented controls, sliders, steppers, and star ratings. Each uses the most native control per platform where one exists, and falls back to a branded Compose control where it does not.

### NativeToggle

An on/off switch bound to a boolean.

**Android:** Material 3 `Switch`.
**iOS:** `UISwitch` via `UIKitView`.

**Use it when**
- A single on/off setting (notifications, dark mode).

**Avoid it when**
- Selecting multiple items from a list — use `NativeCheckbox`.

**Parameters**

| Parameter | Type | Default | Description |
|---|---|---|---|
| `checked` | `Boolean` | — | Current on/off state. |
| `onCheckedChange` | `((Boolean) -> Unit)?` | `null` | Toggle callback. `null` makes it a read-only display toggle (full color, no interaction). |
| `modifier` | `Modifier` | `Modifier` | Layout modifier. |
| `enabled` | `Boolean` | `true` | When `false`, the control is dimmed and non-interactive. |
| `contentDescription` | `String?` | `null` | Accessibility label. |
| `testTag` | `String?` | `null` | Test identifier (maps to the iOS accessibility id). |

**Example**

```kotlin
var notify by remember { mutableStateOf(true) }
NativeToggle(checked = notify, onCheckedChange = { notify = it })
```

**Notes** — Colors come from the active theme; track-on uses `primary`, the thumb uses `onPrimary`. The iOS control reads its light/dark appearance from the surface it sits on (`LocalNativeSurface`), not the page. It is a fixed-size native control, so no width constraint is required.

### NativeCheckbox

A checkbox, optionally with a trailing label that becomes the whole-row tap target.

**Android:** Material 3 `Checkbox`.
**iOS:** Material 3 `Checkbox` (branded Compose — iOS has no native checkbox control).

**Use it when**
- Multi-select within a list (e.g. choosing chapters to download).

**Avoid it when**
- A single on/off setting — use `NativeToggle` (a native `UISwitch` on iOS).

**Parameters**

| Parameter | Type | Default | Description |
|---|---|---|---|
| `checked` | `Boolean` | — | Current checked state. |
| `onCheckedChange` | `((Boolean) -> Unit)?` | — | Toggle callback. `null` makes it read-only. |
| `modifier` | `Modifier` | `Modifier` | Layout modifier. |
| `label` | `String?` | `null` | Optional trailing label. When set, the whole row is the tap target. |
| `enabled` | `Boolean` | `true` | When `false`, the control is dimmed and non-interactive. |
| `contentDescription` | `String?` | `null` | Accessibility label override. With a `label`, defaults to the label text. |
| `testTag` | `String?` | `null` | Test identifier. |

**Example**

```kotlin
var download by remember { mutableStateOf(false) }
NativeCheckbox(checked = download, onCheckedChange = { download = it }, label = "Download chapter")
```

**Notes** — Kept cross-platform as a documented exception: iOS has no native checkbox, so this is a branded Compose control themed by `MaterialTheme` on both platforms. With a `label`, the row is one merged `Role.Checkbox` node, at least 48dp tall, with the label as its accessible name; the inner checkbox is decorative so there is a single tap target.

### NativeRadioGroup

A single-select group over a list of options of any type `T`.

**Android:** Material 3 `RadioButton` rows in a `selectableGroup` (branded Compose).
**iOS:** the same branded Compose rows — iOS has no native radio-group control.

**Use it when**
- A visible single-select list where all options should stay on screen.

**Avoid it when**
- A small, fixed option set — use `NativeSegmentedControl` (a native `UISegmentedControl` on iOS).
- A long list — use a menu or dropdown.

**Parameters**

| Parameter | Type | Default | Description |
|---|---|---|---|
| `options` | `List<T>` | — | The selectable options. |
| `selected` | `T?` | — | The selected option, or `null` for "nothing selected yet". |
| `onSelectedChange` | `(T) -> Unit` | — | Selection callback. |
| `modifier` | `Modifier` | `Modifier` | Layout modifier. |
| `label` | `(T) -> String` | `{ it.toString() }` | Maps an option to its display string. |
| `enabled` | `Boolean` | `true` | When `false`, the group is dimmed and non-interactive. |
| `style` | `NativeSelectionStyle` | `NativeSelectionStyle.Radio` | `Radio` (leading dot, Android idiom) or `Checkmark` (trailing check on the selected row, iOS idiom). |
| `testTag` | `String?` | `null` | Test identifier. |

**Example**

```kotlin
var quality by remember { mutableStateOf(Quality.High) }
NativeRadioGroup(
    options = Quality.entries,
    selected = quality,
    onSelectedChange = { quality = it },
    label = { it.name },
)
```

**Notes** — Kept cross-platform as a documented exception. Selection uses `==`, so `T` must have a stable `equals` (a data class, enum, or primitive — not identity types like lambdas). Each row is a merged `Role.RadioButton` node at least 48dp tall inside a `selectableGroup` for correct screen-reader grouping. On iOS, prefer `NativeSelectionStyle.Checkmark` (the grouped-table idiom).

### NativeSegmentedControl

A horizontal single-select control across a few string options.

**Android:** `SingleChoiceSegmentedButtonRow`.
**iOS:** `UISegmentedControl` via `UIKitView`.

**Use it when**
- Switching between a small, fixed set of options (roughly five or fewer).

**Avoid it when**
- The options do not fit on one line, or there are many — use `NativeRadioGroup` or a menu.

**Parameters**

| Parameter | Type | Default | Description |
|---|---|---|---|
| `options` | `List<String>` | — | Segment labels. |
| `selectedIndex` | `Int` | — | Index of the selected segment. |
| `onSelectedIndexChange` | `(Int) -> Unit` | — | Selection callback. |
| `modifier` | `Modifier` | `Modifier` | Layout modifier. Give it a width (see Notes). |
| `enabled` | `Boolean` | `true` | When `false`, the control is dimmed and non-interactive. |
| `contentDescription` | `String?` | `null` | Accessibility label. |
| `testTag` | `String?` | `null` | Test identifier. |

**Example**

```kotlin
var tab by remember { mutableStateOf(0) }
NativeSegmentedControl(
    options = listOf("All", "Unread", "Saved"),
    selectedIndex = tab,
    onSelectedIndexChange = { tab = it },
    modifier = Modifier.fillMaxWidth(),
)
```

**Notes** — The iOS control is content-sized and does not expose a reliable intrinsic width through interop, so with no width constraint it collapses to about zero width and becomes invisible. Always give it a width — `Modifier.fillMaxWidth()` is the usual choice. The iOS control reads its light/dark appearance from the surrounding surface.

### NativeSlider

A continuous value within a float range.

**Android:** Material 3 `Slider`.
**iOS:** `UISlider` via `UIKitView`.

**Use it when**
- Adjusting a continuous quantity (volume, brightness, reading position).

**Avoid it when**
- The value is a small set of discrete integers — use `NativeStepper`.

**Parameters**

| Parameter | Type | Default | Description |
|---|---|---|---|
| `value` | `Float` | — | Current value. |
| `onValueChange` | `(Float) -> Unit` | — | Value-change callback. |
| `modifier` | `Modifier` | `Modifier` | Layout modifier. |
| `valueRange` | `ClosedFloatingPointRange<Float>` | `0f..1f` | Allowed range. |
| `enabled` | `Boolean` | `true` | When `false`, the control is dimmed and non-interactive. |
| `contentDescription` | `String?` | `null` | Accessibility label. |
| `testTag` | `String?` | `null` | Test identifier. |

**Example**

```kotlin
var volume by remember { mutableStateOf(0.5f) }
NativeSlider(value = volume, onValueChange = { volume = it })
```

**Notes** — The active track and thumb use `primary`; the inactive track uses `surfaceVariant`. The iOS control reads its light/dark appearance from the surrounding surface.

### NativeStepper

An integer adjusted up or down by a fixed step within bounds.

**Android:** a Material -/+ row.
**iOS:** `UIStepper` via `UIKitView`.

**Use it when**
- A small integer count (quantity, page count) where the exact value matters.

**Avoid it when**
- A continuous range — use `NativeSlider`.

**Parameters**

| Parameter | Type | Default | Description |
|---|---|---|---|
| `value` | `Int` | — | Current value. |
| `onValueChange` | `(Int) -> Unit` | — | Value-change callback. |
| `modifier` | `Modifier` | `Modifier` | Layout modifier. |
| `min` | `Int` | `0` | Lower bound. |
| `max` | `Int` | `100` | Upper bound. |
| `step` | `Int` | `1` | Increment per tap. |
| `enabled` | `Boolean` | `true` | When `false`, the control is dimmed and non-interactive. |
| `contentDescription` | `String?` | `null` | Accessibility label. |
| `testTag` | `String?` | `null` | Test identifier. |

**Example**

```kotlin
var count by remember { mutableStateOf(1) }
NativeStepper(value = count, onValueChange = { count = it }, min = 1, max = 10)
```

**Notes** — The tint uses `primary`. It is a fixed-size native control, so no width constraint is required. The iOS control reads its light/dark appearance from the surrounding surface.

### NativeRating

A star rating, read-only by default or interactive when given a change callback.

**Android:** Compose-drawn stars.
**iOS:** Compose-drawn stars (no native star control on either platform).

**Use it when**
- Showing or capturing a 0-to-max star score.

**Avoid it when**
- Capturing an arbitrary numeric value — use `NativeSlider` or `NativeStepper`.

**Parameters**

| Parameter | Type | Default | Description |
|---|---|---|---|
| `rating` | `Float` | — | Current rating, clamped to `0..max`. |
| `modifier` | `Modifier` | `Modifier` | Layout modifier. |
| `onRatingChange` | `((Float) -> Unit)?` | `null` | When set, the control is interactive; tapping star *n* sets the rating to *n* whole stars. `null` is read-only. |
| `enabled` | `Boolean` | `true` | When `false`, stars are dimmed (38% alpha) and the control is read-only regardless of `onRatingChange`. |
| `max` | `Int` | `5` | Number of stars. |
| `starSize` | `Dp` | `20.dp` | Size of each star glyph. |
| `allowHalf` | `Boolean` | `true` | Shows half-star glyphs for fractional display values. Display-only. |
| `color` | `Color?` | `null` | Filled-star tint. Defaults to the amber `warning` status color. |
| `trackColor` | `Color?` | `null` | Empty-star tint. Defaults to `outlineVariant`. |
| `contentDescription` | `String?` | `null` | Accessibility label. Defaults to e.g. "Rating: 4.5 out of 5". |
| `testTag` | `String?` | `null` | Test identifier. |

**Example**

```kotlin
NativeRating(4.5f)                                            // read-only display

var stars by remember { mutableStateOf(0f) }
NativeRating(stars, onRatingChange = { stars = it })          // interactive
```

**Notes** — Half-star glyphs are display-only: an interactive control only sets whole stars, so it never shows a half it cannot produce. Read-only mode exposes a single `contentDescription`; interactive mode exposes a live `stateDescription` plus one labeled button per star ("Rate 4 of 5"), each with a touch target of at least 48dp. A `NaN` rating maps to 0.
