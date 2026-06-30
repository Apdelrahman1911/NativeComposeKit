package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.github.apdelrahman1911.nativecomposekit.components.internal.resolveSurfaceFill
import io.github.apdelrahman1911.nativecomposekit.theme.LocalBrandSurface

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
 * `mode` parameter (with the matching Android time wiring) is a planned fast-follow. On Android the picker
 * is effectively uncontrolled after first composition (Material's `DatePickerState` owns the selection);
 * `minMillis`/`maxMillis`/`enabled` are honored on both platforms (Android via `SelectableDates` + a dimmed,
 * non-selectable calendar when disabled).
 */
@Composable
public fun BrandDatePicker(
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
    PlatformBrandDatePicker(
        selectedMillis = selectedMillis,
        onSelectedMillisChange = onSelectedMillisChange,
        modifier = modifier,
        minMillis = minMillis,
        maxMillis = maxMillis,
        enabled = enabled,
        tint = scheme.primary,
        // Drive the iOS control's light/dark from the surface it actually sits on (card/page/glass), not the page.
        surface = resolveSurfaceFill(LocalBrandSurface.current, scheme.background),
        contentDescription = contentDescription,
        testTag = testTag,
    )
}

/** Native date renderer. Android → Material `DatePicker`; iOS → `UIDatePicker` (compact). */
@Composable
internal expect fun PlatformBrandDatePicker(
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
