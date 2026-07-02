package io.github.apdelrahman1911.nativecomposekit.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.apdelrahman1911.nativecomposekit.chrome.NativeChromeAction
import io.github.apdelrahman1911.nativecomposekit.chrome.NativeChromeState
import io.github.apdelrahman1911.nativecomposekit.chrome.NativeChromeTab
import io.github.apdelrahman1911.nativecomposekit.components.NativeLoadState
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeCharacterLimit
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeFieldInput
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeIcon
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Value-semantics guards for the public model classes. These were converted from `data class` to
 * plain classes for binary-compatible evolution — the hand-written equals/hashCode below are
 * load-bearing (composition locals skip on equals; NativeIcon equality feeds the iOS interop
 * size fingerprints), so a silently-dropped override must fail here, not on device.
 */
class ThemeModelsTest {

    @Test
    fun tokens_compare_by_value_and_copy_replaces_selectively() {
        assertEquals(NativeTokens(), NativeTokens())
        assertEquals(NativeTokens().hashCode(), NativeTokens().hashCode())
        val custom = NativeTokens().copy(spacingMd = 20.dp)
        assertNotEquals(NativeTokens(), custom)
        assertEquals(20.dp, custom.spacingMd)
        assertEquals(NativeTokens().spacingLg, custom.spacingLg)
    }

    @Test
    fun status_colors_factories_inherit_unspecified_fields() {
        val brand = Color(0xFF123456)
        val custom = lightNativeStatusColors(success = brand)
        assertEquals(brand, custom.success)
        assertEquals(lightNativeStatusColors().warning, custom.warning)
        assertEquals(lightNativeStatusColors(), lightNativeStatusColors())
        assertNotEquals(lightNativeStatusColors(), darkNativeStatusColors())
        assertEquals(brand, darkNativeStatusColors().copy(info = brand).info)
    }

    @Test
    fun capabilities_compare_by_value() {
        assertEquals(NativeCapabilities(true), NativeCapabilities(true))
        assertNotEquals(NativeCapabilities(true), NativeCapabilities(false))
        assertEquals(NativeCapabilities(true).hashCode(), NativeCapabilities(true).hashCode())
    }

    @Test
    fun icons_compare_by_value_for_recomposition_skipping() {
        assertEquals(NativeIcon(sfSymbolName = "plus"), NativeIcon(sfSymbolName = "plus"))
        assertNotEquals(NativeIcon(sfSymbolName = "plus"), NativeIcon(sfSymbolName = "minus"))
        assertEquals(
            NativeIcon(sfSymbolName = "plus", contentDescription = "Add").hashCode(),
            NativeIcon(sfSymbolName = "plus", contentDescription = "Add").hashCode(),
        )
    }

    @Test
    fun chrome_state_compares_by_value_so_shells_can_skip_noop_emissions() {
        fun state() = NativeChromeState(
            title = "Library",
            canGoBack = false,
            selectedTabId = "library",
            tabs = listOf(NativeChromeTab("library", "Library", "books.vertical")),
            actions = listOf(NativeChromeAction("add", "plus")),
            sheetId = null,
        )
        assertEquals(state(), state())
        assertEquals(state().hashCode(), state().hashCode())
        assertNotEquals(
            state(),
            NativeChromeState(
                title = "Library",
                canGoBack = false,
                selectedTabId = "library",
                tabs = listOf(NativeChromeTab("library", "Library", "books.vertical")),
                actions = listOf(NativeChromeAction("add", "plus")),
                sheetId = "sheet",
            ),
        )
    }

    @Test
    fun field_input_rejects_invalid_line_counts_with_a_kit_error() {
        assertFailsWith<IllegalArgumentException> { NativeFieldInput(singleLine = false, minLines = 0) }
        assertFailsWith<IllegalArgumentException> { NativeFieldInput(singleLine = false, minLines = 3, maxLines = 2) }
        // The defaults and the single-line shortcut stay valid.
        NativeFieldInput()
        NativeFieldInput(singleLine = false, minLines = 3, maxLines = 6)
    }

    @Test
    fun field_input_and_character_limit_compare_by_value() {
        assertEquals(NativeFieldInput(), NativeFieldInput())
        assertNotEquals(NativeFieldInput(), NativeFieldInput(secure = true))
        assertEquals(NativeCharacterLimit(10), NativeCharacterLimit(10))
        assertNotEquals(NativeCharacterLimit(10), NativeCharacterLimit(11))
    }

    @Test
    fun load_state_error_compares_by_value_and_carries_a_cause() {
        val boom = IllegalStateException("boom")
        assertEquals(NativeLoadState.Error("x"), NativeLoadState.Error("x"))
        assertNotEquals(NativeLoadState.Error("x"), NativeLoadState.Error("y"))
        assertEquals(boom, NativeLoadState.Error("x", boom).cause)
        assertTrue(NativeLoadState.Error("x") != NativeLoadState.Error("x", boom))
    }
}
