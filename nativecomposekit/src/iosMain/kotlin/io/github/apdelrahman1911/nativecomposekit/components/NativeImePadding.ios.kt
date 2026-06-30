package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.Foundation.NSNotification
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.UIKit.CGRectValue
import platform.UIKit.UIKeyboardFrameEndUserInfoKey
import platform.UIKit.UIKeyboardWillChangeFrameNotification
import platform.UIKit.UIScreen
import platform.darwin.NSObjectProtocol
import kotlin.math.max

/** Opt-in diagnostic logging for [nativeImePadding]'s keyboard-frame observer (off by default). */
public object NativeImeLog {
    public var enabled: Boolean = false
}

/**
 * iOS: pad by the real keyboard frame. The system's `UIKeyboardWillChangeFrame` reports the end frame of the
 * whole keyboard — keys + input-accessory bar + QuickType row — so the overlap we apply already includes the
 * accessory height, and that bar can't cover content. Falls to zero when the keyboard leaves the screen.
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
public actual fun Modifier.nativeImePadding(): Modifier {
    var bottomPoints by remember { mutableStateOf(0.0) }
    DisposableEffect(Unit) {
        val center = NSNotificationCenter.defaultCenter
        val observer: NSObjectProtocol = center.addObserverForName(
            name = UIKeyboardWillChangeFrameNotification,
            `object` = null,
            queue = NSOperationQueue.mainQueue,
        ) { note: NSNotification? ->
            val frameValue = note?.userInfo?.get(UIKeyboardFrameEndUserInfoKey)
            bottomPoints = if (frameValue is platform.Foundation.NSValue) {
                val keyboardTop = frameValue.CGRectValue().useContents { origin.y }
                val screenHeight = UIScreen.mainScreen.bounds.useContents { size.height }
                max(0.0, screenHeight - keyboardTop)
            } else {
                0.0
            }
            if (NativeImeLog.enabled) {
                println("NCK-Kbd: frameEnd present=${frameValue != null} inset=${bottomPoints}pt")
            }
        }
        onDispose { center.removeObserver(observer) }
    }
    // iOS Compose Dp maps 1:1 to UIKit points, so the overlap in points is the padding in dp.
    return this.padding(bottom = bottomPoints.dp)
}
