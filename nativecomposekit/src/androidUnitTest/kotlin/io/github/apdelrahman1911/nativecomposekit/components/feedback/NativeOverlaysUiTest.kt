package io.github.apdelrahman1911.nativecomposekit.components.feedback

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import io.github.apdelrahman1911.nativecomposekit.components.NativeDialog
import io.github.apdelrahman1911.nativecomposekit.components.NativeShare
import io.github.apdelrahman1911.nativecomposekit.components.rememberNativeShare
import io.github.apdelrahman1911.nativecomposekit.theme.NativeKitTheme
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

/**
 * Render-level guards for the overlay surfaces the pure-logic tests can't cover (they need a composition):
 * the Android banner's affordances, the alert's wrapping action row, the dialog's pane semantics, and the
 * share chooser intent. The bottom sheet stays out — its show-animation never settles under Robolectric's
 * test clock, so sheet behavior is asserted through controller state in `FeedbackHostUiTest` instead.
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34]) // Robolectric's bundled runtime doesn't cover compileSdk 36 yet
class NativeOverlaysUiTest {

    @Test
    fun banner_renders_and_its_close_button_advances_the_lane() = runComposeUiTest {
        lateinit var controller: NativeFeedbackController
        setContent {
            NativeKitTheme {
                controller = rememberNativeFeedbackController()
                NativeFeedbackHost(controller = controller) {}
            }
        }
        var dismissed = 0
        val ids = runOnIdle {
            val first = controller.banner(message = "Changes will sync later", title = "Offline", onDismiss = { dismissed++ })
            val second = controller.banner(message = "Back online")
            first to second
        }
        waitUntil(timeoutMillis = 5_000) {
            onAllNodes(hasText("Changes will sync later")).fetchSemanticsNodes().isNotEmpty()
        }
        onNodeWithText("Changes will sync later").assertIsDisplayed()
        onNodeWithText("Offline").assertIsDisplayed()
        // The close button (labeled with strings.dismiss) runs onDismiss and promotes the queued banner.
        onNodeWithContentDescription("Dismiss").performClick()
        waitForIdle()
        runOnIdle {
            assertEquals(1, dismissed)
            assertEquals(ids.second, controller.activeTransient?.id)
        }
        waitUntil(timeoutMillis = 5_000) {
            onAllNodes(hasText("Back online")).fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun banner_action_fires_and_advances_the_lane() = runComposeUiTest {
        lateinit var controller: NativeFeedbackController
        setContent {
            NativeKitTheme {
                controller = rememberNativeFeedbackController()
                NativeFeedbackHost(controller = controller) {}
            }
        }
        var acted = 0
        runOnIdle {
            controller.banner(message = "Sync failed", actionLabel = "Retry", onAction = { acted++ })
        }
        waitUntil(timeoutMillis = 5_000) {
            onAllNodes(hasText("Retry")).fetchSemanticsNodes().isNotEmpty()
        }
        onNodeWithText("Retry").performClick()
        waitForIdle()
        runOnIdle {
            assertEquals(1, acted)
            assertNull(controller.activeTransient) // an action resolves the record, it doesn't linger
        }
    }

    @Test
    fun alert_with_three_long_labeled_actions_renders_all_three_buttons() = runComposeUiTest {
        // Regression: a nested Row in the confirm slot defeated M3's wrapping flow row, clipping the
        // overflowing buttons. Emitted directly, three long labels must all stay visible (wrapped).
        lateinit var controller: NativeFeedbackController
        setContent {
            NativeKitTheme {
                controller = rememberNativeFeedbackController()
                NativeFeedbackHost(controller = controller) {}
            }
        }
        runOnIdle {
            controller.alert(
                title = "Unsaved changes",
                message = "Pick what happens to this draft.",
                actions = listOf(
                    NativeAlertAction("Save to shared workspace"),
                    NativeAlertAction("Discard all local changes"),
                    NativeAlertAction("Continue editing offline"),
                ),
            )
        }
        waitUntil(timeoutMillis = 5_000) {
            onAllNodes(hasText("Save to shared workspace")).fetchSemanticsNodes().isNotEmpty()
        }
        onNodeWithText("Save to shared workspace").assertIsDisplayed()
        onNodeWithText("Discard all local changes").assertIsDisplayed()
        onNodeWithText("Continue editing offline").assertIsDisplayed()
    }

    @Test
    fun dialog_renders_its_title_and_carries_pane_title_semantics() = runComposeUiTest {
        setContent {
            NativeKitTheme {
                NativeDialog(title = "Rename file", onDismissRequest = {}) {
                    Text("Pick a new name.")
                }
            }
        }
        waitUntil(timeoutMillis = 5_000) {
            onAllNodes(hasText("Rename file")).fetchSemanticsNodes().isNotEmpty()
        }
        onNodeWithText("Rename file").assertIsDisplayed()
        // The String-title overload reuses its title as the announced pane title.
        onNode(SemanticsMatcher.expectValue(SemanticsProperties.PaneTitle, "Rename file")).assertExists()
    }

    @Test
    fun share_fires_an_action_send_intent_and_empty_content_fires_nothing() = runComposeUiTest {
        lateinit var share: NativeShare
        lateinit var context: Context
        setContent {
            NativeKitTheme {
                context = LocalContext.current
                share = rememberNativeShare()
            }
        }
        val shadow = Shadows.shadowOf(context.findActivity())

        runOnIdle { share.share(text = "Hello", url = "https://example.com") }
        val chooser = shadow.nextStartedActivity
        assertNotNull(chooser)
        assertEquals(Intent.ACTION_CHOOSER, chooser.action)
        @Suppress("DEPRECATION")
        val send = chooser.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)
        assertNotNull(send)
        assertEquals(Intent.ACTION_SEND, send.action)
        assertEquals("Hello\nhttps://example.com", send.getStringExtra(Intent.EXTRA_TEXT))

        // Empty content is a documented no-op: no chooser (or anything else) may leave the app.
        runOnIdle { share.share() }
        assertNull(shadow.nextStartedActivity)
    }

    /** The compose test host hands out the activity behind zero or more [ContextWrapper]s. */
    private tailrec fun Context.findActivity(): Activity = when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> error("No Activity in the context chain")
    }
}
