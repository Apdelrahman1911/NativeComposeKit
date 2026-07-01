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
public data class NativeNavBarItem(val tab: NativeTab, val label: String, val icon: ImageVector)

/**
 * The Compose navigation **renderer**, driven entirely by [NativeNavigator] (the source of truth). This is the
 * shell on BOTH platforms — Android, and iOS (hosted in one `ComposeUIViewController`). Compose owns the stack;
 * no native SwiftUI/UIKit container owns or reconciles it, which is what keeps the source of truth single-owned.
 * The `NavigationBar` chrome here is Material; a platform can wrap this renderer in its own native chrome while
 * still driving the same public [NativeNavigator] API.
 *
 * Renders the selected tab's **top** route inside an `AnimatedContent` (push slides forward, pop backward);
 * system/predictive back → [NativeNavigator.pop]; the `NavigationBar` → [NativeNavigator.selectTab]; a non-null
 * [NativeNavigationState.sheet] → `ModalBottomSheet`.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun NativeNavHost(
    navigator: NativeNavigator,
    graph: NativeNavGraph,
    tabs: List<NativeNavBarItem>,
    modifier: Modifier = Modifier,
    title: (NativeRoute) -> String = { "" },
    actions: @Composable RowScope.() -> Unit = {},
) {
    val state = navigator.state
    val selectedTab = state.selectedTab
    val stack = state.currentStack()
    val top = stack.last()
    val canPop = stack.size > 1

    // System / predictive back pops the current tab's stack (only enabled when there's somewhere to go back to).
    NativeBackHandler(enabled = canPop) { navigator.pop() }

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
            label = "NativeNavHost",
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
