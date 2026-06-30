package com.ukkera.brandkit.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import androidx.compose.ui.viewinterop.remeasureRequester
import androidx.compose.ui.viewinterop.rememberUIKitInteropRemeasureRequester
import com.ukkera.brandkit.components.model.ResolvedPageControlStyle
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import platform.UIKit.UIControlEventValueChanged
import platform.UIKit.UIPageControl
import platform.UIKit.UIView
import platform.UIKit.accessibilityLabel
import platform.darwin.NSObject
import platform.objc.sel_registerName

/** Forwards `UIPageControl` value changes (tap-to-page) to a Kotlin lambda. Retained for the view's life
 * (UIControl targets are not retained by UIKit). */
@OptIn(BetaInteropApi::class)
private class PageControlHandler : NSObject() {
    var control: UIPageControl? = null
    var onSelect: (Int) -> Unit = {}

    @ObjCAction
    fun pageChanged() {
        control?.let { onSelect(it.currentPage.toInt()) }
    }
}

/** iOS page control → a real `UIPageControl` (dots + native tap-to-page). */
@OptIn(ExperimentalComposeUiApi::class, ExperimentalForeignApi::class)
@Composable
internal actual fun PlatformBrandPageControl(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier,
    onCurrentPageChange: ((Int) -> Unit)?,
    style: ResolvedPageControlStyle,
    contentDescription: String?,
    testTag: String?,
) {
    val handler = remember { PageControlHandler() }
    handler.onSelect = { onCurrentPageChange?.invoke(it) }
    val control = remember {
        UIPageControl().apply {
            addTarget(handler, sel_registerName("pageChanged"), UIControlEventValueChanged)
        }
    }
    handler.control = control
    // UIPageControl has a transparent region around the dots; without a backing it exposes the host's system
    // backdrop (the black box behind the dots). Pin it in a backing that matches the published surface, and
    // stays clear on Liquid Glass so the dots float on the material.
    val backing = remember { UIView() }
    val backingColor = interopBackingColor()
    val remeasure = rememberUIKitInteropRemeasureRequester()
    // Re-measure when the dot count changes (it drives width), NOT on every scroll frame (avoids drift).
    LaunchedEffect(pageCount) { remeasure.requestRemeasure() }

    UIKitView(
        factory = {
            backing.pinFilling(control)
            backing
        },
        modifier = modifier.remeasureRequester(remeasure),
        properties = scrollSafeInteropProperties(), // overlay placement so the backing isn't clipped on scroll
        update = {
            backing.backgroundColor = backingColor
            control.numberOfPages = pageCount.toLong()
            control.hidesForSinglePage = true // iOS convention: no dots for a single page
            control.currentPage = currentPage.toLong()
            control.pageIndicatorTintColor = style.inactive.toUIColor()
            control.currentPageIndicatorTintColor = style.current.toUIColor()
            control.userInteractionEnabled = onCurrentPageChange != null
            contentDescription?.let { control.accessibilityLabel = it }
            testTag?.let { control.setAccessibilityId(it) }
        },
    )
}
