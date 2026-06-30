package com.ukkera.brandkit.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import com.ukkera.brandkit.components.model.ResolvedSearchStyle

/**
 * Android search field: a Material filled [TextField] with its indicators removed and a rounded shape, a
 * leading magnifier and a trailing clear button — the conventional Android search look. `showCancelButton`
 * is iOS-only and ignored here (Android uses the trailing clear affordance).
 */
@Composable
internal actual fun PlatformBrandSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier,
    placeholder: String,
    onSearch: (() -> Unit)?,
    onCancel: (() -> Unit)?,
    enabled: Boolean,
    showCancelButton: Boolean,
    style: ResolvedSearchStyle,
    contentDescription: String?,
    testTag: String?,
) {
    var m = modifier
    testTag?.let { m = m.testTag(it) }
    contentDescription?.let { cd -> m = m.semantics { this.contentDescription = cd } }

    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = m,
        enabled = enabled,
        singleLine = true,
        textStyle = style.textStyle,
        placeholder = { Text(placeholder) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            if (value.isNotEmpty()) {
                IconButton(onClick = { onValueChange(""); onCancel?.invoke() }) {
                    Icon(Icons.Default.Close, contentDescription = "Clear")
                }
            }
        },
        shape = RoundedCornerShape(style.cornerRadius),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch?.invoke() }),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = style.container,
            unfocusedContainerColor = style.container,
            disabledContainerColor = style.container,
            focusedTextColor = style.text,
            unfocusedTextColor = style.text,
            cursorColor = style.tint,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            focusedLeadingIconColor = style.tint,
            unfocusedLeadingIconColor = style.tint,
            focusedTrailingIconColor = style.tint,
            unfocusedTrailingIconColor = style.tint,
            focusedPlaceholderColor = style.placeholder,
            unfocusedPlaceholderColor = style.placeholder,
        ),
    )
}
