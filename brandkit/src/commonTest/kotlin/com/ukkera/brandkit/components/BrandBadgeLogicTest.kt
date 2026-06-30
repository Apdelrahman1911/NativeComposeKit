package com.ukkera.brandkit.components

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** A null count is a dot, a positive count is a numbered pill, a non-positive count renders nothing. */
class BrandBadgeLogicTest {

    @Test fun null_count_shows_a_dot() = assertTrue(badgeIsVisible(null))

    @Test fun positive_counts_show() {
        assertTrue(badgeIsVisible(1))
        assertTrue(badgeIsVisible(99))
    }

    @Test fun zero_and_negative_counts_hide() {
        assertFalse(badgeIsVisible(0))
        assertFalse(badgeIsVisible(-3))
    }
}
