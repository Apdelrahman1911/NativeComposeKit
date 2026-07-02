package io.github.apdelrahman1911.nativecomposekit.theme

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import io.github.apdelrahman1911.nativecomposekit.components.NativeContentState
import io.github.apdelrahman1911.nativecomposekit.components.NativeLoadState
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Guards the i18n contract: kit-rendered strings come from [LocalNativeStrings] (English defaults), and a
 * translated [NativeStrings] passed to [NativeKitTheme] replaces them everywhere.
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34]) // Robolectric's bundled runtime doesn't cover compileSdk 36 yet
class NativeStringsUiTest {

    @Test
    fun kit_strings_default_to_english() = runComposeUiTest {
        setContent {
            NativeKitTheme {
                NativeContentState<Unit>(state = NativeLoadState.Error(), onRetry = {}) {}
            }
        }
        onNodeWithText("Something went wrong").assertIsDisplayed()
        onNodeWithText("Retry").assertIsDisplayed()
    }

    @Test
    fun apptheme_strings_parameter_localizes_kit_text() = runComposeUiTest {
        setContent {
            NativeKitTheme(strings = NativeStrings(retry = "Wiederholen", errorStateTitle = "Etwas ging schief")) {
                NativeContentState<Unit>(state = NativeLoadState.Error(), onRetry = {}) {}
            }
        }
        onNodeWithText("Etwas ging schief").assertIsDisplayed()
        onNodeWithText("Wiederholen").assertIsDisplayed()
    }
}
