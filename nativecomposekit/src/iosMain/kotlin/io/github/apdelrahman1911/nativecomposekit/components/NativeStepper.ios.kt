package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.viewinterop.UIKitView
import androidx.compose.ui.viewinterop.remeasureRequester
import androidx.compose.ui.viewinterop.rememberUIKitInteropRemeasureRequester
import io.github.apdelrahman1911.nativecomposekit.components.model.ResolvedStepperStyle
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import platform.UIKit.UIControlEventValueChanged
import platform.UIKit.UIStepper
import platform.UIKit.UIUserInterfaceStyle
import platform.UIKit.UIView
import platform.UIKit.accessibilityLabel
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import platform.objc.sel_registerName

@OptIn(BetaInteropApi::class)
private class StepperHandler : NSObject() {
    var onChange: (Int) -> Unit = {}
    var control: UIStepper? = null

    /** The value most recently applied by composition — the truth a rejected step snaps back to. */
    var lastComposed: Double = 0.0

    @ObjCAction
    fun valueChanged() {
        onChange((control?.value ?: 0.0).toInt())
        // Controlled-rejection re-assert (same pattern as NativeToggle): the UIStepper self-mutates on
        // a tap even when the consumer ignores the change, and a rejected change produces no
        // recomposition to correct it. The UIStepper draws no value, so a transient re-assert has no
        // visible artifact; at worst a rejected-and-held autorepeat re-counts from the composed value.
        dispatch_async(dispatch_get_main_queue()) {
            val c = control ?: return@dispatch_async
            if (c.value != lastComposed) c.value = lastComposed
        }
    }
}

/**
 * iOS NativeStepper → a real `UIStepper` (native -/+ control) pinned inside a theme-colored backing.
 * Min/max/step and tint come from the resolved style.
 *
 * The host keeps the `UIStepper`'s native intrinsic size (~94×32pt) — shorter than the 44pt touch
 * minimum by design: per Apple's own Settings pattern, the surrounding **row** supplies the ≥44pt hit
 * area (the caller's layout concern), and stretching the control itself would distort it.
 */
@OptIn(ExperimentalComposeUiApi::class, ExperimentalForeignApi::class)
@Composable
internal actual fun PlatformNativeStepper(
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
    val sizeFp = remember { InteropSizeFingerprint() }

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
            handler.lastComposed = value.toDouble() // the composed truth the rejection re-assert snaps back to
            if (control.value != value.toDouble()) control.value = value.toDouble()
            control.tintColor = style.tint.toUIColor()
            control.enabled = enabled
            control.accessibilityLabel = contentDescription // UIStepper announces its value for free
            testTag?.let { control.setAccessibilityId(it) }
            // Fixed intrinsic size: measure once, on the first update (see InteropSizeFingerprint).
            sizeFp.requestIfChanged(Unit) { remeasure.requestRemeasure() }
        },
        // The released stepper must stop dispatching into the handler once the node has left the
        // composition for good.
        onRelease = {
            control.removeTarget(handler, sel_registerName("valueChanged"), UIControlEventValueChanged)
            handler.control = null
        },
    )
}
