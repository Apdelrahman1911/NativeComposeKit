package io.github.apdelrahman1911.nativecomposekit.app.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import io.github.apdelrahman1911.nativecomposekit.chrome.NativeBarConfig

/** Push/pop slide duration (predictive-back settle uses it too). */
private const val NAV_TRANSITION_MS = 320

/** A tab and how it appears in the Material navigation bar. */
data class NativeNavBarItem(val tab: NativeTab, val label: String, val icon: ImageVector)

/**
 * The Material-chrome navigation renderer, driven entirely by [NativeNavigator] (the source of truth). It drives
 * the whole app on Android; on iOS it stays the pure-Compose fallback (`MainViewController()`), while the
 * production iOS shell renders the same navigator through real native containers instead (one screen per stack
 * entry — see docs/navigation.md for that shell's ratified-projection protocol). Kotlin owns the stack either
 * way; renderers only report user actions back as intents.
 *
 * By default a compact `TopAppBar` sits at the top (with a back arrow once pushed) and a Material
 * `NavigationBar` at the bottom — both are [NativeNavDefaults] and both are replaceable through the
 * [topBar]/[bottomBar] slots (restyle by calling the defaults with parameters, or pass any composable);
 * [barConfig] hides either bar per route (immersive screens). Omitting all three keeps today's exact look.
 * Renders the selected tab's **top** route inside an
 * `AnimatedContent` (push slides forward, pop backward; tab switches swap instantly — the native tab
 * convention, and required so iOS overlay-placed native controls never linger over the incoming tab);
 * system back → [NativeNavigator.pop]; the
 * `NavigationBar` → [NativeNavigator.selectTab]; a non-null [NativeNavigationState.sheet] → `ModalBottomSheet`.
 */
@Composable
fun NativeNavHost(
    navigator: NativeNavigator,
    graph: NativeNavGraph,
    tabs: List<NativeNavBarItem>,
    modifier: Modifier = Modifier,
    title: (NativeRoute) -> String = { "" },
    actions: @Composable RowScope.() -> Unit = {},
    // Per-screen chrome BEHAVIOR (hide either bar while a route is on top). Shares the kit's
    // platform-neutral vocabulary with the iOS shell; the Android default bars ignore the
    // iOS-oriented fields (prefersLargeTitle, actions-as-data) — Compose actions are slots.
    barConfig: (NativeRoute) -> NativeBarConfig = { NativeBarConfig.Default },
    // Bar APPEARANCE/STRUCTURE: omit for today's exact Material bars; call the public defaults with
    // parameters to restyle (NativeNavDefaults.TopBar(it, colors = …, centeredTitle = true)); or pass
    // any composable to replace a bar outright. Replacing chrome never touches the stack.
    topBar: @Composable (NativeNavTopBarState) -> Unit = { NativeNavDefaults.TopBar(it) },
    bottomBar: @Composable (NativeNavBottomBarState) -> Unit = { NativeNavDefaults.NavigationBar(it) },
) {
    val state = navigator.state
    val selectedTab = state.selectedTab
    val top = state.currentStack().last()
    val canPop = state.currentStack().size > 1
    val topConfig = barConfig(top)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            if (!topConfig.hidesTopBar) {
                topBar(
                    NativeNavTopBarState(
                        route = top,
                        title = title(top),
                        canPop = canPop,
                        onBack = { navigator.pop() },
                        actions = actions,
                    ),
                )
            }
        },
        bottomBar = {
            if (!topConfig.hidesTabBar) {
                bottomBar(
                    NativeNavBottomBarState(
                        tabs = tabs,
                        selectedTabId = selectedTab.id,
                        onSelectTab = { navigator.selectTab(it) },
                    ),
                )
            }
        },
    ) { inner ->
        NativeNavContent(navigator, graph, Modifier.padding(inner))
    }
}

/**
 * The **content-only, single-canvas** navigation renderer, hosted on **Navigation 3**: each tab's
 * Kotlin-owned back stack (a `SnapshotStateList` inside [NativeNavigator]) is rendered directly by
 * [NavDisplay], which owns the transitions, per-entry saveable state, and the platform back affordances —
 * on Android the **predictive back preview** (the system gesture seeks the pop; the manifest opts in), and
 * on iOS the in-canvas interactive swipe-back for the pure-Compose fallback. `onBack` reports to
 * [NativeNavigator.pop]; the navigator stays the single stack owner.
 *
 * Transitions are configured as FULL-WIDTH symmetric slides and tab switches remount instantly — both are
 * shared-canvas interop requirements (see docs/interop-notes.md): all screens here share ONE Compose canvas,
 * so overlapping screens or fades would float overlay-placed native controls (Library's
 * `UISegmentedControl`) over the other screen, which is also why Navigation 3's authentic iOS defaults
 * (quarter-parallax + veil dim) are deliberately not used. It draws NO chrome — [NativeNavHost] wraps it in
 * Material chrome. (The iOS native-chrome shell does NOT use this renderer: it hosts one Compose screen per
 * stack entry inside real `UINavigationController`s, where UIKit's own transitions are interop-safe.)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NativeNavContent(
    navigator: NativeNavigator,
    graph: NativeNavGraph,
    modifier: Modifier = Modifier,
    // Whether to render the sheet inline as a Compose `ModalBottomSheet`. Android uses true; iOS passes false and
    // presents the sheet as a real native `UISheetPresentationController` from its chrome shell instead.
    renderSheet: Boolean = true,
) {
    val state = navigator.state

    // One NavDisplay per tab: the key() remount makes a tab switch an INSTANT swap — the native
    // convention on both platforms (UIKit's UITabBarController never animates tab changes) and a hard
    // interop requirement (overlay-placed native iOS controls cannot fade with Compose content, so an
    // animated tab transition would float the outgoing tab's controls over the incoming screen).
    key(state.selectedTab.id) {
        NavDisplay(
            backStack = state.currentStack(),
            modifier = modifier.fillMaxSize(),
            onBack = { navigator.pop() },
            // Full-width symmetric slides; Start/End keep them layout-direction aware under forced RTL.
            transitionSpec = {
                ContentTransform(
                    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(NAV_TRANSITION_MS)),
                    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(NAV_TRANSITION_MS)),
                )
            },
            popTransitionSpec = {
                ContentTransform(
                    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(NAV_TRANSITION_MS)),
                    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(NAV_TRANSITION_MS)),
                )
            },
            predictivePopTransitionSpec = {
                ContentTransform(
                    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(NAV_TRANSITION_MS)),
                    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(NAV_TRANSITION_MS)),
                )
            },
            entryProvider = { route ->
                NavEntry(route, contentKey = route.id) { graph.Content(it) }
            },
        )
    }

    if (renderSheet) {
        state.sheet?.let { sheetRoute ->
            ModalBottomSheet(onDismissRequest = { navigator.dismissSheet() }) {
                graph.Content(sheetRoute)
            }
        }
    }
}
