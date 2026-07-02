package io.github.apdelrahman1911.nativecomposekit.components

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** A null count is a dot, a positive count is a numbered pill, a non-positive count renders nothing. */
class NativeBadgeLogicTest {

    @Test fun null_count_shows_a_dot() = assertTrue(badgeIsVisible(null))

    @Test fun positive_counts_show() {
        assertTrue(badgeIsVisible(1))
        assertTrue(badgeIsVisible(99))
    }

    @Test fun zero_and_negative_counts_hide() {
        assertFalse(badgeIsVisible(0))
        assertFalse(badgeIsVisible(-3))
    }

    // ---- badgeDisplayText: the cap + "99+" rule ----

    @Test fun over_cap_shows_cap_plus() {
        assertEquals("99+", badgeDisplayText(count = 100, maxCount = 99))
        assertEquals("5+", badgeDisplayText(count = 42, maxCount = 5))
    }

    @Test fun exact_cap_shows_the_number_not_the_plus() {
        assertEquals("99", badgeDisplayText(count = 99, maxCount = 99))
        assertEquals("1", badgeDisplayText(count = 1, maxCount = 1))
    }

    @Test fun under_cap_shows_the_count() {
        assertEquals("7", badgeDisplayText(count = 7, maxCount = 99))
    }

    @Test fun zero_or_negative_cap_is_coerced_to_one() {
        // Without the coercion these would render "0+" / "-5+".
        assertEquals("1+", badgeDisplayText(count = 5, maxCount = 0))
        assertEquals("1+", badgeDisplayText(count = 2, maxCount = -5))
        assertEquals("1", badgeDisplayText(count = 1, maxCount = 0))
    }
}
