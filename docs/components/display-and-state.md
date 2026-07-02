# Display & state

Components for showing content lifecycle, placeholders, and small status decorations: load-state switching, skeletons, empty states, pull-to-refresh, badges, avatars, and chips.

### NativeContentState

A switcher that renders the right UI for a `NativeLoadState`: a spinner while loading, an empty state, an error state with an optional retry action, or your content.

**Android:** Compose-drawn. A centered `NativeProgressIndicator`, `NativeEmptyState`, or your `content`.
**iOS:** same. Compose-drawn on both platforms.

**Use it when**
- A screen loads data and you want the standard loading → empty → error → content flow in one place.
- Your view-model already models state as a sealed type; `NativeLoadState` is Compose-free and can live there.

**Avoid it when**
- The content has no async lifecycle (render it directly).

**Parameters**

| Parameter | Type | Default | Description |
|---|---|---|---|
| `state` | `NativeLoadState<T>` | — | Current load state. |
| `modifier` | `Modifier` | `Modifier` | Applied to the content box and the centered placeholder box. |
| `onRetry` | `(() -> Unit)?` | `null` | When set, the default error state shows a retry button. |
| `emptyTitle` | `String` | `LocalNativeStrings.current.emptyStateTitle` (English: `"Nothing here yet"`) | Title for the default empty state; localized via `NativeStrings`. |
| `emptyMessage` | `String?` | `null` | Message for the default empty state. |
| `emptyIcon` | `ImageVector?` | `null` | Icon for the default empty and error states. |
| `errorTitle` | `String` | `LocalNativeStrings.current.errorStateTitle` (English: `"Something went wrong"`) | Title for the default error state; localized via `NativeStrings`. |
| `retryLabel` | `String` | `LocalNativeStrings.current.retry` (English: `"Retry"`) | Label for the retry button; localized via `NativeStrings`. |
| `loadingContent` | `(@Composable () -> Unit)?` | `null` | Override for the loading visual (e.g. skeleton rows). |
| `emptyContent` | `(@Composable () -> Unit)?` | `null` | Override for the empty visual. |
| `errorContent` | `(@Composable (NativeLoadState.Error) -> Unit)?` | `null` | Override for the error visual; receives the error state. |
| `content` | `@Composable (T) -> Unit` | — | Renders the loaded payload. |

**Example**

```kotlin
NativeContentState(state = uiState, onRetry = ::reload) { items ->
    LazyColumn {
        items(items) { item -> NativeListItem(headline = item.name) }
    }
}
```

**Notes** — `NativeLoadState<T>` is a sealed interface with `Loading`, `Empty`, `Error(message: String?, cause: Throwable?)`, and `Content<T>(value: T)`. It is Compose-free so view-models can drive it without a UI dependency (`cause` is for logging — never rendered). The retry button only appears when `onRetry` is provided. `content` is rendered directly and owns its own layout; the loading, empty, and error visuals are centered with `fillMaxSize`. Failures use `errorIcon`, falling back to `emptyIcon` when unset. Branch changes cross-fade by default (`animate = true`) and hard-cut automatically under the OS reduce-motion setting.

### NativeSkeleton

A loading-placeholder block with an animated shimmer sweep, for grids, list rows, and detail screens before data arrives.

**Android:** Compose-drawn shimmer block.
**iOS:** same. Compose-drawn on both platforms.

**Use it when**
- You want a placeholder that suggests layout while loading.
- You are composing several blocks to mock a card or row.

**Avoid it when**
- The content is not actually loading; show skeletons only while a load is in flight.

**Parameters**

| Parameter | Type | Default | Description |
|---|---|---|---|
| `modifier` | `Modifier` | `Modifier` | Sizes the block. The caller supplies the size. |
| `shape` | `Shape?` | `null` | Clip shape; defaults to a small rounded corner from the theme tokens. |
| `shimmer` | `Boolean` | `!LocalNativeCapabilities.current.isReduceMotionEnabled` | Whether the sweep animates. |
| `testTag` | `String?` | `null` | Test tag on the block. |

**Example**

```kotlin
Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    NativeSkeleton(Modifier.size(120.dp, 170.dp))
    NativeSkeleton(Modifier.fillMaxWidth().height(16.dp))
    NativeSkeleton(Modifier.fillMaxWidth(0.6f).height(16.dp))
}
```

**Notes** — the caller sizes the block via `modifier` (e.g. `Modifier.size(120.dp, 170.dp)` for a cover, `Modifier.fillMaxWidth().height(16.dp)` for a text line). Colors derive from the surface the skeleton sits on (the published `LocalNativeSurface` nudged toward `onSurface`), so it stays visible on the page, inside a Filled card, and in dark mode; it falls back to `surfaceVariant` only when no surface is published. The sweep follows the layout direction and reverses in RTL. The animation runs only while composed, so swap to real content to stop it. `shimmer` defaults to off when the OS reduce-motion setting is on; pass `false` for an always-static placeholder or `true` to force the animation.

