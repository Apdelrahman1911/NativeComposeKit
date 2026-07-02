# Overlays

Components that present content above the current screen: bottom sheets, popovers, modal dialogs, and the system share UI.

### NativeSheet

A bottom sheet for a scoped task, shown while `visible` is true.

**Android:** Material 3 `ModalBottomSheet`.
**iOS:** a native `UISheetPresentationController` (system detents, grabber, swipe-to-dismiss, iOS 26 Liquid Glass) hosting the Compose `content`.

**Use it when**
- You need a detented panel for a focused, dismissible task (filters, a short form, an action list).
- You want a compact-width panel on iPhone (prefer this over a popover there).

**Avoid it when**
- The content is a plain text + buttons confirmation — use `NativeFeedbackController.alert` for a real system alert instead.
- You need centered custom content rather than a bottom-anchored panel — use `NativeDialog`.

**Parameters**

| Parameter | Type | Default | Description |
|---|---|---|---|
| `visible` | `Boolean` | — | Sheet is shown while this is true. |
| `onDismissRequest` | `() -> Unit` | — | Fires on swipe-down, scrim tap, or back. |
| `modifier` | `Modifier` | `Modifier` | Applies to the Android sheet only. |
| `detents` | `List<NativeSheetDetent>` | `listOf(NativeSheetDetent.Large)` | Allowed sizes. `Medium` ≈ half screen, `Large` ≈ full. |
| `showDragHandle` | `Boolean` | `true` | Shows the drag handle / grabber. |
| `testTag` | `String?` | `null` | Test tag for the sheet. |
| `content` | `@Composable () -> Unit` | — | Sheet body. |

**Example**

```kotlin
var open by remember { mutableStateOf(false) }
NativeButton("Filters", onClick = { open = true })
NativeSheet(
    visible = open,
    onDismissRequest = { open = false },
    detents = listOf(NativeSheetDetent.Medium, NativeSheetDetent.Large),
) {
    FilterControls()
}
```

**Notes**
- iOS runs `content` in a separate Compose composition (a presented `ComposeUIViewController`) that inherits no CompositionLocals. The component captures the parent theme (color scheme, typography, shapes), brand tokens and status colors, and layout direction, then re-provides them inside the sheet so the content matches your app in dark mode and RTL. Non-theme providers such as `LocalNativeFeedbackController` are not resolvable inside; pass a captured reference if the content needs one.
- The iOS host view controller is transparent so the sheet's native material (Liquid Glass) shows through behind the content; only light/dark is matched.
- The theme is captured at present time. A theme change while the sheet is open is not re-applied. Android keeps full context in-composition.
- `detents` containing `Medium` enables the partially-expanded state on Android; otherwise the sheet only goes full.

### NativePopover

A transient popover that floats a small surface near an `anchor`, shown while `visible` is true.

**Android:** a themed Compose `Popup` containing an elevated `NativeCard`.
**iOS (iPad / regular width):** a native `UIPopoverPresentationController` (system material and arrow) anchored to the `anchor`'s on-screen rect, hosting the Compose `content`.
**iOS (iPhone / compact width):** the same themed Compose popover as Android.

**Use it when**
- You need a small contextual panel attached to a trigger (an overflow menu, a quick detail) on iPad.
- You want a lightweight elevated surface anchored inline in layout.

**Avoid it when**
- You are targeting compact-width iPhone, where UIKit adapts a native popover to full screen — prefer `NativeSheet` for a compact-width panel.

**Parameters**

| Parameter | Type | Default | Description |
|---|---|---|---|
| `visible` | `Boolean` | — | Popover is shown while this is true. |
| `onDismissRequest` | `() -> Unit` | — | Fires on an outside tap. |
| `modifier` | `Modifier` | `Modifier` | Applies to the popover surface. |
| `alignment` | `Alignment` | `Alignment.BottomCenter` | Positions the Compose popover relative to the anchor. On the iPad native path it is a hint; UIKit picks the arrow direction. |
| `testTag` | `String?` | `null` | Test tag for the surface. |
| `anchor` | `(@Composable () -> Unit)?` | `null` | Optional trigger rendered inline in layout; the popover points at it. |
| `content` | `@Composable () -> Unit` | — | Popover body. |

**Example**

```kotlin
var open by remember { mutableStateOf(false) }
NativePopover(
    visible = open,
    onDismissRequest = { open = false },
    anchor = { NativeIconButton(moreIcon, onClick = { open = true }) },
) {
    NativeText("Details")
}
```

**Notes**
- The `anchor` slot is additive and backward-compatible. With `anchor = null`, iOS presents centered and Android positions by `alignment`.
- The iPhone / Android Compose path is pure Compose with no `UIKitView` interop, so there is no backdrop artifact. The card draws with brand surface, elevation, corner, spacing, and typography and adapts to light and dark.
- On the iPad native path the iOS content runs in a separate transparent composition with the parent theme re-provided, mirroring `NativeSheet`. If no anchor or presenter resolves, it falls back to a centered presentation rather than crashing.

### NativeDialog

A modal dialog for custom centered content, built to fit *your* design system rather than one fixed layout — a themed surface in a Compose `Dialog`, shown while it is in composition. `icon` / `title` / `actions` / `content` are slots; colors, shape, elevation, padding, and alignment are all overridable, with theme-resolved defaults.

**Android:** Compose `Dialog` + a themed surface.
**iOS:** the same Compose `Dialog` + surface (Compose-on-both).

**Use it when**
- You need custom centered content the native paths do not cover (a form, a list, an image).
- You want the dialog to match your product's look — its own icon, title, colors, shape, and layout.

