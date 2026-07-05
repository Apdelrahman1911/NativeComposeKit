package io.github.apdelrahman1911.nativecomposekit.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import io.github.apdelrahman1911.nativecomposekit.theme.nativeBackgroundUIColor
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.NSForegroundColorAttributeName
import platform.UIKit.UIBarButtonItem
import platform.UIKit.UIBarButtonItemStyle
import platform.UIKit.UIColor
import platform.Foundation.NSNumber
import platform.Foundation.NSSelectorFromString
import platform.Foundation.numberWithBool
import platform.Foundation.setValue
import platform.UIKit.UIImage
import platform.UIKit.UINavigationBar
import platform.UIKit.UINavigationBarAppearance
import platform.UIKit.UINavigationItem
import platform.UIKit.UITabBar
import platform.UIKit.UITabBarAppearance
import platform.UIKit.UITabBarItem
import platform.UIKit.UIUserInterfaceStyle
import platform.UIKit.colorWithDynamicProvider
import platform.UIKit.labelColor

// Preview-local Color→UIColor bridge (the kit's converter is internal by design).
private fun Color.toPreviewUIColor(): UIColor =
    UIColor(red = red.toDouble(), green = green.toDouble(), blue = blue.toDouble(), alpha = alpha.toDouble())

// iOS 26 wraps every UIBarButtonItem (even custom-view ones) in a Liquid Glass capsule; a standalone
// clipped preview bar gives the capsule's backdrop sampling nothing to sample, so it degrades into a
// washed-out disc. UIKit's per-item opt-out is `hidesSharedBackground` — set via KVC behind a selector
// check so this still compiles/runs against pre-26 SDK bindings. Previews therefore show the classic
// flat icons; the app's real bars and the live pushed demos show the true glass treatment.
@OptIn(ExperimentalForeignApi::class)
private fun flatPreviewItem(symbol: String): UIBarButtonItem {
    val item = UIBarButtonItem(
        image = UIImage.systemImageNamed(symbol),
        style = UIBarButtonItemStyle.UIBarButtonItemStylePlain,
        target = null,
        action = null,
    )
    if (item.respondsToSelector(NSSelectorFromString("setHidesSharedBackground:"))) {
        item.setValue(NSNumber.numberWithBool(true), forKey = "hidesSharedBackground")
    }
    return item
}

/**
 * A real `UINavigationBar` as an inline, non-interactive preview. Raw `UIKitView` on purpose (display-only
 * demo; the kit's scroll-safe backing machinery is for its own components) — a slight scroll lag on this
 * debug page is acceptable and documented behavior for raw interop.
 */
@OptIn(ExperimentalComposeUiApi::class, ExperimentalForeignApi::class)
@Composable
actual fun IosNavBarPreview(
    title: String,
    modifier: Modifier,
    background: IosPreviewBackground,
    customBackground: Color?,
    tint: Color?,
    actionSymbols: List<String>,
    showsBack: Boolean,
    hairline: Boolean,
) {
    val bar = remember { UINavigationBar() }
    // The 44pt bar sits centered in a padded frame; for a Custom background the frame is painted in the
    // same color, so the preview block has even breathing room above AND below the bar content.
    val frameModifier =
        if (background == IosPreviewBackground.Custom && customBackground != null) {
            modifier.fillMaxWidth().background(customBackground)
        } else {
            modifier.fillMaxWidth()
        }
    Box(frameModifier.padding(vertical = 10.dp)) {
        UIKitView(
        factory = {
            bar.userInteractionEnabled = false
            // A standalone UINavigationBar paints its background UPWARD past its bounds (the status-bar
            // extension); unclipped that bleeds above the preview slot as fake top padding.
            bar.clipsToBounds = true
            bar
        },
        modifier = Modifier.fillMaxWidth().height(44.dp),
        properties = UIKitInteropProperties(interactionMode = null, placedAsOverlay = true),
        update = { _ ->
            val appearance = UINavigationBarAppearance()
            if (background == IosPreviewBackground.Material) {
                appearance.configureWithDefaultBackground()
            } else {
                appearance.configureWithOpaqueBackground()
                appearance.backgroundColor =
                    if (background == IosPreviewBackground.Custom && customBackground != null) {
                        customBackground.toPreviewUIColor()
                    } else {
                        UIColor.colorWithDynamicProvider { traits ->
                            nativeBackgroundUIColor(
                                dark = traits?.userInterfaceStyle == UIUserInterfaceStyle.UIUserInterfaceStyleDark,
                            )
                        }
                    }
            }
            if (!hairline) appearance.shadowColor = UIColor.clearColor()
            appearance.titleTextAttributes = mapOf<Any?, Any?>(NSForegroundColorAttributeName to UIColor.labelColor())
            bar.standardAppearance = appearance
            bar.compactAppearance = appearance
            bar.scrollEdgeAppearance = appearance
            tint?.let { bar.tintColor = it.toPreviewUIColor() }

            val item = UINavigationItem(title = title)
            item.rightBarButtonItems = actionSymbols.map { symbol -> flatPreviewItem(symbol) }
            if (showsBack) {
                item.leftBarButtonItems = listOf(flatPreviewItem("chevron.left"))
            }
            bar.setItems(listOf(item), animated = false)
        },
        )
    }
}

/** A real `UITabBar` as an inline, non-interactive preview. */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun IosTabBarPreview(
    modifier: Modifier,
    selectedColor: Color?,
    unselectedColor: Color?,
    tint: Color?,
) {
    val bar = remember { UITabBar() }
    UIKitView(
        factory = {
            bar.userInteractionEnabled = false
            bar.clipsToBounds = true // same out-of-bounds background painting as the nav bar
            val items = listOf(
                UITabBarItem(title = "Components", image = UIImage.systemImageNamed("square.grid.2x2"), tag = 0),
                UITabBarItem(title = "Library", image = UIImage.systemImageNamed("books.vertical"), tag = 1),
                UITabBarItem(title = "Settings", image = UIImage.systemImageNamed("gearshape"), tag = 2),
            )
            bar.setItems(items, animated = false)
            bar.selectedItem = items.first()
            bar
        },
        // A bare UITabBar needs room for the iOS 26 pill's icon-above-label layout — squeezed below
        // ~84pt the labels collapse onto the icons.
        modifier = modifier.fillMaxWidth().height(84.dp),
        properties = UIKitInteropProperties(interactionMode = null, placedAsOverlay = true),
        update = { _ ->
            val appearance = UITabBarAppearance()
            appearance.configureWithDefaultBackground()
            selectedColor?.toPreviewUIColor()?.let { sel ->
                appearance.stackedLayoutAppearance.selected.iconColor = sel
                appearance.stackedLayoutAppearance.selected.titleTextAttributes =
                    mapOf<Any?, Any?>(NSForegroundColorAttributeName to sel)
            }
            unselectedColor?.toPreviewUIColor()?.let { unsel ->
                appearance.stackedLayoutAppearance.normal.iconColor = unsel
                appearance.stackedLayoutAppearance.normal.titleTextAttributes =
                    mapOf<Any?, Any?>(NSForegroundColorAttributeName to unsel)
            }
            bar.standardAppearance = appearance
            tint?.let { bar.tintColor = it.toPreviewUIColor() }
        },
    )
}
