package io.github.apdelrahman1911.nativecomposekit.components

import kotlin.test.Test
import kotlin.test.assertEquals

/** The shared OTP input filter: digits only, capped at length, non-positive length is safe. */
class NativeOtpFieldLogicTest {

    @Test
    fun keeps_only_digits() {
        assertEquals("123", filterOtpInput("1a2b3c", length = 6))
        assertEquals("", filterOtpInput("abc-def", length = 6))
    }

    @Test
    fun caps_at_length() {
        assertEquals("1234", filterOtpInput("1234567", length = 4))
    }

    @Test
    fun exact_length_is_kept() {
        assertEquals("0000", filterOtpInput("0000", length = 4))
    }

    @Test
    fun non_positive_length_is_empty_and_does_not_throw() {
        assertEquals("", filterOtpInput("123", length = 0))
        assertEquals("", filterOtpInput("123", length = -2))
    }

    @Test
    fun non_ascii_digits_are_rejected_so_the_emitted_code_matches_server_comparison() {
        // Arabic-Indic and Devanagari digits pass Char.isDigit() but are not the ASCII the server expects.
        assertEquals("", filterOtpInput("\u0661\u0662\u0663", length = 6))
        assertEquals("", filterOtpInput("\u0967\u0968\u0969", length = 6))
        assertEquals("45", filterOtpInput("\u06614\u06625", length = 6)) // mixed input keeps only ASCII
    }
}
