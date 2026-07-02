package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeCharacterLimitBehavior
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeFieldColors
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeFieldFocus
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeFieldInput
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeIcon
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeInteropTouch
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeTextContentType
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeTextFieldIosOptions
import io.github.apdelrahman1911.nativecomposekit.components.model.ResolvedFieldStyle
import io.github.apdelrahman1911.nativecomposekit.theme.NativeTheme

/**
 * Native text field — the reference implementation for the rich-but-clean Native component API
 * (see docs/architecture.md §4 and docs/components/). Renders the most native control per platform:
 * a Material 3 `OutlinedTextField` on Android, a real `UITextField` (single-line) or `UITextView`
 * (multiline) on iOS, with the label/helper/error drawn in shared Compose around the native input.
 *
 * High-frequency props are top-level; advanced knobs are grouped into [input]/[focus] and the
 * platform-specific [ios]/[android] options. Everything has a safe default, so the simple call is short:
 * `NativeTextField(value, onValueChange, label = "Name")`.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
public fun NativeTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    label: String? = null,
    placeholder: String? = null,
    helperText: String? = null,
    errorText: String? = null,
    isError: Boolean = errorText != null,
    leadingIcon: NativeIcon? = null,
    trailingIcon: NativeIcon? = null,
    onTrailingIconClick: (() -> Unit)? = null,
    input: NativeFieldInput = NativeFieldInput(),
    focus: NativeFieldFocus = NativeFieldFocus(),
    contentType: NativeTextContentType? = null,
    colorsOverride: NativeFieldColors? = null,
    cornerRadius: Dp? = null,
    textStyleOverride: TextStyle? = null,
    touch: NativeInteropTouch = NativeInteropTouch.Cooperative,
    contentDescription: String? = null,
    testTag: String? = null,
    ios: NativeTextFieldIosOptions = NativeTextFieldIosOptions(),
) {
    val resolved = resolveFieldStyle(cornerRadius, colorsOverride, textStyleOverride)

    // Character limit applied here so both platforms behave identically. Enforce hard-caps (paste
    // trimmed); WarnOnly passes input through so the caller can surface a counter/error itself.
    val limit = input.characterLimit
    val guardedOnValueChange: (String) -> Unit = { s ->
        val capped =
            if (limit != null && limit.behavior == NativeCharacterLimitBehavior.Enforce && s.length > limit.max) {
                s.take(limit.max)
            } else {
                s
            }
        onValueChange(capped)
    }

    // Keyboard avoidance: the native interop input isn't tracked by Compose's automatic bring-into-view
    // (that only follows Compose focus), so when the field gains focus we scroll it into view ourselves —
    // ONCE per focus. Keying this on the live IME inset re-ran it on every keyboard frame; the iOS keyboard
    // end-frame bounces (keys vs keys+suggestions) so the scroll never settled and rows below the field were
    // relocated mid-relayout. The host scroll's `nativeImePadding()` keeps the content clear of the keyboard.
    val bringIntoView = remember { BringIntoViewRequester() }
    var isFocused by remember { mutableStateOf(false) }
    LaunchedEffect(isFocused) {
        if (isFocused) bringIntoView.bringIntoView()
    }
    val focusForwarding = focus.copy(
        onFocusChanged = { f ->
            isFocused = f
            focus.onFocusChanged?.invoke(f)
        },
    )

    PlatformNativeTextField(
        value = value,
        onValueChange = guardedOnValueChange,
        modifier = modifier.bringIntoViewRequester(bringIntoView),
        enabled = enabled,
        readOnly = readOnly,
        label = label,
        placeholder = placeholder,
        helperText = helperText,
        errorText = errorText,
        isError = isError,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        onTrailingIconClick = onTrailingIconClick,
        input = input,
        focus = focusForwarding,
        contentType = contentType,
        style = resolved,
        touch = touch,
        contentDescription = contentDescription,
        testTag = testTag,
        ios = ios,
    )
}

@Composable
private fun resolveFieldStyle(
    cornerRadius: Dp?,
    colorsOverride: NativeFieldColors?,
    textStyleOverride: TextStyle?,
): ResolvedFieldStyle {
    val tokens = NativeTheme.tokens
    val scheme = MaterialTheme.colorScheme
    val colors = colorsOverride ?: NativeFieldColors(
        text = scheme.onSurface,
        placeholder = scheme.onSurfaceVariant,
        container = scheme.surface,
        border = scheme.outline,
        focusedBorder = scheme.primary,
        errorBorder = scheme.error,
        label = scheme.onSurfaceVariant,
        helper = scheme.onSurfaceVariant,
        error = scheme.error,
        cursor = scheme.primary,
    )
    val base = MaterialTheme.typography.bodyLarge.copy(color = colors.text)
    val textStyle = if (textStyleOverride != null) base.merge(textStyleOverride) else base
    return ResolvedFieldStyle(
        colors = colors,
        cornerRadius = cornerRadius ?: tokens.cornerSmall,
        minHeight = tokens.fieldMinHeight,
        textStyle = textStyle,
    )
}

/** Native text-field renderer. Android → `OutlinedTextField`; iOS → `UITextField`/`UITextView`. */
@Composable
internal expect fun PlatformNativeTextField(
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
    touch: NativeInteropTouch,
    contentDescription: String?,
    testTag: String?,
    ios: NativeTextFieldIosOptions,
)
