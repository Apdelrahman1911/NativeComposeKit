package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.runComposeUiTest
import io.github.apdelrahman1911.nativecomposekit.theme.LocalNativeSurface
import io.github.apdelrahman1911.nativecomposekit.theme.NativeAppearanceScope
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Structure-component guards: list rows (disabled/selected semantics), sections (heading), scaffold
 * surface publication, card overrides, and pull-refresh accessibility.
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34]) // Robolectric's bundled runtime doesn't cover compileSdk 36 yet
class NativeStructureUiTest {

    @Test
    fun disabled_row_announces_disabled_and_exposes_no_actions() = runComposeUiTest {
        var clicked = 0
        setContent {
            NativeAppearanceScope {
                NativeListItem(
                    headline = "Archive",
                    onClick = { clicked++ },
                    enabled = false,
                    swipeAction = NativeSwipeAction("Delete", onAction = { clicked++ }),
                    testTag = "row",
                )
            }
        }
        val row = onNodeWithTag("row")
        // The click semantics stay present but DISABLED (screen readers announce the state)…
        row.assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Disabled))
        row.performClick()
        assertEquals(0, clicked)
        // …and gesture-only affordances are fully withdrawn.
        val actions = row.fetchSemanticsNode().config.getOrNull(
            androidx.compose.ui.semantics.SemanticsActions.CustomActions,
        )
        assertTrue(actions.isNullOrEmpty())
    }

    @Test
    fun selected_row_shows_state_and_enabled_row_keeps_actions() = runComposeUiTest {
        var swiped = false
        setContent {
            NativeAppearanceScope {
                NativeListItem(
                    headline = "Ongoing",
                    onClick = {},
                    selected = true,
                    swipeAction = NativeSwipeAction("Archive", onAction = { swiped = true }),
                    testTag = "row",
                )
            }
        }
        val row = onNodeWithTag("row")
        row.assert(SemanticsMatcher.expectValue(SemanticsProperties.Selected, true))
        val actions = row.fetchSemanticsNode().config[androidx.compose.ui.semantics.SemanticsActions.CustomActions]
        assertEquals("Archive", actions.single().label)
        actions.single().action?.invoke()
        assertTrue(swiped)
    }

    @Test
    fun section_header_is_a_heading_and_keyed_overload_renders_rows() = runComposeUiTest {
        setContent {
            NativeAppearanceScope {
                NativeListSection(
                    items = listOf("A", "B"),
                    key = { it },
                    header = "READER",
                ) { item -> NativeListItem(headline = item) }
            }
        }
        onNodeWithText("READER").assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading))
        onNodeWithText("A").assertIsDisplayed()
        onNodeWithText("B").assertIsDisplayed()
    }

    @Test
    fun scaffold_publishes_its_container_as_the_native_surface() = runComposeUiTest {
        val page = Color(0xFF102030)
        var seen: Color = Color.Unspecified
        setContent {
            NativeAppearanceScope {
                NativeScaffold(containerColor = page) { _ ->
                    seen = LocalNativeSurface.current
                }
            }
        }
        assertEquals(page, seen)
    }

    @Test
    fun card_overrides_reach_content_and_translucent_container_is_not_published() = runComposeUiTest {
        val brand = Color(0xFF223344)
        var surface: Color = Color.Unspecified
        var translucentSurface: Color = Color.Unspecified
        setContent {
            NativeAppearanceScope {
                NativeCard(containerColor = brand) { surface = LocalNativeSurface.current }
                NativeCard(containerColor = brand.copy(alpha = 0.5f)) {
                    translucentSurface = LocalNativeSurface.current
                }
            }
        }
        assertEquals(brand, surface)
        // The see-through card passes the OUTER surface through (the page behind it), never its own fill.
        assertTrue(translucentSurface != brand.copy(alpha = 0.5f))
        assertTrue(translucentSurface != Color.Unspecified)
    }

    @Test
    fun card_long_click_fires_and_disabled_card_announces_disabled() = runComposeUiTest {
        var long = 0
        setContent {
            NativeAppearanceScope {
                NativeCard(
                    onClick = {},
                    onLongClick = { long++ },
                    onLongClickLabel = "Options",
                    testTag = "card",
                ) { NativeText("Cell") }
                NativeCard(onClick = {}, enabled = false, testTag = "disabled-card") { NativeText("Off") }
            }
        }
        onNodeWithTag("card").performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.OnLongClick)
        assertEquals(1, long)
        onNodeWithTag("disabled-card").assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Disabled))
    }

    @Test
    fun pull_refresh_exposes_a_refresh_action_that_fires() = runComposeUiTest {
        var refreshed = 0
        setContent {
            NativeAppearanceScope {
                NativePullRefresh(
                    isRefreshing = false,
                    onRefresh = { refreshed++ },
                    testTag = "refresh",
                ) { NativeText("List") }
            }
        }
        val node = onNodeWithTag("refresh")
        val actions = node.fetchSemanticsNode().config[androidx.compose.ui.semantics.SemanticsActions.CustomActions]
        assertEquals("Refresh", actions.single().label)
        actions.single().action?.invoke()
        assertEquals(1, refreshed)
    }

    @Test
    fun top_bar_title_is_a_heading_and_back_control_gets_the_fallback_name() = runComposeUiTest {
        setContent {
            NativeAppearanceScope {
                NativeTopBar(
                    title = "Library",
                    navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                    onNavigationClick = {},
                )
            }
        }
        onNodeWithText("Library").assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading))
        // No navigationContentDescription passed — the localized fallback names the control.
        onNodeWithContentDescription("Back").assertIsDisplayed()
    }

    @Test
    fun content_state_error_uses_its_own_icon_slot_and_retry_fires() = runComposeUiTest {
        var retried = 0
        var state by mutableStateOf<NativeLoadState<String>>(NativeLoadState.Loading)
        setContent {
            NativeAppearanceScope {
                NativeContentState(
                    state = state,
                    onRetry = { retried++ },
                ) { NativeText(it) }
            }
        }
        state = NativeLoadState.Error("boom")
        waitForIdle()
        onNodeWithText("boom").assertIsDisplayed()
        onNodeWithText("Retry").performClick()
        assertEquals(1, retried)
        state = NativeLoadState.Content("Loaded")
        waitForIdle()
        onNodeWithText("Loaded").assertIsDisplayed()
    }
}
