package io.github.apdelrahman1911.nativecomposekit.chrome

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ExperimentalComposeApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeUIViewController
import io.github.apdelrahman1911.nativecomposekit.components.feedback.NativeFeedbackHost
import io.github.apdelrahman1911.nativecomposekit.theme.NativeAppearanceScope
import platform.UIKit.UIColor
import platform.UIKit.UIViewController

/**
 * The iOS native-chrome contract the shell renders: the nav-agnostic [NativeChromeStateSource] (state out +
 * intents in) plus the one genuinely iOS-specific piece — the Compose content for the currently-presented sheet.
 *
 * A consumer typically implements [NativeChromeStateSource] in shared code (where it is unit-testable), then
 * exposes it as a `NativeChromeSource` on iOS by supplying [sheetViewController] — usually built with
 * [nativeSheetHostViewController]. The shell owns presentation; this only provides the content.
 */
public interface NativeChromeSource : NativeChromeStateSource {
    /**
     * The Compose content view controller for the currently-presented sheet, or null. Build it with
     * [nativeSheetHostViewController] so the native sheet's material shows through; the shell handles presentation.
     */
    public fun sheetViewController(): UIViewController?
}

/**
 * Build a transparent Compose host `UIViewController` suitable for presenting inside a native
 * `UISheetPresentationController`. The [content] is wrapped in the kit's appearance + feedback scopes and the host
 * is made non-opaque with a clear background, so the native sheet's Liquid Glass material shows through. This only
 * builds the content host — presentation (detents, grabber, interactive swipe-to-dismiss) stays the shell's job.
 */
@OptIn(ExperimentalComposeApi::class, ExperimentalComposeUiApi::class)
public fun nativeSheetHostViewController(content: @Composable () -> Unit): UIViewController {
    val vc = ComposeUIViewController(configure = { opaque = false }) {
        NativeAppearanceScope(drawBackground = false) { NativeFeedbackHost { content() } }
    }
    vc.view.backgroundColor = UIColor.clearColor()
    return vc
}
