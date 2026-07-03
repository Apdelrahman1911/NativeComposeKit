package io.github.apdelrahman1911.nativecomposekit.app.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.runtime.CompositionLocalProvider
import io.github.apdelrahman1911.nativecomposekit.app.LocalNativeContentBottomInset
import io.github.apdelrahman1911.nativecomposekit.chrome.NativeChromeAction
import io.github.apdelrahman1911.nativecomposekit.chrome.NativeChromeCancellable
import io.github.apdelrahman1911.nativecomposekit.chrome.NativeChromeEntry
import io.github.apdelrahman1911.nativecomposekit.chrome.NativeChromeSource
import io.github.apdelrahman1911.nativecomposekit.chrome.NativeChromeState
import io.github.apdelrahman1911.nativecomposekit.chrome.NativeChromeTab
import io.github.apdelrahman1911.nativecomposekit.chrome.nativeContentHostViewController
import io.github.apdelrahman1911.nativecomposekit.chrome.nativeSheetHostViewController
import platform.UIKit.UIViewController

/**
 * Sample **reference** adapter: projects the sample's [NativeNavigator] into the kit's nav-agnostic
 * [NativeChromeSource] so the native chrome shell (a real `UITabBarController` + per-tab
 * `UINavigationController`s + native sheet) can render it. State goes out ([NativeChromeState], including
 * every tab's stack as [NativeChromeEntry]s and a fresh Compose host per entry); user actions come back as
 * navigator intents — including [backCommitted], the after-the-fact ratification of a pop the platform's own
 * interactive swipe/back button already performed, which maps to the idempotent [NativeNavigator.popTo].
 * The adapter never exposes a mutable stack; the navigator stays the single source of truth. A consumer with
 * a different navigation system writes their own [NativeChromeSource] the same way; the kit owns no navigation.
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
            backStacksByTab = state.tabs.associate { tab ->
                tab.id to state.stackFor(tab).map { NativeChromeEntry(it.id, titleForRoute(it)) }
            },
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

    /** The shell's native stack committed a user pop — ratify via the idempotent, tab-scoped [NativeNavigator.popTo]. */
    override fun backCommitted(tabId: String, entryId: String) {
        navigator.state.tabById(tabId)?.let { navigator.popTo(it, entryId) }
    }

    /**
     * A fresh Compose host for one stack entry's screen. The shell owns the returned controller for that
     * entry's lifetime (one controller per entry — never shared). Wrapped in the kit scopes; publishes the
     * bottom safe-area inset (the shell's Liquid Glass tab bar overlap) as [LocalNativeContentBottomInset]
     * so scrollable screens end clear of the bar while still rendering behind it.
     */
    override fun contentViewController(entryId: String): UIViewController? {
        val state = navigator.state
        val route = state.tabs.firstNotNullOfOrNull { tab -> state.stackFor(tab).firstOrNull { it.id == entryId } }
            ?: return null
        return nativeContentHostViewController {
            val bottomInset = WindowInsets.safeDrawing.asPaddingValues().calculateBottomPadding()
            CompositionLocalProvider(LocalNativeContentBottomInset provides bottomInset) {
                graph.Content(route)
            }
        }
    }

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
