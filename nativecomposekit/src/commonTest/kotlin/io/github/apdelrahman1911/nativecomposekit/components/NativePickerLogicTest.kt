package io.github.apdelrahman1911.nativecomposekit.components

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Pure-logic guards for the picker/display helpers: avatar initials (code-point-aware) and the UTC
 * day-floor behind [NativeDatePicker]'s "UTC epoch millis at the start of the day" contract on iOS.
 */
class NativePickerLogicTest {

    private companion object {
        const val DAY_MS = 86_400_000L
    }

    // ---- firstTwoCodePointsUpper (NativeAvatar initials) ----

    @Test
    fun initials_take_the_first_two_characters_uppercased() {
        assertEquals("JO", "john".firstTwoCodePointsUpper())
        assertEquals("JD", "jd".firstTwoCodePointsUpper())
    }

    @Test
    fun initials_keep_a_surrogate_pair_emoji_whole() {
        // "😀" is one code point but two UTF-16 units — a unit-based take(2) would emit half a glyph.
        assertEquals("😀X", "😀xyz".firstTwoCodePointsUpper())
        assertEquals("😀😀", "😀😀😀".firstTwoCodePointsUpper())
    }

    @Test
    fun initials_from_a_single_character() {
        assertEquals("J", "j".firstTwoCodePointsUpper())
    }

    @Test
    fun initials_from_a_blank_string_stay_empty() {
        // NativeAvatar trims and drops empties before calling; the helper itself must not throw on "".
        assertEquals("", "".firstTwoCodePointsUpper())
    }

    // ---- utcDayStart (the iOS date picker's UTC-day floor) ----

    @Test
    fun exact_utc_midnight_is_a_fixed_point() {
        assertEquals(0L, utcDayStart(0L))
        val jan5of2024 = 1_704_412_800_000L // 2024-01-05T00:00:00Z
        assertEquals(jan5of2024, utcDayStart(jan5of2024))
    }

    @Test
    fun positive_instants_floor_to_their_days_start() {
        val jan5of2024 = 1_704_412_800_000L
        assertEquals(jan5of2024, utcDayStart(jan5of2024 + 1L)) // just past midnight
        assertEquals(jan5of2024, utcDayStart(jan5of2024 + 13 * 3_600_000L)) // 13:00 wall-clock
        assertEquals(jan5of2024, utcDayStart(jan5of2024 + DAY_MS - 1L)) // last millisecond of the day
    }

    @Test
    fun pre_epoch_instants_floor_to_their_own_days_start_not_the_next() {
        // Truncating division would send these to 0 / the next day's start (rounds toward zero).
        assertEquals(-DAY_MS, utcDayStart(-1L)) // 1969-12-31T23:59:59.999Z → Dec 31's start
        assertEquals(-DAY_MS, utcDayStart(-DAY_MS)) // exact pre-epoch midnight is a fixed point
        assertEquals(-2 * DAY_MS, utcDayStart(-DAY_MS - 1L)) // one ms earlier lands on Dec 30
    }
}
