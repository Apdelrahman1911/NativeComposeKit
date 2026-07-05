package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.viewinterop.UIKitView
import androidx.compose.ui.viewinterop.remeasureRequester
import androidx.compose.ui.viewinterop.rememberUIKitInteropRemeasureRequester
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import platform.Foundation.NSCalendar
import platform.Foundation.NSDate
import platform.Foundation.NSTimeZone
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.timeIntervalSince1970
import platform.Foundation.timeZoneWithName
import platform.UIKit.UIControlEventValueChanged
import platform.UIKit.UIDatePicker
import platform.UIKit.UIDatePickerMode
import platform.UIKit.UIDatePickerStyle
import platform.UIKit.UIUserInterfaceStyle
import platform.UIKit.UIView
import platform.UIKit.accessibilityLabel
import platform.darwin.NSObject
import platform.objc.sel_registerName

/** Forwards `UIDatePicker` value changes to a Kotlin lambda (retained; UIControl targets are weak). */
@OptIn(BetaInteropApi::class)
private class DatePickerHandler : NSObject() {
    var control: UIDatePicker? = null
    var onChange: (Long) -> Unit = {}

    @ObjCAction
    fun changed() {
        // Even pinned to UTC, a date-mode UIDatePicker preserves the time-of-day of whatever date it was
        // seeded with — floor to the UTC day start so emitted values honor the kit's contract.
        control?.let { onChange(utcDayStart((it.date.timeIntervalSince1970 * 1000.0).toLong())) }
    }
}

/** iOS date picker → a real `UIDatePicker` (compact style), pinned inside a theme backing for dark mode. */
@OptIn(ExperimentalComposeUiApi::class, ExperimentalForeignApi::class)
@Composable
internal actual fun PlatformNativeDatePicker(
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
    val handler = remember { DatePickerHandler() }
    handler.onChange = onSelectedMillisChange

    val control = remember {
        UIDatePicker().apply {
            datePickerMode = UIDatePickerMode.UIDatePickerModeDate
            preferredDatePickerStyle = UIDatePickerStyle.UIDatePickerStyleCompact
            // The kit's contract is UTC epoch millis at the start of the day, but UIDatePicker resolves
            // days in the device's LOCAL zone by default — a UTC-midnight input renders as the previous
            // day anywhere west of UTC. Pin the control AND its calendar to UTC so days round-trip; the
            // calendar is a copy because currentCalendar is shared (mutating its zone would leak app-wide).
            val utc = NSTimeZone.timeZoneWithName("UTC")
            if (utc != null) {
                timeZone = utc
                (NSCalendar.currentCalendar.copy() as? NSCalendar)?.let { cal ->
                    cal.timeZone = utc
                    calendar = cal
                }
            }
            addTarget(handler, sel_registerName("changed"), UIControlEventValueChanged)
        }
    }
    handler.control = control
    val backing = remember { InteropBackingView() }
    val backingColor = interopBackingColor() // published surface on solid; clear on Liquid Glass
    val remeasure = rememberUIKitInteropRemeasureRequester()
    val sizeFp = remember { InteropSizeFingerprint() }

    InteropDisposeFailSafe(backing) // synchronous ghost-kill; see UiKitInterop.ios.kt
    UIKitView(
        factory = {
            backing.pinFilling(control)
            backing
        },
        modifier = modifier.remeasureRequester(remeasure).then(rememberInteropPositionHeal(backing)),
        properties = scrollSafeInteropProperties(), // overlay placement so the backing isn't clipped on scroll
        update = {
            backing.backgroundColor = backingColor
            control.overrideUserInterfaceStyle =
                if (surface.luminance() < 0.5f) UIUserInterfaceStyle.UIUserInterfaceStyleDark
                else UIUserInterfaceStyle.UIUserInterfaceStyleLight
            control.tintColor = tint.toUIColor()
            selectedMillis?.let {
                val d = NSDate.dateWithTimeIntervalSince1970(it / 1000.0)
                if (control.date.timeIntervalSince1970 != d.timeIntervalSince1970) control.setDate(d, animated = false)
            }
            control.minimumDate = minMillis?.let { NSDate.dateWithTimeIntervalSince1970(it / 1000.0) }
            control.maximumDate = maxMillis?.let { NSDate.dateWithTimeIntervalSince1970(it / 1000.0) }
            control.enabled = enabled
            contentDescription?.let { control.accessibilityLabel = it }
            testTag?.let { control.setAccessibilityId(it) }
            // The compact picker's width tracks the shown date → re-measure when it changes, from update,
            // after it's applied (see InteropSizeFingerprint).
            sizeFp.requestIfChanged(selectedMillis) { remeasure.requestRemeasure() }
        },
    )
}
