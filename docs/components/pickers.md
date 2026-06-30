# Pickers

Controls for selecting a value: a date, a color, or the current page in a pager. Each renders the most native control per platform.

### BrandDatePicker

A control for selecting a calendar date.

**Android:** Material 3 inline `DatePicker` calendar.
**iOS:** `UIDatePicker` in compact style (a tappable field that expands to the system calendar) via `UIKitView`.

**Use it when**
- You need a single date and want the native calendar on each platform.

**Avoid it when**
- You need a time or date-and-time picker. This version is date-only; time and date-and-time are a planned follow-up.

**Parameters**

| Parameter | Type | Default | Description |
|---|---|---|---|
| `selectedMillis` | `Long?` | — | Selected date as UTC epoch milliseconds at the start of the day. `null` means nothing is selected. |
| `onSelectedMillisChange` | `(Long) -> Unit` | — | Called with the user's pick (UTC epoch milliseconds). |
| `modifier` | `Modifier` | `Modifier` | Layout modifier. |
| `minMillis` | `Long?` | `null` | Earliest selectable date (UTC epoch milliseconds). `null` means no lower bound. |
| `maxMillis` | `Long?` | `null` | Latest selectable date (UTC epoch milliseconds). `null` means no upper bound. |
| `enabled` | `Boolean` | `true` | When `false`, the control is non-interactive. |
| `contentDescription` | `String?` | `null` | Accessibility label. |
| `testTag` | `String?` | `null` | Test identifier (`testTag` on Android, accessibility id on iOS). |

**Example**

```kotlin
var picked by remember { mutableStateOf<Long?>(null) }

BrandDatePicker(
    selectedMillis = picked,
    onSelectedMillisChange = { picked = it },
)
```

**Notes**
- `selectedMillis`, `minMillis`, and `maxMillis` are UTC epoch milliseconds at the start of the day. This is what Material's `DatePickerState` emits, and the iOS renderer mirrors it. To show a picked date in the user's zone, convert with the device timezone at the display layer; do not assume the millis are local midnight.
- `minMillis`, `maxMillis`, and `enabled` are honored on both platforms. On Android the bounds are enforced through `SelectableDates`; when disabled the calendar is dimmed (`alpha(0.38f)`) and non-selectable, since Material's `DatePicker` has no `enabled` parameter.
- On Android the picker is effectively uncontrolled after first composition. Material's `DatePickerState` owns the selection; the component reports changes out but does not write back into the state.
- On iOS the compact `UIDatePicker` is pinned inside a theme-colored backing and sets its light/dark appearance from the luminance of the surface it sits on (`LocalBrandSurface`), not the page.

### BrandColorWell

A swatch for selecting a color.

**Android:** a circular swatch that opens an `AlertDialog` of preset colors. Android has no system color picker.
**iOS:** `UIColorWell`, the system color picker (full spectrum, eyedropper, opacity) via `UIKitView`.

**Use it when**
- You want the native iOS color picker and accept a preset palette on Android.

**Avoid it when**
- You need a full HSV picker on Android. The Android side is a fixed preset palette; a full picker can be added later.

**Parameters**

| Parameter | Type | Default | Description |
|---|---|---|---|
| `color` | `Color` | — | The current color. |
| `onColorChange` | `(Color) -> Unit` | — | Called when the user selects a color. |
| `modifier` | `Modifier` | `Modifier` | Layout modifier. |
| `enabled` | `Boolean` | `true` | When `false`, the swatch is non-interactive. |
| `contentDescription` | `String?` | `null` | Accessibility label. Defaults to "Selected color" on Android. |
| `testTag` | `String?` | `null` | Test identifier (`testTag` on Android, accessibility id on iOS). |
| `ios` | `BrandColorWellIosOptions` | `BrandColorWellIosOptions()` | iOS-only options (see below). A documented no-op on Android. |

`BrandColorWellIosOptions`:

| Field | Type | Default | Description |
|---|---|---|---|
| `supportsAlpha` | `Boolean` | `true` | Lets the native `UIColorWell` picker edit opacity. Ignored on Android; the preset swatches are opaque. |

**Example**

```kotlin
var highlight by remember { mutableStateOf(Color(0xFF1E88E5)) }

BrandColorWell(
    color = highlight,
    onColorChange = { highlight = it },
)
```

**Notes**
- `ios.supportsAlpha` applies only on iOS. The Android preset swatches are opaque, so the option is a no-op there.
- The Android presets are picker data (a fixed 16-color palette), not theme styling, and do not adapt to the app theme.

### BrandPageControl

A page indicator (the row of dots) for carousels, banners, and onboarding flows.

**Android:** a branded row of Compose dots, the current one tinted with the resolved color.
**iOS:** `UIPageControl` via `UIKitView`, which also supports native tap-to-page.

**Use it when**
- You want a page indicator paired with a pager that owns the actual paging.

**Avoid it when**
- You need the dots to drive paging on their own. This is an indicator; the pager normally owns the page state.

**Parameters**

| Parameter | Type | Default | Description |
|---|---|---|---|
| `pageCount` | `Int` | — | Total number of pages. Coerced to at least 0. |
| `currentPage` | `Int` | — | The active page index. Clamped to `0..pageCount-1`. |
| `modifier` | `Modifier` | `Modifier` | Layout modifier. |
| `onCurrentPageChange` | `((Int) -> Unit)?` | `null` | When non-null, tapping the indicator changes the page (matches `UIPageControl`). `null` makes it display-only. |
| `color` | `Color?` | `null` | Color of the active dot. Defaults to the theme primary. |
| `inactiveColor` | `Color?` | `null` | Color of inactive dots. Defaults to the theme `outlineVariant`. |
| `contentDescription` | `String?` | `null` | Accessibility label. Defaults to "Page N of M" on Android. |
| `testTag` | `String?` | `null` | Test identifier (`testTag` on Android, accessibility id on iOS). |

**Example**

```kotlin
val pagerState = rememberPagerState(pageCount = { covers.size })

BrandPageControl(
    pageCount = covers.size,
    currentPage = pagerState.currentPage,
    modifier = Modifier.weight(1f),
)
```

**Notes**
- Give it a width. As a content-sized `UIKitView`-backed control on iOS, it does not expose a reliable intrinsic width through interop, so with no width constraint it collapses to ~0 width and becomes invisible. Use `Modifier.weight(1f)` in a `Row` or another width constraint.
- On iOS, `hidesForSinglePage` is set, so a single page shows no dots (the iOS convention).
- On Android, interactive dots (with `onCurrentPageChange`) drop the inter-dot gap and give each dot a >=48dp touch target; display-only dots stay compact with 8dp spacing.
- Colors come straight from the theme-resolved style, so the dots adapt to light and dark without `overrideUserInterfaceStyle`.
