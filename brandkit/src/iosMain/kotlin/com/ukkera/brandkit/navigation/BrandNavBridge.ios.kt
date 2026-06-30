package com.ukkera.brandkit.navigation

import androidx.compose.runtime.ExperimentalComposeApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeUIViewController
import com.ukkera.brandkit.components.feedback.BrandFeedbackHost
import com.ukkera.brandkit.components.topmostUIViewController
import com.ukkera.brandkit.theme.BrandAppearanceScope
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.valueForKey
import platform.UIKit.UIAdaptivePresentationControllerDelegateProtocol
import platform.UIKit.UIColor
import platform.UIKit.UIPresentationController
import platform.UIKit.UISheetPresentationController
import platform.UIKit.UISheetPresentationControllerDetent
import platform.UIKit.UIViewController
import platform.darwin.NSObject

/**
 * The single object the SwiftUI shell talks to. Wraps the shared [BrandNavigator] + [BrandNavGraph] and exposes
 * a **string-only** surface (Swift never sees `BrandRoute`). Built once at launch by an app entry
 * (`createBrandNavBridge()` in `MainViewController.kt`) and held by the SwiftUI shell.
 *
 * - READ: [observe] (SoT → SwiftUI for tabs + per-tab stacks) + scalar/array accessors.
 * - WRITE: [selectTab]/[pop]/[reconcileStack]/[presentSheet]/[dismissSheet] (SwiftUI gestures → intents).
 * - RENDER: [viewController] resolves a route id → its shared screen in a `ComposeUIViewController`.
 *
 * **Sheets are presented natively by the bridge itself** (not via SwiftUI's `.sheet`): it observes the
 * navigator and, when [BrandNavigationState.sheet] becomes non-null, presents a transparent
 * `ComposeUIViewController` in a real `UISheetPresentationController` (medium/large detents, grabber, Liquid
 * Glass) — exactly the proven `BrandSheet` path — and dismisses it when the sheet clears. This guarantees the
 * same native sheet flow everywhere, independent of where the trigger lives in the SwiftUI tree.
 */
