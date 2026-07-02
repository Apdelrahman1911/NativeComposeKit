package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeCapitalization
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeFieldFocus
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeFieldInput
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeIcon
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeImeAction
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeInteropTouch
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeKeyboardType
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeTextContentType
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeTextFieldIosOptions
import io.github.apdelrahman1911.nativecomposekit.components.model.ResolvedFieldStyle

@Composable
internal actual fun PlatformNativeTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    readOnly: Boolean,
    label: String?,
    placeholder: String?,
    helperText: String?,
    errorText: String?,
    isError: Boolean,
    leadingIcon: NativeIcon?,
    trailingIcon: NativeIcon?,
    onTrailingIconClick: (() -> Unit)?,
    input: NativeFieldInput,
    focus: NativeFieldFocus,
    contentType: NativeTextContentType?,
    style: ResolvedFieldStyle,
    touch: NativeInteropTouch, // n/a on Android (native Compose, no UIKit interop)
    contentDescription: String?,
    testTag: String?,
    ios: NativeTextFieldIosOptions, // iOS-only options; ignored on Android
) {
    var m = modifier
    if (testTag != null) m = m.testTag(testTag)
    val onFocus = focus.onFocusChanged
    if (onFocus != null) m = m.onFocusChanged { onFocus(it.isFocused) }
    // Accessible name + autofill content type. Only set an explicit name when given; otherwise
    // OutlinedTextField's `label` is the accessible name. The content type drives Android autofill.
    val autofill = contentType?.toAutofillContentType()
    if (contentDescription != null || autofill != null) {
        m = m.semantics {
            if (contentDescription != null) this.contentDescription = contentDescription
            if (autofill != null) this.contentType = autofill
        }
    }

    // Honor the resolved minimum field height (token-driven; a caller's tokens override was silently ignored).
    m = m.defaultMinSize(minHeight = style.minHeight)

    val c = style.colors
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = m,
        enabled = enabled,
        readOnly = readOnly,
        textStyle = style.textStyle,
        label = label?.let { l -> { Text(l) } },
        placeholder = placeholder?.let { p -> { Text(p) } },
        leadingIcon = leadingIcon?.androidImageVector?.let { iv ->
            { Icon(iv, contentDescription = leadingIcon.contentDescription) }
        },
        trailingIcon = trailingIcon?.androidImageVector?.let { iv ->
            if (onTrailingIconClick != null) {
                { IconButton(onClick = onTrailingIconClick) { Icon(iv, contentDescription = trailingIcon.contentDescription) } }
            } else {
                { Icon(iv, contentDescription = trailingIcon.contentDescription) }
            }
        },
        isError = isError,
        supportingText = (errorText ?: helperText)?.let { s -> { Text(s) } },
        visualTransformation = if (input.secure) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(
            // A secure field must use a password keyboard type: it disables IME learning/suggestions, which a
            // plain visual transformation does NOT (the keyboard would happily learn the password). Matches
            // iOS, where isSecureTextEntry disables QuickType. Autocorrect is likewise forced off.
            keyboardType = if (input.secure) {
                when (input.keyboardType) {
                    NativeKeyboardType.Number, NativeKeyboardType.Decimal -> KeyboardType.NumberPassword
                    else -> KeyboardType.Password
                }
            } else {
                input.keyboardType.toKeyboardType()
            },
            imeAction = input.imeAction.toImeAction(),
            capitalization = input.capitalization.toCapitalization(),
            autoCorrectEnabled = if (input.secure) false else input.autoCorrect,
        ),
        keyboardActions = KeyboardActions(
            onDone = { focus.onSubmit?.invoke() },
            onGo = { focus.onSubmit?.invoke() },
            onNext = { focus.onSubmit?.invoke() },
            onSearch = { focus.onSubmit?.invoke() },
            onSend = { focus.onSubmit?.invoke() },
        ),
        singleLine = input.singleLine,
        minLines = input.minLines,
        maxLines = input.maxLines,
        shape = RoundedCornerShape(style.cornerRadius),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = c.text,
            unfocusedTextColor = c.text,
            focusedContainerColor = c.container,
            unfocusedContainerColor = c.container,
            focusedBorderColor = c.focusedBorder,
            unfocusedBorderColor = c.border,
            errorBorderColor = c.errorBorder,
            cursorColor = c.cursor,
            focusedLabelColor = c.focusedBorder,
            unfocusedLabelColor = c.label,
            errorLabelColor = c.error,
            focusedPlaceholderColor = c.placeholder,
            unfocusedPlaceholderColor = c.placeholder,
            focusedSupportingTextColor = c.helper,
            unfocusedSupportingTextColor = c.helper,
            errorSupportingTextColor = c.error,
        ),
    )
}

private fun NativeKeyboardType.toKeyboardType(): KeyboardType = when (this) {
    NativeKeyboardType.Text -> KeyboardType.Text
    NativeKeyboardType.Email -> KeyboardType.Email
    NativeKeyboardType.Number -> KeyboardType.Number
    NativeKeyboardType.Phone -> KeyboardType.Phone
    NativeKeyboardType.Decimal -> KeyboardType.Decimal
}

private fun NativeImeAction.toImeAction(): ImeAction = when (this) {
    NativeImeAction.Default -> ImeAction.Default
    NativeImeAction.Done -> ImeAction.Done
    NativeImeAction.Go -> ImeAction.Go
    NativeImeAction.Next -> ImeAction.Next
    NativeImeAction.Search -> ImeAction.Search
    NativeImeAction.Send -> ImeAction.Send
}

private fun NativeCapitalization.toCapitalization(): KeyboardCapitalization = when (this) {
    NativeCapitalization.None -> KeyboardCapitalization.None
    NativeCapitalization.Characters -> KeyboardCapitalization.Characters
    NativeCapitalization.Words -> KeyboardCapitalization.Words
    NativeCapitalization.Sentences -> KeyboardCapitalization.Sentences
}

// Cross-platform content type → Android Compose autofill content type (matches the iOS UITextContentType
// mapping). FullStreetAddress maps to the full PostalAddress hint to mirror UITextContentTypeFullStreetAddress.
private fun NativeTextContentType.toAutofillContentType(): ContentType = when (this) {
    NativeTextContentType.Name -> ContentType.PersonFullName
    NativeTextContentType.EmailAddress -> ContentType.EmailAddress
    NativeTextContentType.Username -> ContentType.Username
    NativeTextContentType.Password -> ContentType.Password
    NativeTextContentType.NewPassword -> ContentType.NewPassword
    NativeTextContentType.OneTimeCode -> ContentType.SmsOtpCode
    NativeTextContentType.TelephoneNumber -> ContentType.PhoneNumber
    NativeTextContentType.PostalCode -> ContentType.PostalCode
    NativeTextContentType.FullStreetAddress -> ContentType.PostalAddress
}