### NativeEmptyState

A centered empty or no-results state: empty library, no search matches, nothing downloaded.

**Android:** Compose-drawn column of icon, title, message, and optional button.
**iOS:** same. Compose-drawn on both platforms.

**Use it when**
- A list or screen has no content and you want a titled placeholder with an optional call to action.

**Avoid it when**
- You are switching on a load lifecycle; use `NativeContentState`, which renders this for you.

**Parameters**

| Parameter | Type | Default | Description |
|---|---|---|---|
| `title` | `String` | — | Primary line; carries the accessible meaning. |
| `modifier` | `Modifier` | `Modifier` | Applied to the column (fills width, adds large padding). |
| `message` | `String?` | `null` | Secondary explanatory line. |
| `icon` | `ImageVector?` | `null` | Decorative glyph above the title. |
| `actionLabel` | `String?` | `null` | Button label; the button shows only with `onAction`. |
| `onAction` | `(() -> Unit)?` | `null` | Button click handler; the button shows only with `actionLabel`. |
| `contentDescription` | `String?` | `null` | Accessible description for the whole state. |
| `testTag` | `String?` | `null` | Test tag on the column. |

**Example**

```kotlin
NativeEmptyState(
    title = "No results",
    message = "Try a different search.",
    icon = Icons.Default.Search,
)
```

**Notes** — `icon` is a plain Compose `ImageVector`; there is no SF-Symbol slot. The glyph is decorative and the `title` carries the accessible meaning. The call-to-action `NativeButton` appears only when both `actionLabel` and `onAction` are given. To center it on screen, wrap it in `Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center)`.

### NativePullRefresh

A pull-to-refresh container: wrap vertically-scrollable content; a downward overscroll at the top fires `onRefresh` and the themed spinner shows while `isRefreshing` is true.

**Android:** Compose `PullToRefreshBox`, themed by `MaterialTheme`.
**iOS:** same Compose `PullToRefreshBox`, not a native `UIRefreshControl`.

**Use it when**
- You have a scrollable list or column the user should be able to refresh by pulling.

**Avoid it when**
- You need a native iOS `UIRefreshControl`; this is the Compose pull-refresh on both platforms.

**Parameters**

| Parameter | Type | Default | Description |
|---|---|---|---|
| `isRefreshing` | `Boolean` | — | Whether the refresh spinner is shown. The caller owns this flag. |
| `indicatorColor` | `Color` | `Unspecified` | Spinner color (theme `primary` by default). |
| `indicatorContainerColor` | `Color` | `Unspecified` | Spinner bubble color. |
| `testTag` | `String?` | `null` | Test tag on the container. |
| `onRefresh` | `() -> Unit` | — | Fired on a downward overscroll at the top. |
| `modifier` | `Modifier` | `Modifier` | Applied to the box. |
| `content` | `@Composable BoxScope.() -> Unit` | — | The scrollable content. |

**Example**

```kotlin
NativePullRefresh(isRefreshing = loading, onRefresh = ::reload) {
    LazyColumn { items(rows) { row -> NativeListItem(headline = row.title) } }
}
```

**Notes** — the caller owns `isRefreshing` and flips it back to false when the load completes. On iOS this is the Compose pull-refresh, not a native `UIRefreshControl`; a native control would require hosting the scroll in a `UIScrollView` via interop, which is out of scope for a content-level control. This is an intentional, documented platform divergence.

### NativeBadge

A small status or count badge: unread count, notification count, or a "NEW" dot.

**Android:** Compose-drawn Material 3 `Badge` (a styled overlay, not a native leaf control).
**iOS:** same. Compose-drawn on both platforms.

**Use it when**
- You want a count or dot overlaid on an icon or thumbnail.

**Avoid it when**
- The value can be zero or negative and you want nothing shown; that case already renders nothing, but for arbitrary inline text use styled text instead.

**Parameters**

| Parameter | Type | Default | Description |
|---|---|---|---|
| `modifier` | `Modifier` | `Modifier` | Applied to the badge. |
| `count` | `Int?` | `null` | `null` renders a dot; a positive number renders a numbered pill. |
| `maxCount` | `Int` | `99` | Counts above this show as `"<maxCount>+"` (e.g. `100 → "99+"`). |
| `containerColor` | `Color?` | `null` | Background; defaults to `error`. |
| `contentColor` | `Color?` | `null` | Number color; defaults to `onError`. |
| `contentDescription` | `String?` | `null` | Accessible description; defaults to the displayed number. |
| `testTag` | `String?` | `null` | Test tag on the badge. |

**Example**

```kotlin
NativeBadgedBox(badge = { NativeBadge(count = 3, contentDescription = "3 unread") }) {
    Icon(Icons.Default.Notifications, contentDescription = null)
}
```

