package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import io.github.apdelrahman1911.nativecomposekit.theme.NativeAppearanceScope
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Behavior guards for [NativeCollapsible]'s Android actual (a real `AnimatedVisibility`): content is
 * present when visible, leaves the tree when hidden, and returns on re-show. The iOS-specific parts
 * (container-size animation, one-step gating, the Compose-text mode for `NativeText`) are runtime
 * behaviors exercised by the sample app's "Interop churn test" screen.
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class NativeCollapsibleUiTest {

    @Test
    fun content_shows_hides_and_returns_with_visible() = runComposeUiTest {
        var visible by mutableStateOf(true)
        setContent {
            NativeAppearanceScope {
                NativeCollapsible(visible = visible) {
                    NativeText("Collapsible content")
                }
            }
        }

        onNodeWithText("Collapsible content").assertIsDisplayed()

        visible = false
        waitForIdle()
        assertTrue(onAllNodes(hasText("Collapsible content")).fetchSemanticsNodes().isEmpty())

        visible = true
        waitForIdle()
        onNodeWithText("Collapsible content").assertIsDisplayed()
    }
}
