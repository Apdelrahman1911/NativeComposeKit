package io.github.apdelrahman1911.nativecomposekit.components

import io.github.apdelrahman1911.nativecomposekit.components.model.NativeCharacterLimit
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeCharacterLimitBehavior
import kotlin.test.Test
import kotlin.test.assertEquals

/** The shared character-limit logic behind `NativeTextField` — identical on both platforms. */
class NativeTextFieldLogicTest {

    @Test
    fun no_limit_passes_through() {
        assertEquals("hello", applyCharacterLimit("hello", null))
    }

    @Test
    fun warn_only_passes_through_even_over_the_max() {
        val limit = NativeCharacterLimit(max = 3, behavior = NativeCharacterLimitBehavior.WarnOnly)
        assertEquals("hello", applyCharacterLimit("hello", limit))
    }

    @Test
    fun enforce_hard_caps_a_paste() {
        val limit = NativeCharacterLimit(max = 3, behavior = NativeCharacterLimitBehavior.Enforce)
        assertEquals("hel", applyCharacterLimit("hello world", limit))
        assertEquals("ab", applyCharacterLimit("ab", limit)) // under the max is untouched
    }

    @Test
    fun enforce_does_not_split_a_surrogate_pair_at_the_boundary() {
        // "a" + 👍 (one code point, two UTF-16 chars). max = 2 would land between the surrogates; drop both.
        val emoji = "a👍bc"
        val limit = NativeCharacterLimit(max = 2, behavior = NativeCharacterLimitBehavior.Enforce)
        assertEquals("a", applyCharacterLimit(emoji, limit))
        // max = 3 keeps the whole emoji.
        val limit3 = NativeCharacterLimit(max = 3, behavior = NativeCharacterLimitBehavior.Enforce)
        assertEquals("a👍", applyCharacterLimit(emoji, limit3))
    }

    @Test
    fun enforce_with_non_positive_max_caps_to_empty_without_throwing() {
        val zero = NativeCharacterLimit(max = 0, behavior = NativeCharacterLimitBehavior.Enforce)
        assertEquals("", applyCharacterLimit("abc", zero))
        val negative = NativeCharacterLimit(max = -5, behavior = NativeCharacterLimitBehavior.Enforce)
        assertEquals("", applyCharacterLimit("abc", negative))
    }
}
