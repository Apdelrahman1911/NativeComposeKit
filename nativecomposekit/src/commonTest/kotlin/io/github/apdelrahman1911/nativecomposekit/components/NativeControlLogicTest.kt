package io.github.apdelrahman1911.nativecomposekit.components

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class NativeControlLogicTest {

    // ---- stepperNextValue (the Android -/+ arithmetic) ----

    @Test
    fun stepper_moves_by_the_signed_delta() {
        assertEquals(5, stepperNextValue(current = 4, delta = 1, min = 0, max = 10))
        assertEquals(3, stepperNextValue(current = 4, delta = -1, min = 0, max = 10))
        assertEquals(9, stepperNextValue(current = 3, delta = 6, min = 0, max = 10))
    }

    @Test
    fun stepper_clamps_at_both_bounds() {
        assertEquals(10, stepperNextValue(current = 10, delta = 1, min = 0, max = 10))
        assertEquals(0, stepperNextValue(current = 0, delta = -1, min = 0, max = 10))
    }

    @Test
    fun stepper_partial_last_step_lands_on_the_bound() {
        // 7 + 4 = 11 > max: clamps to max rather than refusing to move (and symmetrically at min).
        assertEquals(10, stepperNextValue(current = 7, delta = 4, min = 0, max = 10))
        assertEquals(0, stepperNextValue(current = 2, delta = -5, min = 0, max = 10))
    }

    @Test
    fun stepper_clamps_an_out_of_range_current_value_back_in() {
        assertEquals(10, stepperNextValue(current = 42, delta = 1, min = 0, max = 10))
        assertEquals(0, stepperNextValue(current = -7, delta = 1, min = 0, max = 10))
    }

    // ---- validation (both platforms must fail identically, with a readable message) ----

    @Test
    fun stepper_rejects_a_non_positive_step() {
        val zero = assertFailsWith<IllegalArgumentException> { validateStepperConfig(min = 0, max = 10, step = 0) }
        assertTrue(zero.message!!.contains("step must be > 0"))
        assertFailsWith<IllegalArgumentException> { validateStepperConfig(min = 0, max = 10, step = -2) }
    }

    @Test
    fun stepper_rejects_inverted_bounds() {
        val e = assertFailsWith<IllegalArgumentException> { validateStepperConfig(min = 5, max = 1, step = 1) }
        assertTrue(e.message!!.contains("min (5) must be <= max (1)"))
    }

    @Test
    fun stepper_accepts_a_single_point_range() {
        validateStepperConfig(min = 3, max = 3, step = 1) // min == max is a valid (pinned) config
    }

    @Test
    fun slider_rejects_an_inverted_range() {
        val e = assertFailsWith<IllegalArgumentException> { validateSliderRange(start = 1f, endInclusive = 0f) }
        assertTrue(e.message!!.contains("inverted"))
    }

    @Test
    fun slider_accepts_a_single_point_range_and_zero_steps() {
        validateSliderRange(start = 0.5f, endInclusive = 0.5f)
        validateSliderSteps(0)
    }

    @Test
    fun slider_rejects_negative_steps() {
        val e = assertFailsWith<IllegalArgumentException> { validateSliderSteps(-1) }
        assertTrue(e.message!!.contains("steps must be >= 0"))
    }

    // ---- sliderSnappedValue (the iOS discrete-slider emission) ----

    @Test
    fun slider_snaps_to_the_material_stop_positions() {
        // steps = 3 on 0..1 → stops at 0, .25, .5, .75, 1 (`steps` values between the endpoints).
        assertEquals(0.25f, sliderSnappedValue(0.3f, min = 0f, max = 1f, steps = 3))
        assertEquals(0.5f, sliderSnappedValue(0.45f, min = 0f, max = 1f, steps = 3))
        assertEquals(0f, sliderSnappedValue(0.1f, min = 0f, max = 1f, steps = 3))
        assertEquals(1f, sliderSnappedValue(0.9f, min = 0f, max = 1f, steps = 3))
    }

    @Test
    fun slider_snap_respects_a_shifted_range() {
        // steps = 1 on 10..20 → stops at 10, 15, 20.
        assertEquals(15f, sliderSnappedValue(13f, min = 10f, max = 20f, steps = 1))
        assertEquals(10f, sliderSnappedValue(11f, min = 10f, max = 20f, steps = 1))
        assertEquals(20f, sliderSnappedValue(19f, min = 10f, max = 20f, steps = 1))
    }

    @Test
    fun slider_snap_clamps_an_out_of_range_input_to_the_nearest_endpoint() {
        assertEquals(1f, sliderSnappedValue(7f, min = 0f, max = 1f, steps = 3))
        assertEquals(0f, sliderSnappedValue(-7f, min = 0f, max = 1f, steps = 3))
    }

    @Test
    fun slider_snap_is_a_passthrough_when_continuous_or_degenerate() {
        assertEquals(0.3f, sliderSnappedValue(0.3f, min = 0f, max = 1f, steps = 0))
        assertEquals(0.3f, sliderSnappedValue(0.3f, min = 1f, max = 1f, steps = 2)) // empty range: no snap math
    }
}
