package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSURL
import platform.Foundation.NSValue
import platform.Foundation.setValue
import platform.Foundation.valueForKey
import platform.UIKit.UIActivityViewController
import platform.UIKit.valueWithCGRect
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
@Composable
internal actual fun rememberPlatformShare(): (NativeShareContent) -> Unit = remember {
    { content ->
        val items: List<Any> = buildList {
            content.text?.let { add(it) }
            content.url?.let { u -> NSURL.URLWithString(u)?.let { add(it) } }
        }
        val presenter = topmostUIViewController()
        // Empty content is a documented no-op (mirrored on Android): UIActivityViewController asserts on an
        // empty activityItems list, and a blank share sheet helps no one.
        if (items.isNotEmpty() && presenter != null) {
            val avc = UIActivityViewController(activityItems = items, applicationActivities = null)
            // iPad presents the share sheet as a popover, which asserts without a source anchor. Anchor it
            // via KVC (popoverPresentationController is unbound) to the centre of the presenter, no arrow.
            (avc.valueForKey("popoverPresentationController") as? NSObject)?.let { popover ->
                val hostView = presenter.view
                popover.setValue(hostView, forKey = "sourceView")
                hostView.bounds.useContents {
                    popover.setValue(
                        NSValue.valueWithCGRect(CGRectMake(size.width / 2.0, size.height / 2.0, 1.0, 1.0)),
                        forKey = "sourceRect",
                    )
                }
                popover.setValue(0, forKey = "permittedArrowDirections")
            }
            presenter.presentViewController(avc, animated = true, completion = null)
        }
    }
}
