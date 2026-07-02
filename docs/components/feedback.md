# Feedback & progress

Progress indicators and the feedback system: transient messages (toast, snackbar, banner), blocking modals (alert, confirmation sheet), and in-flow inline status, posted through a single controller mounted near the app root.

### NativeProgressIndicator

A determinate or indeterminate progress indicator. Pass `progress` in `0..1` for determinate; `null` for indeterminate.

**Android:** Material 3 `CircularProgressIndicator` or `LinearProgressIndicator`.
**iOS:** native `UIActivityIndicatorView` (indeterminate circular) or `UIProgressView` (determinate linear) via `UIKitView`. The other two combinations (determinate circular, indeterminate linear) fall back to the Compose-drawn Material indicator.

**Use it when**
- You need a spinner or a load/download bar that matches the platform.

**Avoid it when**
- The content shape is known ahead of time — use `NativeSkeleton` for placeholder loading.

**Parameters**

| Parameter | Type | Default | Description |
|---|---|---|---|
| `modifier` | `Modifier` | `Modifier` | Sizes the indicator. A linear bar is usually `Modifier.fillMaxWidth()`. |
| `kind` | `NativeProgressKind` | `NativeProgressKind.Circular` | `Circular` for an in-place spinner/ring; `Linear` for a horizontal bar. |
| `progress` | `Float?` | `null` | `0..1` for determinate; `null` for indeterminate. Clamped to `0..1`. |
| `color` | `Color?` | `null` | Indicator color. Falls back to `colorScheme.primary`. |
| `trackColor` | `Color?` | `null` | Track color. Falls back to `colorScheme.surfaceVariant`. |
| `contentDescription` | `String?` | `null` | Accessibility label. |
| `testTag` | `String?` | `null` | Test tag (maps to the accessibility id on iOS). |

**Example**

```kotlin
// Indeterminate spinner
NativeProgressIndicator()

// Determinate download bar
NativeProgressIndicator(
    modifier = Modifier.fillMaxWidth(),
    kind = NativeProgressKind.Linear,
    progress = downloaded,
)
```

**Notes**
- iOS is fully native only for indeterminate circular (`UIActivityIndicatorView`) and determinate linear (`UIProgressView`). Determinate circular and indeterminate linear have no native iOS control, so they render the Compose-drawn Material indicator — an intentional, documented mapping.
- The iOS `UIProgressView` is set with `setProgress(animated = false)`.
- Unlike the other iOS interop controls, this one does not set `overrideUserInterfaceStyle`; its colors come from the theme-resolved style and adapt to light/dark on their own.

## The feedback system

`NativeFeedbackController` holds the queue state and exposes plain (non-`@Composable`) post methods callable from any click lambda. Each returns a `Long` id you can pass to `dismiss(id)`. Mount it once near the app root with `NativeFeedbackHost { content }`; descendants post through `LocalNativeFeedbackController.current`.

**Ownership.** The constructor is public, so the controller can live wherever your architecture wants it: `rememberNativeFeedbackController()` for composition-scoped ownership, or construct `NativeFeedbackController()` yourself in a ViewModel, DI graph, or app-scoped singleton and pass it to the host. The contract either way: main thread only (its queues are plain snapshot state), create once and keep the instance, and mount **exactly one** `NativeFeedbackHost` for it — a second host would present every message twice.

The controller runs two parallel lanes: a transient lane (toast / snackbar / banner — one at a time, FIFO) and a modal lane (alert / confirmation sheet) that overlays a transient. It owns no coroutines or timers; visible timing belongs to the platform host (Android `Snackbar`/`delay`, iOS `NSTimer`), so a message is never timed twice. The controller stores only a `NativeFeedbackStatus`, never resolved colors.

### NativeFeedbackHost

Mounts the feedback system once, near the app root inside `NativeKitTheme`. Provides the controller to all descendants and renders the platform-appropriate surfaces over `content`.

**Android:** draws Compose overlays in a `Box` over the content (Material `SnackbarHost` / `AlertDialog` / `ModalBottomSheet` plus a themed banner/HUD).
**iOS:** presents real `UIAlertController`s and adds key-window overlays imperatively. It draws no Compose overlay itself; the host emits `content()` plus side effects, resolving per-status styles in composition before handing values to the UIKit layer.

