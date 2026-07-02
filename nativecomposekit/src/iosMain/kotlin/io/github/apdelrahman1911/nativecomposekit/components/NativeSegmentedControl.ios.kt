package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.viewinterop.UIKitView
import androidx.compose.ui.viewinterop.remeasureRequester
import androidx.compose.ui.viewinterop.rememberUIKitInteropRemeasureRequester
import io.github.apdelrahman1911.nativecomposekit.components.model.ResolvedSegmentedStyle
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import platform.UIKit.NSFontAttributeName
import platform.UIKit.NSForegroundColorAttributeName
import platform.UIKit.UIApplication
import platform.UIKit.UIControlEventValueChanged
import platform.UIKit.UIControlStateNormal
import platform.UIKit.UIControlStateSelected
import platform.UIKit.UISegmentedControl
import platform.UIKit.UIUserInterfaceStyle
import platform.UIKit.UIView
import platform.UIKit.accessibilityLabel
import platform.darwin.NSObject
import platform.objc.sel_registerName

@OptIn(BetaInteropApi::class)
private class SegmentedHandler : NSObject() {
    var onSelect: (Int) -> Unit = {}
    var control: UISegmentedControl? = null

    @ObjCAction
    fun valueChanged() = control?.let { onSelect(it.selectedSegmentIndex.toInt()) } ?: Unit
}

/** Inserts or renames segments to match [options]. Idempotent — shared by the factory seed and update. */
private fun syncSegmentTitles(control: UISegmentedControl, options: List<String>) {
    if (control.numberOfSegments.toInt() != options.size) {
        control.removeAllSegments()
        options.forEachIndexed { i, title ->
            control.insertSegmentWithTitle(title, i.toULong(), false)
        }
    } else {
        // Same count ≠ same titles (a locale switch or dynamic labels change text, not count):
        // sync each title in place so the segments never go stale.
        options.forEachIndexed { i, title ->
            if (control.titleForSegmentAtIndex(i.toULong()) != title) {
                control.setTitle(title, forSegmentAtIndex = i.toULong())
            }
        }
    }
}

/**
 * Applies the resolved title colors and the scaled brand font to both control states. Dynamic Type:
 * `toUIFont` scales via `UIFontMetrics`, so the result depends on the user's current text-size setting
 * as well as [style] — callers gate this on both (see the update block).
 */
private fun applySegmentTitleAttributes(control: UISegmentedControl, style: ResolvedSegmentedStyle) {
    val titleFont = style.textStyle.toUIFont()
    control.setTitleTextAttributes(
        mapOf<Any?, Any?>(
            NSForegroundColorAttributeName to style.textColor.toUIColor(),
            NSFontAttributeName to titleFont,
        ),
        UIControlStateNormal,
    )
    control.setTitleTextAttributes(
        mapOf<Any?, Any?>(
            NSForegroundColorAttributeName to style.selectedTextColor.toUIColor(),
            NSFontAttributeName to titleFont,
        ),
        UIControlStateSelected,
    )
}

/**
 * iOS NativeSegmentedControl → a real `UISegmentedControl` pinned inside a theme-colored backing.
 * The selected segment uses [style.selectedColor]; segment labels use the resolved title colors.
 */
@OptIn(ExperimentalComposeUiApi::class, ExperimentalForeignApi::class)
@Composable
internal actual fun PlatformNativeSegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    style: ResolvedSegmentedStyle,
    contentDescription: String?,
    testTag: String?,
) {
    val handler = remember { SegmentedHandler() }
    handler.onSelect = onSelectedIndexChange

    val control = remember {
        UISegmentedControl().apply {
            addTarget(handler, sel_registerName("valueChanged"), UIControlEventValueChanged)
        }
    }
    handler.control = control
    val backing = remember { UIView() }
    val backingColor = interopBackingColor() // published surface on solid; clear on Liquid Glass
    val remeasure = rememberUIKitInteropRemeasureRequester()
    val sizeFp = remember { InteropSizeFingerprint() }
    // Rebuilding the title attributes derives fresh UIFonts/UIColors — gate it on its actual inputs so
    // per-frame update re-runs (scrolling changes bounds) don't re-derive them every frame.
    val styleFp = remember { InteropSizeFingerprint() }

    UIKitView(
        factory = {
            // Seed the segments + styling the first update would apply: the first Compose measure runs
            // BEFORE the first update (interop updates land at frame-present time), so a factory-fresh
            // segmentless control would measure 0×0 and can flash blank for a frame. update stays the
            // idempotent source of truth for every later change.
            syncSegmentTitles(control, options)
            applySegmentTitleAttributes(control, style)
            if (control.selectedSegmentIndex.toInt() != selectedIndex) {
                control.selectedSegmentIndex = selectedIndex.toLong()
            }
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
            syncSegmentTitles(control, options)
            control.selectedSegmentTintColor = style.selectedColor.toUIColor()
            // The current Dynamic Type category is part of the fingerprint because toUIFont scales with
            // it — a mid-session text-size change must re-derive the fonts even though style is unchanged.
            styleFp.requestIfChanged(
                listOf(
                    style.textColor,
                    style.selectedTextColor,
                    style.textStyle,
                    UIApplication.sharedApplication.preferredContentSizeCategory,
                ),
            ) { applySegmentTitleAttributes(control, style) }
            if (control.selectedSegmentIndex.toInt() != selectedIndex) {
                control.selectedSegmentIndex = selectedIndex.toLong()
            }
            control.enabled = enabled
            control.accessibilityLabel = contentDescription // segment names + selection announced natively
            testTag?.let { control.setAccessibilityId(it) }
            // The segment titles and their font drive the width → re-measure when they change, from update,
            // after they're applied (see InteropSizeFingerprint).
            sizeFp.requestIfChanged(listOf(options, style.textStyle)) { remeasure.requestRemeasure() }
        },
        // The released control must stop dispatching into the handler once the node has left the
        // composition for good.
        onRelease = {
            control.removeTarget(handler, sel_registerName("valueChanged"), UIControlEventValueChanged)
            handler.control = null
        },
    )
}
