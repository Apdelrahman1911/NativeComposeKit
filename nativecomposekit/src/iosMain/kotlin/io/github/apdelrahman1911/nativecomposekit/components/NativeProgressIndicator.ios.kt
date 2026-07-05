package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.foundation.progressSemantics
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.viewinterop.UIKitView
import androidx.compose.ui.viewinterop.remeasureRequester
import androidx.compose.ui.viewinterop.rememberUIKitInteropRemeasureRequester
import io.github.apdelrahman1911.nativecomposekit.components.model.ResolvedProgressStyle
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIActivityIndicatorView
import platform.UIKit.UIActivityIndicatorViewStyleLarge
import platform.UIKit.UIProgressView
import platform.UIKit.UIProgressViewStyle
import platform.UIKit.UIView
import platform.UIKit.accessibilityLabel

/**
 * iOS progress: native `UIActivityIndicatorView` for the indeterminate spinner and native `UIProgressView`
 * for the determinate bar. The two combinations iOS has no native control for — determinate **circular**
 * and indeterminate **linear** — fall back to the Compose-drawn Material indicator (documented).
 */
@Composable
internal actual fun PlatformNativeProgressIndicator(
    modifier: Modifier,
    kind: NativeProgressKind,
    progress: Float?,
    style: ResolvedProgressStyle,
    contentDescription: String?,
    testTag: String?,
) {
    when {
        kind == NativeProgressKind.Circular && progress == null ->
            IosSpinner(modifier, style, contentDescription, testTag)
        kind == NativeProgressKind.Linear && progress != null ->
            IosProgressBar(modifier, style, progress, contentDescription, testTag)
        else -> {
            var m = modifier
            testTag?.let { m = m.testTag(it) }
            contentDescription?.let { cd -> m = m.semantics { this.contentDescription = cd } }
            if (kind == NativeProgressKind.Circular) {
                CircularProgressIndicator(progress = { progress ?: 0f }, modifier = m, color = style.indicator, trackColor = style.track)
            } else {
                LinearProgressIndicator(modifier = m, color = style.indicator, trackColor = style.track)
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalForeignApi::class)
@Composable
private fun IosSpinner(
    modifier: Modifier,
    style: ResolvedProgressStyle,
    contentDescription: String?,
    testTag: String?,
) {
    val spinner = remember { UIActivityIndicatorView(activityIndicatorStyle = UIActivityIndicatorViewStyleLarge) }
    val backing = remember { InteropBackingView() }
    val backingColor = interopBackingColor() // published surface on solid; clear on Liquid Glass
    val remeasure = rememberUIKitInteropRemeasureRequester()
    val sizeFp = remember { InteropSizeFingerprint() }
    // Display-only: mirrored into Compose semantics (VoiceOver never traverses un-flagged native views),
    // and overlay-placed like every pinFilling-backed control so scrolling doesn't clip the backing.
    var m = modifier.remeasureRequester(remeasure)
    contentDescription?.let { cd -> m = m.semantics { this.contentDescription = cd } }
    InteropDisposeFailSafe(backing) // synchronous ghost-kill; see UiKitInterop.ios.kt
    UIKitView(
        factory = {
            backing.pinFilling(spinner.also { it.startAnimating() })
            backing
        },
        modifier = m.then(rememberInteropPositionHeal(backing)),
        properties = scrollSafeInteropProperties(nativeAccessibility = false),
        update = {
            backing.backgroundColor = backingColor
            spinner.color = style.indicator.toUIColor()
            spinner.startAnimating()
            contentDescription?.let { spinner.accessibilityLabel = it }
            testTag?.let { spinner.setAccessibilityId(it) }
            // Fixed intrinsic size: measure once, on the first update (see InteropSizeFingerprint).
            sizeFp.requestIfChanged(Unit) { remeasure.requestRemeasure() }
        },
    )
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalForeignApi::class)
@Composable
private fun IosProgressBar(
    modifier: Modifier,
    style: ResolvedProgressStyle,
    progress: Float,
    contentDescription: String?,
    testTag: String?,
) {
    val bar = remember { UIProgressView() }
    val backing = remember { InteropBackingView() }
    val backingColor = interopBackingColor() // published surface on solid; clear on Liquid Glass
    val remeasure = rememberUIKitInteropRemeasureRequester()
    val sizeFp = remember { InteropSizeFingerprint() }
    // Display-only: mirror the description AND the determinate value into Compose semantics so screen
    // readers can report progress; overlay-placed like every pinFilling-backed control.
    var m = modifier
        .remeasureRequester(remeasure)
        .progressSemantics(value = progress.coerceIn(0f, 1f))
    contentDescription?.let { cd -> m = m.semantics { this.contentDescription = cd } }
    InteropDisposeFailSafe(backing) // synchronous ghost-kill; see UiKitInterop.ios.kt
    UIKitView(
        factory = {
            bar.progressViewStyle = UIProgressViewStyle.UIProgressViewStyleDefault
            backing.pinFilling(bar)
            backing
        },
        modifier = m.then(rememberInteropPositionHeal(backing)),
        properties = scrollSafeInteropProperties(nativeAccessibility = false),
        update = {
            backing.backgroundColor = backingColor
            bar.progressTintColor = style.indicator.toUIColor()
            bar.trackTintColor = style.track.toUIColor()
            bar.setProgress(progress, animated = false)
            contentDescription?.let { bar.accessibilityLabel = it }
            testTag?.let { bar.setAccessibilityId(it) }
            // Fixed intrinsic size: measure once, on the first update (see InteropSizeFingerprint).
            sizeFp.requestIfChanged(Unit) { remeasure.requestRemeasure() }
        },
    )
}