**Use it when**
- Once, near the app root, so any descendant can post feedback.

**Avoid it when**
- You only need an in-flow status message — use `NativeInlineStatus` directly.

**Parameters**

| Parameter | Type | Default | Description |
|---|---|---|---|
| `controller` | `NativeFeedbackController` | `rememberNativeFeedbackController()` | The controller to provide and render. |
| `content` | `@Composable () -> Unit` | — | The app content the overlays sit above. |

**Example**

```kotlin
val feedback = rememberNativeFeedbackController()
NativeFeedbackHost(feedback) {
    AppContent()
}

// Or own it outside the composition (ViewModel / DI) and hand it to the host:
class AppViewModel : ViewModel() {
    val feedback = NativeFeedbackController()
}
NativeFeedbackHost(viewModel.feedback) { AppContent() }

// Anywhere below:
val feedback = LocalNativeFeedbackController.current
feedback.toast("Saved")
```

**Notes**
- `LocalNativeFeedbackController.current` throws if read outside a `NativeFeedbackHost`.
- `rememberNativeFeedbackController()` remembers a controller across recompositions; a directly-constructed controller works the same, as long as exactly one host renders it (see Ownership above).
- Composition locals (including `LocalNativeFeedbackController`) are not resolvable inside the native iOS overlays; pass a captured reference into action lambdas rather than reading the local there.
- Bottom-positioned transients stay above the keyboard: Android pads by the union of the navigation-bar and IME insets; iOS anchors the overlay to the window's keyboard layout guide.
- If a message is posted when nothing can show it (no iOS key window / no presenter — e.g. very early in launch), it resolves immediately through its dismiss/cancel path so the lane never stalls behind an invisible record.

### toast

A small transient HUD message ("Copied", "Saved"). Lightweight, no action. Posted through the controller.

**Android:** themed Compose HUD by default; the real `android.widget.Toast` when `android.useSystemToast` is set.
**iOS:** themed key-window HUD (iOS has no system toast).

**Use it when**
- Confirming a quick, low-stakes action with no follow-up.

**Avoid it when**
- The message needs an action (use `snackbar`) or must stay until dismissed (use `banner`).

**Parameters**

| Parameter | Type | Default | Description |
|---|---|---|---|
| `message` | `String` | — | The text to show. |
| `status` | `NativeFeedbackStatus` | `NativeFeedbackStatus.Info` | Drives color and default icon. |
| `duration` | `NativeFeedbackDuration` | `NativeFeedbackDuration.Short` | Auto-dismiss timing. |
| `position` | `NativeFeedbackPosition` | `NativeFeedbackPosition.Bottom` | Top or bottom of the screen. |
| `queue` | `NativeQueueBehavior` | `NativeQueueBehavior.Enqueue` | Behavior when a transient is already showing. |
| `onDismiss` | `(() -> Unit)?` | `null` | Called when it auto-dismisses or is dismissed. |
| `android` | `NativeToastAndroidOptions` | `NativeToastAndroidOptions()` | Android-only knobs (see below). |

**Example**

```kotlin
feedback.toast("Saved")
feedback.toast("Copied to clipboard", status = NativeFeedbackStatus.Success)
```

**Notes**
- The toast HUD is not swipe-dismissable on either platform.
- The system `Toast` (`android.useSystemToast = true`) renders even outside the app but is unstyleable on Android 12+ and ignores the brand theme and dark mode. Dismissing or replacing the record through the controller also cancels the OS toast, so what is on screen never outlives the lane.

### snackbar

A bottom message with an optional action (e.g. "Item deleted — Undo"). Posted through the controller.

**Android:** Material `Snackbar` with branded container/content/action colors. Material owns its own timing and result routing.
**iOS:** themed bottom overlay with the action button.

**Use it when**
- An action can follow the message — most commonly Undo.

**Avoid it when**
- The message is purely informational (use `toast`) or must be pinned and prominent (use `banner`).

**Parameters**

