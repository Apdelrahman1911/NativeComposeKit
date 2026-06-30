package com.ukkera.brandkit.components

import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import androidx.compose.ui.viewinterop.remeasureRequester
import androidx.compose.ui.viewinterop.rememberUIKitInteropRemeasureRequester
import com.ukkera.brandkit.components.model.BrandIcon
import com.ukkera.brandkit.components.model.BrandInteropTouch
import com.ukkera.brandkit.components.model.BrandMenu
import com.ukkera.brandkit.components.model.ResolvedButtonStyle
import androidx.compose.ui.unit.dp
import kotlinx.cinterop.ExperimentalForeignApi
import platform.QuartzCore.kCALayerMaxXMaxYCorner
import platform.QuartzCore.kCALayerMaxXMinYCorner
import platform.QuartzCore.kCALayerMinXMaxYCorner
import platform.QuartzCore.kCALayerMinXMinYCorner
import platform.UIKit.NSLayoutConstraint
import platform.UIKit.UIStackView
import platform.UIKit.UIView
import platform.UIKit.accessibilityLabel

/**
 * iOS BrandSplitButton → two real `UIButton`s (primary + chevron) in a horizontal `UIStackView` inside a
 * theme-colored backing, reusing the shared [BrandButtonViews] scaffold. The outer corners of each
 * segment are rounded via `layer.maskedCorners` (left corners on primary, right corners on chevron) so
 * the pair reads as one rounded control with a hairline divider. The chevron presents the native `UIMenu`.
 */
@OptIn(ExperimentalForeignApi::class, ExperimentalComposeUiApi::class)
@Composable
internal actual fun PlatformBrandSplitButton(
    text: String,
    onPrimaryClick: () -> Unit,
    menu: BrandMenu,
    modifier: Modifier,
    style: ResolvedButtonStyle,
    enabled: Boolean,
    loading: Boolean,
    leadingIcon: BrandIcon?,
    touch: BrandInteropTouch,
    contentDescription: String?,
    testTag: String?,
) {
    val primary = remember { BrandButtonViews() }
    val chevron = remember { BrandButtonViews() }
    primary.tap.onClick = onPrimaryClick
    chevron.tap.onClick = {} // chevron presents the menu as its primary action

    val backing = remember { UIView() }
    val outer = remember { UIStackView() }
    val divider = remember { UIView() }
    val backingColor = interopBackingColor() // published surface on solid; clear on Liquid Glass
    val remeasure = rememberUIKitInteropRemeasureRequester()
    // Overlay placement inside a BrandDialog (no cut-out hole → no first-frame black flash); cut-out elsewhere.
    val overlay = LocalBrandInteropPlacement.current == BrandInteropPlacement.Overlay

    UIKitView(
        factory = {
            primary.build(style.iconSpacing.value.toDouble(), centered = false)
            chevron.build(style.iconSpacing.value.toDouble(), centered = true)
            // Round only the outer corners of each segment so the pair forms one rounded control.
            primary.button.layer.maskedCorners = kCALayerMinXMinYCorner or kCALayerMinXMaxYCorner
            chevron.button.layer.maskedCorners = kCALayerMaxXMinYCorner or kCALayerMaxXMaxYCorner
            // Outer stack defaults (horizontal, fill, fill, spacing 0) are exactly what we want.
            divider.translatesAutoresizingMaskIntoConstraints = false
            outer.addArrangedSubview(primary.button)
            outer.addArrangedSubview(divider)
            outer.addArrangedSubview(chevron.button)
            NSLayoutConstraint.activateConstraints(
                listOf(divider.widthAnchor.constraintEqualToConstant(1.0)),
            )
            backing.pinFilling(outer)
            backing
        },
        // HIG: ≥44pt touch target (both segments fill the host height).
        modifier = modifier.height(maxOf(style.height, 44.dp)).remeasureRequester(remeasure),
        properties = touch.toInteropProperties(overlay = overlay),
        update = { _ ->
            backing.backgroundColor = backingColor
            divider.backgroundColor = style.colors.content.copy(alpha = 0.3f).toUIColor()
            primary.apply(
                style = style,
                text = if (loading) "" else text,
                showLabel = true,
                enabled = enabled,
                loading = loading,
                leadName = leadingIcon?.sfSymbolName,
                trailName = null,
                menu = null,
            )
            chevron.apply(
                style = style,
                text = "",
                showLabel = false,
                enabled = enabled && !loading,
                loading = false,
                leadName = "chevron.down",
                trailName = null,
                menu = menu,
            )
            (contentDescription ?: text.takeIf { it.isNotBlank() })?.let { primary.button.accessibilityLabel = it }
            testTag?.let { backing.setAccessibilityId(it) }
            remeasure.requestRemeasure()
        },
    )
}
