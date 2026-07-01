package io.github.apdelrahman1911.nativecomposekit.app.navigation

import io.github.apdelrahman1911.nativecomposekit.chrome.NativeChromeAction
import io.github.apdelrahman1911.nativecomposekit.chrome.NativeChromeCancellable
import io.github.apdelrahman1911.nativecomposekit.chrome.NativeChromeSource
import io.github.apdelrahman1911.nativecomposekit.chrome.NativeChromeState
import io.github.apdelrahman1911.nativecomposekit.chrome.NativeChromeTab
import io.github.apdelrahman1911.nativecomposekit.chrome.nativeSheetHostViewController
import platform.UIKit.UIViewController

/**
 * Sample **reference** adapter: projects the sample's [NativeNavigator] into the kit's nav-agnostic
 * [NativeChromeSource] so the native chrome shell (a real `UINavigationBar` + `UITabBar` + native sheet) can
 * render it. It emits [NativeChromeState] out and turns bar taps into navigator intents — it never exposes,
 * mutates, or mirrors the navigation stack. A consumer with a different navigation system writes their own
 * [NativeChromeSource] the same way; the kit itself owns no navigation.
 */
class NativeNavChrome(
    private val navigator: NativeNavigator,
    private val graph: NativeNavGraph,
    private val titleForRoute: (NativeRoute) -> String,
    private val tabs: List<NativeChromeTab>,
    private val actionsForTab: (String) -> List<NativeChromeAction>,
    private val onAction: (String) -> Unit,
) : NativeChromeSource {

    override fun currentState(): NativeChromeState {
        val state = navigator.state
        val stack = state.currentStack()
        return NativeChromeState(
            title = titleForRoute(stack.last()),
            canGoBack = stack.size > 1,
            selectedTabId = state.selectedTab.id,
            tabs = tabs,
            actions = actionsForTab(state.selectedTab.id),
            sheetId = state.sheet?.id,
            backTitle = stack.getOrNull(stack.size - 2)?.let(titleForRoute),
        )
    }

    override fun observe(onChange: (NativeChromeState) -> Unit): NativeChromeCancellable {
        val cancellable = navigator.observe { onChange(currentState()) }
        return NativeChromeCancellable { cancellable.cancel() }
    }

    // ---- Intents in (the ONLY writes; each maps to a navigator intent, none touches the stack directly) ----
    override fun backRequested() { navigator.pop() }
    override fun tabSelected(tabId: String) { navigator.state.tabById(tabId)?.let { navigator.selectTab(it) } }
    override fun actionTapped(actionId: String) { onAction(actionId) }

    /**
     * The Compose content view controller for the currently-presented sheet route, or null. Built as a
     * transparent host via the kit's [nativeSheetHostViewController]; the shell presents it natively.
     */
    override fun sheetViewController(): UIViewController? {
        val route = navigator.state.sheet ?: return null
        return nativeSheetHostViewController { graph.Content(route) }
    }

    override fun dismissSheet() { navigator.dismissSheet() }
}
