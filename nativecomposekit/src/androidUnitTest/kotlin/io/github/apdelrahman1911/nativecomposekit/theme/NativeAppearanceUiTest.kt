package io.github.apdelrahman1911.nativecomposekit.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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

    @Test
    fun dark_override_switches_the_color_scheme_and_null_returns_to_system() = runComposeUiTest {
        var surface: Color? = null
        val light = Color(0xFF111111)
        val dark = Color(0xFF222222)
        setContent {
            NativeAppearanceScope(
                lightColors = androidx.compose.material3.lightColorScheme(surface = light),
                darkColors = androidx.compose.material3.darkColorScheme(surface = dark),
            ) { surface = MaterialTheme.colorScheme.surface }
        }
        assertEquals(light, surface) // Robolectric host is light by default

        NativeAppearance.setDark(true)
        waitForIdle()
        assertEquals(dark, surface)

        NativeAppearance.setDark(null)
        waitForIdle()
        assertEquals(light, surface)
    }

    @Test
    fun scope_forwards_tokens_status_colors_and_strings_to_the_locals() = runComposeUiTest {
        val tokens = NativeTokens(spacingMd = 99.dp)
        val status = lightNativeStatusColors(success = Color(0xFF00FF11))
        val strings = NativeStrings(retry = "Nochmal")
        var seenSpacing = 0.dp
        var seenSuccess = Color.Unspecified
        var seenRetry = ""
        setContent {
            NativeAppearanceScope(
                tokens = tokens,
                lightStatusColors = status,
                darkStatusColors = status,
                strings = strings,
            ) {
                seenSpacing = NativeTheme.tokens.spacingMd
                seenSuccess = NativeTheme.statusColors.success
                seenRetry = NativeTheme.strings.retry
            }
        }
        assertEquals(99.dp, seenSpacing)
        assertEquals(Color(0xFF00FF11), seenSuccess)
        assertEquals("Nochmal", seenRetry)
    }

    @Test
    fun scope_publishes_the_background_as_the_surface_when_drawing_it() = runComposeUiTest {
        var opaque: Color = Color.Unspecified
        setContent {
            NativeAppearanceScope(drawBackground = true) { opaque = LocalNativeSurface.current }
        }
        assertTrue(opaque != Color.Unspecified)
    }

    @Test
    fun transparent_scope_publishes_an_unspecified_surface() = runComposeUiTest {
        var transparent: Color = Color(0xFF123456)
        setContent {
            NativeAppearanceScope(drawBackground = false) { transparent = LocalNativeSurface.current }
        }
        assertEquals(Color.Unspecified, transparent)
    }

    @Test
    fun scope_publishes_capabilities_and_the_default_is_motion_on() = runComposeUiTest {
        var caps: NativeCapabilities? = null
        setContent {
            NativeAppearanceScope { caps = NativeTheme.capabilities }
        }
        // Robolectric's animator duration scale is the default 1f -> reduce motion off.
        assertEquals(NativeCapabilities(isReduceMotionEnabled = false), caps)
    }
}
