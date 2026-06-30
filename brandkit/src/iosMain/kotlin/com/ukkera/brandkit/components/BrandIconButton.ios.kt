package com.ukkera.brandkit.components

import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitView
import androidx.compose.ui.viewinterop.remeasureRequester
import androidx.compose.ui.viewinterop.rememberUIKitInteropRemeasureRequester
import com.ukkera.brandkit.components.model.BrandIcon
import com.ukkera.brandkit.components.model.BrandInteropTouch
import com.ukkera.brandkit.components.model.BrandMenu
import com.ukkera.brandkit.components.model.ResolvedButtonStyle
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIView
import platform.UIKit.accessibilityLabel

/**
 * iOS BrandIconButton → a square `UIButton` (via `UIKitView`) holding a single centered SF Symbol, reusing
 * the shared [BrandButtonViews] scaffold in `centered` mode. A [menu] presents a native `UIMenu` on tap.
 */
@OptIn(ExperimentalForeignApi::class, ExperimentalComposeUiApi::class)
@Composable
internal actual fun PlatformBrandIconButton(
    icon: BrandIcon,
    onClick: () -> Unit,
    modifier: Modifier,
    style: ResolvedButtonStyle,
    enabled: Boolean,
    loading: Boolean,
    menu: BrandMenu?,
    touch: BrandInteropTouch,
    contentDescription: String?,
    testTag: String?,
) {
    val views = remember { BrandButtonViews() }
    views.tap.onClick = onClick // fires on tap; a menu (if any) still opens via showsMenuAsPrimaryAction

    val backing = remember { UIView() }
    val backingColor = interopBackingColor() // published surface on solid; clear on Liquid Glass
    val remeasure = rememberUIKitInteropRemeasureRequester()
    // Overlay placement inside a BrandDialog (no cut-out hole → no first-frame black flash); cut-out elsewhere.
    val overlay = LocalBrandInteropPlacement.current == BrandInteropPlacement.Overlay

    UIKitView(
        factory = {
            views.build(style.iconSpacing.value.toDouble(), centered = true)
            backing.pinFilling(views.button)
            backing
        },
        // HIG: ≥44pt touch target. The square icon button fills the host, so clamp its height to ≥44.
        modifier = modifier.height(maxOf(style.height, 44.dp)).remeasureRequester(remeasure),
        properties = touch.toInteropProperties(overlay = overlay),
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
            remeasure.requestRemeasure()
        },
    )
}