| Parameter | Type | Default | Description |
|---|---|---|---|
| `message` | `String` | — | The text to show. |
| `status` | `NativeFeedbackStatus` | `NativeFeedbackStatus.Info` | Drives color and default icon. |
| `actionLabel` | `String?` | `null` | Label for the action button (e.g. "Undo"). |
| `onAction` | `(() -> Unit)?` | `null` | Called when the action button is tapped. |
| `duration` | `NativeFeedbackDuration` | `if (actionLabel == null) NativeFeedbackDuration.Short else NativeFeedbackDuration.Indefinite` | Auto-dismiss timing. Defaults to indefinite when there is an action. |
| `queue` | `NativeQueueBehavior` | `NativeQueueBehavior.Enqueue` | Behavior when a transient is already showing. |
| `onDismiss` | `(() -> Unit)?` | `null` | Called on plain dismiss (not the action). |
| `swipeToDismiss` | `Boolean` | `true` | Lets the user swipe it downward to dismiss (a plain dismiss, no action). |

**Example**

```kotlin
feedback.snackbar(
    message = "Item deleted",
    actionLabel = "Undo",
    onAction = { restore() },
)
```

**Notes**
- Snackbar is always at the bottom (platform convention); there is no position parameter.
- Swipe-to-dismiss runs `onDismiss`, not `onAction`. On Android the Material snackbar resolves through `SnackbarData.dismiss()`; on iOS a `UIPanGestureRecognizer` drives the key-window overlay.

### banner

A prominent non-blocking message pinned top or bottom, with an optional title, action, and close button. Posted through the controller.

**Android:** themed Compose banner.
**iOS:** key-window banner with the same icon/action/close affordances.

**Use it when**
- A status needs to persist and stay visible (often `Indefinite`), with an optional action.

**Avoid it when**
- The message is brief and transient (use `toast` or `snackbar`).

**Parameters**

| Parameter | Type | Default | Description |
|---|---|---|---|
| `message` | `String` | — | The body text. |
| `title` | `String?` | `null` | Optional title above the message. |
| `status` | `NativeFeedbackStatus` | `NativeFeedbackStatus.Info` | Drives color and default icon. |
| `position` | `NativeFeedbackPosition` | `NativeFeedbackPosition.Top` | Top or bottom of the screen. |
| `actionLabel` | `String?` | `null` | Label for the action button. |
| `onAction` | `(() -> Unit)?` | `null` | Called when the action button is tapped. |
| `dismissible` | `Boolean` | `true` | Shows a trailing close button. |
| `duration` | `NativeFeedbackDuration` | `NativeFeedbackDuration.Indefinite` | Auto-dismiss timing; indefinite stays until dismissed. |
| `queue` | `NativeQueueBehavior` | `NativeQueueBehavior.Enqueue` | Behavior when a transient is already showing. |
| `onDismiss` | `(() -> Unit)?` | `null` | Called on dismiss (close button, swipe, code, or timeout). |
| `swipeToDismiss` | `Boolean` | `true` | Lets the user swipe it toward its pinned edge to dismiss. |

**Example**

```kotlin
feedback.banner(
    title = "Offline",
    message = "Changes will sync when you reconnect.",
    status = NativeFeedbackStatus.Warning,
    actionLabel = "Retry",
    onAction = { retry() },
)
```

**Notes**
- An `Indefinite` banner holds the single transient slot until dismissed, so later transients queue behind it.
- `swipeToDismiss` is independent of `dismissible`. A banner that only code can dismiss sets both `dismissible = false` and `swipeToDismiss = false`.
- The action and close affordances meet the platform minimum touch target (≥48 dp on Android, ≥44 pt on iOS — the glyph stays small; the hit area grows), and the close button is announced with the localized `strings.dismiss`.

### alert

A blocking alert requiring a choice. Posted through the controller (modal lane).

**Android:** Material `AlertDialog`, themed.
**iOS:** native `UIAlertController` (`.alert` style) by default, presented from the top-most view controller; a brand-themed custom overlay when `ios.presentation = Branded`.

**Use it when**
- The user must make a decision before continuing (confirm, discard, retry).

**Avoid it when**
- The choice is a list of actions — use `confirmationSheet`. For custom centered content, use `NativeDialog`.

**Parameters**

| Parameter | Type | Default | Description |
|---|---|---|---|
| `title` | `String?` | `null` | Alert title. |
| `message` | `String?` | `null` | Alert body. |
| `actions` | `List<NativeAlertAction>` | — | The buttons. A `Cancel`-role action maps to the dialog's dismiss button on Android. |
| `onCancel` | `(() -> Unit)?` | `null` | Called when dismissed without choosing an action (scrim/back/cancel gesture). |
| `ios` | `NativeAlertIosOptions` | `NativeAlertIosOptions()` | iOS presentation strategy (see below). |

