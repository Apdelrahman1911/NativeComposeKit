# Accessibility & focus

Modifier extensions for focus, soft-keyboard dismissal, and screen-reader navigation. These are the focus/IME/accessibility helpers the kit uses internally, exposed for app screens. They are Compose-drawn. Keyboard-dismissal (`nativeDismissKeyboardOnTap`) and headings (`nativeHeading`) behave the same on both platforms; the focus-movement helpers (`nativeAutoFocus`, `NativeFocusHandle`, `nativeFocusOrder`, `nativeFocusGroup`) drive **Compose** focus — see the per-helper platform notes for how that interacts with iOS's tap-focused native fields.

### nativeDismissKeyboardOnTap

A `Modifier` extension that clears focus when the element is tapped, which hides the soft keyboard.

**Android:** Compose `pointerInput` + `detectTapGestures`; the tap calls `LocalFocusManager.clearFocus()`, which hides the IME.
**iOS:** same Compose code path; clearing focus resigns the first responder and dismisses the keyboard.

**Use it when**
- A form screen should hide the keyboard when the user taps outside any field (the common iOS/Android idiom).
- Applied to a scrollable root or background that wraps your text fields.

**Avoid it when**
- The tap target also needs its own click handling; the gesture detector here consumes taps on the element.

**Parameters**

None. The modifier takes no arguments.

It is `@Composable` because it reads the focus manager from composition (`LocalFocusManager.current`).

**Example**

```kotlin
Column(Modifier.fillMaxSize().nativeDismissKeyboardOnTap()) {
    NativeTextField(value = name, onValueChange = { name = it })
    NativeTextField(value = email, onValueChange = { email = it })
}
```

**Notes** — apply to the form's root or background so a tap anywhere outside a field clears focus. The detector handles taps on the modified element, so place it on a container rather than on an interactive control.

### nativeHeading

A `Modifier` extension that marks a node as a heading for screen-reader heading/rotor navigation.

**Android:** Compose `semantics { heading() }`; TalkBack treats the node as a heading.
**iOS:** same `semantics { heading() }`; VoiceOver exposes the node to heading/rotor navigation.

**Use it when**
- Marking a section title so assistive tech can jump between sections.

**Avoid it when**
- The text is body content, not a section heading; over-marking headings makes rotor navigation noisier.

**Parameters**

None. The modifier takes no arguments. It is not `@Composable`.

**Example**

```kotlin
NativeText("Settings", modifier = Modifier.nativeHeading())
```

### nativeAutoFocus

A `@Composable` `Modifier` extension that requests focus for the element when it first enters composition (while `enabled`) — e.g. focus the first field when a form or dialog opens.

**Android:** remembers a `FocusRequester` and requests focus in a one-shot `LaunchedEffect`, so the field takes focus and the soft keyboard opens. **iOS:** the same Compose focus request runs, but a `NativeTextField` hosts a native `UITextField`/`UITextView` that is tap-focused and lives outside Compose's focus system — so this does **not** raise the iOS keyboard for a native field (iOS users focus native fields by tapping). It still moves focus among Compose-drawn focusables on both platforms. The same platform note applies to `NativeFocusHandle.requestFocus`, `nativeFocusOrder`, and `nativeFocusGroup`.

**Use it when**
- You want to focus the first field when a form or dialog appears.

**Avoid it when**
- The element is not focusable — apply to a `NativeTextField` or an element made focusable.

**Parameters**

| Parameter | Type | Default | Description |
|---|---|---|---|
| `enabled` | `Boolean` | `true` | When true, requests focus on first composition. |

**Example**

```kotlin
NativeTextField(value, onValueChange, modifier = Modifier.nativeAutoFocus())
```

### rememberNativeFocusHandle / nativeFocusTarget

A `NativeFocusHandle` moves focus to (or releases it from) a target element imperatively — e.g. advance to the next field on submit. Obtain it with `rememberNativeFocusHandle()`, attach it with `Modifier.nativeFocusTarget(handle)`, then call `requestFocus()` / `freeFocus()`.

| Member | Type | Description |
|---|---|---|
| `NativeFocusHandle.requestFocus` | `() -> Unit` | Move focus to the attached element (Android: opens the keyboard for a `NativeTextField`; iOS: native fields are tap-focused — see the nativeAutoFocus platform note). |
| `NativeFocusHandle.freeFocus` | `() -> Unit` | Release focus without moving it elsewhere. |
| `rememberNativeFocusHandle` | `@Composable () -> NativeFocusHandle` | Remember a handle. |
| `Modifier.nativeFocusTarget` | `(handle: NativeFocusHandle) -> Modifier` | Attach a handle to the element. |

**Example**

```kotlin
val next = rememberNativeFocusHandle()
NativeTextField(email, { email = it }, focus = NativeFieldFocus(onSubmit = { next.requestFocus() }))
NativeTextField(pw, { pw = it }, modifier = Modifier.nativeFocusTarget(next))
```

### nativeFocusOrder

A `Modifier` extension that sets explicit focus traversal — where focus goes on **next** / **previous** (Tab, hardware keys, the IME "next" action). Point each field at the next (and optionally previous) handle to build a form's tab chain.

**Parameters**

| Parameter | Type | Default | Description |
|---|---|---|---|
| `next` | `NativeFocusHandle?` | `null` | The element focus moves to on "next". |
| `previous` | `NativeFocusHandle?` | `null` | The element focus moves to on "previous". |

**Example**

```kotlin
Modifier.nativeFocusTarget(emailHandle).nativeFocusOrder(next = passwordHandle)
```

### nativeFocusGroup

A `Modifier` extension that groups descendant focusables so they behave as a single tab-stop / 2D-navigable cluster (Compose `focusGroup`). Not `@Composable`; takes no arguments.

```kotlin
Row(Modifier.nativeFocusGroup()) { /* controls navigated as one unit */ }
```
