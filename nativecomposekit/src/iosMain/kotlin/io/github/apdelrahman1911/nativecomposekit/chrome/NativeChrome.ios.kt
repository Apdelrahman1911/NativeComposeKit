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

    /**
     * A **fresh** Compose content view controller for the back-stack entry [entryId] (from
     * [NativeChromeState.backStacksByTab]), or null if the entry is unknown (stale id — the shell logs and
     * skips; the next state emission re-syncs). Build it with [nativeContentHostViewController].
     *
     * A stack-rendering shell (one native screen per entry) calls this once per NEW entry and then owns the
     * returned controller for that entry's lifetime — so each call must return a NEW controller, never a
     * shared/cached one (two live screens sharing one `UIViewController` is how native stacks corrupt).
     * Sources feeding only flat shells can leave the default (null).
     */
    public fun contentViewController(entryId: String): UIViewController? = null
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

/**
 * Build an opaque Compose host `UIViewController` for one back-stack entry's screen, wrapped in the kit's
 * appearance + feedback scopes — the standard body of [NativeChromeSource.contentViewController]. The host
 * paints the themed background (each screen must be self-opaque: during a native push/pop transition it is
 * the only thing behind the finger). The content must NOT register Compose back handlers or host its own
 * navigation renderer — in a stack-rendering shell the platform owns the back gesture, and a Compose handler
 * inside a screen would steal the edge swipe from it (see docs/navigation.md).
 */
public fun nativeContentHostViewController(content: @Composable () -> Unit): UIViewController =
    ComposeUIViewController {
        NativeAppearanceScope { NativeFeedbackHost { content() } }
    }
