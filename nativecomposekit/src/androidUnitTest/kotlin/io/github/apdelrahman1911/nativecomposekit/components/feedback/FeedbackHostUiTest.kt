package io.github.apdelrahman1911.nativecomposekit.components.feedback

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import io.github.apdelrahman1911.nativecomposekit.theme.AppTheme
import kotlin.test.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Guards the Android feedback host's modal lane. The regression it pins: back-to-back modals of the same
 * kind used to reuse the previous modal's composition slot, so the promoted `ModalBottomSheet` kept the
 * hidden sheet state and never re-showed — an invisible active modal stranding the whole lane.
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34]) // Robolectric's bundled runtime doesn't cover compileSdk 36 yet
class FeedbackHostUiTest {

    @Test
    fun second_confirmation_sheet_presents_after_the_first_resolves() = runComposeUiTest {
        lateinit var controller: NativeFeedbackController
        setContent {
            AppTheme {
                controller = rememberNativeFeedbackController()
                NativeFeedbackHost(controller = controller) {}
            }
        }
        runOnIdle {
            controller.confirmationSheet(title = "First choice", actions = listOf(NativeSheetAction("Go")))
            controller.confirmationSheet(title = "Second choice", actions = listOf(NativeSheetAction("Go")))
        }
        waitUntilExactlyOneExists(hasText("First choice"), timeoutMillis = 5_000)
        // Resolving the first sheet (tapping its action row) must PROMOTE and show the second.
        onNodeWithText("Go").performClick()
        waitUntilExactlyOneExists(hasText("Second choice"), timeoutMillis = 5_000)
        onNodeWithText("Second choice").assertIsDisplayed()
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
        waitUntilExactlyOneExists(hasText("OK"), timeoutMillis = 5_000)
        onNodeWithText("OK").performClick()
        waitForIdle()
        runOnIdle { assertNull(controller.activeModal) }
    }
}