**Avoid it when**
- The content is plain text plus buttons — use `NativeFeedbackController.alert` for a real `UIAlertController` on iOS.
- You want a bottom-anchored or detented panel — use `NativeSheet` for a real `UISheetPresentationController` on iOS.

**Parameters** (slot-based overload)

| Parameter | Type | Default | Description |
|---|---|---|---|
| `onDismissRequest` | `() -> Unit` | — | Fires on scrim tap or back, gated by `dismissOnClickOutside` / `dismissOnBackPress`. |
| `modifier` | `Modifier` | `Modifier` | Applies to the dialog surface. |
| `icon` | `(@Composable () -> Unit)?` | `null` | Optional icon slot, above the title. |
| `title` | `(@Composable () -> Unit)?` | `null` | Optional title slot (uses the title color). |
| `dismissOnBackPress` | `Boolean` | `true` | Allow dismissal via the back gesture. |
| `dismissOnClickOutside` | `Boolean` | `true` | Allow dismissal by tapping the scrim. |
| `colorsOverride` | `NativeDialogColors?` | `null` | Container / content / title / border colors; defaults resolve from the theme. |
| `cornerRadius` | `Dp?` | `null` | Surface corner radius (default `tokens.cornerLarge`). |
| `elevation` | `Dp?` | `null` | Surface shadow elevation (default `tokens.elevationOverlay`). |
| `contentPadding` | `PaddingValues?` | `null` | Inner padding (default `tokens.spacingLg`). |
| `horizontalAlignment` | `Alignment.Horizontal` | `Alignment.Start` | Column alignment of icon / title / content. |
| `properties` | `DialogProperties?` | `null` | Advanced `Dialog` properties (e.g. platform width); overrides the dismiss booleans when set. |
| `testTag` | `String?` | `null` | Test tag for the surface. |
| `actions` | `(@Composable RowScope.() -> Unit)?` | `null` | Trailing, end-aligned button row; omit for an informational dialog. |
| `content` | `@Composable ColumnScope.() -> Unit` | — | Dialog body. |

A convenience overload takes `title: String?` (rendered in the title slot with the dialog's title color) in place of the `icon` / `title` slots — for the common text-title case.

**Example**

```kotlin
// Simple (String title):
if (open) NativeDialog(
    title = "Delete file?",
    onDismissRequest = { open = false },
    actions = { NativeButton("Delete", onClick = ::delete) },
) { NativeText("This can't be undone.") }

// Customized (slots + colors + shape + centered):
NativeDialog(
    onDismissRequest = { open = false },
    icon = { NativeText("✨", style = NativeTextStyle.Display) },
    title = { NativeText("What's new", style = NativeTextStyle.Title) },
    colorsOverride = NativeDialogColors(
        container = scheme.secondaryContainer,
        content = scheme.onSecondaryContainer,
        title = scheme.onSecondaryContainer,
    ),
    cornerRadius = 28.dp,
    horizontalAlignment = Alignment.CenterHorizontally,
    actions = { NativeButton("Got it", onClick = { open = false }) },
) { NativeText("Reader themes and faster sync.", align = TextAlign.Center) }
```

**Notes**
- Kept Compose-on-both: a centered custom-content modal has no single native control to delegate to, so it is the intended primitive for arbitrary centered content. The native *fixed* alert is `NativeFeedbackController.alert`.
- On iOS a `Dialog` mounts a fresh native scene. To avoid a first-frame backdrop flash, the body composes with `LocalNativeSurface = Color.Unspecified` (so `NativeText` takes its Compose-`Text` path, no interop region) and the dialog provides `LocalNativeInteropPlacement = Overlay` (so native controls such as `NativeButton` actions composite above the opaque surface with no cut-out hole). All component types work normally inside.

### NativeShareSheet

The system share UI, presented imperatively through a `NativeShare` handle obtained from `rememberNativeShare()`. Sharing is a one-shot action, so this is a handle you invoke rather than a placed composable.

**Android:** an `ACTION_SEND` chooser with `text/plain` content.
**iOS:** a `UIActivityViewController` presented from the top-most view controller, popover-anchored on iPad.

**Use it when**
- You need to share text or a URL through the platform share sheet from a click handler.

**Avoid it when**
- You need a custom in-app share surface rather than the system sheet — build that with `NativeSheet`.

**Parameters**

`rememberNativeShare(): NativeShare` takes no parameters and returns the handle. Invoke one of its `share` overloads:

| Member | Type | Default | Description |
|---|---|---|---|
| `NativeShare.share` | `(text: String? = null, url: String? = null) -> Unit` | — | Presents the share sheet with the given text and/or URL. |
| `NativeShare.share` | `(content: NativeShareContent) -> Unit` | — | Presents the share sheet with a prepared `NativeShareContent`. |

`NativeShareContent` is a data class:

| Parameter | Type | Default | Description |
|---|---|---|---|
| `text` | `String?` | `null` | Text to share. |
| `url` | `String?` | `null` | URL to share. |

**Example**

```kotlin
val share = rememberNativeShare()
NativeButton("Share", onClick = { share.share(text = title, url = link) })
```

**Notes**
- On Android the text and URL are joined with a newline into the `EXTRA_TEXT` body. The chooser is launched with `FLAG_ACTIVITY_NEW_TASK`, which is safe from a non-Activity context.
- On iOS the sheet presents only when there is at least one item and a presenter resolves. On iPad it is anchored to the center of the presenter via KVC with no arrow, since the popover presentation otherwise asserts without a source anchor.
