package io.github.apdelrahman1911.nativecomposekit.components.model

import androidx.compose.runtime.Immutable

/**
 * Grouped, cross-platform input options for BrandTextField. Cohesive advanced knobs live here so the
 * main signature stays readable; every field has a safe default (see architecture.md §4).
 */
@Immutable
public data class BrandFieldInput(
    val keyboardType: BrandKeyboardType = BrandKeyboardType.Text,
    val imeAction: BrandImeAction = BrandImeAction.Default,
    val secure: Boolean = false,
    val singleLine: Boolean = true,
    val minLines: Int = 1,
    val maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    val capitalization: BrandCapitalization = BrandCapitalization.Sentences,
    val autoCorrect: Boolean = true,
    /** Optional length limit + behavior, applied in shared code so both platforms behave identically. */
    val characterLimit: BrandCharacterLimit? = null,
)

/** A length limit and how it is applied. See [BrandCharacterLimitBehavior]. */
@Immutable
public data class BrandCharacterLimit(
    val max: Int,
    val behavior: BrandCharacterLimitBehavior = BrandCharacterLimitBehavior.Enforce,
)

/**
 * iOS keyboard input-accessory toolbar config. When [doneButton] is true, a native toolbar with a
 * "[doneText]" button appears above the keyboard; tapping it dismisses the keyboard (and fires the
 * field's `onSubmit` for single-line). Documented no-op on Android.
 */
@Immutable
public data class BrandKeyboardAccessory(
    val doneButton: Boolean = false,
    val doneText: String = "Done",
    val style: BrandKeyboardAccessoryStyle = BrandKeyboardAccessoryStyle.FullWidthBar,
)

/** Focus + submit callbacks for BrandTextField. */
@Immutable
public data class BrandFieldFocus(
    val onFocusChanged: ((Boolean) -> Unit)? = null,
    /** Fired when the user triggers the keyboard action / return key (single-line). */
    val onSubmit: (() -> Unit)? = null,
)

/**
 * iOS-only BrandTextField options. Each is a documented no-op on Android.
 *
 * Note: autofill content type is **not** here — it is the cross-platform `BrandTextField(contentType = …)`
 * param ([BrandTextContentType]), wired on both platforms (iOS `UITextContentType` + Android Compose autofill).
 */
@Immutable
public data class BrandTextFieldIosOptions(
    val clearButton: BrandClearButtonMode = BrandClearButtonMode.Never,
    val keyboardAppearance: BrandKeyboardAppearance = BrandKeyboardAppearance.Default,
    val keyboardAccessory: BrandKeyboardAccessory = BrandKeyboardAccessory(),
)
