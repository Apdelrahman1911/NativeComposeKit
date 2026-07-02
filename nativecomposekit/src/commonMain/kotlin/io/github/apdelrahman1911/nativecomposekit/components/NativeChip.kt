package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.apdelrahman1911.nativecomposekit.components.internal.chipBorderColor
import io.github.apdelrahman1911.nativecomposekit.theme.LocalNativeStrings

/**
 * Chip behavior, mapped to the matching Material chip.
 * - [Assist]: a one-shot action ("Add to library").
 * - [Filter]: a selectable filter ([selected] toggles the tinted state) — manga genre filters.
 * - [Input]: a removable entry (set [trailingIcon] = a close glyph + [onTrailingClick]) — active filters/tags.
 * - [Suggestion]: a tappable suggestion (no trailing slot — a [trailingIcon] is ignored).
 */
public enum class NativeChipStyle { Assist, Filter, Input, Suggestion }

/**
 * A compact chip — genre tags, filters, removable selections. **Compose-drawn on both platforms** (iOS has
 * no native chip control; this wraps the well-tested Material chips, themed by `MaterialTheme`). Icons are
 * plain Compose [ImageVector]s on both platforms — this is a Compose-rendered control, so it takes an
 * `ImageVector` directly (not a [io.github.apdelrahman1911.nativecomposekit.components.model.NativeIcon]) and there is no SF-Symbol
 * slot to silently drop. The label is single-line and ellipsized.
 *
 * Chips are **interactive** by Material convention (they show a ripple and announce as a button); for a
 * purely static label, render styled text instead. [onTrailingClick] adds a compact remove target that
 * announces as "Remove"; the remove is also exposed as a chip-level custom accessibility action, so
 * screen-reader users trigger it from the chip itself rather than hunting the small glyph.
 *
 * `NativeChip("Action", style = Filter, selected = isOn, onClick = { isOn = !isOn })`
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun NativeChip(
    label: String,
    modifier: Modifier = Modifier,
    style: NativeChipStyle = NativeChipStyle.Assist,
    selected: Boolean = false,
    onClick: () -> Unit = {},
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    onTrailingClick: (() -> Unit)? = null,
    contentDescription: String? = null,
    testTag: String? = null,
) {
    val strings = LocalNativeStrings.current
    val leadingSlot: (@Composable () -> Unit)? = if (leadingIcon != null) {
        { Icon(leadingIcon, contentDescription = null, modifier = Modifier.size(18.dp)) }
    } else {
        null
    }

    val onTrailing = onTrailingClick
    val trailingSlot: (@Composable () -> Unit)? = when {
        trailingIcon == null -> null
        onTrailing != null -> {
            {
                // A compact remove target: a ≥48dp box inside the ~32dp chip would inflate the chip or
                // overflow it, so the glyph gets a modest 24dp box; screen readers reach the remove
                // through the chip-level custom accessibility action below instead of this small target.
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clickable(enabled = enabled, onClickLabel = strings.chipRemove, role = Role.Button, onClick = onTrailing),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(trailingIcon, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            }
        }
        else -> {
            { Icon(trailingIcon, contentDescription = null, modifier = Modifier.size(18.dp)) }
        }
    }

    var m = modifier
    testTag?.let { m = m.testTag(it) }
    contentDescription?.let { cd -> m = m.semantics { this.contentDescription = cd } }
    if (trailingIcon != null && onTrailing != null && enabled) {
        // The compact remove glyph is below the a11y touch minimum — expose the remove on the chip node
        // itself so screen readers can trigger it without the small target. Gated like the tap target:
        // a disabled chip offers no remove.
        m = m.semantics {
            customActions = listOf(CustomAccessibilityAction(strings.chipRemove) { onTrailing(); true })
        }
    }

    val text: @Composable () -> Unit = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) }
    // Material's default chip border is `outlineVariant` (a faint divider tone) which vanishes on a
    // `surfaceVariant` card. Use `outline` — the M3 boundary role — so every bordered state keeps a visible
    // edge on any surface (page, card, glass). Filter/Input keep their selected state borderless (Material's
    // selected treatment is the tonal fill), but their UNSELECTED border gets the same `outline` fix.
    val outline = MaterialTheme.colorScheme.outline
    val outlineBorder = BorderStroke(1.dp, chipBorderColor(outline, enabled))
    when (style) {
        NativeChipStyle.Assist ->
            AssistChip(onClick = onClick, label = text, modifier = m, enabled = enabled, leadingIcon = leadingSlot, trailingIcon = trailingSlot, border = outlineBorder)
        NativeChipStyle.Filter ->
            FilterChip(
                selected = selected, onClick = onClick, label = text, modifier = m, enabled = enabled,
                leadingIcon = leadingSlot, trailingIcon = trailingSlot,
                border = FilterChipDefaults.filterChipBorder(enabled = enabled, selected = selected, borderColor = outline),
            )
        NativeChipStyle.Input ->
            InputChip(
                selected = selected, onClick = onClick, label = text, modifier = m, enabled = enabled,
                leadingIcon = leadingSlot, trailingIcon = trailingSlot,
                border = InputChipDefaults.inputChipBorder(enabled = enabled, selected = selected, borderColor = outline),
            )
        NativeChipStyle.Suggestion ->
            SuggestionChip(onClick = onClick, label = text, modifier = m, enabled = enabled, icon = leadingSlot, border = outlineBorder)
    }
}
