package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitView
import androidx.compose.ui.viewinterop.remeasureRequester
import androidx.compose.ui.viewinterop.rememberUIKitInteropRemeasureRequester
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeIcon
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeInteropTouch
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeMenu
import io.github.apdelrahman1911.nativecomposekit.components.model.ResolvedButtonStyle
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIView
import platform.UIKit.accessibilityLabel

/**
 * iOS NativeButton → a real `UIButton` (via `UIKitView`) whose content is a themed stack. A leading icon,
 * a trailing icon, or both render in their true positions (no forced-RTL hack); a [menu] makes the tap
 * present a native `UIMenu` and appends a chevron. Loading shows a native spinner. The rounded button is
 * pinned inside a theme-colored backing so its corners blend in dark mode. All values come from [style].
 */
@OptIn(ExperimentalForeignApi::class, ExperimentalComposeUiApi::class)
@Composable
internal actual fun PlatformNativeButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier,
    style: ResolvedButtonStyle,
    enabled: Boolean,
    loading: Boolean,
    fullWidth: Boolean,
    leadingIcon: NativeIcon?,
    trailingIcon: NativeIcon?,
    menu: NativeMenu?,
    touch: NativeInteropTouch,
    contentDescription: String?,
    testTag: String?,
) {
    val views = remember { NativeButtonViews() }
    // Menu-tap rule (same on Android): a menu button's tap only presents the menu
    // (showsMenuAsPrimaryAction); onClick is reserved for menu-less buttons. Wiring that structurally
    // beats relying on iOS suppressing the tap action while presenting.
    views.tap.onClick = if (menu != null) ({}) else onClick

    val backing = remember { InteropBackingView() }
    val remeasure = rememberUIKitInteropRemeasureRequester()

    // HIG: the touch target must be ≥44pt. When the visual height is smaller (Small = 36), host a ≥44pt
    // interop region and center the compact visual in it; MinHitButton expands the hit area to fill the host.
    val minTouch = 44.dp
    val compact = style.height < minTouch
    var m = modifier.height(if (compact) minTouch else style.height)
    if (fullWidth) m = m.fillMaxWidth()
    val backingColor = interopBackingColor() // published surface on solid; clear on Liquid Glass
    // Overlay placement inside a NativeDialog (no cut-out hole → no first-frame black flash); cut-out elsewhere.
    val overlay = LocalNativeInteropPlacement.current == NativeInteropPlacement.Overlay

    // Trailing glyph is an explicit trailing icon, or the auto chevron for a menu button.
    val trailGlyph = trailingIcon?.sfSymbolName ?: if (menu != null) "chevron.down" else null
    val sizeFp = remember { InteropSizeFingerprint() }

    UIKitView(
        factory = {
            views.build(style.iconSpacing.value.toDouble())
            if (compact) {
                views.mountCompactCentered(
                    host = backing,
                    heightPt = style.height.value.toDouble(),
                    minHitPt = minTouch.value.toDouble(),
                    fillWidth = true,
                )
            } else {
                backing.pinFilling(views.button)
            }
            // Seed the same content the first update will apply: the first Compose measure runs BEFORE
            // the first update (interop updates land at frame-present time), so a factory-fresh empty
            // button would measure 0×0 and can flash blank for a frame (aborted Metal frames widen the
            // window). update stays the idempotent source of truth for every later change.
            views.apply(
                style = style,
                text = if (loading) "" else text,
                showLabel = true,
                enabled = enabled,
                loading = loading,
                leadName = leadingIcon?.sfSymbolName,
                trailName = trailGlyph,
                menu = menu,
            )
            backing
        },
        modifier = m.remeasureRequester(remeasure),
        properties = touch.toInteropProperties(overlay = overlay, nativeAccessibility = true),
        update = { _ ->
            backing.backgroundColor = backingColor
            views.apply(
                style = style,
                text = if (loading) "" else text,
                showLabel = true,
                enabled = enabled,
                loading = loading,
                leadName = leadingIcon?.sfSymbolName,
                trailName = trailGlyph,
                menu = menu,
            )
            (contentDescription ?: text.takeIf { it.isNotBlank() }
                ?: leadingIcon?.contentDescription ?: trailingIcon?.contentDescription)
                ?.let { views.button.accessibilityLabel = it }
            testTag?.let { views.button.setAccessibilityId(it) }
            // Size-affecting inputs changed → re-measure, requested from update so it always lands AFTER
            // they were applied (see InteropSizeFingerprint).
            sizeFp.requestIfChanged(listOf(text, loading, leadingIcon?.sfSymbolName, trailGlyph, style, fullWidth)) {
                remeasure.requestRemeasure()
            }
        },
        // The released button must stop dispatching into the tap/press handlers once the node has left
        // the composition for good.
        onRelease = { views.detach() },
    )
}
