package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import platform.Foundation.NSDate
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.timeIntervalSince1970
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
        control?.let { onChange((it.date.timeIntervalSince1970 * 1000.0).toLong()) }
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
            addTarget(handler, sel_registerName("changed"), UIControlEventValueChanged)
        }
    }
    handler.control = control
    val backing = remember { UIView() }
    val backingColor = interopBackingColor() // published surface on solid; clear on Liquid Glass
    val remeasure = rememberUIKitInteropRemeasureRequester()
    // Re-measure when the shown date changes (compact picker width tracks it), NOT per scroll frame (drift).
    LaunchedEffect(selectedMillis) { remeasure.requestRemeasure() }

    UIKitView(
        factory = {
            backing.pinFilling(control)
            backing
        },
        modifier = modifier.remeasureRequester(remeasure),
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
        },
    )
}