**Example**

```kotlin
feedback.alert(
    title = "Delete item?",
    actions = listOf(
        NativeAlertAction("Delete", { delete() }, role = NativeAlertActionRole.Destructive),
        NativeAlertAction("Cancel", role = NativeAlertActionRole.Cancel),
    ),
)
```

**Notes**
- A `NativeAlertAction.onClick` runs after the alert dismisses.
- At most one `Cancel`-role action per alert. `Destructive` renders red (iOS `UIAlertActionStyleDestructive` / Material error).
- iOS chrome intentionally differs from Android — each follows its own platform convention.
- On Android the non-cancel buttons flow-wrap in the dialog's action area, so three or more actions (or long labels) break onto new lines instead of clipping.

### confirmationSheet

A blocking action sheet for choosing among actions. Posted through the controller (modal lane).

**Android:** Material `ModalBottomSheet`, themed, with the actions as rows.
**iOS:** native action-sheet `UIAlertController` (`.actionSheet`) by default; a brand-themed custom overlay when `ios.presentation = Branded`.

**Use it when**
- Offering a short list of actions tied to a single subject (Share, Delete, Rename…).

**Avoid it when**
- There is a single yes/no decision — use `alert`. For a presentational content sheet, use `NativeSheet`.

**Parameters**

| Parameter | Type | Default | Description |
|---|---|---|---|
| `title` | `String?` | `null` | Sheet title. |
| `message` | `String?` | `null` | Sheet body. |
| `actions` | `List<NativeConfirmationAction>` | — | The action rows. Each may carry a `NativeIcon`. |
| `onCancel` | `(() -> Unit)?` | `null` | Called when dismissed without choosing an action. |
| `ios` | `NativeConfirmationSheetIosOptions` | `NativeConfirmationSheetIosOptions()` | iOS presentation strategy (see below). |

**Example**

```kotlin
feedback.confirmationSheet(
    title = "Photo",
    actions = listOf(
        NativeConfirmationAction("Share", { share() }),
        NativeConfirmationAction("Delete", { delete() }, role = NativeAlertActionRole.Destructive),
        NativeConfirmationAction("Cancel", role = NativeAlertActionRole.Cancel),
    ),
)
```

