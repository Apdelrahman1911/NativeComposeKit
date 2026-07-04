package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.UIKitView
import androidx.compose.ui.viewinterop.remeasureRequester
import androidx.compose.ui.viewinterop.rememberUIKitInteropRemeasureRequester
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import platform.UIKit.UIColorWell
import platform.UIKit.UIControlEventValueChanged
import platform.UIKit.UIView
import platform.UIKit.accessibilityLabel
import platform.darwin.NSObject
import platform.objc.sel_registerName

/** Forwards `UIColorWell` selection changes to a Kotlin lambda (retained; UIControl targets are weak). */
@OptIn(BetaInteropApi::class)
private class ColorWellHandler : NSObject() {
    var control: UIColorWell? = null
    var onChange: (Color) -> Unit = {}

    /**
     * The last color the control and Compose agreed on — written by `update` when it pushes a value in, and
     * here when the picker reports one out. `update` only assigns `selectedColor` when the incoming value
     * differs from this: an unconditional write on every update races an in-flight picker drag (each change
     * round-trips through recomposition and lands back on the control mid-gesture).
     */
    var lastSynced: Color? = null

    @ObjCAction
    fun colorChanged() {
        control?.selectedColor?.let {
            val color = it.toComposeColor()
            lastSynced = color
            onChange(color)
        }
    }
}

/** iOS color well → a real `UIColorWell` (the system color picker). */
@OptIn(ExperimentalComposeUiApi::class, ExperimentalForeignApi::class)
@Composable
internal actual fun PlatformNativeColorWell(
    color: Color,
    onColorChange: (Color) -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    supportsAlpha: Boolean,
    contentDescription: String?,
    testTag: String?,
) {
    val handler = remember { ColorWellHandler() }
    handler.onChange = onColorChange

    val control = remember {
        UIColorWell().apply {
            addTarget(handler, sel_registerName("colorChanged"), UIControlEventValueChanged)
        }
    }
    handler.control = control
    // UIColorWell is a circular swatch; without a backing its transparent corners expose the host backdrop.
    val backing = remember { InteropBackingView() }
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
        update = {
            backing.backgroundColor = backingColor
            // Push the app-side color only when it isn't the one already synced (see ColorWellHandler.lastSynced);
            // a caller that transforms/rejects a pick still gets its value applied.
            if (color != handler.lastSynced) {
                control.selectedColor = color.toUIColor()
                handler.lastSynced = color
            }
            control.supportsAlpha = supportsAlpha
            control.enabled = enabled
            contentDescription?.let { control.accessibilityLabel = it }
            testTag?.let { control.setAccessibilityId(it) }
            // Fixed intrinsic size: measure once, on the first update (see InteropSizeFingerprint).
            sizeFp.requestIfChanged(Unit) { remeasure.requestRemeasure() }
        },
    )
}
