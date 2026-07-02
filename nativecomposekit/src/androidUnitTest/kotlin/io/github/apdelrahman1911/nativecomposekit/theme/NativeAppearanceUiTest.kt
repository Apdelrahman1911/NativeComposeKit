package io.github.apdelrahman1911.nativecomposekit.theme

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.LayoutDirection
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Guards the appearance contract: [NativeAppearanceScope] must FOLLOW the host's locale-derived layout
 * direction by default (an RTL-locale user gets RTL with no configuration), and only force a direction when
 * the app sets an explicit [NativeAppearance.setRtl] override — with null returning to the system value.
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34]) // Robolectric's bundled runtime doesn't cover compileSdk 36 yet
class NativeAppearanceUiTest {

    @After
    fun resetProcessGlobalAppearance() {
        // NativeAppearance is process-global snapshot state — reset so tests can't leak into each other.
        NativeAppearance.setRtl(null)
        NativeAppearance.setDark(null)
    }

    @Test
    fun follows_the_system_layout_direction_by_default() = runComposeUiTest {
        var applied: LayoutDirection? = null
        setContent {
            // Simulate an RTL-locale host: the system provides Rtl above the scope.
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                NativeAppearanceScope { applied = LocalLayoutDirection.current }
            }
        }
        assertEquals(LayoutDirection.Rtl, applied)
    }

    @Test
    fun explicit_override_wins_over_the_system_direction() = runComposeUiTest {
        NativeAppearance.setRtl(false)
        var applied: LayoutDirection? = null
        setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                NativeAppearanceScope { applied = LocalLayoutDirection.current }
            }
        }
        assertEquals(LayoutDirection.Ltr, applied)
    }

    @Test
    fun clearing_the_override_returns_to_the_system_direction() = runComposeUiTest {
        var applied: LayoutDirection? = null
        setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                NativeAppearanceScope { applied = LocalLayoutDirection.current }
            }
        }
        NativeAppearance.setRtl(true)
        waitForIdle()
        assertEquals(LayoutDirection.Rtl, applied)

        NativeAppearance.setRtl(null)
        waitForIdle()
        assertEquals(LayoutDirection.Rtl, applied) // back to the system value (Rtl host)
    }
}
