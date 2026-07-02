package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.viewinterop.UIKitView
import androidx.compose.ui.viewinterop.remeasureRequester
import androidx.compose.ui.viewinterop.rememberUIKitInteropRemeasureRequester
import io.github.apdelrahman1911.nativecomposekit.components.model.ResolvedToggleStyle
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import platform.UIKit.UIControlEventValueChanged
import platform.UIKit.UISwitch
import platform.UIKit.UIUserInterfaceStyle
import platform.UIKit.UIView
import platform.UIKit.accessibilityLabel
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import platform.objc.sel_registerName

@OptIn(BetaInteropApi::class)
private class SwitchHandler : NSObject() {
    var onChange: (Boolean) -> Unit = {}
    var control: UISwitch? = null

    /** The value most recently applied by composition — the truth a rejected change snaps back to. */
    var lastComposed: Boolean = false

    @ObjCAction
    fun valueChanged() {
        onChange(control?.on ?: false)
        // Controlled-rejection re-assert: the UISwitch self-mutates on a tap even when the consumer
        // ignores the change (state kept as-is), and a rejected change produces no recomposition to
        // correct it — the switch would sit flipped against the composed state forever. On the next
        // main-queue tick, snap back to the last composed value: an accepted change recomposes and
        // re-applies the new value within a frame (the animated set makes that window invisible); a
        // rejected one visibly springs back.
        dispatch_async(dispatch_get_main_queue()) {
            val c = control ?: return@dispatch_async
            if (c.on != lastComposed) c.setOn(lastComposed, animated = true)
        }
    }
}

/**
 * iOS NativeToggle → a real `UISwitch` (the native green pill) pinned inside a theme-colored backing
 * so its rounded shape blends in dark mode. The on-track color comes from [style]; the thumb stays
 * the native white.
 *
 * The host keeps the `UISwitch`'s native intrinsic size (~51×31pt) — smaller than the 44pt touch
 * minimum by design: per Apple's own Settings pattern, the surrounding **row** supplies the ≥44pt hit
 * area (the caller's layout concern), and stretching the control itself would distort it.
 */
@OptIn(ExperimentalComposeUiApi::class, ExperimentalForeignApi::class)
@Composable
internal actual fun PlatformNativeToggle(
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
            control.onTintColor = style.trackOnColor.toUIColor()
            handler.lastComposed = checked // the composed truth the rejection re-assert snaps back to
            if (control.on != checked) control.setOn(checked, animated = false)
            control.enabled = enabled
            // null callback = read-only: keep the switch full-color but non-interactive (vs `enabled = false`,
            // which greys it). A disabled switch is already non-interactive regardless.
            control.userInteractionEnabled = enabled && onCheckedChange != null
            contentDescription?.let { control.accessibilityLabel = it }
            testTag?.let { control.setAccessibilityId(it) }
            // Fixed intrinsic size: measure once, on the first update — never per update, which re-fires on
            // every scroll frame and would desync the interop layout (see InteropSizeFingerprint).
            sizeFp.requestIfChanged(Unit) { remeasure.requestRemeasure() }
        },
        // The released switch must stop dispatching into the handler once the node has left the
        // composition for good.
        onRelease = {
            control.removeTarget(handler, sel_registerName("valueChanged"), UIControlEventValueChanged)
            handler.control = null
        },
    )
}
