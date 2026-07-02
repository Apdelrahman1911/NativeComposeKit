package io.github.apdelrahman1911.nativecomposekit.components.feedback

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import io.github.apdelrahman1911.nativecomposekit.theme.AppTheme
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Guards the Android feedback host's modal lane.
 *
 * The regression these pin: back-to-back modals used to reuse the previous modal's composition slot, so a
 * promoted modal kept the outgoing one's (hidden) state and never re-showed — an invisible active modal
 * stranding the whole lane. The fix keys each modal by its record id (`key(m.id)`).
 *
 * The bottom sheet's own show-animation doesn't settle under Robolectric's test clock (so its content isn't
 * render-assertable here); its visual presentation is verified manually on device/simulator. What we lock
 * automatically: with the host mounted and observing, resolving the active modal promotes the queued one,
 * and the empty-actions alert fallback renders + resolves.
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34]) // Robolectric's bundled runtime doesn't cover compileSdk 36 yet
class FeedbackHostUiTest {

    @Test
    fun mounted_host_promotes_the_queued_modal_when_the_active_one_resolves() = runComposeUiTest {
        lateinit var controller: NativeFeedbackController
        setContent {
            AppTheme {
                controller = rememberNativeFeedbackController()
                NativeFeedbackHost(controller = controller) {}
            }
        }
        val ids = runOnIdle {
            val first = controller.confirmationSheet(title = "First", actions = listOf(NativeSheetAction("Go")))
            val second = controller.confirmationSheet(title = "Second", actions = listOf(NativeSheetAction("Go")))
            // First is active; second is queued behind it.
            assertEquals(first, controller.activeModal?.id)
            first to second
        }
        val (first, second) = ids
        // Resolving the active modal must promote the queued one (not strand the lane).
        runOnIdle { controller.onModalResult(first, null) }
        runOnIdle { assertEquals(second, controller.activeModal?.id) }
        // And the promoted one still resolves cleanly.
        runOnIdle { controller.onModalResult(second, null) }
        runOnIdle { assertNull(controller.activeModal) }
    }

    @Test
    fun alert_without_actions_still_offers_ok() = runComposeUiTest {
        lateinit var controller: NativeFeedbackController
        setContent {
            AppTheme {
                controller = rememberNativeFeedbackController()
                NativeFeedbackHost(controller = controller) {}
            }
        }
        runOnIdle { controller.alert(title = "Heads up", message = "Body", actions = emptyList()) }
        waitUntil(timeoutMillis = 5_000) {
            onAllNodes(hasText("OK")).fetchSemanticsNodes().isNotEmpty()
        }
        onNodeWithText("OK").assertIsDisplayed()
        onNodeWithText("OK").performClick()
        waitForIdle()
        runOnIdle { assertNull(controller.activeModal) }
    }
}
