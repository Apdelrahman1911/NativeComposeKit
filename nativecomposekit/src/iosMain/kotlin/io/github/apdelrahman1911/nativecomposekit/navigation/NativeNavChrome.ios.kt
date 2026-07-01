package io.github.apdelrahman1911.nativecomposekit.navigation

import androidx.compose.runtime.ExperimentalComposeApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeUIViewController
import io.github.apdelrahman1911.nativecomposekit.components.feedback.NativeFeedbackHost
import io.github.apdelrahman1911.nativecomposekit.theme.NativeAppearanceScope
import platform.UIKit.UIColor
import platform.UIKit.UIViewController

/** A tab as the native `UITabBar` should render it. */
public data class NativeChromeTab(val id: String, val title: String, val sfSymbol: String)

/** A top-bar action (e.g. the Library "+"), rendered as a native `UIBarButtonItem`. */
public data class NativeChromeAction(val id: String, val sfSymbol: String)

/**
 * Immutable chrome projection the native bars render. It is DERIVED from the navigator on every change and is
 * never authoritative — the bars only display it. It carries no route stack; [sheetId] just tells the shell
 * whether a sheet should be presented (the sheet's Compose content comes from [NativeNavChrome.sheetViewController]).
 */
public data class NativeChromeState(
    val title: String,
    val canGoBack: Boolean,
    val selectedTabId: String,
    val tabs: List<NativeChromeTab>,
    val actions: List<NativeChromeAction>,
    val sheetId: String?,
)

/**
 * The iOS native-chrome bridge: a **dumb** projection of [NativeNavigator]. It emits [NativeChromeState] out (so
 * a real `UINavigationBar` + `UITabBar` can render title/back/tabs/actions, and the shell knows when to present a
 * native sheet) and accepts only intents in ([backRequested]/[tabSelected]/[actionTapped]/[dismissSheet]). It
 * NEVER exposes, reads, mutates, mirrors, or reconciles the navigation stack — Compose/[NativeNavigator] stays the
 * sole owner. [sheetViewController] builds the sheet's Compose content; PRESENTING it is the shell's job.
 *
 * Constructor is `public` because the app (a consumer module) constructs it; it takes only public types.
 */
public class NativeNavChrome public constructor(
    private val navigator: NativeNavigator,
    private val graph: NativeNavGraph,
    private val titleForRoute: (NativeRoute) -> String,
    private val tabs: List<NativeChromeTab>,
    private val actionsForTab: (String) -> List<NativeChromeAction>,
    private val onAction: (String) -> Unit,
) {
    /** The current chrome to display, computed fresh from the live navigator state. */
    public fun currentState(): NativeChromeState {
        val state = navigator.state
        val stack = state.currentStack()
        return NativeChromeState(
            title = titleForRoute(stack.last()),
            canGoBack = stack.size > 1,
            selectedTabId = state.selectedTab.id,
            tabs = tabs,
            actions = actionsForTab(state.selectedTab.id),
            sheetId = state.sheet?.id,
        )
    }

    /** Subscribe to chrome changes (fires once immediately, then after every navigation intent). */
    public fun observe(onChange: (NativeChromeState) -> Unit): NativeNavCancellable =
        navigator.observe { onChange(currentState()) }

    // ---- Intents in (the ONLY writes; each maps to a navigator intent, none touches the stack directly) ----
    public fun backRequested() { navigator.pop() }
    public fun tabSelected(tabId: String) { navigator.state.tabById(tabId)?.let { navigator.selectTab(it) } }
    public fun actionTapped(actionId: String) { onAction(actionId) }

    /**
     * The Compose content view controller for the currently-presented sheet route, or null. The shell PRESENTS it
     * natively (`UISheetPresentationController`); this only builds a transparent Compose host so the native
     * sheet's material shows through. Sheet lifecycle/state stays owned by [NativeNavigator].
     */
    @OptIn(ExperimentalComposeApi::class, ExperimentalComposeUiApi::class)
    public fun sheetViewController(): UIViewController? {
        val route = navigator.state.sheet ?: return null
        val vc = ComposeUIViewController(configure = { opaque = false }) {
            NativeAppearanceScope(drawBackground = false) { NativeFeedbackHost { graph.Content(route) } }
        }
        vc.view.backgroundColor = UIColor.clearColor()
        return vc
    }

    /** Report a native sheet dismissal (swipe / tap-outside) back to the source of truth. */
    public fun dismissSheet() { navigator.dismissSheet() }
}
