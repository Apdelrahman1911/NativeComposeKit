# Text & input

Components for displaying text and collecting typed input. Each renders the most native primitive available on the platform.

### NativeText

A text label. Defaults come from the theme type scale via `style`; every other parameter is an optional override.

**Android:** Compose `Text`.
**iOS:** a real `UILabel` via `UIKitView` on a known solid surface, or Compose `Text` on a material/glass surface (see Notes).

**Use it when**
- You need a label, paragraph, or heading anywhere in content.

**Avoid it when**
- The text is the editable content of a field — use `NativeTextField`.

**Parameters**

| Parameter | Type | Default | Description |
|---|---|---|---|
| `text` | `String` | — | The string to display. |
| `modifier` | `Modifier` | `Modifier` | Layout modifier. |
| `style` | `NativeTextStyle` | `NativeTextStyle.Body` | Type-scale role: `Display`, `Title`, `Body`, `Label`. |
| `color` | `Color` | `Color.Unspecified` | Text color. Unspecified falls back to `onSurface`. |
| `fontWeight` | `FontWeight?` | `null` | Overrides the weight from the style. |
| `align` | `TextAlign?` | `null` | Text alignment. |
| `maxLines` | `Int` | `Int.MAX_VALUE` | Maximum line count. |
| `overflow` | `TextOverflow` | `TextOverflow.Clip` | How overflow is handled when `maxLines` is exceeded. |
| `textStyleOverride` | `TextStyle?` | `null` | A `TextStyle` merged over the resolved style for fine-grained control. |
| `testTag` | `String?` | `null` | Test tag (`accessibilityIdentifier` on iOS). |

**Example**

```kotlin
NativeText("Account", style = NativeTextStyle.Title)
```

**Notes**

- iOS picks one of two render paths from `LocalNativeSurface`. On a known solid surface (a page background or `NativeCard`) it renders a `UILabel` filled opaquely with that surface color, so the interop region does not reveal the system backdrop. On a material/glass surface (`LocalNativeSurface` is `Color.Unspecified`) it renders Compose `Text` instead, because a `UIKitView` interop region would show a black or white rectangle over the material. This is an intentional exception to the `UILabel` rule.
- `overflow = TextOverflow.Ellipsis` maps to tail truncation on the iOS `UILabel`; other values wrap by word.
- The iOS `UILabel` sets `adjustsFontForContentSizeCategory = true`, so it honors and live-updates with the user's Dynamic Type size.

### NativeTextField

A single-line or multiline text input with optional label, placeholder, helper, and error decoration. The reference implementation for the rich-but-clean Native component API.

**Android:** Material 3 `OutlinedTextField`.
**iOS:** a `UITextField` (single-line) or `UITextView` (multiline) via `UIKitView`, with the label, helper, and error drawn in Compose around the native input.

**Use it when**
- You collect freeform text: names, emails, passwords, multiline notes.

**Avoid it when**
- You collect a one-time code or PIN — use `NativeOtpField` for the segmented look, or `NativeTextField(contentType = NativeTextContentType.OneTimeCode)` for native SMS autofill.
- You provide an inline search field — use `NativeSearchBar`.

**Parameters**

