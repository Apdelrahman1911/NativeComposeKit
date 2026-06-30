# Accessibility & focus

Modifier extensions for focus, soft-keyboard dismissal, and screen-reader navigation. These are the focus/IME/accessibility helpers the kit uses internally, exposed for app screens. Both are Compose-drawn and behave the same on Android and iOS.

### brandDismissKeyboardOnTap

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
Column(Modifier.fillMaxSize().brandDismissKeyboardOnTap()) {
    BrandTextField(value = name, onValueChange = { name = it })
    BrandTextField(value = email, onValueChange = { email = it })
}
```

**Notes** — apply to the form's root or background so a tap anywhere outside a field clears focus. The detector handles taps on the modified element, so place it on a container rather than on an interactive control.

### brandHeading

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
BrandText("Settings", modifier = Modifier.brandHeading())
```
