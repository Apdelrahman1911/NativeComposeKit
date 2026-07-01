package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
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
public actual fun Modifier.nativeImePadding(minBottom: Dp): Modifier {
    var bottomPoints by remember { mutableStateOf(0.0) }
    DisposableEffect(Unit) {
        val center = NSNotificationCenter.defaultCenter
        val observer: NSObjectProtocol = center.addObserverForName(
            name = UIKeyboardWillChangeFrameNotification,
            `object` = null,
            queue = NSOperationQueue.mainQueue,
        ) { note: NSNotification? ->
            val frameValue = note?.userInfo?.get(UIKeyboardFrameEndUserInfoKey)
            val computed = if (frameValue is platform.Foundation.NSValue) {
                val keyboardTop = frameValue.CGRectValue().useContents { origin.y }
                val screenHeight = UIScreen.mainScreen.bounds.useContents { size.height }
                max(0.0, screenHeight - keyboardTop)
            } else {
                0.0
            }
            // While the keyboard is up, the reported end frame oscillates a few points as the
            // suggestions/QuickType bar toggles. Hold the largest extent seen so the inset stays stable
            // (no per-frame relayout) and drop to zero only when the keyboard actually leaves the screen.
            bottomPoints = if (computed <= 0.0) 0.0 else max(bottomPoints, computed)
            if (NativeImeLog.enabled) {
                println("NCK-Kbd: frameEnd present=${frameValue != null} computed=${computed}pt inset=${bottomPoints}pt")
            }
        }
        onDispose { center.removeObserver(observer) }
    }
    // iOS Compose Dp maps 1:1 to UIKit points. Pad by the LARGER of the keyboard extent and [minBottom] (never
    // their sum): the keyboard covers the bottom bar while it's up, so the keyboard extent alone is right then;
    // [minBottom] keeps content clear of the bar while the keyboard is down.
    return this.padding(bottom = maxOf(bottomPoints.dp, minBottom))
}
