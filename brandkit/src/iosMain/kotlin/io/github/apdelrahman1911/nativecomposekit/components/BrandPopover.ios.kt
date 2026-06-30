package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.ExperimentalComposeApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ComposeUIViewController
import io.github.apdelrahman1911.nativecomposekit.theme.BrandTheme
import io.github.apdelrahman1911.nativecomposekit.theme.LocalBrandStatusColors
import io.github.apdelrahman1911.nativecomposekit.theme.LocalBrandTokens
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSValue
import platform.Foundation.setValue
import platform.Foundation.valueForKey
import platform.UIKit.UIColor
import platform.UIKit.UIPopoverPresentationControllerDelegateProtocol
import platform.UIKit.UIPresentationController
import platform.UIKit.UIUserInterfaceStyle
import platform.UIKit.UIViewController
import platform.UIKit.valueWithCGRect
import platform.UIKit.valueWithCGSize
import platform.darwin.NSObject

/** Fires [onDismiss] on an interactive popover dismissal (outside tap). Retained — the delegate is weak. */
@OptIn(BetaInteropApi::class)
private class PopoverDismissDelegate(val onDismiss: () -> Unit) :
    NSObject(), UIPopoverPresentationControllerDelegateProtocol {
    override fun presentationControllerDidDismiss(presentationController: UIPresentationController) = onDismiss()
}

/** Idempotent dismisser; retains the presentation delegate for the popover's lifetime. */
private class PopoverHandle(
    private val vc: UIViewController?,
    @Suppress("unused") private val delegate: Any?,
) {
    private var dismissed = false
    fun dismiss() {
        if (dismissed) return
        dismissed = true
        vc?.dismissViewControllerAnimated(true, null)
    }
}

@OptIn(ExperimentalForeignApi::class, ExperimentalComposeUiApi::class, ExperimentalComposeApi::class)
private fun presentPopover(
    anchorRectPt: DoubleArray?, // [x, y, w, h] in points, in the presenter view's space; null → centre fallback
    dark: Boolean,
    testTag: String?,
    onDismiss: () -> Unit,
    themedContent: @Composable () -> Unit,
): PopoverHandle {
    // Safe fallback: no presenter → don't present (never crash).
    val presenter = topmostUIViewController() ?: return PopoverHandle(null, null)
    val vc = ComposeUIViewController(configure = { opaque = false }) { themedContent() }
    vc.view.backgroundColor = UIColor.clearColor()
    testTag?.let { vc.view.setAccessibilityId(it) }
    vc.overrideUserInterfaceStyle =
        if (dark) UIUserInterfaceStyle.UIUserInterfaceStyleDark else UIUserInterfaceStyle.UIUserInterfaceStyleLight
    // preferredContentSize + modalPresentationStyle via KVC: this K/N version exposes preferredContentSize as
    // read-only and doesn't resolve the UIModalPresentationStyle entry, so set both via KVC (the kit already
    // drives popoverPresentationController by KVC). 7 == UIModalPresentationPopover. A sensible default size for
    // the iPad arrowed popover (content scrolls if larger); compact width adapts to full screen.
    vc.setValue(NSValue.valueWithCGSize(CGSizeMake(300.0, 320.0)), forKey = "preferredContentSize")
    vc.setValue(7, forKey = "modalPresentationStyle")
    // popoverPresentationController is non-null only for the .popover style and isn't bound as a static member
    // in this K/N version → reach it via KVC, matching feedback/IosNativeModals.ios.kt's anchor pattern.
    val popover = vc.valueForKey("popoverPresentationController") as? NSObject
    val delegate = PopoverDismissDelegate(onDismiss)
    if (popover != null) {
        popover.setValue(presenter.view, forKey = "sourceView")
        val rect = anchorRectPt ?: presenter.view.bounds.useContents {
            doubleArrayOf(size.width / 2.0, size.height / 2.0, 0.0, 0.0) // centre of the presenter view
        }
        popover.setValue(
            NSValue.valueWithCGRect(CGRectMake(rect[0], rect[1], rect[2], rect[3])),
            forKey = "sourceRect",
        )
        popover.setValue(delegate, forKey = "delegate")
        // permittedArrowDirections left at its default (.any) — UIKit chooses; iPhone adapts naturally.
    }
    presenter.presentViewController(vc, animated = true, completion = null)
    return PopoverHandle(vc, delegate)
}

