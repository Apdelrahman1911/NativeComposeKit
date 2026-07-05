package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeButtonIosBackground
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeButtonIosOptions
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeIcon
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeMenu
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeMenuItem
import io.github.apdelrahman1911.nativecomposekit.theme.NativeAppearanceScope
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Behavior-level regression guards for the button/selection family (Robolectric, no emulator) —
 * click dispatch, the loading-state accessible name, the unified menu-tap rule, and bounds/labels
 * that only exist in a real composition. Complements `NativeControlLogicTest` (pure arithmetic) and
 * `ComponentRenderUiTest` (render smoke).
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class NativeButtonsUiTest {

    @Test
    fun glass_ios_options_are_a_no_op_on_android() = runComposeUiTest {
        var clicks = 0
        setContent {
            NativeAppearanceScope {
                NativeButton(
                    "Glassy",
                    onClick = { clicks++ },
                    ios = NativeButtonIosOptions(background = NativeButtonIosBackground.Glass),
                )
            }
        }
        // Same Material rendering and semantics as any other button — the ios knob must change nothing here.
        onNodeWithText("Glassy").assertIsDisplayed()
        onNodeWithText("Glassy").performClick()
        assertEquals(1, clicks)
    }

    /** Matches the node whose click action carries the given `onClickLabel`. */
    private fun hasClickLabel(label: String): SemanticsMatcher =
        SemanticsMatcher("onClickLabel = $label") { node ->
            node.config.getOrNull(SemanticsActions.OnClick)?.label == label
        }

    @Test
    fun button_click_fires_onClick() = runComposeUiTest {
        var clicks = 0
        setContent {
            NativeAppearanceScope { NativeButton("Save", onClick = { clicks++ }) }
        }
        onNodeWithText("Save").performClick()
        assertEquals(1, clicks)
    }

    @Test
    fun loading_button_keeps_its_name_and_does_not_fire() = runComposeUiTest {
        // While loading, the label is replaced by a spinner: the node must keep an accessible name
        // (the text) and must not dispatch onClick.
        var clicks = 0
        setContent {
            NativeAppearanceScope {
                NativeButton("Save", onClick = { clicks++ }, loading = true, testTag = "btn")
            }
        }
        onNodeWithContentDescription("Save").assertIsDisplayed()
        onNodeWithTag("btn").assertIsNotEnabled()
        onNodeWithTag("btn").performClick()
        assertEquals(0, clicks)
    }

    @Test
    fun disabled_button_is_not_interactive() = runComposeUiTest {
        // Foundation keeps the OnClick semantics action defined on a disabled clickable (with Disabled
        // set), so "no click action" is asserted as: disabled semantics + a click that never dispatches.
        var clicks = 0
        setContent {
            NativeAppearanceScope { NativeButton("Save", onClick = { clicks++ }, enabled = false) }
        }
        onNodeWithText("Save").assertIsNotEnabled()
        onNodeWithText("Save").performClick()
        assertEquals(0, clicks)
    }

    @Test
    fun icon_button_keeps_its_contentDescription_while_loading() = runComposeUiTest {
        // The name lives on the button node, not the inner Icon — it must survive the spinner swap.
        setContent {
            NativeAppearanceScope {
                NativeIconButton(
                    icon = NativeIcon(Icons.Default.Add, sfSymbolName = "plus"),
                    onClick = {},
                    contentDescription = "Add",
                    loading = true,
                )
            }
        }
        onNodeWithContentDescription("Add").assertIsDisplayed()
    }

    @Test
    fun chip_remove_is_labeled_and_fires() = runComposeUiTest {
        var removed = false
        setContent {
            NativeAppearanceScope {
                NativeChip(
                    "Action",
                    style = NativeChipStyle.Input,
                    trailingIcon = Icons.Default.Close,
                    onTrailingClick = { removed = true },
                )
            }
        }
        // The remove target announces via its onClickLabel; the chip also exposes it as a custom action.
        onNode(SemanticsMatcher.keyIsDefined(SemanticsActions.CustomActions)).assertIsDisplayed()
        onNode(hasClickLabel("Remove")).performClick()
        assertTrue(removed)
    }

    @Test
    fun stepper_increment_is_disabled_at_max() = runComposeUiTest {
        setContent {
            NativeAppearanceScope { NativeStepper(value = 10, onValueChange = {}, min = 0, max = 10) }
        }
        onNodeWithContentDescription("Increment").assertIsNotEnabled()
        onNodeWithContentDescription("Decrement").assertIsEnabled()
    }

    @Test
    fun stepper_decrement_is_disabled_at_min() = runComposeUiTest {
        setContent {
            NativeAppearanceScope { NativeStepper(value = 0, onValueChange = {}, min = 0, max = 10) }
        }
        onNodeWithContentDescription("Decrement").assertIsNotEnabled()
        onNodeWithContentDescription("Increment").assertIsEnabled()
    }

    @Test
    fun segmented_control_click_reports_the_tapped_index() = runComposeUiTest {
        var picked = -1
        setContent {
            NativeAppearanceScope {
                NativeSegmentedControl(
                    options = listOf("Day", "Week", "Month"),
                    selectedIndex = 0,
                    onSelectedIndexChange = { picked = it },
                )
            }
        }
        onNodeWithText("Week").performClick()
        assertEquals(1, picked)
    }

    @Test
    fun radio_group_exposes_its_contentDescription() = runComposeUiTest {
        setContent {
            NativeAppearanceScope {
                NativeRadioGroup(
                    options = listOf("Latest", "A–Z"),
                    selected = "Latest",
                    onSelectedChange = {},
                    contentDescription = "Sort order",
                )
            }
        }
        onNodeWithContentDescription("Sort order").assertIsDisplayed()
    }

    @Test
    fun menu_bearing_button_tap_only_opens_the_menu() = runComposeUiTest {
        // Unified menu-tap rule: with a menu attached, the tap expands the menu and onClick is NOT called.
        var clicks = 0
        setContent {
            NativeAppearanceScope {
                NativeButton(
                    text = "Options",
                    onClick = { clicks++ },
                    menu = NativeMenu(items = listOf(NativeMenuItem("Duplicate", onSelect = {}))),
                )
            }
        }
        onNodeWithText("Options").performClick()
        assertEquals(0, clicks)
        // The tap went to the menu instead — its row is now composed.
        onNodeWithText("Duplicate").assertIsDisplayed()
    }
}