| Parameter | Type | Default | Description |
|---|---|---|---|
| `value` | `String` | — | Current text. |
| `onValueChange` | `(String) -> Unit` | — | Called with the new text on edit. Character limits are applied before this fires. |
| `modifier` | `Modifier` | `Modifier` | Layout modifier. |
| `enabled` | `Boolean` | `true` | Whether the field accepts input. |
| `readOnly` | `Boolean` | `false` | Shows the value but blocks editing. |
| `label` | `String?` | `null` | Field label. |
| `placeholder` | `String?` | `null` | Placeholder shown when empty. |
| `helperText` | `String?` | `null` | Helper text below the field. |
| `errorText` | `String?` | `null` | Error text shown when in the error state. |
| `isError` | `Boolean` | `errorText != null` | Whether the field is in the error state. |
| `leadingIcon` | `NativeIcon?` | `null` | Leading icon. |
| `trailingIcon` | `NativeIcon?` | `null` | Trailing icon. |
| `onTrailingIconClick` | `(() -> Unit)?` | `null` | Click handler for the trailing icon. |
| `input` | `NativeFieldInput` | `NativeFieldInput()` | Grouped input options: keyboard type, IME action, secure, line count, capitalization, autocorrect, character limit. |
| `focus` | `NativeFieldFocus` | `NativeFieldFocus()` | Focus and submit callbacks: `onFocusChanged`, `onSubmit`. |
| `contentType` | `NativeTextContentType?` | `null` | Cross-platform autofill hint, wired on both platforms. |
| `colors` | `NativeFieldColors?` | `null` | Color overrides. `null` uses theme colors. |
| `cornerRadius` | `Dp?` | `null` | Corner radius. `null` uses the theme small corner. |
| `textStyle` | `TextStyle?` | `null` | Text style merged over the resolved body style. |
| `touch` | `NativeInteropTouch` | `NativeInteropTouch.Cooperative` | iOS interop touch strategy inside scrolls. |
| `contentDescription` | `String?` | `null` | Accessibility description. |
| `testTag` | `String?` | `null` | Test tag (`accessibilityIdentifier` on iOS). |
| `ios` | `NativeTextFieldIosOptions` | `NativeTextFieldIosOptions()` | iOS-only options: `clearButton`, `keyboardAppearance`, `keyboardAccessory`. No-op on Android. |

`NativeFieldInput` fields and their defaults: `keyboardType = NativeKeyboardType.Text`, `imeAction = NativeImeAction.Default`, `secure = false`, `singleLine = true`, `minLines = 1`, `maxLines = if (singleLine) 1 else Int.MAX_VALUE`, `capitalization = NativeCapitalization.Sentences`, `autoCorrect = true`, `characterLimit = null`.

`NativeTextFieldIosOptions` fields and their defaults: `clearButton = NativeClearButtonMode.Never`, `keyboardAppearance = NativeKeyboardAppearance.Default`, `keyboardAccessory = NativeKeyboardAccessory()`.

**Example**

```kotlin
var email by remember { mutableStateOf("") }
NativeTextField(
    value = email,
    onValueChange = { email = it },
    label = "Email",
    input = NativeFieldInput(keyboardType = NativeKeyboardType.Email, autoCorrect = false),
    contentType = NativeTextContentType.EmailAddress,
)
```

**Notes**

- On iOS the label, helper, and error are Compose-drawn around the native input so they match Android's semantics and theming. The input itself is native.
- Multiline (`input.singleLine = false`) swaps the iOS native view to `UITextView`, which has no built-in placeholder or clear button. The placeholder is rendered as a themed overlay and the clear button is omitted in multiline mode.
- The clear button (`ios.clearButton`) is a real `UITextField` affordance on iOS. Android has no native equivalent; add one with `trailingIcon` + `onTrailingIconClick` (works on both platforms). A leading `ios.clearButton` is suppressed when a `trailingIcon` is set, since they share the right slot.
- `ios.keyboardAppearance` and `ios.keyboardAccessory` are iOS-only and are no-ops on Android. The Done accessory dismisses the keyboard and, for single-line fields, fires `focus.onSubmit`.
- Character limit behavior is identical on both platforms (enforced in shared code): `Enforce` hard-caps input (typing past the max is rejected, paste is trimmed); `WarnOnly` lets the value exceed the max and leaves it to the caller to surface a counter or error.
- Keyboard avoidance: a focused field scrolls to stay above the keyboard. The host scroll container must apply the kit's `Modifier.nativeImePadding(minBottom = …)`; the component supplies the bring-into-view. On Android it delegates to Compose's IME inset; on iOS it tracks the real keyboard frame in window coordinates (correct under the edge-to-edge chrome shell, and zero for a floating iPad keyboard).
- Single-line fields keep a fixed height, so very large accessibility text sizes may clip.

### NativeSearchBar

An inline search field for browse and search screens. Distinct from a nav-bar `.searchable`, which stays native-shell chrome; this is a leaf control placed in content.

**Android:** an indicator-less rounded, search-styled Material `TextField` with a leading magnifier and a trailing clear button.
**iOS:** a real `UISearchBar` via `UIKitView` (magnifier, rounded field, optional Cancel button, system search keyboard).

