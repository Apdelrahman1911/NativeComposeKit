package io.github.apdelrahman1911.nativecomposekit.components

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Pure-logic guard for the load-more trigger rule that drives [NativeLoadMoreEffect]. */
class NativePaginationLogicTest {

    @Test
    fun empty_list_never_loads_more() {
        assertFalse(shouldLoadMore(lastVisibleIndex = -1, totalItems = 0, buffer = 3))
    }

    @Test
    fun triggers_within_buffer_of_the_end() {
        // total 10, buffer 3 → trigger once the last visible index reaches 10 - 1 - 3 = 6.
        assertFalse(shouldLoadMore(lastVisibleIndex = 5, totalItems = 10, buffer = 3))
        assertTrue(shouldLoadMore(lastVisibleIndex = 6, totalItems = 10, buffer = 3))
        assertTrue(shouldLoadMore(lastVisibleIndex = 9, totalItems = 10, buffer = 3))
    }

    @Test
    fun zero_buffer_triggers_only_at_the_last_item() {
        assertFalse(shouldLoadMore(lastVisibleIndex = 8, totalItems = 10, buffer = 0))
        assertTrue(shouldLoadMore(lastVisibleIndex = 9, totalItems = 10, buffer = 0))
    }

    @Test
    fun negative_buffer_is_coerced_to_zero() {
        assertFalse(shouldLoadMore(lastVisibleIndex = 8, totalItems = 10, buffer = -5))
        assertTrue(shouldLoadMore(lastVisibleIndex = 9, totalItems = 10, buffer = -5))
    }

    // ---- the re-arm rule (loadMoreShouldFire): once per item count, again on growth ----

    @Test
    fun first_approach_fires() {
        assertTrue(loadMoreShouldFire(lastVisibleIndex = 9, totalItems = 10, buffer = 3, lastFiredTotal = null))
    }

    @Test
    fun same_total_does_not_refire() {
        // Scroll jitter near the end at an unchanged count must not spam the loader.
        assertFalse(loadMoreShouldFire(lastVisibleIndex = 9, totalItems = 10, buffer = 3, lastFiredTotal = 10))
    }

    @Test
    fun data_growth_rearms_while_still_within_the_buffer() {
        // A short page that doesn't fill the viewport keeps the end visible: the count change alone must
        // re-fire, or the list stalls (the pre-fix edge-triggered boolean never re-armed).
        assertTrue(loadMoreShouldFire(lastVisibleIndex = 11, totalItems = 12, buffer = 3, lastFiredTotal = 10))
    }

    @Test
    fun growth_away_from_the_end_does_not_fire() {
        // A full page pushed the end out of the buffer — wait for the next approach.
        assertFalse(loadMoreShouldFire(lastVisibleIndex = 9, totalItems = 30, buffer = 3, lastFiredTotal = 10))
    }

    @Test
    fun failed_load_with_no_growth_does_not_loop() {
        assertFalse(loadMoreShouldFire(lastVisibleIndex = 9, totalItems = 10, buffer = 0, lastFiredTotal = 10))
    }
}
