package io.github.apdelrahman1911.nativecomposekit.navigation

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
public data class BrandNavBarItem(val tab: BrandTab, val label: String, val icon: ImageVector)

/**
 * The Compose/Material navigation **adapter** — a renderer driven entirely by [BrandNavigator] (the SoT). Used
 * as Android's Tier-1 shell (and the iOS-15 fallback); the iOS-16+ production shell is native SwiftUI driving
 * the same navigator via `BrandNavBridge`. A later `BrandNav3Shell(navigator, graph){}` can replace this
 * against the same public `BrandNavigator` API without touching the core.
 *
 * Renders the selected tab's **top** route inside an `AnimatedContent` (push slides forward, pop backward);
 * system/predictive back → [BrandNavigator.pop]; the `NavigationBar` → [BrandNavigator.selectTab]; a non-null
 * [BrandNavigationState.sheet] → `ModalBottomSheet`.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun BrandNavHost(
    navigator: BrandNavigator,
    graph: BrandNavGraph,
    tabs: List<BrandNavBarItem>,
    modifier: Modifier = Modifier,
    title: (BrandRoute) -> String = { "" },
    actions: @Composable RowScope.() -> Unit = {},
) {
    val state = navigator.state
    val selectedTab = state.selectedTab
    val stack = state.currentStack()
    val top = stack.last()
    val canPop = stack.size > 1

    // System / predictive back pops the current tab's stack (only enabled when there's somewhere to go back to).
    BrandBackHandler(enabled = canPop) { navigator.pop() }

    // Track depth to choose the slide direction (push = forward, pop = backward).
    var prevDepth by remember { mutableStateOf(stack.size) }
    val forward = stack.size >= prevDepth
    SideEffect { prevDepth = stack.size }

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
        AnimatedContent(
            targetState = top,
            modifier = Modifier.padding(inner),
            transitionSpec = {
                val enter = slideInHorizontally { w -> if (forward) w else -w }
                val exit = slideOutHorizontally { w -> if (forward) -w else w }
                enter togetherWith exit
            },
            contentKey = { it.id },
            label = "BrandNavHost",
        ) { route ->
            graph.Content(route)
        }
    }

    state.sheet?.let { sheetRoute ->
        ModalBottomSheet(onDismissRequest = { navigator.dismissSheet() }) {
            graph.Content(sheetRoute)
        }
    }
}
