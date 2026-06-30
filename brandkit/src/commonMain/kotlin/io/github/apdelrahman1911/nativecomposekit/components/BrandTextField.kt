package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import io.github.apdelrahman1911.nativecomposekit.components.model.BrandCharacterLimitBehavior
import io.github.apdelrahman1911.nativecomposekit.components.model.BrandFieldColors
import io.github.apdelrahman1911.nativecomposekit.components.model.BrandFieldFocus
import io.github.apdelrahman1911.nativecomposekit.components.model.BrandFieldInput
import io.github.apdelrahman1911.nativecomposekit.components.model.BrandIcon
import io.github.apdelrahman1911.nativecomposekit.components.model.BrandInteropTouch
import io.github.apdelrahman1911.nativecomposekit.components.model.BrandTextContentType
import io.github.apdelrahman1911.nativecomposekit.components.model.BrandTextFieldIosOptions
import io.github.apdelrahman1911.nativecomposekit.components.model.ResolvedFieldStyle
import io.github.apdelrahman1911.nativecomposekit.theme.BrandTheme

/**
 * Brand text field — the reference implementation for the rich-but-clean Brand component API
 * (see docs/architecture.md §4 and docs/components/). Renders the most native control per platform:
 * a Material 3 `OutlinedTextField` on Android, a real `UITextField` (single-line) or `UITextView`
 * (multiline) on iOS, with the label/helper/error drawn in shared Compose around the native input.
 *
 * High-frequency props are top-level; advanced knobs are grouped into [input]/[focus] and the
 * platform-specific [ios]/[android] options. Everything has a safe default, so the simple call is short:
 * `BrandTextField(value, onValueChange, label = "Name")`.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
public fun BrandTextField(
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
    leadingIcon: BrandIcon? = null,
    trailingIcon: BrandIcon? = null,
    onTrailingIconClick: (() -> Unit)? = null,
    input: BrandFieldInput = BrandFieldInput(),
    focus: BrandFieldFocus = BrandFieldFocus(),
    contentType: BrandTextContentType? = null,
    colors: BrandFieldColors? = null,
    cornerRadius: Dp? = null,
    textStyle: TextStyle? = null,
    touch: BrandInteropTouch = BrandInteropTouch.Cooperative,
    contentDescription: String? = null,
    testTag: String? = null,
    ios: BrandTextFieldIosOptions = BrandTextFieldIosOptions(),
) {
    val resolved = resolveFieldStyle(cornerRadius, colors, textStyle)

    // Character limit applied here so both platforms behave identically. Enforce hard-caps (paste
    // trimmed); WarnOnly passes input through so the caller can surface a counter/error itself.
    val limit = input.characterLimit
    val guardedOnValueChange: (String) -> Unit = { s ->
        val capped =
            if (limit != null && limit.behavior == BrandCharacterLimitBehavior.Enforce && s.length > limit.max) {
                s.take(limit.max)
            } else {
                s
            }
        onValueChange(capped)
    }

    // Keyboard avoidance: the native interop input isn't tracked by Compose's automatic
    // bring-into-view (that only follows Compose focus), so when the field reports focus we scroll it
    // into view ourselves, re-running as the IME inset settles. Pairs with the host scroll applying
    // `imePadding()` so the viewport ends above the keyboard (+ accessory + safe area).
    val bringIntoView = remember { BringIntoViewRequester() }
    var isFocused by remember { mutableStateOf(false) }
    val imeBottom = WindowInsets.ime.getBottom(LocalDensity.current)
    LaunchedEffect(isFocused, imeBottom) {
        if (isFocused) bringIntoView.bringIntoView()
    }
    val focusForwarding = focus.copy(
        onFocusChanged = { f ->
            isFocused = f
            focus.onFocusChanged?.invoke(f)
        },
    )

    PlatformBrandTextField(
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
    colorsOverride: BrandFieldColors?,
    textStyleOverride: TextStyle?,
): ResolvedFieldStyle {
    val tokens = BrandTheme.tokens
    val scheme = MaterialTheme.colorScheme
    val colors = colorsOverride ?: BrandFieldColors(
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
internal expect fun PlatformBrandTextField(
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
    touch: BrandInteropTouch,
    contentDescription: String?,
    testTag: String?,
    ios: BrandTextFieldIosOptions,
)
