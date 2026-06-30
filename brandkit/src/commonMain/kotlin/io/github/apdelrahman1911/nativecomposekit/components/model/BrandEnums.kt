package io.github.apdelrahman1911.nativecomposekit.components.model

/** Button visual variants — mapped to native idioms per platform (Material buttons / UIButton configs). */
public enum class BrandButtonVariant { Primary, Secondary, Tertiary, Outline, Destructive }

/** Button sizes — drive height, content padding, and text style from BrandTokens. */
public enum class BrandButtonSize { Small, Medium, Large }

/**
 * Button shape.
 * - [Rounded] (default): uses the theme corner radius (BrandTokens.cornerMedium).
 * - [Pill]: a full capsule (corner radius = height / 2).
 * An explicit `cornerRadius` argument always overrides the shape.
 */
public enum class BrandButtonShape { Rounded, Pill }

/**
 * Role of a [BrandMenuItem]. [Destructive] renders red and maps to the native destructive affordance
 * (iOS `UIMenuElementAttributes.destructive` / red Material `DropdownMenuItem` text).
 */
public enum class BrandMenuItemRole { Normal, Destructive }

/** Text roles — mapped to the MaterialTheme type scale (and native fonts on iOS). */
public enum class BrandTextStyle { Display, Title, Body, Label }

/** Keyboard hint for BrandTextField — maps to KeyboardType (Android) / UIKeyboardType (iOS). */
public enum class BrandKeyboardType { Text, Email, Number, Phone, Decimal }

/** Keyboard action/return key — maps to ImeAction (Android) / UIReturnKeyType (iOS). */
public enum class BrandImeAction { Default, Done, Go, Next, Search, Send }

/** Auto-capitalization — maps to KeyboardCapitalization (Android) / UITextAutocapitalizationType (iOS). */
public enum class BrandCapitalization { None, Characters, Words, Sentences }

/**
 * How a character limit is applied. Behavior is identical on both platforms (enforced in shared code).
 * - [Enforce]: input is hard-capped at the limit (extra typing rejected, paste trimmed).
 * - [WarnOnly]: input is allowed past the limit; the app decides how to surface it (counter/error).
 */
public enum class BrandCharacterLimitBehavior { Enforce, WarnOnly }

/** iOS clear-button visibility (UITextField.clearButtonMode). Android synthesizes a trailing clear icon. */
public enum class BrandClearButtonMode { Never, WhileEditing, UnlessEditing, Always }

/** iOS keyboard appearance (UIKeyboardAppearance). No-op on Android. */
public enum class BrandKeyboardAppearance { Default, Light, Dark }

/**
 * Visual style of the iOS keyboard input-accessory.
 * - [FullWidthBar] (default): a clean full-width rectangular bar above the keyboard with a Done button.
 * - [Toolbar]: a standard translucent `UIToolbar` with a Done bar-button.
 */
public enum class BrandKeyboardAccessoryStyle { FullWidthBar, Toolbar }

/**
 * Cross-platform autofill content hint. Passed via `BrandTextField(contentType = …)` and wired on BOTH
 * platforms: iOS `UITextField.textContentType` (`UITextContentType`) and Android Compose autofill
 * (`Modifier.semantics { contentType = … }` → `androidx.compose.ui.autofill.ContentType`).
 */
public enum class BrandTextContentType {
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
public enum class BrandInteropTouch { Cooperative, NonCooperative, NonInteractive }
