package io.github.apdelrahman1911.nativecomposekit.app.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

/** A tab and how it appears in the Material navigation bar. */
data class NativeNavBarItem(val tab: NativeTab, val label: String, val icon: ImageVector)

/**
 * The Material-chrome navigation renderer, driven entirely by [NativeNavigator] (the source of truth). It drives
 * the whole app on Android; on iOS the native-chrome shell renders content through [NativeNavContent] and draws
 * its own real `UINavigationBar` + `UITabBar` instead (this Material host stays the iOS Compose-chrome fallback).
 * Compose owns the stack; no native container owns or reconciles it, which is what keeps the source of truth
 * single-owned. A platform can wrap this renderer in its own native chrome while still driving the same
 * [NativeNavigator].
 *
 * A compact `TopAppBar` sits at the top (with a back arrow once pushed) so the content fills the screen directly
 * beneath it — no tall header eating vertical space. Renders the selected tab's **top** route inside an
 * `AnimatedContent` (push slides forward, pop backward; tab switches swap instantly — the native tab
 * convention, and required so iOS overlay-placed native controls never linger over the incoming tab);
 * system back → [NativeNavigator.pop]; the
 * `NavigationBar` → [NativeNavigator.selectTab]; a non-null [NativeNavigationState.sheet] → `ModalBottomSheet`.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NativeNavHost(
    navigator: NativeNavigator,
    graph: NativeNavGraph,
    tabs: List<NativeNavBarItem>,
    modifier: Modifier = Modifier,
    title: (NativeRoute) -> String = { "" },
    actions: @Composable RowScope.() -> Unit = {},
) {
    val state = navigator.state
    val selectedTab = state.selectedTab
    val top = state.currentStack().last()
    val canPop = state.currentStack().size > 1

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(title(top)) },
                navigationIcon = {
                    if (canPop) {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = actions,
            )
        },
        bottomBar = {
            NavigationBar {
                tabs.forEach { item ->
                    NavigationBarItem(
                        selected = item.tab.id == selectedTab.id,
                        onClick = { navigator.selectTab(item.tab) },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                    )
                }
            }
        },
    ) { inner ->
        NativeNavContent(navigator, graph, Modifier.padding(inner))
    }
}

/**
 * The **content-only** navigation renderer: the current top route (push slides forward, pop backward), the
 * platform back handler (system back and the iOS edge-swipe → [NativeNavigator.pop]), and the sheet.
 * It draws NO chrome. [NativeNavHost] wraps this in Material chrome; the iOS native-chrome shell wraps it in real
 * native chrome (a `UINavigationBar` + `UITabBar`) — and either way this stays the single Kotlin-owned stack
 * renderer.
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
    val stack = state.currentStack()
    val top = stack.last()
    val canPop = stack.size > 1

    // System (gesture/button) and iOS edge-swipe back pop the current tab's stack (only when there's
    // somewhere to go).
    NativeBackHandler(enabled = canPop) { navigator.pop() }

    // Screens keep their `rememberSaveable` state (scroll positions, filters, expanded rows) across
    // back navigation, tab switches, and re-entry: each route composes inside its own saved-state slot.
    val stateHolder = rememberSaveableStateHolder()
    // Drop saved state for routes that no longer exist ANYWHERE (popped/replaced) — without this,
    // dismissed screens would accumulate state for the app's lifetime.
    val aliveIds = state.tabs.flatMapTo(mutableSetOf()) { tab -> state.stackFor(tab).map { it.id } }
        .also { ids -> state.sheet?.let { ids.add(it.id) } }
    var prevAlive by remember { mutableStateOf(aliveIds.toSet()) }
    SideEffect {
        (prevAlive - aliveIds).forEach(stateHolder::removeState)
        prevAlive = aliveIds.toSet()
    }

    // Track depth to choose the slide direction (push = forward, pop = backward) — and detect tab
    // switches, which are lateral (not hierarchical) and must not masquerade as a push/pop slide.
    var prevDepth by remember { mutableStateOf(stack.size) }
    var prevTabId by remember { mutableStateOf(state.selectedTab.id) }
    val tabChanged = prevTabId != state.selectedTab.id
    val forward = stack.size >= prevDepth
    SideEffect {
        prevDepth = stack.size
        prevTabId = state.selectedTab.id
    }

    AnimatedContent(
        targetState = top,
        modifier = modifier.fillMaxSize(),
        transitionSpec = {
            if (tabChanged) {
                // Tab switches swap INSTANTLY — the native convention on both platforms (UIKit's
                // UITabBarController never animates tab changes), and a hard requirement here: iOS
                // overlay-placed native controls (e.g. Library's UISegmentedControl) composite ABOVE the
                // Compose canvas and cannot fade with Compose content, so any animated tab transition
                // leaves the outgoing tab's native controls floating over the incoming screen until the
                // transition ends. Instant swap removes the outgoing composition the same frame.
                EnterTransition.None togetherWith ExitTransition.None
            } else {
                val enter = slideInHorizontally { w -> if (forward) w else -w }
                val exit = slideOutHorizontally { w -> if (forward) -w else w }
                enter togetherWith exit
            }
        },
        contentKey = { it.id },
        label = "NativeNavContent",
    ) { route ->
        stateHolder.SaveableStateProvider(route.id) {
            graph.Content(route)
        }
    }

    if (renderSheet) {
        state.sheet?.let { sheetRoute ->
            ModalBottomSheet(onDismissRequest = { navigator.dismissSheet() }) {
                stateHolder.SaveableStateProvider(sheetRoute.id) {
                    graph.Content(sheetRoute)
                }
            }
        }
    }
}
