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
import com.ukkera.brandkit.components.model.ResolvedStepperStyle
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import platform.UIKit.UIControlEventValueChanged
import platform.UIKit.UIStepper
import platform.UIKit.UIUserInterfaceStyle
import platform.UIKit.UIView
import platform.UIKit.accessibilityLabel
import platform.darwin.NSObject
import platform.objc.sel_registerName

@OptIn(BetaInteropApi::class)
private class StepperHandler : NSObject() {
    var onChange: (Int) -> Unit = {}
    var control: UIStepper? = null

    @ObjCAction
    fun valueChanged() = onChange((control?.value ?: 0.0).toInt())
}

/**
 * iOS BrandStepper → a real `UIStepper` (native -/+ control) pinned inside a theme-colored backing.
 * Min/max/step and tint come from the resolved style.
 */
@OptIn(ExperimentalComposeUiApi::class, ExperimentalForeignApi::class)
@Composable
internal actual fun PlatformBrandStepper(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier,
    min: Int,
    max: Int,
    step: Int,
    enabled: Boolean,
    style: ResolvedStepperStyle,
    contentDescription: String?,
    testTag: String?,
) {
    val handler = remember { StepperHandler() }
    handler.onChange = onValueChange

    val control = remember {
        UIStepper().apply {
            addTarget(handler, sel_registerName("valueChanged"), UIControlEventValueChanged)
        }
    }
    handler.control = control
    val backing = remember { UIView() }
    val backingColor = interopBackingColor() // published surface on solid; clear on Liquid Glass
    val remeasure = rememberUIKitInteropRemeasureRequester()
    LaunchedEffect(Unit) { remeasure.requestRemeasure() } // measure once; NOT per scroll frame (avoids drift)

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
            control.minimumValue = min.toDouble()
            control.maximumValue = max.toDouble()
            control.stepValue = step.toDouble()
            if (control.value != value.toDouble()) control.value = value.toDouble()
            control.tintColor = style.tint.toUIColor()
            control.enabled = enabled
            control.accessibilityLabel = contentDescription // UIStepper announces its value for free
            testTag?.let { control.setAccessibilityId(it) }
        },
    )
}
