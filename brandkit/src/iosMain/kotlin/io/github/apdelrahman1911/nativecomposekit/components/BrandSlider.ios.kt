package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitView
import io.github.apdelrahman1911.nativecomposekit.components.model.ResolvedSliderStyle
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import platform.UIKit.UIControlEventValueChanged
import platform.UIKit.UISlider
import platform.UIKit.UIUserInterfaceStyle
import platform.UIKit.UIView
import platform.UIKit.accessibilityLabel
import platform.darwin.NSObject
import platform.objc.sel_registerName

@OptIn(BetaInteropApi::class)
private class SliderHandler : NSObject() {
    var onChange: (Float) -> Unit = {}
    var control: UISlider? = null

    @ObjCAction
    fun valueChanged() = onChange(control?.value ?: 0f)
}

/**
 * iOS BrandSlider → a real `UISlider` pinned inside a theme-colored backing. Track/thumb tints come
 * from [style]; the caller supplies the width (e.g. `Modifier.fillMaxWidth()`), height is fixed here.
 */
@OptIn(ExperimentalComposeUiApi::class, ExperimentalForeignApi::class)
@Composable
internal actual fun PlatformBrandSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier,
    min: Float,
    max: Float,
    enabled: Boolean,
    style: ResolvedSliderStyle,
    contentDescription: String?,
    testTag: String?,
) {
    val handler = remember { SliderHandler() }
    handler.onChange = onValueChange

    val control = remember {
        UISlider().apply {
            addTarget(handler, sel_registerName("valueChanged"), UIControlEventValueChanged)
        }
    }
    handler.control = control
    val backing = remember { UIView() }
    val backingColor = interopBackingColor() // published surface on solid; clear on Liquid Glass

    UIKitView(
        factory = {
            backing.pinFilling(control)
            backing
        },
        modifier = modifier.height(36.dp),
        properties = scrollSafeInteropProperties(), // overlay placement so the backing isn't clipped on scroll
        update = { _ ->
            backing.backgroundColor = backingColor
            control.overrideUserInterfaceStyle =
                if (style.surface.luminance() < 0.5f) UIUserInterfaceStyle.UIUserInterfaceStyleDark
                else UIUserInterfaceStyle.UIUserInterfaceStyleLight
            control.minimumValue = min
            control.maximumValue = max
            if (control.value != value) control.setValue(value, animated = false)
            control.minimumTrackTintColor = style.activeTrackColor.toUIColor()
            control.maximumTrackTintColor = style.inactiveTrackColor.toUIColor()
            control.thumbTintColor = style.thumbColor.toUIColor()
            control.enabled = enabled
            control.accessibilityLabel = contentDescription // UISlider announces its value for free
            testTag?.let { control.setAccessibilityId(it) }
        },
    )
}
