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
import platform.objc.sel_registerName

@OptIn(BetaInteropApi::class)
private class SwitchHandler : NSObject() {
    var onChange: (Boolean) -> Unit = {}
    var control: UISwitch? = null

    /** The value most recently applied by composition — the truth a rejected change snaps back to. */
    var lastComposed: Boolean = false

    /** Bumped by every composition update — lets the deferred re-assert see whether composition responded. */
    var updateGeneration: Long = 0L

    @ObjCAction
    fun valueChanged() {
        val genAtEvent = updateGeneration
        onChange(control?.on ?: false)
        // Controlled-rejection re-assert: the UISwitch self-mutates on a tap even when the consumer
        // ignores the change (state kept as-is), and a rejected change produces no recomposition to
        // correct it — the switch would sit flipped against the composed state forever. The check runs
        // AFTER the recomposition window and only acts when composition stayed silent: an ACCEPTED
        // change recomposes (bumping [updateGeneration]) and needs no correction here — a same-tick
        // re-assert used to act on the stale composed value and snap the switch backwards mid tap-morph,
        // corrupting the thumb/track animation. A rejected change visibly springs back instead.
        afterRecompositionWindow {
            val c = control ?: return@afterRecompositionWindow
            if (updateGeneration != genAtEvent) return@afterRecompositionWindow // composition responded — its update is authoritative
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
    val backing = remember { InteropBackingView() }
    val remeasure = rememberUIKitInteropRemeasureRequester()
    // Backing matches the published surface (so it doesn't show a box on a card/page) and is CLEAR on
    // Liquid Glass (so the switch capsule floats on the material instead of sitting on a solid rectangle).
    val backingColor = interopBackingColor()
    val sizeFp = remember { InteropSizeFingerprint() }

    InteropDisposeFailSafe(backing) // synchronous ghost-kill; see UiKitInterop.ios.kt
    UIKitView(
        factory = {
            // Cut-out geometry: 2pt bleed margin absorbs CMP's whole-pixel rounding of the interop
            // wrapper frame — edge-pinned, the capsule could overhang the canvas hole by up to 1px
            // and get its rounded edge shaved (see pinCenteredWithBleed).
            backing.pinCenteredWithBleed(control, bleed = 2.0)
            backing
        },
        modifier = modifier.remeasureRequester(remeasure).then(rememberInteropPositionHeal(backing)),
        // Cut-out placement on purpose: an overlay-placed switch composites above the WHOLE canvas —
        // in single-canvas hosts it drew over incoming routes during navigation transitions and over
        // floating chrome, and delayed position updates stranded it over the new screen. Cut-out keeps
        // it under the canvas; the brief scroll edge clip (interop-notes §1) is the lesser trade for a
        // control that lives in settings rows. Known limitation: an in-app theme flip can flash the
        // backing rect for 1-2 frames (UIKit repaints before the canvas presents) — see
        // docs/interop-notes.md §5 and docs/upstream/cmp-interop-transaction-lag.md.
        properties = scrollSafeInteropProperties(placedAsOverlay = false),
        update = { _ ->
            handler.updateGeneration++ // composition responded — disarms any pending rejection re-assert
            // Appearance setters rebuild the switch's layers and must not re-fire on the recomposition an
            // accepted tap triggers (it lands mid tap-morph) — assign only on an actual change.
            if (backing.backgroundColor?.isEqual(backingColor) != true) backing.backgroundColor = backingColor
            val interfaceStyle =
                if (style.surface.luminance() < 0.5f) UIUserInterfaceStyle.UIUserInterfaceStyleDark
                else UIUserInterfaceStyle.UIUserInterfaceStyleLight
            if (control.overrideUserInterfaceStyle != interfaceStyle) control.overrideUserInterfaceStyle = interfaceStyle
            val onTint = style.trackOnColor.toUIColor()
            if (control.onTintColor?.isEqual(onTint) != true) control.onTintColor = onTint
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