**Use it when**
- You place a search input inside a screen's content.

**Avoid it when**
- You want search in the navigation bar — that stays native-shell chrome, outside this kit.
- You collect general text — use `NativeTextField`.

**Parameters**

| Parameter | Type | Default | Description |
|---|---|---|---|
| `value` | `String` | — | Current query text. |
| `onValueChange` | `(String) -> Unit` | — | Called with the new query on edit. |
| `modifier` | `Modifier` | `Modifier` | Layout modifier. |
| `placeholder` | `String` | `"Search"` | Placeholder text. |
| `onSearch` | `(() -> Unit)?` | `null` | Fired on the keyboard Search/Return key. |
| `onCancel` | `(() -> Unit)?` | `null` | Fired on the iOS Cancel button and the Android clear button (which also clears the text). |
| `enabled` | `Boolean` | `true` | Whether the field accepts input. |
| `contentDescription` | `String?` | `null` | Accessibility description. |
| `testTag` | `String?` | `null` | Test tag (`accessibilityIdentifier` on iOS). |
| `ios` | `NativeSearchBarIosOptions` | `NativeSearchBarIosOptions()` | iOS-only options. No-op on Android. |

`NativeSearchBarIosOptions` fields and their defaults: `showCancelButton = false` (shows the native `UISearchBar` Cancel button; Android uses the trailing clear affordance instead).

**Example**

```kotlin
var query by remember { mutableStateOf("") }
NativeSearchBar(
    value = query,
    onValueChange = { query = it },
    onSearch = { run(query) },
)
```

**Notes**

- The resolved `container` color is Android-only. iOS uses the native search-field appearance, adapted to light/dark via `overrideUserInterfaceStyle` from the surface the bar sits on (`LocalNativeSurface`).
- `ios.showCancelButton` is iOS-only and a no-op on Android, which uses the trailing clear affordance instead.
- The iOS text is value-synced: the native field is written only when the external value differs, avoiding an edit loop.
- As a `UIKitView`-backed control inside a Compose scroll, the iOS bar uses overlay placement and may drift slightly during scrolling; it settles when scrolling stops.

### NativeOtpField

A one-time-code / PIN entry field: a row of `length` digit cells backed by a single hidden text field. Input is filtered to digits and capped at `length`; `onFilled` fires once the code is complete. Supports manual entry and paste.

**Android:** Compose-drawn digit cells.
**iOS:** Compose-drawn digit cells (same rendering as Android).

**Use it when**
- You collect a fixed-length numeric code or PIN and want the segmented digit-cell look.

**Avoid it when**
- You need native iOS SMS one-time-code autofill — use `NativeTextField(contentType = NativeTextContentType.OneTimeCode)`, which is a native `UITextField`.

**Parameters**

| Parameter | Type | Default | Description |
|---|---|---|---|
| `value` | `String` | — | Current code. |
| `onValueChange` | `(String) -> Unit` | — | Called with the new code (digits only, capped at `length`). |
| `modifier` | `Modifier` | `Modifier` | Layout modifier. |
| `length` | `Int` | `6` | Number of digit cells. |
| `enabled` | `Boolean` | `true` | Whether the field accepts input. |
| `isError` | `Boolean` | `false` | Renders cells in the error state. |
| `onFilled` | `((String) -> Unit)?` | `null` | Fired once the code reaches `length`. |
| `contentDescription` | `String?` | `null` | Accessibility description. Defaults to "Enter the {length}-digit code". |
| `testTag` | `String?` | `null` | Test tag. |

**Example**

```kotlin
var code by remember { mutableStateOf("") }
NativeOtpField(
    value = code,
    onValueChange = { code = it },
    length = 6,
    onFilled = { verify(it) },
)
```

**Notes**

- This is a branded visual component, Compose-rendered on both platforms. Because the cells are Compose-drawn rather than a native `UITextField`, iOS SMS one-time-code autofill is not available here by design. Use the `NativeTextField(contentType = NativeTextContentType.OneTimeCode)` path when you need native autofill.
- The cursor is not shown; the cells render the value and the active cell is highlighted.
