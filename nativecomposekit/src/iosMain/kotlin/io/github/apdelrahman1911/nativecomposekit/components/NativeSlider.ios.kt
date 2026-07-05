package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitView
import androidx.compose.ui.viewinterop.remeasureRequester
import androidx.compose.ui.viewinterop.rememberUIKitInteropRemeasureRequester
import io.github.apdelrahman1911.nativecomposekit.components.model.ResolvedSliderStyle
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import platform.UIKit.UIControlEventAllEvents
import platform.UIKit.UIControlEventTouchCancel
import platform.UIKit.UIControlEventTouchUpInside
import platform.UIKit.UIControlEventTouchUpOutside
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
    var onFinished: (() -> Unit)? = null
    var control: UISlider? = null
    var steps: Int = 0
    var min: Float = 0f
    var max: Float = 1f

    /** The value most recently applied by composition — the truth a rejected drag glides back to. */
    var lastComposed: Float = 0f

    /** Bumped by every composition update — lets the deferred re-assert see whether composition responded. */
    var updateGeneration: Long = 0L

    @ObjCAction
    fun valueChanged() {
        val raw = control?.value ?: 0f
        // A discrete slider (steps > 0) snaps the EMITTED value; the native control stays continuous
        // and the thumb follows when composition echoes the snapped value back through update.
        onChange(if (steps > 0) sliderSnappedValue(raw, min, max, steps) else raw)
    }

    @ObjCAction
    fun touchEnded() {
        val genAtEvent = updateGeneration
        onFinished?.invoke()
        // Controlled-rejection re-assert, at gesture END only — doing it per value-changed event would
        // fight the user's drag frame by frame. The check runs AFTER the recomposition window and only
        // acts when composition stayed silent (same pattern and rationale as NativeToggle: a same-tick
        // re-assert races the accepted change's recomposition and nudges the thumb with a stale value).
        // If the consumer ignored the drag, the thumb glides back to the composed value.
        afterRecompositionWindow {
            val c = control ?: return@afterRecompositionWindow
            if (updateGeneration != genAtEvent) return@afterRecompositionWindow // composition responded — its update is authoritative
            if (c.value != lastComposed) c.setValue(lastComposed, animated = true)
        }
    }
}

/**
 * iOS NativeSlider → a real `UISlider` pinned inside a theme-colored backing. Track/thumb tints come
 * from [style]; the caller supplies the width (e.g. `Modifier.fillMaxWidth()`), height is fixed here.
 *
 * The host keeps the `UISlider`'s native intrinsic height (~34pt inside the fixed 36dp strip) —
 * smaller than the 44pt touch minimum by design: per Apple's own Settings pattern, the surrounding
 * **row** supplies the ≥44pt hit area (the caller's layout concern).
 */
@OptIn(ExperimentalComposeUiApi::class, ExperimentalForeignApi::class)
@Composable
internal actual fun PlatformNativeSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier,
    min: Float,
    max: Float,
    steps: Int,
    onValueChangeFinished: (() -> Unit)?,
    enabled: Boolean,
    style: ResolvedSliderStyle,
    contentDescription: String?,
    testTag: String?,
) {
    val handler = remember { SliderHandler() }
    handler.onChange = onValueChange
    handler.onFinished = onValueChangeFinished
    handler.steps = steps
    handler.min = min
    handler.max = max

    val control = remember {
        UISlider().apply {
            addTarget(handler, sel_registerName("valueChanged"), UIControlEventValueChanged)
            // Gesture end (finger up inside/outside, or cancelled) → onValueChangeFinished + the
            // controlled-rejection re-assert.
            addTarget(handler, sel_registerName("touchEnded"), UIControlEventTouchUpInside)
            addTarget(handler, sel_registerName("touchEnded"), UIControlEventTouchUpOutside)
            addTarget(handler, sel_registerName("touchEnded"), UIControlEventTouchCancel)
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
        modifier = modifier.height(36.dp).remeasureRequester(remeasure),
        properties = scrollSafeInteropProperties(), // overlay placement so the backing isn't clipped on scroll
        update = { _ ->
            handler.updateGeneration++ // composition responded — disarms any pending rejection re-assert
            // Appearance setters rebuild control layers and must not re-fire on the recomposition a drag's
            // accepted values trigger every frame — assign only on an actual change.
            if (backing.backgroundColor?.isEqual(backingColor) != true) backing.backgroundColor = backingColor
            val interfaceStyle =
                if (style.surface.luminance() < 0.5f) UIUserInterfaceStyle.UIUserInterfaceStyleDark
                else UIUserInterfaceStyle.UIUserInterfaceStyleLight
            if (control.overrideUserInterfaceStyle != interfaceStyle) control.overrideUserInterfaceStyle = interfaceStyle
            if (control.minimumValue != min) control.minimumValue = min
            if (control.maximumValue != max) control.maximumValue = max
            handler.lastComposed = value // the composed truth the gesture-end re-assert glides back to
            if (control.value != value) control.setValue(value, animated = false)
            val activeTint = style.activeTrackColor.toUIColor()
            if (control.minimumTrackTintColor?.isEqual(activeTint) != true) control.minimumTrackTintColor = activeTint
            val inactiveTint = style.inactiveTrackColor.toUIColor()
            if (control.maximumTrackTintColor?.isEqual(inactiveTint) != true) control.maximumTrackTintColor = inactiveTint
            val thumbTint = style.thumbColor.toUIColor()
            if (control.thumbTintColor?.isEqual(thumbTint) != true) control.thumbTintColor = thumbTint
            control.enabled = enabled
            control.accessibilityLabel = contentDescription // UISlider announces its value for free
            testTag?.let { control.setAccessibilityId(it) }
            // Height is fixed but a wrap-content width measures the slider's fitting size: request one
            // post-content measure so that width is never the factory-fresh one (see InteropSizeFingerprint).
            sizeFp.requestIfChanged(Unit) { remeasure.requestRemeasure() }
        },
        // The released slider must stop dispatching into the handler once the node has left the
        // composition for good (a null action removes every action registered for the target).
        onRelease = {
            control.removeTarget(handler, null, UIControlEventAllEvents)
            handler.control = null
        },
    )
}
