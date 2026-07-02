package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import platform.UIKit.NSLayoutConstraint
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
    // onClick fires on tap; for a menu button the menu still opens (showsMenuAsPrimaryAction). Note iOS
    // suppresses the tap action while presenting the menu, so onClick may not fire for menu buttons there.
    views.tap.onClick = onClick

    val backing = remember { UIView() }
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
    // Re-measure only when something SIZE-affecting changes — never in `update`, which re-fires on every
    // scroll frame (bounds change) and would force a per-frame interop re-layout (the "drift"/"cut" bug).
    LaunchedEffect(text, loading, leadingIcon?.sfSymbolName, trailGlyph, style, fullWidth) {
        remeasure.requestRemeasure()
    }

    UIKitView(
        factory = {
            views.build(style.iconSpacing.value.toDouble())
            if (compact) {
                val b = views.button
                b.translatesAutoresizingMaskIntoConstraints = false
                backing.addSubview(b)
                NSLayoutConstraint.activateConstraints(
                    listOf(
                        b.leadingAnchor.constraintEqualToAnchor(backing.leadingAnchor),
                        b.trailingAnchor.constraintEqualToAnchor(backing.trailingAnchor),
                        b.centerYAnchor.constraintEqualToAnchor(backing.centerYAnchor),
                        b.heightAnchor.constraintEqualToConstant(style.height.value.toDouble()),
                    ),
                )
                b.minHitHeight = minTouch.value.toDouble()
            } else {
                backing.pinFilling(views.button)
            }
            backing
        },
        modifier = m.remeasureRequester(remeasure),
        properties = touch.toInteropProperties(overlay = overlay),
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
        },
    )
}
