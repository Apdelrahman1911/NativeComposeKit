package io.github.apdelrahman1911.nativecomposekit.app.navigation

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * Everything [NativeNavHost]'s **top-bar slot** receives — enough to rebuild the default bar or replace it
 * wholesale (the slot gets the current [route], so a custom bar can vary per screen without any extra API).
 * Pure display data + intents; replacing the bar never touches the stack.
 */
@Immutable
class NativeNavTopBarState(
    val route: NativeRoute,
    val title: String,
    val canPop: Boolean,
    val onBack: () -> Unit,
    val actions: @Composable RowScope.() -> Unit,
)

/** Everything [NativeNavHost]'s **bottom-bar slot** receives; same contract as [NativeNavTopBarState]. */
@Immutable
class NativeNavBottomBarState(
    val tabs: List<NativeNavBarItem>,
    val selectedTabId: String,
    val onSelectTab: (NativeTab) -> Unit,
)

/**
 * The default Material bars [NativeNavHost] renders — public so a consumer can *re-configure* them from a
 * slot (colors, centered title, item colors) without rebuilding them, keeping the simple path simple:
 * omit the slots and nothing changes; pass `topBar = { NativeNavDefaults.TopBar(it, colors = …) }` to
 * restyle; pass a fully custom composable to replace. Android stays Compose-native here on purpose — the
 * iOS shell has its own UIKit styling surface (`NativeShellStyle`), and the two are meant to diverge.
 */
object NativeNavDefaults {

    /**
     * The stock top bar: compact, back arrow when [NativeNavTopBarState.canPop], the host's actions on the
     * right. [colors] and [centeredTitle] restyle it; defaults are exactly the pre-slot appearance.
     */
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun TopBar(
        state: NativeNavTopBarState,
        modifier: Modifier = Modifier,
        colors: TopAppBarColors? = null,
        centeredTitle: Boolean = false,
        // M3 default (status-bar aware) for real host placement; pass WindowInsets(0) when embedding the
        // bar as an inline preview/exhibit.
        windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    ) {
        val barColors = colors ?: TopAppBarDefaults.topAppBarColors()
        val navigationIcon: @Composable () -> Unit = {
            if (state.canPop) {
                IconButton(onClick = state.onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        }
        if (centeredTitle) {
            CenterAlignedTopAppBar(
                title = { Text(state.title) },
                modifier = modifier,
                navigationIcon = navigationIcon,
                actions = state.actions,
                windowInsets = windowInsets,
                colors = barColors,
            )
        } else {
            TopAppBar(
                title = { Text(state.title) },
                modifier = modifier,
                navigationIcon = navigationIcon,
                actions = state.actions,
                windowInsets = windowInsets,
                colors = barColors,
            )
        }
    }

    /**
     * The stock bottom bar: one Material [NavigationBarItem] per tab. [containerColor]/[contentColor]/
     * [itemColors] restyle it; defaults are exactly the pre-slot appearance.
     */
    @Composable
    fun NavigationBar(
        state: NativeNavBottomBarState,
        modifier: Modifier = Modifier,
        containerColor: Color = NavigationBarDefaults.containerColor,
        contentColor: Color = MaterialTheme.colorScheme.contentColorFor(containerColor),
        itemColors: NavigationBarItemColors? = null,
        // M3 default (navigation-bar aware) for real host placement; WindowInsets(0) for inline exhibits.
        windowInsets: WindowInsets = NavigationBarDefaults.windowInsets,
    ) {
        val resolvedItemColors = itemColors ?: NavigationBarItemDefaults.colors()
        NavigationBar(
            modifier = modifier,
            containerColor = containerColor,
            contentColor = contentColor,
            windowInsets = windowInsets,
        ) {
            state.tabs.forEach { item ->
                NavigationBarItem(
                    selected = item.tab.id == state.selectedTabId,
                    onClick = { state.onSelectTab(item.tab) },
                    icon = { Icon(item.icon, contentDescription = item.label) },
                    label = { Text(item.label) },
                    colors = resolvedItemColors,
                )
            }
        }
    }
}
