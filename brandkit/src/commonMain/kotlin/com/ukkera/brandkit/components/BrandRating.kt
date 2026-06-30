package com.ukkera.brandkit.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.StarHalf
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ukkera.brandkit.theme.BrandTheme

/**
 * A star rating — manga/series scores. **Compose-drawn on both platforms** (no native star control on
 * either). Read-only by default; pass [onRatingChange] to make it interactive (tapping star *n* sets the
 * rating to *n* whole stars). [allowHalf] shows half-star glyphs for fractional **display** values only —
 * an interactive control never shows a half it can't produce. The filled tint defaults to the amber
 * `warning` status color; the empty track to `outlineVariant`. [rating] is clamped to `0..max`.
 *
 * Accessibility: read-only exposes a `contentDescription` ("Rating: 4.5 out of 5"); interactive exposes a
 * live `stateDescription` plus one labeled button per star ("Rate 4 of 5"), each with a ≥48dp touch target.
 * [enabled] = false dims the stars (38% alpha) and makes it non-interactive (read-only) regardless of
 * [onRatingChange].
 *
 * `BrandRating(4.5f)` (display) · `BrandRating(stars, onRatingChange = { stars = it })` (interactive)
 */
@Composable
public fun BrandRating(
    rating: Float,
    modifier: Modifier = Modifier,
    onRatingChange: ((Float) -> Unit)? = null,
    enabled: Boolean = true,
    max: Int = 5,
    starSize: Dp = 20.dp,
    allowHalf: Boolean = true,
    color: Color? = null,
    trackColor: Color? = null,
    contentDescription: String? = null,
    testTag: String? = null,
) {
    val baseFilled = color ?: BrandTheme.statusColors.warning
    val baseEmpty = trackColor ?: MaterialTheme.colorScheme.outlineVariant
    // Disabled dims both tints and forces read-only (no clickable stars), regardless of onRatingChange.
    val filledTint = if (enabled) baseFilled else baseFilled.copy(alpha = 0.38f)
    val emptyTint = if (enabled) baseEmpty else baseEmpty.copy(alpha = 0.38f)
    val onChange = if (enabled) onRatingChange else null
    val interactive = onChange != null
    val r = clampRating(rating, max)
    val rText = if (r % 1f == 0f) r.toInt().toString() else r.toString() // "4" not "4.0"; "4.5" stays
    val desc = contentDescription ?: "Rating: $rText out of $max"

    var m = modifier.semantics {
        if (interactive) stateDescription = desc else this.contentDescription = desc
    }
    testTag?.let { m = m.testTag(it) }

    Row(m) {
        for (i in 0 until max) {
            val starValue = i + 1
            // Half glyphs are display-only — an interactive control only produces whole stars (see starFill).
            val (vector, tint) = when (starFill(i, r, allowHalf, interactive)) {
                StarFill.Full -> Icons.Filled.Star to filledTint
                StarFill.Half -> Icons.AutoMirrored.Filled.StarHalf to filledTint
                StarFill.Empty -> Icons.Filled.StarBorder to emptyTint
            }
            if (onChange != null) {
                Box(
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .clickable(onClickLabel = "Rate $starValue of $max", role = Role.Button) { onChange(starValue.toFloat()) },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(vector, contentDescription = null, tint = tint, modifier = Modifier.size(starSize))
                }
            } else {
                Icon(vector, contentDescription = null, tint = tint, modifier = Modifier.size(starSize))
            }
        }
    }
}

/** Glyph state of a single star (pure, unit-testable — drives the icon/tint in [BrandRating]). */
internal enum class StarFill { Full, Half, Empty }

/** Clamps [rating] to `0..max`, mapping NaN to 0 (so a bad input never throws or over-fills). */
internal fun clampRating(rating: Float, max: Int): Float =
    if (rating.isNaN()) 0f else rating.coerceIn(0f, max.toFloat())

/**
 * Which glyph the 0-based star [index] shows for a clamped [rating]. A [Half][StarFill.Half] is **display-only**
 * (returned only when [allowHalf] and not [interactive]) — an interactive control produces whole stars only.
 */
internal fun starFill(index: Int, rating: Float, allowHalf: Boolean, interactive: Boolean): StarFill {
    val starValue = index + 1
    return when {
        rating >= starValue -> StarFill.Full
        allowHalf && !interactive && rating >= starValue - 0.5f -> StarFill.Half
        else -> StarFill.Empty
    }
}
