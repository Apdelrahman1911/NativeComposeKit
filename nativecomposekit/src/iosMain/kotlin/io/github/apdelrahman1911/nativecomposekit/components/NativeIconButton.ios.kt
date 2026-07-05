package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.foundation.layout.size
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
 * iOS NativeIconButton → a square `UIButton` (via `UIKitView`) holding a single centered SF Symbol, reusing
 * the shared [NativeButtonViews] scaffold in `centered` mode. A [menu] presents a native `UIMenu` on tap.
 * A compact size (Small = 36pt) renders its true circle centered inside the ≥44pt host — filling the host
 * would render 44pt and break the side/2 circular radius — with the hit area expanded to the full host.
 */
@OptIn(ExperimentalForeignApi::class, ExperimentalComposeUiApi::class)
@Composable
internal actual fun PlatformNativeIconButton(
    icon: NativeIcon,
    onClick: () -> Unit,
    modifier: Modifier,
    style: ResolvedButtonStyle,
    enabled: Boolean,
    loading: Boolean,
    menu: NativeMenu?,
    touch: NativeInteropTouch,
    contentDescription: String?,
    testTag: String?,
) {
    val views = remember { NativeButtonViews() }
    // Menu-tap rule (same on Android): a menu button's tap only presents the menu
    // (showsMenuAsPrimaryAction); onClick is reserved for menu-less buttons.
    views.tap.onClick = if (menu != null) ({}) else onClick

    val backing = remember { InteropBackingView() }
    val backingColor = interopBackingColor() // published surface on solid; clear on Liquid Glass
    val remeasure = rememberUIKitInteropRemeasureRequester()
    // Overlay placement inside a NativeDialog (no cut-out hole → no first-frame black flash); cut-out elsewhere.
    val overlay = LocalNativeInteropPlacement.current == NativeInteropPlacement.Overlay

    // HIG: ≥44pt touch target. A compact visual keeps its true size, centered in a 44pt host square,
    // with MinHitButton accepting taps in the surrounding margin.
    val minTouch = 44.dp
    val compact = style.height < minTouch

    val sizeFp = remember { InteropSizeFingerprint() }

    InteropDisposeFailSafe(backing) // synchronous ghost-kill; see UiKitInterop.ios.kt
    UIKitView(
        factory = {
            views.build(style.iconSpacing.value.toDouble(), centered = true)
            if (compact) {
                views.mountCompactCentered(
                    host = backing,
                    heightPt = style.height.value.toDouble(),
                    minHitPt = minTouch.value.toDouble(),
                    fillWidth = false,
                )
            } else {
                backing.pinFilling(views.button)
            }
            // Seed the same content the first update will apply: the first Compose measure runs BEFORE
            // the first update (interop updates land at frame-present time), so a factory-fresh empty
            // button would measure 0×0 and can flash blank for a frame. update stays the idempotent
            // source of truth for every later change.
            views.apply(
                style = style,
                text = "",
                showLabel = false,
                enabled = enabled,
                loading = loading,
                leadName = icon.sfSymbolName,
                trailName = null,
                menu = menu,
            )
            backing
        },
        modifier = modifier.size(if (compact) minTouch else style.height).remeasureRequester(remeasure).then(rememberInteropPositionHeal(backing)),
        properties = touch.toInteropProperties(overlay = overlay, nativeAccessibility = true),
        update = { _ ->
            backing.backgroundColor = backingColor
            views.apply(
                style = style,
                text = "",
                showLabel = false,
                enabled = enabled,
                loading = loading,
                leadName = icon.sfSymbolName, // single glyph occupies the leading slot, centered
                trailName = null,
                menu = menu,
            )
            (contentDescription ?: icon.contentDescription)?.let { views.button.accessibilityLabel = it }
            testTag?.let { views.button.setAccessibilityId(it) }
            // Size-affecting inputs changed → re-measure, from update, after they're applied (see
            // InteropSizeFingerprint).
            sizeFp.requestIfChanged(listOf(icon.sfSymbolName, style)) { remeasure.requestRemeasure() }
        },
        // The released button must stop dispatching into the tap/press handlers once the node has left
        // the composition for good.
        onRelease = { views.detach() },
    )
}
