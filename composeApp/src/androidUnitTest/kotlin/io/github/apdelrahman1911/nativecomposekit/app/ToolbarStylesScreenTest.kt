package io.github.apdelrahman1911.nativecomposekit.app

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.runComposeUiTest
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Smoke-pins the Android path of the toolbar-styles catalog: the live Material exhibits actually render
 * (default, tinted, actions incl. a TEXT action, centered title, tab bars, the fully custom slot bar),
 * the iOS-only exhibits show their honest placeholder, and the immersive demo wires its back intent.
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34]) // Robolectric's bundled runtime doesn't cover compileSdk 36 yet
class ToolbarStylesScreenTest {

    @Test
    fun catalog_renders_the_android_exhibits_live() = runComposeUiTest {
        setContent { ToolbarStylesScreen() }

        onNodeWithText("1. Default toolbar").assertIsDisplayed()
        onNodeWithText("Default title").assertIsDisplayed()               // live default Material bar
        onNodeWithText("Tinted title").performScrollTo().assertIsDisplayed() // live tinted bar
        onNodeWithText("Edit").performScrollTo().assertIsDisplayed()      // TEXT action in the slot
        onNodeWithText("Centered").performScrollTo().assertIsDisplayed()  // centered-title variant
        onNodeWithText("Custom slot").performScrollTo().assertIsDisplayed() // fully custom slot bar
        onNodeWithText("Subtitle — anything goes in a slot").performScrollTo().assertIsDisplayed()
        // iOS-only exhibits are honest placeholders on Android (several across the sections).
        val placeholders = onAllNodes(
            androidx.compose.ui.test.hasText("iOS-only preview (real UINavigationBar) — run the iOS app"),
        ).fetchSemanticsNodes()
        assertTrue(placeholders.size >= 3)
    }

    @Test
    fun immersive_demo_renders_and_pops_via_its_button() = runComposeUiTest {
        var popped = false
        setContent { ImmersiveDemoScreen(onBack = { popped = true }) }
        onNodeWithText("Immersive").assertIsDisplayed()
        onNodeWithText("Go back").performClick()
        assertTrue(popped)
    }
}
