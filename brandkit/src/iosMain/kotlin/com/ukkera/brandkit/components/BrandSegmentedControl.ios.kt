package com.ukkera.brandkit.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.viewinterop.UIKitView
import androidx.compose.ui.viewinterop.remeasureRequester
import androidx.compose.ui.viewinterop.rememberUIKitInteropRemeasureRequester
import com.ukkera.brandkit.components.model.ResolvedSegmentedStyle
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import platform.UIKit.NSFontAttributeName
import platform.UIKit.NSForegroundColorAttributeName
import platform.UIKit.UIControlEventValueChanged
import platform.UIKit.UIControlStateNormal
import platform.UIKit.UIControlStateSelected
import platform.UIKit.UISegmentedControl
import platform.UIKit.UIUserInterfaceStyle
import platform.UIKit.UIView
import platform.UIKit.accessibilityLabel
import platform.darwin.NSObject
import platform.objc.sel_registerName

@OptIn(BetaInteropApi::class)
private class SegmentedHandler : NSObject() {
    var onSelect: (Int) -> Unit = {}
    var control: UISegmentedControl? = null

    @ObjCAction
    fun valueChanged() = control?.let { onSelect(it.selectedSegmentIndex.toInt()) } ?: Unit
}

/**
 * iOS BrandSegmentedControl → a real `UISegmentedControl` pinned inside a theme-colored backing.
 * The selected segment uses [style.selectedColor]; segment labels use the resolved title colors.
 */
@OptIn(ExperimentalComposeUiApi::class, ExperimentalForeignApi::class)
@Composable
internal actual fun PlatformBrandSegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    style: ResolvedSegmentedStyle,
    contentDescription: String?,
    testTag: String?,
) {
    val handler = remember { SegmentedHandler() }
    handler.onSelect = onSelectedIndexChange

    val control = remember {
        UISegmentedControl().apply {
            addTarget(handler, sel_registerName("valueChanged"), UIControlEventValueChanged)
        }
    }
    handler.control = control
    val backing = remember { UIView() }
    val backingColor = interopBackingColor() // published surface on solid; clear on Liquid Glass
    val remeasure = rememberUIKitInteropRemeasureRequester()
    // Re-measure when the segment set changes (it drives width), NOT on every scroll frame (avoids drift).
    LaunchedEffect(options) { remeasure.requestRemeasure() }

    UIKitView(
        factory = {
            backing.pinFilling(control)
            backing
        },
        modifier = modifier.remeasureRequester(remeasure),
        properties = scrollSafeInteropProperties(), // overlay placement so the backing isn't clipped on scroll
        update = { _ ->
            backing.backgroundColor = backingColor
            control.overrideUserInterfaceStyle =
                if (style.surface.luminance() < 0.5f) UIUserInterfaceStyle.UIUserInterfaceStyleDark
                else UIUserInterfaceStyle.UIUserInterfaceStyleLight
            if (control.numberOfSegments.toInt() != options.size) {
                control.removeAllSegments()
                options.forEachIndexed { i, title ->
                    control.insertSegmentWithTitle(title, i.toULong(), false)
                }
            }
            control.selectedSegmentTintColor = style.selectedColor.toUIColor()
            // Dynamic Type: the scaled brand font (toUIFont scales via UIFontMetrics) on the segment titles.
            val titleFont = style.textStyle.toUIFont()
            control.setTitleTextAttributes(
                mapOf<Any?, Any?>(
                    NSForegroundColorAttributeName to style.textColor.toUIColor(),
                    NSFontAttributeName to titleFont,
                ),
                UIControlStateNormal,
            )
            control.setTitleTextAttributes(
                mapOf<Any?, Any?>(
                    NSForegroundColorAttributeName to style.selectedTextColor.toUIColor(),
                    NSFontAttributeName to titleFont,
                ),
                UIControlStateSelected,
            )
            if (control.selectedSegmentIndex.toInt() != selectedIndex) {
                control.selectedSegmentIndex = selectedIndex.toLong()
            }
            control.enabled = enabled
            control.accessibilityLabel = contentDescription // segment names + selection announced natively
            testTag?.let { control.setAccessibilityId(it) }
        },
    )
}
