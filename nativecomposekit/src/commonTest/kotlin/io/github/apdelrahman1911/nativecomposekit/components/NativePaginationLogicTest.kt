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
}
