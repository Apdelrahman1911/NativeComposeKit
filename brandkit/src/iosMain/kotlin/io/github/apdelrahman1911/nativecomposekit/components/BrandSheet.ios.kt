package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ExperimentalComposeApi
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.window.ComposeUIViewController
import io.github.apdelrahman1911.nativecomposekit.theme.LocalBrandStatusColors
import io.github.apdelrahman1911.nativecomposekit.theme.LocalBrandTokens
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.valueForKey
import platform.UIKit.UIAdaptivePresentationControllerDelegateProtocol
import platform.UIKit.UIColor
import platform.UIKit.UIPresentationController
import platform.UIKit.UISheetPresentationController
import platform.UIKit.UISheetPresentationControllerDetent
import platform.UIKit.UIUserInterfaceStyle
import platform.UIKit.UIViewController
import platform.darwin.NSObject

/** Fires [onDismiss] when the user swipes the sheet down (retained — `UIPresentationController.delegate` is weak). */
@OptIn(BetaInteropApi::class)
private class SheetDismissDelegate(val onDismiss: () -> Unit) : NSObject(), UIAdaptivePresentationControllerDelegateProtocol {
    override fun presentationControllerDidDismiss(presentationController: UIPresentationController) = onDismiss()
}

/** Idempotent dismisser; retains the presentation delegate for the sheet's lifetime. */
private class SheetHandle(
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
private fun presentSheet(
    detents: List<BrandSheetDetent>,
    showDragHandle: Boolean,
    dark: Boolean,
    testTag: String?,
    onSwipeDismiss: () -> Unit,
    themedContent: @Composable () -> Unit,
): SheetHandle {
    val presenter = topmostUIViewController() ?: return SheetHandle(null, null)
    // Transparent host (opaque = false + clear background) so the native sheet's Liquid Glass shows through;
    // the Compose content stays themed (matched to the app) and draws over the glass. An opaque background
    // would cover the system material — which is exactly what made the sheet look non-native before.
    val vc = ComposeUIViewController(configure = { opaque = false }) { themedContent() }
    vc.view.backgroundColor = UIColor.clearColor()
    testTag?.let { vc.view.setAccessibilityId(it) }
    vc.overrideUserInterfaceStyle =
        if (dark) UIUserInterfaceStyle.UIUserInterfaceStyleDark else UIUserInterfaceStyle.UIUserInterfaceStyleLight
    // sheetPresentationController isn't bound as a static member in this K/N version → reach it via KVC.
    val spc = vc.valueForKey("sheetPresentationController") as? UISheetPresentationController
    if (spc != null) {
        spc.detents = detents.map {
            when (it) {
                BrandSheetDetent.Medium -> UISheetPresentationControllerDetent.mediumDetent()
                BrandSheetDetent.Large -> UISheetPresentationControllerDetent.largeDetent()
            }
        }
        spc.prefersGrabberVisible = showDragHandle
    }
    val delegate = SheetDismissDelegate(onSwipeDismiss)
    spc?.delegate = delegate
    presenter.presentViewController(vc, animated = true, completion = null)
    return SheetHandle(vc, delegate)
}

@Composable
internal actual fun PlatformBrandSheet(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier, // not applicable to the native iOS sheet
    detents: List<BrandSheetDetent>,
    showDragHandle: Boolean,
    testTag: String?,
    content: @Composable () -> Unit,
) {
    val currentOnDismiss by rememberUpdatedState(onDismissRequest)

    // The hosted ComposeUIViewController runs a SEPARATE composition, so it inherits no CompositionLocals.
    // Capture the parent's theme + brand locals + layout direction here (in the parent composition) and
    // re-provide them inside the sheet, so its content matches the app exactly (brand colors, dark mode, RTL)
    // instead of falling back to a default "iOS" look. (App providers like the feedback controller still
    // aren't resolvable inside — pass a captured reference if needed.)
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
                content()
            }
        }
    }

    DisposableEffect(visible) {
        val handle = if (visible) {
            presentSheet(detents, showDragHandle, dark, testTag, onSwipeDismiss = { currentOnDismiss() }, themedContent = themedContent)
        } else {
            null
        }
        onDispose { handle?.dismiss() }
    }
}
