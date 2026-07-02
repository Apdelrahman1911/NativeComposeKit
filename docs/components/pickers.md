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
- `selectedMillis`, `minMillis`, and `maxMillis` are UTC epoch milliseconds at the start of the day. This is what Material's `DatePickerState` emits, and the iOS renderer mirrors it: the `UIDatePicker` (and its calendar) is pinned to UTC, and emitted values are floored to the UTC day start — so a UTC-midnight input shows the same calendar day everywhere, and picks round-trip without a wall-clock time smuggled in. To show a picked date in the user's zone, convert with the device timezone at the display layer; do not assume the millis are local midnight.
- The control is controlled on both platforms: programmatic `selectedMillis` changes (including `null` to clear) reach the UI. On Android this is a guarded write-back into Material's `DatePickerState`, so there is no feedback loop with `onSelectedMillisChange`.
- `minMillis`, `maxMillis`, and `enabled` are honored on both platforms. On Android the bounds are enforced through `SelectableDates`; when disabled the calendar is dimmed (`alpha(0.38f)`) and non-selectable, since Material's `DatePicker` has no `enabled` parameter.
- On Android, later `minMillis`/`maxMillis` updates re-gate which dates are selectable, but the **year dropdown range is fixed at first composition** — Material's `DatePickerState` captures `yearRange` once, and rebuilding the state to widen the dropdown would drop the user's selection.
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
- On Android the swatch has a ≥48dp touch target, a button role with a "Pick a color" action label, and announces as disabled when `enabled = false`; each preset swatch in the dialog is announced as "Color N of M" with its selected state.

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
- A single page (or none) shows no dots on either platform — iOS via `hidesForSinglePage` (the iOS convention), Android by rendering nothing at all.
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

Two overloads — one owner of the page count each. `NativePager(pageCount, …)` owns its state internally; `NativePager(state, …)` takes a caller-owned `PagerState` and has **no count parameter** (the state's `pageCount` lambda is the single source of truth — previously a passed count was silently ignored when a state was also given).

| Parameter | Type | Default | Description |
|---|---|---|---|
| `pageCount` | `Int` | — | *(count overload only)* Total pages; feeds the internal `PagerState`. |
| `state` | `PagerState` | — | *(state overload only)* Caller-owned pager state; carries the page count. |
| `modifier` | `Modifier` | `Modifier` | Layout modifier. |
| `contentPadding` | `PaddingValues` | `PaddingValues(0.dp)` | Padding around the pages (e.g. to peek neighbors). |
| `pageSize` | `PageSize` | `PageSize.Fill` | Page sizing: full-viewport pages, or `PageSize.Fixed(dp)` for carousel-style cells. |
| `pageSpacing` | `Dp` | `0.dp` | Gap between pages. |
| `beyondViewportPageCount` | `Int` | `0` | Pages composed ahead/behind the visible ones (pre-load heavy pages). |
| `userScrollEnabled` | `Boolean` | `true` | Whether swipe gestures page. |
| `key` | `((page: Int) -> Any)?` | `null` | Stable key per page. |
| `testTag` | `String?` | `null` | Test identifier on the pager. |
| `pageContent` | `@Composable PagerScope.(page: Int) -> Unit` | — | The page slot. |

**Example**

```kotlin
// Self-contained: the pager owns its state.
NativePager(items.size) { page -> PageCard(items[page]) }

// Driven: own the state to bind the dots.
val state = rememberPagerState { items.size }
val scope = rememberCoroutineScope()
NativePager(state) { page -> PageCard(items[page]) }
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

Infinite-scroll helpers for a paginated `LazyColumn` or lazy grid: an effect that fires as the list nears its end, and a slottable footer. Compose-on-both (built on `LazyListState`/`LazyGridState` and `LazyListScope`/`LazyGridScope`).

**Use it when**
- A long, server-paged list or grid should load the next page as the user approaches the end.

**API**

| Symbol | Signature | Description |
|---|---|---|
| `NativePageLoadState` | `enum { Idle, Loading, Error, EndReached }` | The list's load state; you own the transitions. |
| `NativeLoadMoreEffect` | `@Composable (listState: LazyListState, buffer: Int = 3, enabled: Boolean = true, onLoadMore: () -> Unit)` | Fires `onLoadMore` when the list scrolls within `buffer` items of the end — at most once per item count, re-armed when the count grows (a page that doesn't fill the viewport still chains into the next load). Guard concurrent loads via `enabled`. |
| `NativeLoadMoreEffect` (grid) | `@Composable (gridState: LazyGridState, buffer: Int = 3, enabled: Boolean = true, onLoadMore: () -> Unit)` | The same trigger and re-arm rules for a lazy grid. |
| `LazyListScope.nativePaginationFooter` | `(state, onRetry: (() -> Unit)? = null, loading = { spinner }, error = { retry -> button })` | A footer row: spinner (announced as "Loading") while `Loading`, a retry slot on `Error`, nothing at `Idle`/`EndReached`. With `onRetry = null` the default error slot renders no Retry button (no dead affordance); the slot receives the nullable callback. Both slots overridable. |
| `LazyGridScope.nativePaginationFooter` | same as the list variant | The grid footer spans the full line (`GridItemSpan(maxLineSpan)`), so it sits centered under the grid instead of inside one cell. |

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
- `NativeLoadMoreEffect` holds no paging state — you decide what "load more" does and track your own `NativePageLoadState`. Set `enabled = false` (e.g. at `EndReached` or while loading) to pause; re-enabling re-arms, so a list still near its end after a load chains into the next page.
- A load that adds no items (an error) does not re-fire — surface it via the footer's `Error` state and `onRetry` instead.
- Nesting a `LazyColumn` inside another vertical scroll needs a bounded height on the inner list.
