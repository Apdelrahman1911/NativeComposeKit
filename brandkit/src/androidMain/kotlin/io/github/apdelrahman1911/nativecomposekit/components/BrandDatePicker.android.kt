package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import java.time.Instant
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal actual fun PlatformBrandDatePicker(
    selectedMillis: Long?,
    onSelectedMillisChange: (Long) -> Unit,
    modifier: Modifier,
    minMillis: Long?,
    maxMillis: Long?,
    enabled: Boolean,
    tint: Color,
    surface: Color,
    contentDescription: String?,
    testTag: String?,
) {
    var m = modifier
    testTag?.let { m = m.testTag(it) }
    contentDescription?.let { cd -> m = m.semantics { this.contentDescription = cd } }
    if (!enabled) m = m.alpha(0.38f) // dim to read as disabled (Material DatePicker has no `enabled` param)

    fun yearOf(millis: Long): Int = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).year
    // Enforce min/max (and disabled = nothing selectable) — previously dropped on Android, honored only on iOS.
    val selectable = remember(minMillis, maxMillis, enabled) {
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                enabled &&
                    (minMillis == null || utcTimeMillis >= minMillis) &&
                    (maxMillis == null || utcTimeMillis <= maxMillis)

            override fun isSelectableYear(year: Int): Boolean =
                enabled &&
                    (minMillis == null || year >= yearOf(minMillis)) &&
                    (maxMillis == null || year <= yearOf(maxMillis))
        }
    }
    val yearRange = (minMillis?.let(::yearOf) ?: DatePickerDefaults.YearRange.first)..
        (maxMillis?.let(::yearOf) ?: DatePickerDefaults.YearRange.last)

    val state = rememberDatePickerState(
        initialSelectedDateMillis = selectedMillis,
        yearRange = yearRange,
        selectableDates = selectable,
    )
    // Report the user's selection out (no write-back into state → no loop).
    LaunchedEffect(state.selectedDateMillis) {
        val sel = state.selectedDateMillis
        if (enabled && sel != null && sel != selectedMillis) onSelectedMillisChange(sel)
    }
    DatePicker(state = state, modifier = m)
}
