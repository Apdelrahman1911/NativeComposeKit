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
import com.ukkera.brandkit.components.model.ResolvedToggleStyle
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import platform.UIKit.UIControlEventValueChanged
import platform.UIKit.UISwitch
import platform.UIKit.UIUserInterfaceStyle
import platform.UIKit.UIView
import platform.UIKit.accessibilityLabel
import platform.darwin.NSObject
import platform.objc.sel_registerName

@OptIn(BetaInteropApi::class)
private class SwitchHandler : NSObject() {
    var onChange: (Boolean) -> Unit = {}
    var control: UISwitch? = null

    @ObjCAction
    fun valueChanged() = onChange(control?.on ?: false)
}

/**
 * iOS BrandToggle → a real `UISwitch` (the native green pill) pinned inside a theme-colored backing
 * so its rounded shape blends in dark mode. The on-track color comes from [style]; the thumb stays
 * the native white.
 */
@OptIn(ExperimentalComposeUiApi::class, ExperimentalForeignApi::class)
@Composable
internal actual fun PlatformBrandToggle(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier,
    enabled: Boolean,
    style: ResolvedToggleStyle,
    contentDescription: String?,
    testTag: String?,
) {
    val handler = remember { SwitchHandler() }
    handler.onChange = onCheckedChange ?: {}

    val control = remember {
        UISwitch().apply {
            addTarget(handler, sel_registerName("valueChanged"), UIControlEventValueChanged)
        }
    }
    handler.control = control
    val backing = remember { UIView() }
    val remeasure = rememberUIKitInteropRemeasureRequester()
    // Backing matches the published surface (so it doesn't show a box on a card/page) and is CLEAR on
    // Liquid Glass (so the switch capsule floats on the material instead of sitting on a solid rectangle).
    val backingColor = interopBackingColor()
    // Re-measure only ONCE (size is fixed) — NOT on every update. The update block re-fires on every scroll
    // frame (bounds change), and requesting a remeasure there forced a per-frame interop re-layout that
    // desynced the native view from the scroll (the "drift"/"cut"). One initial measure is enough here.
    LaunchedEffect(Unit) { remeasure.requestRemeasure() }

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
            control.onTintColor = style.trackOnColor.toUIColor()
            if (control.on != checked) control.setOn(checked, animated = false)
            control.enabled = enabled
            // null callback = read-only: keep the switch full-color but non-interactive (vs `enabled = false`,
            // which greys it). A disabled switch is already non-interactive regardless.
            control.userInteractionEnabled = enabled && onCheckedChange != null
            contentDescription?.let { control.accessibilityLabel = it }
            testTag?.let { control.setAccessibilityId(it) }
        },
    )
}
