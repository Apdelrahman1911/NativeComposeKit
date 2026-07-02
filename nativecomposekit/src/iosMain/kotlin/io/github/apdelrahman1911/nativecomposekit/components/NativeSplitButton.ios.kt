package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.viewinterop.UIKitView
import androidx.compose.ui.viewinterop.remeasureRequester
import androidx.compose.ui.viewinterop.rememberUIKitInteropRemeasureRequester
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeIcon
import io.github.apdelrahman1911.nativecomposekit.theme.LocalNativeStrings
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeInteropTouch
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeMenu
import io.github.apdelrahman1911.nativecomposekit.components.model.ResolvedButtonStyle
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
 * iOS NativeSplitButton → two real `UIButton`s (primary + chevron) in a horizontal `UIStackView` inside a
 * theme-colored backing, reusing the shared [NativeButtonViews] scaffold. The outer corners of each
 * segment are rounded via `layer.maskedCorners` — resolved against the effective layout direction (the
 * stack flips under RTL, corner masks don't) — so the pair reads as one rounded control with a hairline
 * divider. The chevron presents the native `UIMenu`.
 */
@OptIn(ExperimentalForeignApi::class, ExperimentalComposeUiApi::class)
@Composable
internal actual fun PlatformNativeSplitButton(
    text: String,
    onPrimaryClick: () -> Unit,
    menu: NativeMenu,
    modifier: Modifier,
    style: ResolvedButtonStyle,
    enabled: Boolean,
    loading: Boolean,
    leadingIcon: NativeIcon?,
    touch: NativeInteropTouch,
    contentDescription: String?,
    testTag: String?,
) {
    val primary = remember { NativeButtonViews() }
    val chevron = remember { NativeButtonViews() }
    primary.tap.onClick = onPrimaryClick
    chevron.tap.onClick = {} // chevron presents the menu as its primary action

    val backing = remember { UIView() }
    val outer = remember { UIStackView() }
    val divider = remember { UIView() }
    val backingColor = interopBackingColor() // published surface on solid; clear on Liquid Glass
    val remeasure = rememberUIKitInteropRemeasureRequester()
    // Overlay placement inside a NativeDialog (no cut-out hole → no first-frame black flash); cut-out elsewhere.
    val overlay = LocalNativeInteropPlacement.current == NativeInteropPlacement.Overlay
    val layoutDirection = LocalLayoutDirection.current
    val moreLabel = LocalNativeStrings.current.splitButtonMore

    val sizeFp = remember { InteropSizeFingerprint() }

    UIKitView(
        factory = {
            primary.build(style.iconSpacing.value.toDouble(), centered = false)
            chevron.build(style.iconSpacing.value.toDouble(), centered = true)
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
        properties = touch.toInteropProperties(overlay = overlay, nativeAccessibility = true),
        update = { _ ->
            backing.backgroundColor = backingColor
            divider.backgroundColor = style.colors.content.copy(alpha = 0.3f).toUIColor()
            // Round only the OUTER corners of each segment so the pair forms one rounded control. UIStackView
            // flips arrangement under RTL but layer corner masks don't follow — resolve them against the
            // effective layout direction (applied in update so a runtime direction flip re-rounds correctly).
            outer.semanticContentAttribute = layoutDirection.toUISemanticContentAttribute()
            val startCorners = kCALayerMinXMinYCorner or kCALayerMinXMaxYCorner
            val endCorners = kCALayerMaxXMinYCorner or kCALayerMaxXMaxYCorner
            val rtl = layoutDirection == LayoutDirection.Rtl
            primary.button.layer.maskedCorners = if (rtl) endCorners else startCorners
            chevron.button.layer.maskedCorners = if (rtl) startCorners else endCorners
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
            // The chevron is image-only — without a label VoiceOver announces an unnamed button.
            chevron.button.accessibilityLabel = moreLabel
            testTag?.let { backing.setAccessibilityId(it) }
            // Size-affecting inputs changed → re-measure, from update, after they're applied (see
            // InteropSizeFingerprint).
            sizeFp.requestIfChanged(listOf(text, loading, leadingIcon?.sfSymbolName, style)) {
                remeasure.requestRemeasure()
            }
        },
    )
}
