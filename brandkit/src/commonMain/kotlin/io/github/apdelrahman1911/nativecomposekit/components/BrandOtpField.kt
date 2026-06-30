package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.github.apdelrahman1911.nativecomposekit.theme.BrandTheme

/**
 * A one-time-code / PIN entry field — a row of [length] digit cells backed by a single hidden text field
 * (the idiomatic Compose pattern: a `BasicTextField` whose `decorationBox` draws the cells). Input is filtered
 * to digits and capped at [length]; [onFilled] fires once it's complete. Supports manual entry and paste.
 *
 * **Kit thesis — kept as a documented exception (Compose-on-both).** This is a *branded visual* OTP component:
 * the segmented digit-cell look. iOS has no native OTP-cell control, and the two roles are intentionally split:
 * - **`BrandOtpField`** — the segmented digit-cell OTP *UI*, Compose-rendered on both platforms.
 * - **`BrandTextField(contentType = BrandTextContentType.OneTimeCode)`** — the **preferred native iOS path** when
 *   you need real SMS one-time-code **autofill** (a native `UITextField` with `textContentType = .oneTimeCode`).
 *
 * So: because the cells are Compose-drawn (not a native `UITextField`), iOS SMS autofill is **not** available on
 * `BrandOtpField` by design — use the `BrandTextField(OneTimeCode)` path for autofill. A hidden-native-field
 * hybrid was deliberately rejected (fragile UIKit/Compose interop; see CMP-10398/10399/10400).
 */
@Composable
public fun BrandOtpField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    length: Int = 6,
    enabled: Boolean = true,
    isError: Boolean = false,
    onFilled: ((String) -> Unit)? = null,
    contentDescription: String? = null,
    testTag: String? = null,
) {
    val scheme = MaterialTheme.colorScheme

    var m = modifier
    testTag?.let { m = m.testTag(it) }
    val cd = contentDescription ?: "Enter the $length-digit code"
    m = m.semantics { this.contentDescription = cd }

    BasicTextField(
        value = value,
        onValueChange = { raw ->
            val filtered = raw.filter { it.isDigit() }.take(length)
            if (filtered != value) {
                onValueChange(filtered)
                if (filtered.length == length) onFilled?.invoke(filtered)
            }
        },
        enabled = enabled,
        modifier = m,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(),
        // The cursor isn't shown (OTP convention); the cells render the value. innerTextField is intentionally
        // not placed — the field still captures focus/input across its decoration bounds.
        decorationBox = {
            Row(horizontalArrangement = Arrangement.spacedBy(BrandTheme.tokens.spacingSm)) {
                repeat(length) { i ->
                    val char = value.getOrNull(i)?.toString() ?: ""
                    val isCursorCell = enabled && i == value.length.coerceAtMost(length - 1) && value.length < length
                    val borderColor = when {
                        isError -> scheme.error
                        isCursorCell -> scheme.primary
                        char.isNotEmpty() -> scheme.outline
                        else -> scheme.outlineVariant
                    }
                    Box(
                        modifier = Modifier
                            .width(44.dp)
                            .height(52.dp)
                            .clipBackgroundBorder(
                                radius = BrandTheme.tokens.cornerSmall,
                                bg = scheme.surface,
                                border = borderColor,
                                borderWidth = if (isCursorCell || isError) 2.dp else 1.dp,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = char,
                            style = MaterialTheme.typography.titleLarge,
                            color = if (enabled) scheme.onSurface else scheme.onSurface.copy(alpha = 0.38f),
                        )
                    }
                }
            }
        },
    )
}

/** Small helper to keep the cell modifier readable (clip + background + border in one rounded shape). */
private fun Modifier.clipBackgroundBorder(
    radius: androidx.compose.ui.unit.Dp,
    bg: androidx.compose.ui.graphics.Color,
    border: androidx.compose.ui.graphics.Color,
    borderWidth: androidx.compose.ui.unit.Dp,
): Modifier {
    val shape = RoundedCornerShape(radius)
    return this
        .background(bg, shape)
        .border(borderWidth, border, shape)
}
