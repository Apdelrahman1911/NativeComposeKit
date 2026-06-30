package com.ukkera.brandkit.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import com.ukkera.brandkit.components.feedback.BrandFeedbackStatus
import com.ukkera.brandkit.components.feedback.BrandInlineStatus
import com.ukkera.brandkit.components.model.BrandTextContentType
import com.ukkera.brandkit.theme.BrandAppearanceScope
import com.ukkera.brandkit.theme.BrandCapabilities
import com.ukkera.brandkit.theme.LocalBrandCapabilities
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Render-level regression guards that the pure-logic tests can't cover (they need a real composition + layout).
 * Run on the JVM via Robolectric — no emulator. Complements `SurfaceColorsTest` etc.
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ComponentRenderUiTest {

    @Test
    fun inlineStatus_dismiss_is_present_labeled_and_interactive() = runComposeUiTest {
        setContent {
            BrandAppearanceScope {
                BrandInlineStatus("Couldn't reach the server.", status = BrandFeedbackStatus.Error, onDismiss = {})
            }
        }
        // The dismiss affordance must be present, labeled, and the (only) clickable node. Its ≥48dp touch target
        // is set by minimumInteractiveComponentSize() in code (verified visually in ComponentMatrixScreen);
        // the multiplatform test artifact doesn't expose the assertTouch* APIs to read touch bounds under Robolectric.
        onNodeWithContentDescription("Dismiss").assertIsDisplayed()
        onNode(hasClickAction()).assertIsDisplayed()
    }

    @Test
    fun surface_sensitive_components_compose_inside_a_filled_card() = runComposeUiTest {
        // The card publishes surfaceVariant; this is the context that exposed the surface-adaptation bugs.
        // If any of these threw or failed to lay out, the assertions below would fail.
        setContent {
            BrandAppearanceScope {
                BrandCard(variant = BrandCardVariant.Filled) {
                    Column {
                        BrandSkeleton(Modifier.fillMaxWidth().height(16.dp))
                        BrandChip("Fantasy", style = BrandChipStyle.Suggestion)
                        BrandRating(4.5f)
                        BrandListItem(
                            "Chapter 1",
                            onClick = {},
                            swipeAction = BrandSwipeAction(label = "Mark read", onAction = {}),
                        )
                    }
                }
            }
        }
        onNodeWithText("Fantasy").assertIsDisplayed()
        onNodeWithText("Chapter 1").assertIsDisplayed()
    }

    @Test
    fun rating_exposes_value_semantics() = runComposeUiTest {
        setContent {
            BrandAppearanceScope { BrandRating(4.5f) }
        }
        onNodeWithContentDescription("Rating: 4.5 out of 5").assertIsDisplayed()
    }

    @Test
    fun slider_exposes_name_without_losing_its_value_semantics() = runComposeUiTest {
        setContent {
            BrandAppearanceScope { BrandSlider(value = 0.5f, onValueChange = {}, contentDescription = "Volume") }
        }
        // The added name must NOT clobber Material's built-in range semantics (the mergeDescendants concern).
        onNodeWithContentDescription("Volume").assertIsDisplayed()
        onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo)).assertIsDisplayed()
    }

    @Test
    fun stepper_exposes_its_name() = runComposeUiTest {
        setContent {
            BrandAppearanceScope { BrandStepper(value = 3, onValueChange = {}, contentDescription = "Quantity") }
        }
        onNodeWithContentDescription("Quantity").assertIsDisplayed()
    }

    @Test
    fun segmentedControl_exposes_its_name() = runComposeUiTest {
        setContent {
            BrandAppearanceScope {
                BrandSegmentedControl(
                    options = listOf("Day", "Week"),
                    selectedIndex = 0,
                    onSelectedIndexChange = {},
                    contentDescription = "Period",
                )
            }
        }
        onNodeWithContentDescription("Period").assertIsDisplayed()
    }

    @Test
    fun textField_applies_explicit_contentDescription() = runComposeUiTest {
        setContent {
            BrandAppearanceScope { BrandTextField(value = "", onValueChange = {}, contentDescription = "Search") }
        }
        onNodeWithContentDescription("Search").assertIsDisplayed()
    }

    @Test
    fun listItem_swipe_action_is_reachable_as_a_custom_action() = runComposeUiTest {
        setContent {
            BrandAppearanceScope {
                BrandListItem(
                    "Chapter 1",
                    onClick = {},
                    swipeAction = BrandSwipeAction(label = "Mark read", onAction = {}),
                )
            }
        }
        // The gesture-only swipe must be exposed to screen readers via a custom accessibility action.
        onNode(SemanticsMatcher.keyIsDefined(SemanticsActions.CustomActions)).assertIsDisplayed()
    }

    @Test
    fun inlineStatus_error_is_an_assertive_live_region() = runComposeUiTest {
        setContent {
            BrandAppearanceScope {
                BrandInlineStatus("Couldn't reach the server.", status = BrandFeedbackStatus.Error)
            }
        }
        onNode(SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Assertive)).assertIsDisplayed()
    }

    @Test
    fun card_with_onClick_is_an_accessible_button() = runComposeUiTest {
        setContent {
            BrandAppearanceScope {
                BrandCard(onClick = {}, onClickLabel = "Open") { BrandText("Cover") }
            }
        }
        // The clickable card declares Role.Button (was a bare clickable with no role).
        onNode(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button)).assertIsDisplayed()
    }

    @Test
    fun listItem_with_onClick_is_an_accessible_button() = runComposeUiTest {
        setContent {
            BrandAppearanceScope {
                BrandListItem("Settings", onClick = {}, onClickLabel = "Open settings")
            }
        }
        onNode(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button)).assertIsDisplayed()
    }

    @Test
    fun skeleton_composes_when_reduce_motion_is_on() = runComposeUiTest {
        // Under reduce-motion the shimmer defaults off (static block); it must still lay out.
        setContent {
            BrandAppearanceScope {
                CompositionLocalProvider(LocalBrandCapabilities provides BrandCapabilities(isReduceMotionEnabled = true)) {
                    BrandSkeleton(Modifier.testTag("sk").fillMaxWidth().height(16.dp))
                }
            }
        }
        onNodeWithTag("sk").assertIsDisplayed()
    }

    // ---- P1 Batch 2 (T5) regressions ----

    @Test
    fun toggle_readonly_null_callback_still_renders() = runComposeUiTest {
        // Batch 2 made onCheckedChange nullable (read-only display toggle). It must still render + be named.
        setContent {
            BrandAppearanceScope { BrandToggle(checked = true, onCheckedChange = null, contentDescription = "Wifi") }
        }
        onNodeWithContentDescription("Wifi").assertIsDisplayed()
    }

    @Test
    fun toggle_interactive_click_fires_callback() = runComposeUiTest {
        var changed = false
        setContent {
            BrandAppearanceScope {
                BrandToggle(checked = false, onCheckedChange = { changed = true }, contentDescription = "Wifi")
            }
        }
        onNodeWithContentDescription("Wifi").performClick()
        assertTrue(changed)
    }

    @Test
    fun textField_contentType_exposes_autofill_semantics() = runComposeUiTest {
        // Batch 2 unified content type into a cross-platform param wired to Android Compose autofill.
        setContent {
            BrandAppearanceScope {
                BrandTextField(value = "", onValueChange = {}, label = "Email", contentType = BrandTextContentType.EmailAddress)
            }
        }
        onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.ContentType)).assertIsDisplayed()
    }

    @Test
    fun segmentedControl_renders_all_options() = runComposeUiTest {
        // Smoke for the onSelect → onSelectedIndexChange rename: all options render with the brand style.
        setContent {
            BrandAppearanceScope {
                BrandSegmentedControl(
                    options = listOf("Day", "Week", "Month"),
                    selectedIndex = 0,
                    onSelectedIndexChange = {},
                    contentDescription = "Period",
                )
            }
        }
        onNodeWithText("Day").assertIsDisplayed()
        onNodeWithText("Week").assertIsDisplayed()
        onNodeWithText("Month").assertIsDisplayed()
    }

    @Test
    fun radioGroup_select_fires_change() = runComposeUiTest {
        // Behavioral guard for the onSelect → onSelectedChange rename, on the pure-Compose control where
        // Robolectric hit-testing is reliable (the row is a single selectable target).
        var picked = ""
        setContent {
            BrandAppearanceScope {
                BrandRadioGroup(
                    options = listOf("Latest", "A–Z"),
                    selected = "Latest",
                    onSelectedChange = { picked = it },
                )
            }
        }
        onNodeWithText("A–Z").performClick()
        assertEquals("A–Z", picked)
    }

    // ---- previously-untested renderers ----

    @Test
    fun pageControl_announces_clamped_position() = runComposeUiTest {
        // currentPage is clamped to 0..pageCount-1 in commonMain; the Android dots announce "Page N of M".
        setContent {
            BrandAppearanceScope { BrandPageControl(pageCount = 5, currentPage = 99, onCurrentPageChange = {}) }
        }
        onNodeWithContentDescription("Page 5 of 5").assertIsDisplayed()
    }

    @Test
    fun checkbox_with_label_is_a_merged_checkbox() = runComposeUiTest {
        setContent {
            BrandAppearanceScope { BrandCheckbox(checked = true, onCheckedChange = {}, label = "Download in HD") }
        }
        onNode(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Checkbox)).assertIsDisplayed()
        onNodeWithText("Download in HD").assertIsDisplayed()
    }

    @Test
    fun searchBar_shows_its_placeholder() = runComposeUiTest {
        setContent {
            BrandAppearanceScope { BrandSearchBar(value = "", onValueChange = {}, placeholder = "Search manga…") }
        }
        onNodeWithText("Search manga…").assertIsDisplayed()
    }

    @Test
    fun colorWell_exposes_a_default_selected_color_name() = runComposeUiTest {
        setContent {
            BrandAppearanceScope { BrandColorWell(color = Color(0xFF1E88E5), onColorChange = {}) }
        }
        onNodeWithContentDescription("Selected color").assertIsDisplayed()
    }

    @Test
    fun emptyState_renders_title_and_action() = runComposeUiTest {
        setContent {
            BrandAppearanceScope { BrandEmptyState(title = "Empty", actionLabel = "Browse", onAction = {}) }
        }
        onNodeWithText("Empty").assertIsDisplayed()
        onNodeWithText("Browse").assertIsDisplayed()
    }

    @Test
    fun avatar_announces_its_content_description() = runComposeUiTest {
        setContent {
            BrandAppearanceScope { BrandAvatar(initials = "JD", contentDescription = "Jane Doe") }
        }
        onNodeWithContentDescription("Jane Doe").assertIsDisplayed()
    }

    // ---- P1 Batch 4 (T7) structural components ----

    @Test
    fun topBar_renders_title_and_navigation() = runComposeUiTest {
        var backed = false
        setContent {
            BrandAppearanceScope {
                BrandTopBar(
                    title = "Library",
                    navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                    onNavigationClick = { backed = true },
                    navigationContentDescription = "Back",
                )
            }
        }
        onNodeWithText("Library").assertIsDisplayed()
        onNodeWithContentDescription("Back").performClick()
        assertTrue(backed)
    }

    @Test
    fun contentState_loading_shows_spinner() = runComposeUiTest {
        setContent {
            BrandAppearanceScope { BrandContentState<String>(BrandLoadState.Loading) { BrandText(it) } }
        }
        onNodeWithContentDescription("Loading").assertIsDisplayed()
    }

    @Test
    fun contentState_empty_shows_empty_title() = runComposeUiTest {
        setContent {
            BrandAppearanceScope {
                BrandContentState<String>(BrandLoadState.Empty, emptyTitle = "No items") { BrandText(it) }
            }
        }
        onNodeWithText("No items").assertIsDisplayed()
    }

    @Test
    fun contentState_error_shows_retry_only_when_onRetry_present() = runComposeUiTest {
        var retried = false
        setContent {
            BrandAppearanceScope {
                BrandContentState<String>(BrandLoadState.Error("Network down"), onRetry = { retried = true }) { BrandText(it) }
            }
        }
        onNodeWithText("Network down").assertIsDisplayed()
        onNodeWithText("Retry").performClick()
        assertTrue(retried)
    }

    @Test
    fun contentState_error_hides_retry_when_no_onRetry() = runComposeUiTest {
        setContent {
            BrandAppearanceScope {
                BrandContentState<String>(BrandLoadState.Error("Network down")) { BrandText(it) }
            }
        }
        onNodeWithText("Retry").assertDoesNotExist()
    }

    @Test
    fun contentState_content_renders_the_payload() = runComposeUiTest {
        setContent {
            BrandAppearanceScope {
                BrandContentState(BrandLoadState.Content("Chapter 1")) { BrandText(it) }
            }
        }
        onNodeWithText("Chapter 1").assertIsDisplayed()
    }

    // ---- P2 Batch 2 (native-parity additions) ----

    @Test
    fun rating_disabled_is_readonly_even_with_callback() = runComposeUiTest {
        // enabled = false forces read-only: even with onRatingChange given, it must take the display path
        // (a `contentDescription`, not the interactive `stateDescription` / per-star buttons).
        setContent {
            BrandAppearanceScope {
                BrandRating(rating = 3f, onRatingChange = {}, enabled = false)
            }
        }
        onNodeWithContentDescription("Rating: 3 out of 5").assertIsDisplayed()
    }

    @Test
    fun rating_enabled_with_callback_is_interactive() = runComposeUiTest {
        // Contrast: enabled + onRatingChange = interactive. max = 1 → exactly one clickable star node.
        var picked = 0f
        setContent {
            BrandAppearanceScope {
                BrandRating(rating = 0f, onRatingChange = { picked = it }, max = 1)
            }
        }
        onNode(hasClickAction()).performClick()
        assertEquals(1f, picked)
    }

    // ---- P2 Batch 3 (new components) ----

    @Test
    fun dialog_renders_title_content_and_actions() = runComposeUiTest {
        setContent {
            BrandAppearanceScope {
                BrandDialog(onDismissRequest = {}, title = "Rename", actions = { BrandButton("Save", onClick = {}) }) {
                    BrandText("Body")
                }
            }
        }
        onNodeWithText("Rename").assertIsDisplayed()
        onNodeWithText("Body").assertIsDisplayed()
        onNodeWithText("Save").assertIsDisplayed()
    }

    @Suppress("DEPRECATION") // BrandTabBar is deprecated (→ BrandSegmentedControl); kept under test until removal.
    @Test
    fun tabBar_renders_tabs_and_selection_fires() = runComposeUiTest {
        var picked = -1
        setContent {
            BrandAppearanceScope {
                BrandTabBar(tabs = listOf("Overview", "Chapters"), selectedIndex = 0, onSelectedIndexChange = { picked = it })
            }
        }
        onNodeWithText("Overview").assertIsDisplayed()
        onNodeWithText("Chapters").performClick()
        assertEquals(1, picked)
    }

    @Suppress("DEPRECATION") // BrandTooltip is deprecated; kept under test until removal.
    @Test
    fun tooltip_renders_its_anchor() = runComposeUiTest {
        setContent {
            BrandAppearanceScope { BrandTooltip(text = "Add to library") { BrandText("Anchor") } }
        }
        onNodeWithText("Anchor").assertIsDisplayed()
    }

    @Test
    fun brandHeading_marks_the_node_as_a_heading() = runComposeUiTest {
        setContent {
            BrandAppearanceScope { BrandText("Section", modifier = Modifier.brandHeading()) }
        }
        onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading)).assertIsDisplayed()
    }
}
