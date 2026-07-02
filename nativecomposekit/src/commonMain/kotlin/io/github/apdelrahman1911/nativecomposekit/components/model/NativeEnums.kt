package io.github.apdelrahman1911.nativecomposekit.components.model

/** Button visual variants — mapped to native idioms per platform (Material buttons / UIButton configs). */
public enum class NativeButtonVariant { Primary, Secondary, Tertiary, Outline, Destructive }

/** Button sizes — drive height, content padding, and text style from NativeTokens. */
public enum class NativeButtonSize { Small, Medium, Large }

/**
 * Button shape.
 * - [Rounded] (default): uses the theme corner radius (NativeTokens.cornerMedium).
 * - [Pill]: a full capsule (corner radius = height / 2).
 * An explicit `cornerRadius` argument always overrides the shape.
 */
public enum class NativeButtonShape { Rounded, Pill }

/**
 * Role of a [NativeMenuItem]. [Destructive] renders red and maps to the native destructive affordance
 * (iOS `UIMenuElementAttributes.destructive` / red Material `DropdownMenuItem` text).
 */
public enum class NativeMenuItemRole { Normal, Destructive }

/** Text roles — mapped to the MaterialTheme type scale (and native fonts on iOS). */
public enum class NativeTextStyle { Display, Title, Body, Label }

/** Keyboard hint for NativeTextField — maps to KeyboardType (Android) / UIKeyboardType (iOS). */
public enum class NativeKeyboardType { Text, Email, Number, Phone, Decimal }

/** Keyboard action/return key — maps to ImeAction (Android) / UIReturnKeyType (iOS). */
public enum class NativeImeAction { Default, Done, Go, Next, Search, Send }

/** Auto-capitalization — maps to KeyboardCapitalization (Android) / UITextAutocapitalizationType (iOS). */
public enum class NativeCapitalization { None, Characters, Words, Sentences }

/**
 * How a character limit is applied. Behavior is identical on both platforms (enforced in shared code).
 * - [Enforce]: input is hard-capped at the limit (extra typing rejected, paste trimmed).
 * - [WarnOnly]: input is allowed past the limit; the app decides how to surface it (counter/error).
 */
public enum class NativeCharacterLimitBehavior { Enforce, WarnOnly }

/** iOS clear-button visibility (UITextField.clearButtonMode). Android synthesizes a trailing clear icon. */
public enum class NativeClearButtonMode { Never, WhileEditing, UnlessEditing, Always }

/** iOS keyboard appearance (UIKeyboardAppearance). No-op on Android. */
public enum class NativeKeyboardAppearance { Default, Light, Dark }

/**
 * Visual style of the iOS keyboard input-accessory.
 * - [FullWidthBar]: a clean full-width rectangular bar above the keyboard with a Done button.
 * - [Toolbar] (default): a standard translucent `UIToolbar` with a Done bar-button.
 */
public enum class NativeKeyboardAccessoryStyle { FullWidthBar, Toolbar }

/**
 * Cross-platform autofill content hint. Passed via `NativeTextField(contentType = …)` and wired on BOTH
 * platforms: iOS `UITextField.textContentType` (`UITextContentType`) and Android Compose autofill
 * (`Modifier.semantics { contentType = … }` → `androidx.compose.ui.autofill.ContentType`).
 */
public enum class NativeTextContentType {
    Name, EmailAddress, Username, Password, NewPassword, OneTimeCode, TelephoneNumber, PostalCode, FullStreetAddress
}

/**
 * Native-interop touch strategy for views embedded in a Compose scroll. Wraps the (experimental)
 * Compose `UIKitInteropProperties.interactionMode` so the public API stays stable. See architecture.md §6.
 *
 * - [Cooperative]: Compose may intercept scroll gestures first (UIScrollView-like delay), then forward
 *   taps/short drags to the native control. Default for interactive controls inside scrolls.
 * - [NonCooperative]: the native view owns all gestures (maps, web views, complex pan/zoom).
 * - [NonInteractive]: touches pass through to Compose (display-only views, e.g. labels).
 */
public enum class NativeInteropTouch { Cooperative, NonCooperative, NonInteractive }
