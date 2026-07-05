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
 * transaction delay/desync and the kit's dispose fail-safe / position heal) is iOS-runtime-only — this
 * guards that the shared screen composes, starts expanded with both safe flavors, and exposes the
 * wedge-repro toggle. `autoRun = false` because the endless auto-cycle would keep the compose test
 * clock from ever going idle.
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
        onNodeWithText("Reproduce the wedge").assertIsDisplayed() // the AnimatedVisibility repro stays opt-in
        onNodeWithText("Row 1 · collapsible").assertIsDisplayed() // NativeCollapsible flavor, expanded
    }
}