**Notes** — with `count` null it is a dot; with a positive count it is a numbered pill capped at `maxCount`; a non-positive count renders nothing (the "no badge when zero" UX). The default is the unread red (`error` / `onError`); pass `containerColor` and `contentColor` (e.g. `NativeTheme.statusColors.success`) for a semantic badge. A numbered badge announces its count to screen readers by default; pass a contextual `contentDescription` like `"5 unread"` when the bare number is ambiguous. Overlay it with `NativeBadgedBox(badge, modifier, content)`, which anchors the badge on the top-end corner and stays correct in RTL via Material's `BadgedBox`.

### NativeAvatar

A user, author, or series avatar that renders an image, initials, or an icon.

**Android:** Compose-drawn clipped box with the resolved content.
**iOS:** same. Compose-drawn on both platforms.

**Use it when**
- You need a circular profile image, initials fallback, or an icon avatar.
- You have a loaded `Painter` to display.

**Avoid it when**
- You want network image loading built in; this kit ships no loader, so pass your own loaded painter.

**Parameters**

| Parameter | Type | Default | Description |
|---|---|---|---|
| `modifier` | `Modifier` | `Modifier` | Applied to the sized, clipped box. |
| `image` | `Painter?` | `null` | Caller-supplied painter; takes priority. |
| `initials` | `String?` | `null` | Shown when no image; first two code points, uppercased. |
| `icon` | `ImageVector?` | `null` | Shown when no image or initials. |
| `size` | `NativeAvatarSize` | `NativeAvatarSize.Medium` | `Small` 32dp · `Medium` 40dp · `Large` 56dp. |
| `shape` | `NativeAvatarShape` | `NativeAvatarShape.Circle` | `Circle` for people; `Rounded` for content thumbnails. |
| `containerColor` | `Color?` | `null` | Background; defaults to `primaryContainer`. |
| `contentColor` | `Color?` | `null` | Initials/icon color; defaults to `onPrimaryContainer`. |
| `contentDescription` | `String?` | `null` | Accessible description for the whole avatar. |
| `testTag` | `String?` | `null` | Test tag on the box. |

**Example**

```kotlin
NativeAvatar(initials = "JD")
NativeAvatar(image = painter, shape = NativeAvatarShape.Rounded, size = NativeAvatarSize.Large)
```

**Notes** — renders the first available of `image`, `initials`, or `icon`, falling back to an empty tinted shape. `initials` uses the first two code points (surrogate-pair safe, so an emoji yields one whole glyph), uppercased. `icon` is a plain Compose `ImageVector`; there is no SF-Symbol slot. A `contentDescription` merges descendants so the avatar announces as one node rather than reading out the raw initials.

### NativeChip

A compact chip for genre tags, filters, and removable selections.

**Android:** the matching Material chip per `style` (`AssistChip` / `FilterChip` / `InputChip` / `SuggestionChip`), themed by `MaterialTheme`.
**iOS:** same. Compose-drawn on both platforms (iOS has no native chip control).

**Use it when**
- You need a tappable tag, a selectable filter, or a removable entry.

**Avoid it when**
- You want a purely static, non-interactive label; chips show a ripple and announce as a button, so render styled text instead.

**Parameters**

| Parameter | Type | Default | Description |
|---|---|---|---|
| `label` | `String` | — | Chip text; single-line and ellipsized. |
| `modifier` | `Modifier` | `Modifier` | Applied to the chip. |
| `style` | `NativeChipStyle` | `NativeChipStyle.Assist` | `Assist` action · `Filter` toggle · `Input` removable · `Suggestion` tappable. |
| `selected` | `Boolean` | `false` | Tinted selected state for `Filter` and `Input`. |
| `onClick` | `() -> Unit` | `{}` | Click handler. |
| `enabled` | `Boolean` | `true` | Whether the chip is interactive. |
| `leadingIcon` | `ImageVector?` | `null` | Leading glyph (18dp). |
| `trailingIcon` | `ImageVector?` | `null` | Trailing glyph; ignored for `Suggestion`. |
| `onTrailingClick` | `(() -> Unit)?` | `null` | When set with `trailingIcon`, adds a labeled remove target. |
| `contentDescription` | `String?` | `null` | Accessible description for the chip. |
| `testTag` | `String?` | `null` | Test tag on the chip. |

**Example**

```kotlin
NativeChip("Action", style = NativeChipStyle.Filter, selected = isOn, onClick = { isOn = !isOn })
```

**Notes** — `style` maps to the matching Material chip: `Assist` is a one-shot action, `Filter` is a selectable filter (`selected` toggles the tinted state), `Input` is a removable entry (set `trailingIcon` to a close glyph plus `onTrailingClick`), and `Suggestion` is a tappable suggestion with no trailing slot (a `trailingIcon` is ignored). Icons are plain Compose `ImageVector`s on both platforms; there is no SF-Symbol slot. The label is single-line and ellipsized. When set, `onTrailingClick` wraps the 18dp glyph in a ≥48dp target that announces as "Remove". The `Assist` and `Suggestion` styles draw a visible `outline` border so the edge stays visible on any surface; selection styles keep their state-driven Material border.