/**
 * iOS BrandPopover — phone-first split (Group-3 cleanup):
 * - **iPad / regular width** → a native `UIPopoverPresentationController` (arrowed), anchored to the [anchor]'s
 *   measured on-screen rect (`boundsInWindow`, px → points); [content] is hosted in a separate, theme-re-provided
 *   `ComposeUIViewController` (mirrors BrandSheet). Falls back to a centred presentation if no anchor/presenter.
 * - **iPhone / compact width** → the shared [ComposeBrandPopover] (a lightweight `BrandCard` in a `Popup`
 *   anchored to the button) — avoids UIKit's full-screen popover adaptation; better phone UX, no interop hole.
 * Compact vs regular is decided by window width (`>= 600.dp` ⇒ regular, the Material compact breakpoint).
 */
@OptIn(ExperimentalForeignApi::class, ExperimentalComposeUiApi::class)
@Composable
internal actual fun PlatformBrandPopover(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier,
    alignment: Alignment, // positions the compact Compose popover; a hint on the iPad native path
    testTag: String?,
    anchor: (@Composable () -> Unit)?,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val regularWidth = with(density) { LocalWindowInfo.current.containerSize.width.toDp() } >= 600.dp
    if (!regularWidth) {
        // iPhone / compact → polished Compose popover (no full-screen takeover; pure Compose, no backdrop artifact).
        ComposeBrandPopover(visible, onDismissRequest, modifier, alignment, testTag, anchor, content)
        return
    }

    // iPad / regular → native UIPopoverPresentationController.
    val currentOnDismiss by rememberUpdatedState(onDismissRequest)
    val anchorRect = remember { mutableStateOf<DoubleArray?>(null) }

    // Render the anchor inline (in layout) so we can measure its on-screen rect for the popover's sourceRect.
    if (anchor != null) {
        Box(
            Modifier.onGloballyPositioned { coords ->
                val b = coords.boundsInWindow()
                val s = density.density
                anchorRect.value = doubleArrayOf(
                    (b.left / s).toDouble(),
                    (b.top / s).toDouble(),
                    (b.width / s).toDouble(),
                    (b.height / s).toDouble(),
                )
            },
        ) { anchor() }
    }

    // The hosted ComposeUIViewController runs a SEPARATE composition (no inherited CompositionLocals): capture
    // the parent theme + brand locals + layout direction and re-provide them inside (mirrors BrandSheet).
    val scheme = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val shapes = MaterialTheme.shapes
    val layoutDir = LocalLayoutDirection.current
    val tokens = LocalBrandTokens.current
    val statusColors = LocalBrandStatusColors.current
    val dark = scheme.surface.luminance() < 0.5f

    val themedContent: @Composable () -> Unit = {
        MaterialTheme(colorScheme = scheme, shapes = shapes, typography = typography) {
            CompositionLocalProvider(
                LocalLayoutDirection provides layoutDir,
                LocalBrandTokens provides tokens,
                LocalBrandStatusColors provides statusColors,
            ) {
                // Column (not Box) so multiple content composables stack vertically, matching the Android
                // BrandCard's ColumnScope — a Box would overlap them on the z-axis. Padded like the Android card.
                Column(Modifier.padding(BrandTheme.tokens.spacingMd)) { content() }
            }
        }
    }

    DisposableEffect(visible) {
        val handle = if (visible) {
            presentPopover(anchorRect.value, dark, testTag, onDismiss = { currentOnDismiss() }, themedContent)
        } else {
            null
        }
        onDispose { handle?.dismiss() }
    }
}