// Constructor is `public` (not `internal`): the app's `createBrandNavBridge()` lives in the consumer module
// (it wires app-specific routes), so once `navigation` ships in the `:brandkit` library the factory must be
// able to construct this across the module boundary. The ctor takes only public types (no app types leak in).
public class BrandNavBridge public constructor(
    private val navigator: BrandNavigator,
    private val graph: BrandNavGraph,
    /** Resolve a not-yet-live route id (e.g. a sheet) to a route — supplied by the app. */
    private val routeForId: (String) -> BrandRoute?,
    /** Human-readable chrome title for a route — supplied by the app (the shared `appRouteTitle`). */
    private val titleForRoute: (BrandRoute) -> String,
) {
    private var sheetVC: UIViewController? = null
    private var sheetDelegate: NavSheetDismissDelegate? = null

    init {
        // The bridge owns native sheet presentation, driven by the SoT (so it works no matter which tab/stack
        // the trigger lives in). Other observers (the Swift shell) handle tabs + stacks.
        navigator.observe { snapshot -> syncNativeSheet(snapshot.sheetId) }
    }

    // ---- READ ----
    public fun observe(onChange: (BrandNavSnapshot) -> Unit): BrandNavCancellable = navigator.observe(onChange)

    public fun selectedTabId(): String = navigator.state.selectedTab.id
    public fun tabIds(): List<String> = navigator.state.tabs.map { it.id }

    /** The current stack (root-first route ids) of [tabId]. */
    public fun stackIds(tabId: String): List<String> =
        navigator.state.tabById(tabId)?.let { tab -> navigator.state.stackFor(tab).map { it.id } } ?: emptyList()

    public fun sheetId(): String? = navigator.state.sheet?.id

    /** The root route id of [tabId] (the `NavigationStack`'s own root view). */
    public fun rootRouteId(tabId: String): String = stackIds(tabId).firstOrNull() ?: ""

    /** The chrome title for a live route id (the SwiftUI nav bar reads this) — empty if not resolvable yet. */
    public fun title(forRouteId: String): String =
        navigator.state.routeById(forRouteId)?.let(titleForRoute) ?: ""

    // ---- WRITE (report-back from SwiftUI) ----
    public fun selectTab(tabId: String): Unit = navigator.selectTabId(tabId)

    public fun pop() {
        navigator.pop()
    }

    /** Make [tabId]'s stack match [routeIds] (root-first) — what SwiftUI reports after a back-swipe / pop. */
    public fun reconcileStack(tabId: String, routeIds: List<String>): Unit = navigator.reconcileStack(tabId, routeIds)

    /** Present the route with [routeId] as a sheet (resolved via the app's route factory). */
    public fun presentSheet(routeId: String) {
        routeForId(routeId)?.let { navigator.presentSheet(it) }
    }

    public fun dismissSheet(): Unit = navigator.dismissSheet()

    // ---- RENDER (nav destinations: opaque, hosted by SwiftUI NavigationStack) ----
    public fun viewController(forRouteId: String): UIViewController {
        val route = navigator.state.routeById(forRouteId)
            ?: error("BrandNavBridge: no live route for id '$forRouteId'")
        // Each hosted route is its own composition, so it needs its own appearance scope AND feedback host
        // (the latter provides LocalBrandFeedbackController, which screens like the catalog read).
        return ComposeUIViewController { BrandAppearanceScope { BrandFeedbackHost { graph.Content(route) } } }
    }

    // ---- Native sheet presentation (the proven BrandSheet path) ----
    @OptIn(ExperimentalForeignApi::class, ExperimentalComposeUiApi::class, ExperimentalComposeApi::class)
    private fun syncNativeSheet(sheetId: String?) {
        val target = sheetId?.let { navigator.state.routeById(it) }
        if (target == null) {
            // The state wants no sheet. (A user swipe already cleared [sheetVC] via the delegate before this
            // runs; this branch dismisses a sheet that was cleared programmatically via dismissSheet().)
            sheetVC?.dismissViewControllerAnimated(true, null)
            sheetVC = null
            sheetDelegate = null
            return
        }
        if (sheetVC != null) return // a sheet is already presented for the current state
        val presenter = topmostUIViewController() ?: return
        // Transparent host so the native sheet's Liquid Glass shows through; drawBackground = false keeps the
        // sheet content from painting an opaque background, and the feedback host matches the nav destinations.
        val vc = ComposeUIViewController(configure = { opaque = false }) {
            BrandAppearanceScope(drawBackground = false) { BrandFeedbackHost { graph.Content(target) } }
        }
        vc.view.backgroundColor = UIColor.clearColor()
        // sheetPresentationController isn't a bound static member in this K/N version → reach it via KVC.
        val spc = vc.valueForKey("sheetPresentationController") as? UISheetPresentationController
        if (spc != null) {
            spc.detents = listOf(
                UISheetPresentationControllerDetent.mediumDetent(),
                UISheetPresentationControllerDetent.largeDetent(),
            )
            spc.prefersGrabberVisible = true
        }
        // Clear our tracking the moment an interactive dismiss BEGINS (will-dismiss), not after the animation
        // finishes — otherwise a "+" tap during the ~0.4s close animation is skipped (sheetVC still set).
        // did-dismiss is a backstop. Both just clear refs + sync the SoT (dismissSheet is idempotent).
        val delegate = NavSheetDismissDelegate {
            sheetVC = null
            sheetDelegate = null
            navigator.dismissSheet()
        }
        spc?.delegate = delegate
        sheetVC = vc
        sheetDelegate = delegate
        presenter.presentViewController(vc, animated = true, completion = null)
    }
}

/** Reports an interactive sheet dismiss back to the SoT. Fires on **will**-dismiss (gesture committed) so the
 * next present is honored immediately, and on **did**-dismiss as a backstop (both calls are idempotent).
 * Retained for the sheet's lifetime — `UIPresentationController.delegate` is weak. */
@OptIn(BetaInteropApi::class)
private class NavSheetDismissDelegate(val onDismiss: () -> Unit) :
    NSObject(), UIAdaptivePresentationControllerDelegateProtocol {
    override fun presentationControllerWillDismiss(presentationController: UIPresentationController) = onDismiss()
    override fun presentationControllerDidDismiss(presentationController: UIPresentationController) = onDismiss()
}
