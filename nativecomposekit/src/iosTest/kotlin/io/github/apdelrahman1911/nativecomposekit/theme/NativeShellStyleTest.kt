package io.github.apdelrahman1911.nativecomposekit.theme

import androidx.compose.ui.graphics.Color
import io.github.apdelrahman1911.nativecomposekit.components.toComposeColor
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

/**
 * Pins the shell style registry the Swift chrome reads: unstyled defaults are today's exact behavior,
 * a registered style resolves per trait, and the Custom background falls back safely.
 */
class NativeShellStyleTest {

    @AfterTest
    fun restoreDefault() {
        applyNativeShellStyle(NativeShellStyle.Default)
    }

    @Test
    fun unstyled_defaults_keep_todays_chrome() {
        val style = nativeShellStyle()
        assertEquals(NativeShellBarBackground.Themed, style.barBackground)
        assertNull(style.tint)
        assertNull(style.tabItemSelected)
        assertNull(style.tabItemUnselected)
        assertNull(style.titleFont)
        assertFalse(style.showsHairline)
        assertFalse(style.largeTitles)
        // Resolvers: no tint/tab colors registered; bar background = the theme background.
        assertNull(nativeShellTintUIColor(dark = false))
        assertNull(nativeShellTabItemSelectedUIColor(dark = true))
        assertEquals(
            nativeBackgroundUIColor(dark = false).toComposeColor(),
            nativeShellBarBackgroundUIColor(dark = false).toComposeColor(),
        )
    }

    @Test
    fun registered_style_resolves_per_trait() {
        val tintLight = Color(0.10f, 0.60f, 0.50f)
        val tintDark = Color(0.20f, 0.80f, 0.70f)
        applyNativeShellStyle(
            NativeShellStyle(
                barBackground = NativeShellBarBackground.Custom,
                customBarBackground = NativeShellColor(light = Color.White, dark = Color.Black),
                tint = NativeShellColor(light = tintLight, dark = tintDark),
                largeTitles = true,
            ),
        )
        assertEquals(Color.White, nativeShellBarBackgroundUIColor(dark = false).toComposeColor())
        assertEquals(Color.Black, nativeShellBarBackgroundUIColor(dark = true).toComposeColor())
        assertEquals(tintLight, nativeShellTintUIColor(dark = false)?.toComposeColor())
        assertEquals(tintDark, nativeShellTintUIColor(dark = true)?.toComposeColor())
        assertEquals(true, nativeShellStyle().largeTitles)
    }

    @Test
    fun custom_background_without_colors_falls_back_to_the_theme() {
        applyNativeShellStyle(NativeShellStyle(barBackground = NativeShellBarBackground.Custom))
        assertEquals(
            nativeBackgroundUIColor(dark = true).toComposeColor(),
            nativeShellBarBackgroundUIColor(dark = true).toComposeColor(),
        )
    }
}
