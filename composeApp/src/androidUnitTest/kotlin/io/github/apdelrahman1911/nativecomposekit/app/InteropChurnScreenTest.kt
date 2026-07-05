package io.github.apdelrahman1911.nativecomposekit.app

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Smoke-pins the Android path of the interop-churn harness. The interesting behavior (the CMP interop
 * transaction-loss ghosting and the kit's `InteropDisposeFailSafe`) is iOS-runtime-only — this guards
 * that the shared screen composes, starts expanded, and exposes its controls. `autoRun = false` because
 * the endless auto-cycle would keep the compose test clock from ever going idle.
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34]) // Robolectric's bundled runtime doesn't cover compileSdk 36 yet
class InteropChurnScreenTest {

    @Test
    fun churn_screen_renders_expanded_rows() = runComposeUiTest {
        setContent { InteropChurnScreen(autoRun = false) }

        onNodeWithText("Auto-cycle").assertIsDisplayed()
        onNodeWithText("Completed cycles: 0").assertIsDisplayed()
        onNodeWithText("Row 1").assertIsDisplayed() // rows start expanded, controls composed
    }
}
