package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isPopup
import androidx.compose.ui.test.isSelected
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeMenu
import io.github.apdelrahman1911.nativecomposekit.theme.NativeAppearanceScope
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Render-level guards for the picker/pagination batch that the pure-logic tests can't cover (they need a
 * real composition + layout). Run on the JVM via Robolectric — no emulator. The color-well dialog is an
 * `AlertDialog`, which does render under the test clock (unlike `ModalBottomSheet` content).
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class NativePickersUiTest {

    @Test
    fun colorWell_click_opens_the_dialog_and_a_preset_pick_fires_and_closes() = runComposeUiTest {
        var picked: Color? = null
        setContent {
            NativeAppearanceScope {
                NativeColorWell(color = Color(0xFF1E88E5), onColorChange = { picked = it }, testTag = "well")
            }
        }
        onNodeWithTag("well").performClick()
        waitUntil { onAllNodes(hasText("Pick a color")).fetchSemanticsNodes().isNotEmpty() }

        // Preset swatches are named "Color N of M" (the a11y fix); the first preset is 0xFFE53935.
        onNodeWithContentDescription("Color 1 of 16").performClick()
        assertEquals(Color(0xFFE53935), picked)
        waitUntil { onAllNodes(hasText("Pick a color")).fetchSemanticsNodes().isEmpty() }
    }

    @Test
    fun menu_with_empty_items_opens_no_dropdown_surface() = runComposeUiTest {
        setContent {
            NativeAppearanceScope {
                NativeButton(text = "More", onClick = {}, menu = NativeMenu(items = emptyList()))
            }
        }
        // The tap still lands (and would normally expand the menu) — but an itemless model must not open
        // an empty floating surface.
        onNodeWithText("More").performClick()
        assertEquals(0, onAllNodes(isPopup()).fetchSemanticsNodes().size)
    }

    @Test
    fun datePicker_renders_and_takes_a_programmatic_selection() = runComposeUiTest {
        var selected by mutableStateOf<Long?>(1_704_412_800_000L) // 2024-01-05T00:00:00Z
        setContent {
            NativeAppearanceScope {
                NativeDatePicker(selectedMillis = selected, onSelectedMillisChange = { selected = it })
            }
        }
        // The M3 headline renders the selected date as plain text — the deterministic observable here
        // (day-cell semantics vary by Material version/locale plumbing under Robolectric).
        onNode(hasText("Jan 5", substring = true)).assertExists()

        // Write-back parity: a programmatic change must reach Material's state (the headline follows).
        selected = 1_705_708_800_000L // 2024-01-20T00:00:00Z
        waitForIdle()
        onNode(hasText("Jan 20", substring = true)).assertExists()
    }

    @Test
    fun paginationFooter_loading_announces_the_localized_description() = runComposeUiTest {
        setContent {
            NativeAppearanceScope {
                LazyColumn(Modifier.fillMaxWidth().height(200.dp)) {
                    items(2) { NativeText("Row $it") }
                    nativePaginationFooter(NativePageLoadState.Loading)
                }
            }
        }
        onNodeWithContentDescription("Loading").assertIsDisplayed()
    }

    @Test
    fun paginationFooter_error_renders_retry_that_fires() = runComposeUiTest {
        var retried = false
        setContent {
            NativeAppearanceScope {
                LazyColumn(Modifier.fillMaxWidth().height(200.dp)) {
                    items(2) { NativeText("Row $it") }
                    nativePaginationFooter(NativePageLoadState.Error, onRetry = { retried = true })
                }
            }
        }
        onNodeWithText("Retry").performClick()
        assertTrue(retried)
    }

    @Test
    fun paginationFooter_error_without_onRetry_shows_no_dead_retry_button() = runComposeUiTest {
        setContent {
            NativeAppearanceScope {
                LazyColumn(Modifier.fillMaxWidth().height(200.dp)) {
                    items(2) { NativeText("Row $it") }
                    nativePaginationFooter(NativePageLoadState.Error)
                }
            }
        }
        onNodeWithText("Retry").assertDoesNotExist()
    }

    @Test
    fun paginationFooter_endReached_renders_nothing_interactive() = runComposeUiTest {
        setContent {
            NativeAppearanceScope {
                LazyColumn(Modifier.fillMaxWidth().height(200.dp)) {
                    items(2) { NativeText("Row $it") }
                    nativePaginationFooter(NativePageLoadState.EndReached)
                }
            }
        }
        assertEquals(0, onAllNodes(hasClickAction()).fetchSemanticsNodes().size)
    }

    @Test
    fun pager_state_overload_renders_the_current_page() = runComposeUiTest {
        setContent {
            NativeAppearanceScope {
                val state = rememberPagerState { 3 }
                NativePager(state = state, modifier = Modifier.fillMaxWidth().height(120.dp)) { page ->
                    NativeText("Page $page")
                }
            }
        }
        onNodeWithText("Page 0").assertIsDisplayed()
    }

    @Test
    fun pageControl_with_a_single_page_renders_no_dots() = runComposeUiTest {
        var pages by mutableStateOf(1)
        setContent {
            NativeAppearanceScope {
                NativePageControl(pageCount = pages, currentPage = 0, testTag = "dots")
            }
        }
        // One page → nothing renders (matches iOS hidesForSinglePage), so the tag must not exist…
        assertEquals(0, onAllNodesWithTag("dots").fetchSemanticsNodes().size)

        // …and the probe is valid: with several pages the same tag does appear.
        pages = 3
        waitForIdle()
        assertEquals(1, onAllNodesWithTag("dots").fetchSemanticsNodes().size)
    }
}
