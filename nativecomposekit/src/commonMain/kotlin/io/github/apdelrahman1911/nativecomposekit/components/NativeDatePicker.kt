package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.github.apdelrahman1911.nativecomposekit.components.internal.resolveSurfaceFill
import io.github.apdelrahman1911.nativecomposekit.theme.LocalNativeSurface

/**
 * A date picker. Renders the most native control per platform: on **iOS** a real `UIDatePicker` (compact
 * style — a tappable field that expands to the system calendar); on **Android** the Material 3 inline
 * `DatePicker` calendar. [selectedMillis] is epoch milliseconds (`null` = nothing selected);
 * [onSelectedMillisChange] reports the user's pick.
 *
 * **Epoch/timezone contract:** [selectedMillis]/[minMillis]/[maxMillis] are **UTC** epoch milliseconds at the
 * start of the day (this is what Material's `DatePickerState` emits, and the iOS renderer mirrors it). To show
 * a picked date in the user's local zone, convert with the device timezone at the display layer — don't assume
 * the millis are local-midnight.
 *
 * Scope: this v1 is **date selection**. iOS `UIDatePicker` also supports time / date-and-time natively; a
 * `mode` parameter (with the matching Android time wiring) is a planned fast-follow. The control is
 * controlled on both platforms: programmatic [selectedMillis] changes (including `null` to clear) reach the
 * UI. `minMillis`/`maxMillis`/`enabled` are honored on both platforms (Android via `SelectableDates` + a
 * dimmed, non-selectable calendar when disabled). On Android, min/max updates re-gate which dates are
 * selectable, but the **year dropdown range is fixed at first composition** (Material's `DatePickerState`
 * captures `yearRange` once; rebuilding the state to widen it would drop the user's selection).
 */
@Composable
public fun NativeDatePicker(
    selectedMillis: Long?,
    onSelectedMillisChange: (Long) -> Unit,
    modifier: Modifier = Modifier,
    minMillis: Long? = null,
    maxMillis: Long? = null,
    enabled: Boolean = true,
    contentDescription: String? = null,
    testTag: String? = null,
) {
    val scheme = MaterialTheme.colorScheme
    PlatformNativeDatePicker(
        selectedMillis = selectedMillis,
        onSelectedMillisChange = onSelectedMillisChange,
        modifier = modifier,
        minMillis = minMillis,
        maxMillis = maxMillis,
        enabled = enabled,
        tint = scheme.primary,
        // Drive the iOS control's light/dark from the surface it actually sits on (card/page/glass), not the page.
        surface = resolveSurfaceFill(LocalNativeSurface.current, scheme.background),
        contentDescription = contentDescription,
        testTag = testTag,
    )
}

private const val MillisPerUtcDay = 86_400_000L

/**
 * Floors epoch [millis] to 00:00:00.000 UTC of the day it falls in — the value shape of the kit's
 * "UTC start of day" contract. Floored (not truncated) division, so pre-1970 instants land on their own
 * day's start rather than the next day's (plain `/`/`%` round toward zero for negatives).
 */
internal fun utcDayStart(millis: Long): Long = millis.floorDiv(MillisPerUtcDay) * MillisPerUtcDay

/** Native date renderer. Android → Material `DatePicker`; iOS → `UIDatePicker` (compact). */
@Composable
internal expect fun PlatformNativeDatePicker(
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
)
