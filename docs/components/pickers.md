# Pickers

Controls for selecting a value — a date, a color, the current page in a pager — plus a swipeable pager and list load-more helpers. Native controls per platform where one exists (`UIDatePicker` / `UIColorWell` / `UIPageControl` on iOS); the pager and load-more helpers are Compose-drawn on both.

### NativeDatePicker

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

NativeDatePicker(
    selectedMillis = picked,
    onSelectedMillisChange = { picked = it },
)
```

**Notes**
- `selectedMillis`, `minMillis`, and `maxMillis` are UTC epoch milliseconds at the start of the day. This is what Material's `DatePickerState` emits, and the iOS renderer mirrors it. To show a picked date in the user's zone, convert with the device timezone at the display layer; do not assume the millis are local midnight.
- `minMillis`, `maxMillis`, and `enabled` are honored on both platforms. On Android the bounds are enforced through `SelectableDates`; when disabled the calendar is dimmed (`alpha(0.38f)`) and non-selectable, since Material's `DatePicker` has no `enabled` parameter.
- On Android the picker is effectively uncontrolled after first composition. Material's `DatePickerState` owns the selection; the component reports changes out but does not write back into the state.
- On iOS the compact `UIDatePicker` is pinned inside a theme-colored backing and sets its light/dark appearance from the luminance of the surface it sits on (`LocalNativeSurface`), not the page.

### NativeColorWell

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
| `ios` | `NativeColorWellIosOptions` | `NativeColorWellIosOptions()` | iOS-only options (see below). A documented no-op on Android. |

`NativeColorWellIosOptions`:

| Field | Type | Default | Description |
|---|---|---|---|
| `supportsAlpha` | `Boolean` | `true` | Lets the native `UIColorWell` picker edit opacity. Ignored on Android; the preset swatches are opaque. |

**Example**

```kotlin
var highlight by remember { mutableStateOf(Color(0xFF1E88E5)) }

NativeColorWell(
    color = highlight,
    onColorChange = { highlight = it },
)
```

**Notes**
- `ios.supportsAlpha` applies only on iOS. The Android preset swatches are opaque, so the option is a no-op there.
- The Android presets are picker data (a fixed 16-color palette), not theme styling, and do not adapt to the app theme.

### NativePageControl

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

NativePageControl(
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

### NativePager

A horizontally swipeable pager for carousels, onboarding, and image galleries. Pair it with `NativePageControl` for the dots.

**Android / iOS:** Compose-drawn on both — a thin wrapper over Compose Foundation's `HorizontalPager` (no UIKit control hosts arbitrary Compose pages; the pager supplies the swipe/fling physics).

**Use it when**
- You need swipeable paged content — a carousel, onboarding flow, or gallery.

**Avoid it when**
- You only need the dots indicator — use `NativePageControl` alone.

**Parameters**

| Parameter | Type | Default | Description |
|---|---|---|---|
| `pageCount` | `Int` | — | Total pages (feeds the default `state`). |
| `modifier` | `Modifier` | `Modifier` | Layout modifier. |
| `state` | `PagerState` | `rememberPagerState { pageCount }` | Pager state; pass your own to drive it and bind `NativePageControl`. |
| `contentPadding` | `PaddingValues` | `PaddingValues(0.dp)` | Padding around the pages (e.g. to peek neighbors). |
| `pageSpacing` | `Dp` | `0.dp` | Gap between pages. |
| `userScrollEnabled` | `Boolean` | `true` | Whether swipe gestures page. |
| `key` | `((page: Int) -> Any)?` | `null` | Stable key per page. |
| `pageContent` | `@Composable PagerScope.(page: Int) -> Unit` | — | The page slot. |

**Example**

```kotlin
val state = rememberPagerState { items.size }
val scope = rememberCoroutineScope()
NativePager(items.size, state = state) { page -> PageCard(items[page]) }
NativePageControl(
    pageCount = items.size,
    currentPage = state.currentPage,
    onCurrentPageChange = { scope.launch { state.animateScrollToPage(it) } },
    modifier = Modifier.fillMaxWidth(),
)
```

**Notes**
- State-driven: it reuses Compose `PagerState`, so binding to `NativePageControl` (read `state.currentPage`, page via `animateScrollToPage`) is direct.
- Compose-on-both by necessity — no UIKit control hosts arbitrary Compose pages. (A real gap, unlike the retired `NativeTabBar`, which had a native `UISegmentedControl` alternative.)

### Load more (list pagination)

Infinite-scroll helpers for a paginated `LazyColumn`: an effect that fires as the list nears its end, and a slottable footer. Compose-on-both (built on `LazyListState` / `LazyListScope`).

**Use it when**
- A long, server-paged list should load the next page as the user approaches the end.

**API**

| Symbol | Signature | Description |
|---|---|---|
| `NativePageLoadState` | `enum { Idle, Loading, Error, EndReached }` | The list's load state; you own the transitions. |
| `NativeLoadMoreEffect` | `@Composable (listState: LazyListState, buffer: Int = 3, enabled: Boolean = true, onLoadMore: () -> Unit)` | Fires `onLoadMore` once when the list scrolls within `buffer` items of the end. Guard concurrent loads via `enabled`. |
| `LazyListScope.nativePaginationFooter` | `(state, onRetry = {}, loading = { spinner }, error = { retry -> button })` | A footer row: spinner while `Loading`, a retry (slot) on `Error`, nothing at `Idle`/`EndReached`. Both slots overridable. |

**Example**

```kotlin
val listState = rememberLazyListState()
LazyColumn(state = listState) {
    items(rows, key = { it.id }) { Row(it) }
    nativePaginationFooter(loadState, onRetry = ::retry)
}
NativeLoadMoreEffect(listState, enabled = loadState == NativePageLoadState.Idle) { loadNextPage() }
```

**Notes**
- `NativeLoadMoreEffect` holds no paging state — you decide what "load more" does and track your own `NativePageLoadState`. Set `enabled = false` (e.g. at `EndReached` or while loading) to pause.
- Nesting a `LazyColumn` inside another vertical scroll needs a bounded height on the inner list.
