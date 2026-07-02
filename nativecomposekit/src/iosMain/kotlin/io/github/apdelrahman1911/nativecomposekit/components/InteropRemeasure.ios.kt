package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.viewinterop.UIKitInteropRemeasureRequester
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.UIKit.UIContentSizeCategoryDidChangeNotification

/**
 * Tracks the last size-affecting fingerprint a `UIKitView.update` block applied, so the interop host is
 * re-measured exactly when that fingerprint changes — requested from `update` itself, never from an effect.
 *
 * Why `update` and not a keyed `LaunchedEffect`: `update` runs in the interop-sync phase AFTER a frame's
 * composition and layout. On a screen's very first frame the interop node is measured while the UIView is
 * still factory-fresh — a content-sized view (`UILabel`, `UIButton`, `UISegmentedControl`…) measures 0×0 —
 * and a `LaunchedEffect` keyed on the same inputs fires BEFORE that first `update` has applied them, so its
 * remeasure re-measures the still-empty view and the keys never change again: the view stays invisible until
 * an unrelated recomposition happens to re-fire the effect (the "missing text / blank screen on entry" bug).
 * Requesting from `update` makes "content applied → a remeasure follows" a structural guarantee. The
 * fingerprint guard keeps per-frame `update` re-runs (scrolling changes bounds) from re-measuring every
 * frame — the "drift"/"cut" bug the effect pattern was originally introduced to avoid.
 */
internal class InteropSizeFingerprint {
    private var last: Any? = Unset

    /** Runs [request] iff [fingerprint] differs from the last one seen (and always on the first call). */
    fun requestIfChanged(fingerprint: Any?, request: () -> Unit) {
        if (last != fingerprint) {
            last = fingerprint
            request()
        }
    }

    private object Unset
}

/**
 * Re-measures a **content-sized** interop node when the user changes the Dynamic Type text size
 * mid-session. `adjustsFontForContentSizeCategory` re-renders the native glyphs live, but nothing in the
 * size fingerprint changes (text/style are the same values), so the interop host would keep its stale
 * measured size and clip the rescaled content until an unrelated recomposition. Safe against the
 * measure-before-first-update race: the notification only ever fires long after the first update.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun DynamicTypeRemeasureEffect(remeasure: UIKitInteropRemeasureRequester) {
    DisposableEffect(remeasure) {
        val center = NSNotificationCenter.defaultCenter
        val observer = center.addObserverForName(
            name = UIContentSizeCategoryDidChangeNotification,
            `object` = null,
            queue = NSOperationQueue.mainQueue,
        ) { _ -> remeasure.requestRemeasure() }
        onDispose { center.removeObserver(observer) }
    }
}
