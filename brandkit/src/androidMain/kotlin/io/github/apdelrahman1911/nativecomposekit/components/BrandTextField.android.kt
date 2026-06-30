package io.github.apdelrahman1911.nativecomposekit.components

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
import io.github.apdelrahman1911.nativecomposekit.components.model.BrandCapitalization
import io.github.apdelrahman1911.nativecomposekit.components.model.BrandFieldFocus
import io.github.apdelrahman1911.nativecomposekit.components.model.BrandFieldInput
import io.github.apdelrahman1911.nativecomposekit.components.model.BrandIcon
import io.github.apdelrahman1911.nativecomposekit.components.model.BrandImeAction
import io.github.apdelrahman1911.nativecomposekit.components.model.BrandInteropTouch
import io.github.apdelrahman1911.nativecomposekit.components.model.BrandKeyboardType
import io.github.apdelrahman1911.nativecomposekit.components.model.BrandTextContentType
import io.github.apdelrahman1911.nativecomposekit.components.model.BrandTextFieldIosOptions
import io.github.apdelrahman1911.nativecomposekit.components.model.ResolvedFieldStyle

@Composable
internal actual fun PlatformBrandTextField(
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
    leadingIcon: BrandIcon?,
    trailingIcon: BrandIcon?,
    onTrailingIconClick: (() -> Unit)?,
    input: BrandFieldInput,
    focus: BrandFieldFocus,
    contentType: BrandTextContentType?,
    style: ResolvedFieldStyle,
    touch: BrandInteropTouch, // n/a on Android (native Compose, no UIKit interop)
    contentDescription: String?,
    testTag: String?,
    ios: BrandTextFieldIosOptions, // iOS-only options; ignored on Android
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
            keyboardType = input.keyboardType.toKeyboardType(),
            imeAction = input.imeAction.toImeAction(),
            capitalization = input.capitalization.toCapitalization(),
            autoCorrectEnabled = input.autoCorrect,
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
            errorSupportingTextColor = c.error,
        ),
    )
}

private fun BrandKeyboardType.toKeyboardType(): KeyboardType = when (this) {
    BrandKeyboardType.Text -> KeyboardType.Text
    BrandKeyboardType.Email -> KeyboardType.Email
    BrandKeyboardType.Number -> KeyboardType.Number
    BrandKeyboardType.Phone -> KeyboardType.Phone
    BrandKeyboardType.Decimal -> KeyboardType.Decimal
}

private fun BrandImeAction.toImeAction(): ImeAction = when (this) {
    BrandImeAction.Default -> ImeAction.Default
    BrandImeAction.Done -> ImeAction.Done
    BrandImeAction.Go -> ImeAction.Go
    BrandImeAction.Next -> ImeAction.Next
    BrandImeAction.Search -> ImeAction.Search
    BrandImeAction.Send -> ImeAction.Send
}

private fun BrandCapitalization.toCapitalization(): KeyboardCapitalization = when (this) {
    BrandCapitalization.None -> KeyboardCapitalization.None
    BrandCapitalization.Characters -> KeyboardCapitalization.Characters
    BrandCapitalization.Words -> KeyboardCapitalization.Words
    BrandCapitalization.Sentences -> KeyboardCapitalization.Sentences
}

// Cross-platform content type → Android Compose autofill content type (matches the iOS UITextContentType
// mapping). FullStreetAddress maps to the full PostalAddress hint to mirror UITextContentTypeFullStreetAddress.
private fun BrandTextContentType.toAutofillContentType(): ContentType = when (this) {
    BrandTextContentType.Name -> ContentType.PersonFullName
    BrandTextContentType.EmailAddress -> ContentType.EmailAddress
    BrandTextContentType.Username -> ContentType.Username
    BrandTextContentType.Password -> ContentType.Password
    BrandTextContentType.NewPassword -> ContentType.NewPassword
    BrandTextContentType.OneTimeCode -> ContentType.SmsOtpCode
    BrandTextContentType.TelephoneNumber -> ContentType.PhoneNumber
    BrandTextContentType.PostalCode -> ContentType.PostalCode
    BrandTextContentType.FullStreetAddress -> ContentType.PostalAddress
}
