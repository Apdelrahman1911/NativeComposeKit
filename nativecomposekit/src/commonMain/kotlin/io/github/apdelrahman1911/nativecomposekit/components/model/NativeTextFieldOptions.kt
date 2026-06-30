package io.github.apdelrahman1911.nativecomposekit.components.model

import androidx.compose.runtime.Immutable

/**
 * Grouped, cross-platform input options for NativeTextField. Cohesive advanced knobs live here so the
 * main signature stays readable; every field has a safe default (see architecture.md §4).
 */
@Immutable
public data class NativeFieldInput(
    val keyboardType: NativeKeyboardType = NativeKeyboardType.Text,
    val imeAction: NativeImeAction = NativeImeAction.Default,
    val secure: Boolean = false,
    val singleLine: Boolean = true,
    val minLines: Int = 1,
    val maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    val capitalization: NativeCapitalization = NativeCapitalization.Sentences,
    val autoCorrect: Boolean = true,
    /** Optional length limit + behavior, applied in shared code so both platforms behave identically. */
    val characterLimit: NativeCharacterLimit? = null,
)

/** A length limit and how it is applied. See [NativeCharacterLimitBehavior]. */
@Immutable
public data class NativeCharacterLimit(
    val max: Int,
    val behavior: NativeCharacterLimitBehavior = NativeCharacterLimitBehavior.Enforce,
)

/**
 * iOS keyboard input-accessory toolbar config. When [doneButton] is true, a native toolbar with a
 * "[doneText]" button appears above the keyboard; tapping it dismisses the keyboard (and fires the
 * field's `onSubmit` for single-line). Documented no-op on Android.
 */
@Immutable
public data class NativeKeyboardAccessory(
    val doneButton: Boolean = false,
    val doneText: String = "Done",
    val style: NativeKeyboardAccessoryStyle = NativeKeyboardAccessoryStyle.FullWidthBar,
)

/** Focus + submit callbacks for NativeTextField. */
@Immutable
public data class NativeFieldFocus(
    val onFocusChanged: ((Boolean) -> Unit)? = null,
    /** Fired when the user triggers the keyboard action / return key (single-line). */
    val onSubmit: (() -> Unit)? = null,
)

/**
 * iOS-only NativeTextField options. Each is a documented no-op on Android.
 *
 * Note: autofill content type is **not** here — it is the cross-platform `NativeTextField(contentType = …)`
 * param ([NativeTextContentType]), wired on both platforms (iOS `UITextContentType` + Android Compose autofill).
 */
@Immutable
public data class NativeTextFieldIosOptions(
    val clearButton: NativeClearButtonMode = NativeClearButtonMode.Never,
    val keyboardAppearance: NativeKeyboardAppearance = NativeKeyboardAppearance.Default,
    val keyboardAccessory: NativeKeyboardAccessory = NativeKeyboardAccessory(),
)
