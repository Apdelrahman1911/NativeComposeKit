package io.github.apdelrahman1911.nativecomposekit.app.navigation

import androidx.compose.animation.AnimatedContent
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
 * `AnimatedContent` (push slides forward, pop backward); system/predictive back → [NativeNavigator.pop]; the
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
 * platform back handler (system/predictive back and the iOS edge-swipe → [NativeNavigator.pop]), and the sheet.
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

    // System / predictive / edge-swipe back pops the current tab's stack (only when there's somewhere to go).
    NativeBackHandler(enabled = canPop) { navigator.pop() }

    // Track depth to choose the slide direction (push = forward, pop = backward).
    var prevDepth by remember { mutableStateOf(stack.size) }
    val forward = stack.size >= prevDepth
    SideEffect { prevDepth = stack.size }

    AnimatedContent(
        targetState = top,
        modifier = modifier.fillMaxSize(),
        transitionSpec = {
            val enter = slideInHorizontally { w -> if (forward) w else -w }
            val exit = slideOutHorizontally { w -> if (forward) -w else w }
            enter togetherWith exit
        },
        contentKey = { it.id },
        label = "NativeNavContent",
    ) { route ->
        graph.Content(route)
    }

    if (renderSheet) {
        state.sheet?.let { sheetRoute ->
            ModalBottomSheet(onDismissRequest = { navigator.dismissSheet() }) {
                graph.Content(sheetRoute)
            }
        }
    }
}
