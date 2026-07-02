package io.github.apdelrahman1911.nativecomposekit.components

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
 * iOS NativeIconButton → a square `UIButton` (via `UIKitView`) holding a single centered SF Symbol, reusing
 * the shared [NativeButtonViews] scaffold in `centered` mode. A [menu] presents a native `UIMenu` on tap.
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
    views.tap.onClick = onClick // fires on tap; a menu (if any) still opens via showsMenuAsPrimaryAction

    val backing = remember { UIView() }
    val backingColor = interopBackingColor() // published surface on solid; clear on Liquid Glass
    val remeasure = rememberUIKitInteropRemeasureRequester()
    // Overlay placement inside a NativeDialog (no cut-out hole → no first-frame black flash); cut-out elsewhere.
    val overlay = LocalNativeInteropPlacement.current == NativeInteropPlacement.Overlay

    val sizeFp = remember { InteropSizeFingerprint() }

    UIKitView(
        factory = {
            views.build(style.iconSpacing.value.toDouble(), centered = true)
            backing.pinFilling(views.button)
            backing
        },
        // HIG: ≥44pt touch target. The square icon button fills the host, so clamp its height to ≥44.
        modifier = modifier.height(maxOf(style.height, 44.dp)).remeasureRequester(remeasure),
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
    )
}
