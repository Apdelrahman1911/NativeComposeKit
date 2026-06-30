package com.ukkera.brandkit.components.feedback

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Pure state-machine tests for the modal lane — no composition needed (the controller owns no UI). These
 * guard the P0 iOS modal-stranding fix, whose safety rests on [BrandFeedbackController.onModalResult] being
 * id-guarded (so the iPad-popover dismissal delegate can't double-advance the lane).
 */
class BrandFeedbackControllerTest {

    private fun controller() = BrandFeedbackController()

    @Test
    fun modalResult_advances_the_lane() {
        val c = controller()
        val id = c.alert(title = "Hi", actions = listOf(BrandAlertAction("OK")))
        assertEquals(id, c.activeModal?.id)
        c.onModalResult(id, null)
        assertNull(c.activeModal) // queue empty → lane cleared
    }

    @Test
    fun modalResult_runs_the_chosen_action() {
        val c = controller()
        var ran = false
        val id = c.alert(actions = listOf(BrandAlertAction("OK")))
        c.onModalResult(id) { ran = true }
        assertTrue(ran)
    }

    @Test
    fun modalResult_is_idempotent_for_a_stale_id() {
        // The iOS fix may route both a button tap AND a presentation-dismiss callback through onModalResult.
        // The id guard must make the second call a no-op so the lane never skips the next queued modal.
        val c = controller()
        val first = c.alert(actions = listOf(BrandAlertAction("OK")))
        val second = c.alert(actions = listOf(BrandAlertAction("OK")))
        c.onModalResult(first, null) // first resolved → second promoted
        assertEquals(second, c.activeModal?.id)
        c.onModalResult(first, null) // stale id: must NOT advance past `second`
        assertEquals(second, c.activeModal?.id)
    }

    @Test
    fun dismissCurrentModal_invokes_onCancel_then_advances() {
        val c = controller()
        var cancelled = false
        val id = c.confirmationSheet(actions = listOf(BrandSheetAction("Delete")), onCancel = { cancelled = true })
        assertEquals(id, c.activeModal?.id)
        c.dismissCurrentModal()
        assertTrue(cancelled)
        assertNull(c.activeModal)
    }

    @Test
    fun second_modal_queues_then_promotes_in_order() {
        val c = controller()
        val a = c.alert(actions = listOf(BrandAlertAction("A")))
        val b = c.alert(actions = listOf(BrandAlertAction("B")))
        assertEquals(a, c.activeModal?.id) // first shows, second waits
        c.onModalResult(a, null)
        assertEquals(b, c.activeModal?.id) // second promoted
        c.onModalResult(b, null)
        assertNull(c.activeModal)
    }
}
