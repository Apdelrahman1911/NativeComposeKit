package io.github.apdelrahman1911.nativecomposekit.chrome

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ExperimentalComposeApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeUIViewController
import io.github.apdelrahman1911.nativecomposekit.components.feedback.NativeFeedbackHost
import io.github.apdelrahman1911.nativecomposekit.theme.NativeAppearanceScope
import platform.UIKit.UIColor
import platform.UIKit.UIViewController

/** A tab as the native `UITabBar` should render it. */
public data class NativeChromeTab(val id: String, val title: String, val sfSymbol: String)

/** A top-bar action (e.g. a "+"), rendered as a native `UIBarButtonItem`. */
public data class NativeChromeAction(val id: String, val sfSymbol: String)

/**
 * An immutable projection the native iOS chrome (a `UINavigationBar` + `UITabBar`) renders. It is pure display
 * data — it carries **no** route stack and is never authoritative. [sheetId] only tells the shell whether a sheet
 * should be presented; the sheet's Compose content comes from [NativeChromeSource.sheetViewController].
 */
public data class NativeChromeState(
    val title: String,
    val canGoBack: Boolean,
    val selectedTabId: String,
    val tabs: List<NativeChromeTab>,
    val actions: List<NativeChromeAction>,
    val sheetId: String?,
)

/** Handle returned by [NativeChromeSource.observe]; [cancel] stops delivery (and breaks any retain cycle). */
public fun interface NativeChromeCancellable {
    public fun cancel()
}

/**
 * The **nav-agnostic contract** the native iOS chrome renders. This kit provides the native chrome — a real
 * `UINavigationBar`, a Liquid Glass `UITabBar`, and a `UISheetPresentationController` — but it owns **no**
 * navigation: bring your own navigation system and adapt it to this contract.
 *
 * An implementation is a **dumb, one-way projection** of whatever navigation state you already have: it emits a
 * [NativeChromeState] out (so the bars can draw the title/back/tabs/actions, and the shell knows when to present a
 * sheet) and accepts only intents in ([backRequested]/[tabSelected]/[actionTapped]/[dismissSheet]). It must never
 * expose, mutate, or mirror a navigation stack — your navigation system stays the single source of truth. The
 * sample app ships a reference implementation over its own lightweight navigator; a real consumer writes one over
 * whatever navigation library it uses.
 */
public interface NativeChromeSource {
    /** The current chrome to display, computed fresh from your live navigation state. */
    public fun currentState(): NativeChromeState

    /** Subscribe to chrome changes; must fire once immediately with the current state, then after every change. */
    public fun observe(onChange: (NativeChromeState) -> Unit): NativeChromeCancellable

    /** The back affordance was tapped — perform your navigation's "back". */
    public fun backRequested()

    /** The tab with [tabId] was tapped. */
    public fun tabSelected(tabId: String)

    /** The top-bar action with [actionId] was tapped. */
    public fun actionTapped(actionId: String)

    /**
     * The Compose content view controller for the currently-presented sheet, or null. Build it with
     * [nativeSheetHostViewController] so the native sheet's material shows through; the shell handles presentation.
     */
    public fun sheetViewController(): UIViewController?

    /** A native sheet dismissal (swipe / tap-outside) happened — clear your sheet state. Must be idempotent. */
    public fun dismissSheet()
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
