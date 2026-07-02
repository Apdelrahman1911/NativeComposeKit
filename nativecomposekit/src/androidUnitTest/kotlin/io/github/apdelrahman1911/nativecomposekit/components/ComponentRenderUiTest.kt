package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import io.github.apdelrahman1911.nativecomposekit.components.feedback.NativeFeedbackStatus
import io.github.apdelrahman1911.nativecomposekit.components.feedback.NativeInlineStatus
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeDialogColors
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeTextContentType
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeFieldFocus
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeFieldInput
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeImeAction
import io.github.apdelrahman1911.nativecomposekit.theme.NativeAppearanceScope
import io.github.apdelrahman1911.nativecomposekit.theme.NativeCapabilities
import io.github.apdelrahman1911.nativecomposekit.theme.LocalNativeCapabilities
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
            NativeAppearanceScope {
                NativeInlineStatus("Couldn't reach the server.", status = NativeFeedbackStatus.Error, onDismiss = {})
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
            NativeAppearanceScope {
                NativeCard(variant = NativeCardVariant.Filled) {
                    Column {
                        NativeSkeleton(Modifier.fillMaxWidth().height(16.dp))
                        NativeChip("Fantasy", style = NativeChipStyle.Suggestion)
                        NativeRating(4.5f)
                        NativeListItem(
                            "Chapter 1",
                            onClick = {},
                            swipeAction = NativeSwipeAction(label = "Mark read", onAction = {}),
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
            NativeAppearanceScope { NativeRating(4.5f) }
        }
        onNodeWithContentDescription("Rating: 4.5 out of 5").assertIsDisplayed()
    }

    @Test
    fun slider_exposes_name_without_losing_its_value_semantics() = runComposeUiTest {
        setContent {
            NativeAppearanceScope { NativeSlider(value = 0.5f, onValueChange = {}, contentDescription = "Volume") }
        }
        // The added name must NOT clobber Material's built-in range semantics (the mergeDescendants concern).
        onNodeWithContentDescription("Volume").assertIsDisplayed()
        onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo)).assertIsDisplayed()
    }

    @Test
    fun stepper_exposes_its_name() = runComposeUiTest {
        setContent {
            NativeAppearanceScope { NativeStepper(value = 3, onValueChange = {}, contentDescription = "Quantity") }
        }
        onNodeWithContentDescription("Quantity").assertIsDisplayed()
    }

    @Test
    fun segmentedControl_exposes_its_name() = runComposeUiTest {
        setContent {
            NativeAppearanceScope {
                NativeSegmentedControl(
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
            NativeAppearanceScope { NativeTextField(value = "", onValueChange = {}, contentDescription = "Search") }
        }
        onNodeWithContentDescription("Search").assertIsDisplayed()
    }

    @Test
    fun listItem_swipe_action_is_reachable_as_a_custom_action() = runComposeUiTest {
        setContent {
            NativeAppearanceScope {
                NativeListItem(
                    "Chapter 1",
                    onClick = {},
                    swipeAction = NativeSwipeAction(label = "Mark read", onAction = {}),
                )
            }
        }
        // The gesture-only swipe must be exposed to screen readers via a custom accessibility action.
        onNode(SemanticsMatcher.keyIsDefined(SemanticsActions.CustomActions)).assertIsDisplayed()
    }

    @Test
    fun inlineStatus_error_is_an_assertive_live_region() = runComposeUiTest {
        setContent {
            NativeAppearanceScope {
                NativeInlineStatus("Couldn't reach the server.", status = NativeFeedbackStatus.Error)
            }
        }
        onNode(SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Assertive)).assertIsDisplayed()
    }

    @Test
    fun card_with_onClick_is_an_accessible_button() = runComposeUiTest {
        setContent {
            NativeAppearanceScope {
                NativeCard(onClick = {}, onClickLabel = "Open") { NativeText("Cover") }
            }
        }
        // The clickable card declares Role.Button (was a bare clickable with no role).
        onNode(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button)).assertIsDisplayed()
    }

    @Test
    fun listItem_with_onClick_is_an_accessible_button() = runComposeUiTest {
        setContent {
            NativeAppearanceScope {
                NativeListItem("Settings", onClick = {}, onClickLabel = "Open settings")
            }
        }
        onNode(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button)).assertIsDisplayed()
    }

    @Test
    fun skeleton_composes_when_reduce_motion_is_on() = runComposeUiTest {
        // Under reduce-motion the shimmer defaults off (static block); it must still lay out.
        setContent {
            NativeAppearanceScope {
                CompositionLocalProvider(LocalNativeCapabilities provides NativeCapabilities(isReduceMotionEnabled = true)) {
                    NativeSkeleton(Modifier.testTag("sk").fillMaxWidth().height(16.dp))
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
            NativeAppearanceScope { NativeToggle(checked = true, onCheckedChange = null, contentDescription = "Wifi") }
        }
        onNodeWithContentDescription("Wifi").assertIsDisplayed()
    }

    @Test
    fun toggle_interactive_click_fires_callback() = runComposeUiTest {
        var changed = false
        setContent {
            NativeAppearanceScope {
                NativeToggle(checked = false, onCheckedChange = { changed = true }, contentDescription = "Wifi")
            }
        }
        onNodeWithContentDescription("Wifi").performClick()
        assertTrue(changed)
    }

    @Test
    fun textField_contentType_exposes_autofill_semantics() = runComposeUiTest {
        // Batch 2 unified content type into a cross-platform param wired to Android Compose autofill.
        setContent {
            NativeAppearanceScope {
                NativeTextField(value = "", onValueChange = {}, label = "Email", contentType = NativeTextContentType.EmailAddress)
            }
        }
        onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.ContentType)).assertIsDisplayed()
    }

    @Test
    fun segmentedControl_renders_all_options() = runComposeUiTest {
        // Smoke for the onSelect → onSelectedIndexChange rename: all options render with the brand style.
        setContent {
            NativeAppearanceScope {
                NativeSegmentedControl(
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
            NativeAppearanceScope {
                NativeRadioGroup(
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
            NativeAppearanceScope { NativePageControl(pageCount = 5, currentPage = 99, onCurrentPageChange = {}) }
        }
        onNodeWithContentDescription("Page 5 of 5").assertIsDisplayed()
    }

    @Test
    fun checkbox_with_label_is_a_merged_checkbox() = runComposeUiTest {
        setContent {
            NativeAppearanceScope { NativeCheckbox(checked = true, onCheckedChange = {}, label = "Download in HD") }
        }
        onNode(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Checkbox)).assertIsDisplayed()
        onNodeWithText("Download in HD").assertIsDisplayed()
    }

    @Test
    fun searchBar_shows_its_placeholder() = runComposeUiTest {
        setContent {
            NativeAppearanceScope { NativeSearchBar(value = "", onValueChange = {}, placeholder = "Search manga…") }
        }
        onNodeWithText("Search manga…").assertIsDisplayed()
    }

    @Test
    fun colorWell_exposes_a_default_selected_color_name() = runComposeUiTest {
        setContent {
            NativeAppearanceScope { NativeColorWell(color = Color(0xFF1E88E5), onColorChange = {}) }
        }
        onNodeWithContentDescription("Selected color").assertIsDisplayed()
    }

    @Test
    fun emptyState_renders_title_and_action() = runComposeUiTest {
        setContent {
            NativeAppearanceScope { NativeEmptyState(title = "Empty", actionLabel = "Browse", onAction = {}) }
        }
        onNodeWithText("Empty").assertIsDisplayed()
        onNodeWithText("Browse").assertIsDisplayed()
    }

    @Test
    fun avatar_announces_its_content_description() = runComposeUiTest {
        setContent {
            NativeAppearanceScope { NativeAvatar(initials = "JD", contentDescription = "Jane Doe") }
        }
        onNodeWithContentDescription("Jane Doe").assertIsDisplayed()
    }

    // ---- P1 Batch 4 (T7) structural components ----

    @Test
    fun topBar_renders_title_and_navigation() = runComposeUiTest {
        var backed = false
        setContent {
            NativeAppearanceScope {
                NativeTopBar(
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
            NativeAppearanceScope { NativeContentState<String>(NativeLoadState.Loading) { NativeText(it) } }
        }
        onNodeWithContentDescription("Loading").assertIsDisplayed()
    }

    @Test
    fun contentState_empty_shows_empty_title() = runComposeUiTest {
        setContent {
            NativeAppearanceScope {
                NativeContentState<String>(NativeLoadState.Empty, emptyTitle = "No items") { NativeText(it) }
            }
        }
        onNodeWithText("No items").assertIsDisplayed()
    }

    @Test
    fun contentState_error_shows_retry_only_when_onRetry_present() = runComposeUiTest {
        var retried = false
        setContent {
            NativeAppearanceScope {
                NativeContentState<String>(NativeLoadState.Error("Network down"), onRetry = { retried = true }) { NativeText(it) }
            }
        }
        onNodeWithText("Network down").assertIsDisplayed()
        onNodeWithText("Retry").performClick()
        assertTrue(retried)
    }

    @Test
    fun contentState_error_hides_retry_when_no_onRetry() = runComposeUiTest {
        setContent {
            NativeAppearanceScope {
                NativeContentState<String>(NativeLoadState.Error("Network down")) { NativeText(it) }
            }
        }
        onNodeWithText("Retry").assertDoesNotExist()
    }

    @Test
    fun contentState_content_renders_the_payload() = runComposeUiTest {
        setContent {
            NativeAppearanceScope {
                NativeContentState(NativeLoadState.Content("Chapter 1")) { NativeText(it) }
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
            NativeAppearanceScope {
                NativeRating(rating = 3f, onRatingChange = {}, enabled = false)
            }
        }
        onNodeWithContentDescription("Rating: 3 out of 5").assertIsDisplayed()
    }

    @Test
    fun rating_enabled_with_callback_is_interactive() = runComposeUiTest {
        // Contrast: enabled + onRatingChange = interactive. max = 1 → exactly one clickable star node.
        var picked = 0f
        setContent {
            NativeAppearanceScope {
                NativeRating(rating = 0f, onRatingChange = { picked = it }, max = 1)
            }
        }
        onNode(hasClickAction()).performClick()
        assertEquals(1f, picked)
    }

    // ---- P2 Batch 3 (new components) ----

    @Test
    fun dialog_renders_title_content_and_actions() = runComposeUiTest {
        setContent {
            NativeAppearanceScope {
                NativeDialog(onDismissRequest = {}, title = "Rename", actions = { NativeButton("Save", onClick = {}) }) {
                    NativeText("Body")
                }
            }
        }
        onNodeWithText("Rename").assertIsDisplayed()
        onNodeWithText("Body").assertIsDisplayed()
        onNodeWithText("Save").assertIsDisplayed()
    }

    @Test
    fun nativeHeading_marks_the_node_as_a_heading() = runComposeUiTest {
        setContent {
            NativeAppearanceScope { NativeText("Section", modifier = Modifier.nativeHeading()) }
        }
        onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading)).assertIsDisplayed()
    }

    // ---- Component-gap track (dialog customization, pager, load-more, focus utils) ----

    @Test
    fun dialog_renders_icon_title_slot_and_custom_colors() = runComposeUiTest {
        setContent {
            NativeAppearanceScope {
                NativeDialog(
                    onDismissRequest = {},
                    icon = { NativeText("★") },
                    title = { NativeText("Custom title") },
                    colorsOverride = NativeDialogColors(
                        container = Color.Black,
                        content = Color.White,
                        title = Color.White,
                    ),
                    actions = { NativeButton("OK", onClick = {}) },
                ) {
                    NativeText("Body text")
                }
            }
        }
        onNodeWithText("★").assertIsDisplayed()
        onNodeWithText("Custom title").assertIsDisplayed()
        onNodeWithText("Body text").assertIsDisplayed()
        onNodeWithText("OK").assertIsDisplayed()
    }

    @Test
    fun pager_renders_the_current_page() = runComposeUiTest {
        setContent {
            NativeAppearanceScope {
                val state = rememberPagerState { 3 }
                NativePager(
                    pageCount = 3,
                    state = state,
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                ) { page ->
                    NativeText("Page $page")
                }
            }
        }
        onNodeWithText("Page 0").assertIsDisplayed()
    }

    @Test
    fun loadMoreEffect_fires_once_when_the_end_is_visible() = runComposeUiTest {
        var loads = 0
        setContent {
            NativeAppearanceScope {
                val listState = rememberLazyListState()
                LazyColumn(modifier = Modifier.fillMaxWidth().height(600.dp), state = listState) {
                    items(4) { i ->
                        NativeText("Row $i", modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))
                    }
                }
                NativeLoadMoreEffect(listState = listState, buffer = 3) { loads++ }
            }
        }
        waitForIdle()
        assertEquals(1, loads)
    }

    @Test
    fun focus_utilities_compose_and_render() = runComposeUiTest {
        setContent {
            NativeAppearanceScope {
                val first = rememberNativeFocusHandle()
                val second = rememberNativeFocusHandle()
                Column {
                    Box(
                        Modifier
                            .size(40.dp)
                            .nativeAutoFocus()
                            .nativeFocusTarget(first)
                            .nativeFocusOrder(next = second)
                            .focusable()
                            .testTag("focus-a"),
                    )
                    Box(
                        Modifier
                            .size(40.dp)
                            .nativeFocusTarget(second)
                            .nativeFocusOrder(previous = first)
                            .nativeFocusGroup()
                            .focusable()
                            .testTag("focus-b"),
                    )
                }
            }
        }
        onNodeWithTag("focus-a").assertIsDisplayed()
        onNodeWithTag("focus-b").assertIsDisplayed()
    }

    /**
     * Regression guard for the restored Android IME defaults: a field WITH an onSubmit must run the
     * callback on its IME action, and a field WITHOUT one must still accept the action (the kit passes
     * KeyboardActions.Default — a custom empty handler would suppress the platform behavior and, before
     * the fix, Done stopped hiding the keyboard and Next stopped moving focus).
     */
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun text_field_ime_action_runs_the_submit_callback_and_defaults_stay_wired() = runComposeUiTest {
        var submitted = 0
        setContent {
            NativeAppearanceScope {
                Column {
                    NativeTextField(
                        value = "abc",
                        onValueChange = {},
                        input = NativeFieldInput(imeAction = NativeImeAction.Done),
                        focus = NativeFieldFocus(onSubmit = { submitted++ }),
                        testTag = "field-with-submit",
                    )
                    NativeTextField(
                        value = "",
                        onValueChange = {},
                        input = NativeFieldInput(imeAction = NativeImeAction.Done),
                        testTag = "field-default",
                    )
                }
            }
        }
        onNodeWithTag("field-with-submit").performClick()
        onNodeWithTag("field-with-submit").performImeAction()
        assertEquals(1, submitted)

        // No onSubmit: the action must still be performable (Default keeps the platform handler).
        onNodeWithTag("field-default").performClick()
        onNodeWithTag("field-default").performImeAction()
        assertEquals(1, submitted)
    }
}
