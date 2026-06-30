package com.ukkera.brandkit.components

import kotlin.test.Test
import kotlin.test.assertEquals

class BrandRatingLogicTest {

    @Test
    fun clamps_into_range_and_maps_nan_to_zero() {
        assertEquals(3f, clampRating(3f, 5))
        assertEquals(0f, clampRating(-2f, 5))
        assertEquals(5f, clampRating(9f, 5))
        assertEquals(0f, clampRating(Float.NaN, 5))
    }

    @Test
    fun display_rating_fills_whole_stars_and_a_half() {
        // rating 4.5 (display): stars 1..4 full, star 5 half.
        assertEquals(StarFill.Full, starFill(index = 0, rating = 4.5f, allowHalf = true, interactive = false))
        assertEquals(StarFill.Full, starFill(index = 3, rating = 4.5f, allowHalf = true, interactive = false))
        assertEquals(StarFill.Half, starFill(index = 4, rating = 4.5f, allowHalf = true, interactive = false))
    }

    @Test
    fun interactive_never_shows_a_half() {
        // Same 4.5, but interactive controls produce whole stars only → star 5 is empty, not half.
        assertEquals(StarFill.Empty, starFill(index = 4, rating = 4.5f, allowHalf = true, interactive = true))
    }

    @Test
    fun half_disabled_rounds_down_visually() {
        assertEquals(StarFill.Empty, starFill(index = 4, rating = 4.5f, allowHalf = false, interactive = false))
    }

    @Test
    fun whole_rating_fills_exactly() {
        // rating 3: stars 1..3 full, 4..5 empty.
        assertEquals(StarFill.Full, starFill(index = 2, rating = 3f, allowHalf = true, interactive = false))
        assertEquals(StarFill.Empty, starFill(index = 3, rating = 3f, allowHalf = true, interactive = false))
    }
}
