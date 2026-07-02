package io.github.apdelrahman1911.nativecomposekit.components.model

import androidx.compose.runtime.Immutable

/**
 * Grouped, cross-platform input options for NativeTextField. Cohesive advanced knobs live here so the
 * main signature stays readable; every field has a safe default (see architecture.md §4). Compares by
 * value; not a `data class` so new knobs stay binary-compatible to add.
 */
@Immutable
public class NativeFieldInput(
    public val keyboardType: NativeKeyboardType = NativeKeyboardType.Text,
    public val imeAction: NativeImeAction = NativeImeAction.Default,
    public val secure: Boolean = false,
    public val singleLine: Boolean = true,
    public val minLines: Int = 1,
    public val maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    public val capitalization: NativeCapitalization = NativeCapitalization.Sentences,
    public val autoCorrect: Boolean = true,
    /** Optional length limit + behavior, applied in shared code so both platforms behave identically. */
    public val characterLimit: NativeCharacterLimit? = null,
) {
    init {
        require(minLines >= 1 && maxLines >= minLines) {
            "minLines ($minLines) must be >= 1 and <= maxLines ($maxLines)"
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NativeFieldInput) return false
        return keyboardType == other.keyboardType &&
            imeAction == other.imeAction &&
            secure == other.secure &&
            singleLine == other.singleLine &&
            minLines == other.minLines &&
            maxLines == other.maxLines &&
            capitalization == other.capitalization &&
            autoCorrect == other.autoCorrect &&
            characterLimit == other.characterLimit
    }

    override fun hashCode(): Int {
        var result = keyboardType.hashCode()
        result = 31 * result + imeAction.hashCode()
        result = 31 * result + secure.hashCode()
        result = 31 * result + singleLine.hashCode()
        result = 31 * result + minLines
        result = 31 * result + maxLines
        result = 31 * result + capitalization.hashCode()
        result = 31 * result + autoCorrect.hashCode()
        result = 31 * result + (characterLimit?.hashCode() ?: 0)
        return result
    }
}

/** A length limit and how it is applied. See [NativeCharacterLimitBehavior]. Compares by value. */
@Immutable
public class NativeCharacterLimit(
    public val max: Int,
    public val behavior: NativeCharacterLimitBehavior = NativeCharacterLimitBehavior.Enforce,
) {
    override fun equals(other: Any?): Boolean =
        this === other || (other is NativeCharacterLimit && max == other.max && behavior == other.behavior)

    override fun hashCode(): Int = max * 31 + behavior.hashCode()
}

/**
 * iOS keyboard input-accessory config. When [doneButton] is true (the default), a standard `UIToolbar` with a
 * "[doneText]" button appears above the keyboard; tapping it dismisses the keyboard (and fires the field's
 * `onSubmit` for single-line). It's on by default so every field — including keyboards whose return key isn't a
 * dismiss (number/decimal/email) and the multiline editor (whose return key inserts a newline) — has a reliable
 * way to close. A `UIToolbar` is the standard accessory the OS measures into the keyboard frame, so the kit's
 * keyboard insets account for it and it doesn't overlap content. Documented no-op on Android.
 */
@Immutable
public class NativeKeyboardAccessory(
    public val doneButton: Boolean = true,
    /** Button title. null (the default) uses the localized table — `LocalNativeStrings.current.done`. */
    public val doneText: String? = null,
    public val style: NativeKeyboardAccessoryStyle = NativeKeyboardAccessoryStyle.Toolbar,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NativeKeyboardAccessory) return false
        return doneButton == other.doneButton && doneText == other.doneText && style == other.style
    }

    override fun hashCode(): Int =
        (doneButton.hashCode() * 31 + (doneText?.hashCode() ?: 0)) * 31 + style.hashCode()
}

/** Focus + submit callbacks for NativeTextField. Compared by identity (it holds lambdas). */
@Immutable
public class NativeFieldFocus(
    public val onFocusChanged: ((Boolean) -> Unit)? = null,
    /** Fired when the user triggers the keyboard action / return key (single-line). */
    public val onSubmit: (() -> Unit)? = null,
) {
    /** Returns a copy with the given callbacks replaced. */
    public fun copy(
        onFocusChanged: ((Boolean) -> Unit)? = this.onFocusChanged,
        onSubmit: (() -> Unit)? = this.onSubmit,
    ): NativeFieldFocus = NativeFieldFocus(onFocusChanged, onSubmit)
}

/**
 * iOS-only NativeTextField options. Each is a documented no-op on Android.
 *
 * Note: autofill content type is **not** here — it is the cross-platform `NativeTextField(contentType = …)`
 * param ([NativeTextContentType]), wired on both platforms (iOS `UITextContentType` + Android Compose autofill).
 */
@Immutable
public class NativeTextFieldIosOptions(
    public val clearButton: NativeClearButtonMode = NativeClearButtonMode.Never,
    public val keyboardAppearance: NativeKeyboardAppearance = NativeKeyboardAppearance.Default,
    public val keyboardAccessory: NativeKeyboardAccessory = NativeKeyboardAccessory(),
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NativeTextFieldIosOptions) return false
        return clearButton == other.clearButton &&
            keyboardAppearance == other.keyboardAppearance &&
            keyboardAccessory == other.keyboardAccessory
    }

    override fun hashCode(): Int =
        (clearButton.hashCode() * 31 + keyboardAppearance.hashCode()) * 31 + keyboardAccessory.hashCode()
}
