package com.ukkera.brandkit.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ukkera.brandkit.components.internal.chipBorderColor

/**
 * Chip behavior, mapped to the matching Material chip.
 * - [Assist]: a one-shot action ("Add to library").
 * - [Filter]: a selectable filter ([selected] toggles the tinted state) — manga genre filters.
 * - [Input]: a removable entry (set [trailingIcon] = a close glyph + [onTrailingClick]) — active filters/tags.
 * - [Suggestion]: a tappable suggestion (no trailing slot — a [trailingIcon] is ignored).
 */
public enum class BrandChipStyle { Assist, Filter, Input, Suggestion }

/**
 * A compact chip — genre tags, filters, removable selections. **Compose-drawn on both platforms** (iOS has
 * no native chip control; this wraps the well-tested Material chips, themed by `MaterialTheme`). Icons are
 * plain Compose [ImageVector]s on both platforms — this is a Compose-rendered control, so it takes an
 * `ImageVector` directly (not a [com.ukkera.brandkit.components.model.BrandIcon]) and there is no SF-Symbol
 * slot to silently drop. The label is single-line and ellipsized.
 *
 * Chips are **interactive** by Material convention (they show a ripple and announce as a button); for a
 * purely static label, render styled text instead. [onTrailingClick] adds a ≥48dp remove target that
 * announces as "Remove".
 *
 * `BrandChip("Action", style = Filter, selected = isOn, onClick = { isOn = !isOn })`
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun BrandChip(
    label: String,
    modifier: Modifier = Modifier,
    style: BrandChipStyle = BrandChipStyle.Assist,
    selected: Boolean = false,
    onClick: () -> Unit = {},
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    onTrailingClick: (() -> Unit)? = null,
    contentDescription: String? = null,
    testTag: String? = null,
) {
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
                // ≥48dp, labeled remove target around the 18dp glyph (the glyph alone is below the a11y min).
                Box(
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .clickable(onClickLabel = "Remove", role = Role.Button, onClick = onTrailing),
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

    val text: @Composable () -> Unit = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) }
    // Material's default chip border is `outlineVariant` (a faint divider tone) which vanishes on a
    // `surfaceVariant` card. Use `outline` — the M3 boundary role — so the always-bordered styles keep a
    // visible edge on any surface (page, card, glass). Selection styles keep their state-driven Material border.
    val outlineBorder = BorderStroke(1.dp, chipBorderColor(MaterialTheme.colorScheme.outline, enabled))
    when (style) {
        BrandChipStyle.Assist ->
            AssistChip(onClick = onClick, label = text, modifier = m, enabled = enabled, leadingIcon = leadingSlot, trailingIcon = trailingSlot, border = outlineBorder)
        BrandChipStyle.Filter ->
            FilterChip(selected = selected, onClick = onClick, label = text, modifier = m, enabled = enabled, leadingIcon = leadingSlot, trailingIcon = trailingSlot)
        BrandChipStyle.Input ->
            InputChip(selected = selected, onClick = onClick, label = text, modifier = m, enabled = enabled, leadingIcon = leadingSlot, trailingIcon = trailingSlot)
        BrandChipStyle.Suggestion ->
            SuggestionChip(onClick = onClick, label = text, modifier = m, enabled = enabled, icon = leadingSlot, border = outlineBorder)
    }
}
