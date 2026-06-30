package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import io.github.apdelrahman1911.nativecomposekit.components.internal.chipBorderColor
import io.github.apdelrahman1911.nativecomposekit.components.internal.resolveSurfaceFill
import io.github.apdelrahman1911.nativecomposekit.components.internal.skeletonColors
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Guards the surface-adaptation rules from the hardening pass (see docs/design-system-rules.md): a
 * surface-relative fill/border must differ from the surface it sits on, and must always resolve to an opaque
 * color. These are the regressions that previously only showed up visually inside a Filled card.
 */
class SurfaceColorsTest {

    private val surfaceVariant = Color(0xFFE7E0EC) // a typical M3 light surfaceVariant
    private val onSurface = Color(0xFF1C1B1F)

    @Test
    fun skeleton_block_is_visible_against_its_own_container() {
        // The bug: a fixed surfaceVariant base vanished inside a surfaceVariant card. Base must differ.
        val (base, highlight) = skeletonColors(surfaceVariant, onSurface)
        assertNotEquals(surfaceVariant, base, "skeleton base must differ from its container (else invisible)")
        assertNotEquals(base, highlight, "shimmer highlight must differ from the base")
    }

    @Test
    fun skeleton_colors_are_deterministic() {
        assertEquals(skeletonColors(surfaceVariant, onSurface), skeletonColors(surfaceVariant, onSurface))
    }

    @Test
    fun surface_fill_uses_published_when_specified_else_fallback() {
        assertEquals(Color.Red, resolveSurfaceFill(Color.Red, Color.Blue))
        assertEquals(Color.Blue, resolveSurfaceFill(Color.Unspecified, Color.Blue))
    }

    @Test
    fun surface_fill_is_always_opaque() {
        // Guards the NativeListItem swipe reveal: the foreground must never be Unspecified (transparent).
        assertTrue(resolveSurfaceFill(Color.Unspecified, Color.Blue).isSpecified)
        assertTrue(resolveSurfaceFill(Color.Red, Color.Unspecified).isSpecified)
    }

    @Test
    fun chip_border_uses_outline_full_alpha_when_enabled() {
        assertEquals(Color.Red, chipBorderColor(Color.Red, enabled = true))
    }

    @Test
    fun chip_border_dims_when_disabled() {
        assertEquals(Color.Red.copy(alpha = 0.38f), chipBorderColor(Color.Red, enabled = false))
    }
}
