package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import io.github.apdelrahman1911.nativecomposekit.theme.BrandAppearanceScope
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Feasibility gate: proves the Robolectric + Compose-UI-test stack resolves and runs on this toolchain
 * (Kotlin 2.3.21 / Compose Multiplatform 1.11.0) on the JVM, with no emulator. If this is green, the real
 * render-level component tests are viable.
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34]) // Robolectric's bundled runtime doesn't cover compileSdk 36 yet
class GateUiTest {

    @Test
    fun composeUiTestStackRuns() = runComposeUiTest {
        setContent {
            BrandAppearanceScope {
                BrandText("hello-gate")
            }
        }
        onNodeWithText("hello-gate").assertIsDisplayed()
    }
}