**Notes**
- `NativeConfirmationAction.icon` renders as a leading glyph on Android (`NativeIcon.androidImageVector`) and in the iOS **Branded** presentation (`NativeIcon.sfSymbolName`, tinted with the row's text color — destructive rows stay red). The iOS **Native** system sheet has no public action-image API and drops the icon.
- On iPad the native action sheet is anchored to the centre of the presenter's view via KVC (`popoverPresentationController` is not a static member in this Kotlin/Native UIKit version); on iPhone the popover controller is `nil`, so the anchoring is a no-op and behavior is unchanged.

### NativeInlineStatus

An inline status message that lives in the layout flow (field validation, an empty-state note, a "Saved locally" hint). It is not a floating overlay and is not posted through `NativeFeedbackController`; place it in your layout and drive its visibility from your own state.

**Android:** Compose-drawn in-flow.
**iOS:** Compose-drawn in-flow (no UIKit interop), so it sizes to its content within a Compose column and still reads as native (system font, themed colors).

**Use it when**
- A status belongs in the content (a validation message under a field, a sync hint).

**Avoid it when**
- The message should float over the UI — use `toast`/`snackbar`/`banner` through the controller.

**Parameters**

| Parameter | Type | Default | Description |
|---|---|---|---|
| `text` | `String` | — | The body text. |
| `status` | `NativeFeedbackStatus` | `NativeFeedbackStatus.Info` | Drives color and default icon. |
| `modifier` | `Modifier` | `Modifier` | Applied to the container; the component fills max width. |
| `title` | `String?` | `null` | Optional title above the text. |
| `icon` | `ImageVector?` | `null` | A Compose vector overriding the default status glyph. |
| `showIcon` | `Boolean` | `true` | Hides the leading icon entirely when `false`. |
| `filled` | `Boolean` | `true` | `true` = soft tonal container fill; `false` = outlined on the page surface. |
| `actionLabel` | `String?` | `null` | Label of the optional inline text action. The action renders only when **both** `actionLabel` and `onAction` are set. |
| `onAction` | `(() -> Unit)?` | `null` | Called when the inline action is tapped (the action renders only when both `actionLabel` and `onAction` are set). |
| `onDismiss` | `(() -> Unit)?` | `null` | Adds a trailing close button that calls this. |
| `contentDescription` | `String?` | `null` | Accessibility label for the container. |
| `testTag` | `String?` | `null` | Test tag. |

**Example**

```kotlin
NativeInlineStatus(
    text = "Saved locally — will sync when online.",
    status = NativeFeedbackStatus.Info,
)
```

**Notes**
- The status is announced to screen readers when it appears or changes: `Error` interrupts (Assertive), other statuses are queued (Polite).
- The default merged accessibility description leads with the localized status name (`strings.statusSuccess` / `statusWarning` / `statusInfo` / `statusError`), so severity is not conveyed by color alone. An explicit `contentDescription` replaces the whole description untouched.
- The inline action and the close button meet the ≥48 dp minimum touch target (the visible text/glyph stays small; the hit area grows).
- `icon` takes a plain Compose `ImageVector` (there is no SF-Symbol slot); it is Compose-drawn on both platforms.
- The outlined variant (`filled = false`) matches the surface it is embedded in (page or card) via `LocalNativeSurface`.

## Shared enums and models

These back the feedback API surface above.

| Type | Values / fields | Notes |
|---|---|---|
| `NativeProgressKind` | `Circular`, `Linear` | Progress shape. |
| `NativeFeedbackStatus` | `Info`, `Success`, `Warning`, `Error` | Drives color and default icon. `Error` reuses `colorScheme.error`; the others come from `NativeStatusColors`. |
| `NativeFeedbackPosition` | `Top`, `Bottom` | Where a toast/banner appears. Snackbar is always bottom. |
| `NativeFeedbackDuration` | `Short` (≈2 s), `Long` (≈3.5 s), `Indefinite` | Indefinite has no timer; it stays until dismissed by the user or code. |
| `NativeQueueBehavior` | `Enqueue`, `ReplaceCurrent`, `DropIfShowing` | What happens when a transient is posted while one is showing. |
| `NativeAlertActionRole` | `Default`, `Cancel`, `Destructive` | At most one `Cancel` per alert. `Destructive` renders red. |
| `NativePresentation` | `Native`, `Branded` | iOS presentation strategy for alert/sheet. |
| `NativeAlertAction` | `label: String`, `onClick: () -> Unit = {}`, `role: NativeAlertActionRole = Default` | One alert button. `onClick` runs after the alert dismisses. |
| `NativeConfirmationAction` | `label: String`, `onClick: () -> Unit = {}`, `role: NativeAlertActionRole = Default`, `icon: NativeIcon? = null` | One sheet row. |
| `NativeToastAndroidOptions` | `useSystemToast: Boolean = false` | Android-only toast knob. |
| `NativeAlertIosOptions` | `presentation: NativePresentation = Native` | iOS-only alert knob. |
| `NativeConfirmationSheetIosOptions` | `presentation: NativePresentation = Native` | iOS-only confirmation-sheet knob. |

### Controller dismiss methods

Besides the post methods, `NativeFeedbackController` exposes:

| Method | Effect |
|---|---|
| `dismiss(id: Long)` | Dismiss a specific message by its returned id (either lane). |
| `dismissCurrent()` | Dismiss whatever transient is showing now (e.g. an indefinite banner) and advance the queue. |
| `dismissCurrentModal()` | Dismiss the current modal as a cancel (runs its `onCancel`) and advance the modal queue. |
| `clearAll()` | Hard reset — clears both lanes immediately without invoking callbacks. |

### Known limitations

- **iOS: a transient can draw above a native modal.** The toast/snackbar/banner overlays attach to the key window, while a `Native`-presentation alert/sheet is a `UIAlertController` presented into the same window — whichever appeared last stacks on top. A toast posted while a native alert is up therefore renders above the alert. The kit does not reorder window subviews to correct this (see the interop notes on why non-normal windows and presentation containers are never touched); if the overlap matters, post the transient after the modal resolves (e.g. from the action's callback), or use the `Branded` modal presentation, which lives in the same overlay layer.
