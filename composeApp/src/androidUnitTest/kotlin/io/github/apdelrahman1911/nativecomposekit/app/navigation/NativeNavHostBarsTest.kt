package io.github.apdelrahman1911.nativecomposekit.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Text
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import io.github.apdelrahman1911.nativecomposekit.chrome.NativeBarConfig
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Pins the Material host's chrome contract: the DEFAULT bars keep today's exact structure (title, back
 * arrow when pushed, tab items), [NativeBarConfig] hides bars per route, and the [NativeNavHost] slots
 * replace or restyle the bars without touching navigation.
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34]) // Robolectric's bundled runtime doesn't cover compileSdk 36 yet
class NativeNavHostBarsTest {

    private enum class Tab(override val id: String) : NativeTab { Home("home"), Settings("settings") }
    private data class Route(override val id: String) : NativeRoute

    private fun navigator() = createNativeNavigator(
        tabs = listOf(Tab.Home, Tab.Settings),
        initialTab = Tab.Home,
        rootRoutes = { tab -> Route("${tab.id}-root") },
    )

    private val graph = nativeNavGraph {
        screen<Route> { route -> Text("content:${route.id}") }
    }

    private val tabs = listOf(
        NativeNavBarItem(Tab.Home, "Home", Icons.Filled.Home),
        NativeNavBarItem(Tab.Settings, "Settings", Icons.Filled.Settings),
    )

    @Test
    fun default_bars_keep_todays_structure() = runComposeUiTest {
        val nav = navigator()
        setContent {
            NativeNavHost(nav, graph, tabs, title = { "Title:${it.id}" })
        }
        // Root: title + both tab items, no back arrow.
        onNodeWithText("Title:home-root").assertIsDisplayed()
        onNodeWithText("Home").assertIsDisplayed()
        onNodeWithText("Settings").assertIsDisplayed()
        assertEquals(0, onAllNodes(androidx.compose.ui.test.hasContentDescription("Back")).fetchSemanticsNodes().size)

        // Pushed: back arrow appears and pops on click.
        nav.push(Route("detail"))
        waitForIdle()
        onNodeWithText("Title:detail").assertIsDisplayed()
        onNodeWithContentDescription("Back").assertIsDisplayed().performClick()
        waitForIdle()
        onNodeWithText("Title:home-root").assertIsDisplayed()
    }

    @Test
    fun bar_config_hides_bars_for_an_immersive_route() = runComposeUiTest {
        val nav = navigator()
        setContent {
            NativeNavHost(
                nav, graph, tabs,
                title = { "Title:${it.id}" },
                barConfig = { route ->
                    if (route.id == "reader") NativeBarConfig(hidesTopBar = true, hidesTabBar = true)
                    else NativeBarConfig.Default
                },
            )
        }
        onNodeWithText("Title:home-root").assertIsDisplayed()

        nav.push(Route("reader"))
        waitForIdle()
        // Both bars gone, content still there.
        assertEquals(0, onAllNodes(androidx.compose.ui.test.hasText("Title:reader")).fetchSemanticsNodes().size)
        assertEquals(0, onAllNodes(androidx.compose.ui.test.hasText("Home")).fetchSemanticsNodes().size)
        onNodeWithText("content:reader").assertIsDisplayed()

        // Popping restores both bars.
        nav.pop()
        waitForIdle()
        onNodeWithText("Title:home-root").assertIsDisplayed()
        onNodeWithText("Home").assertIsDisplayed()
    }

    @Test
    fun top_bar_slot_replaces_the_default_and_restyled_default_still_works() = runComposeUiTest {
        val nav = navigator()
        setContent {
            NativeNavHost(
                nav, graph, tabs,
                title = { "Title:${it.id}" },
                topBar = { state ->
                    if (state.route.id == "home-root") Text("custom-bar:${state.title}")
                    else NativeNavDefaults.TopBar(state, centeredTitle = true) // restyled default
                },
            )
        }
        // Custom slot rendered; the default bar's title node is the custom one now.
        onNodeWithText("custom-bar:Title:home-root").assertIsDisplayed()

        // A pushed route falls back to the (restyled) default bar with the working back arrow.
        nav.push(Route("detail"))
        waitForIdle()
        onNodeWithText("Title:detail").assertIsDisplayed()
        onNodeWithContentDescription("Back").assertIsDisplayed()
    }
}
