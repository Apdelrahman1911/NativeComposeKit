package io.github.apdelrahman1911.nativecomposekit.components.feedback

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Pure state-machine tests for the modal lane — no composition needed (the controller owns no UI). These
 * guard the P0 iOS modal-stranding fix, whose safety rests on [NativeFeedbackController.onModalResult] being
 * id-guarded (so the iPad-popover dismissal delegate can't double-advance the lane).
 */
class NativeFeedbackControllerTest {

    private fun controller() = NativeFeedbackController()

    @Test
    fun modalResult_advances_the_lane() {
        val c = controller()
        val id = c.alert(title = "Hi", actions = listOf(NativeAlertAction("OK")))
        assertEquals(id, c.activeModal?.id)
        c.onModalResult(id, null)
        assertNull(c.activeModal) // queue empty → lane cleared
    }

    @Test
    fun modalResult_runs_the_chosen_action() {
        val c = controller()
        var ran = false
        val id = c.alert(actions = listOf(NativeAlertAction("OK")))
        c.onModalResult(id) { ran = true }
        assertTrue(ran)
    }

    @Test
    fun modalResult_is_idempotent_for_a_stale_id() {
        // The iOS fix may route both a button tap AND a presentation-dismiss callback through onModalResult.
        // The id guard must make the second call a no-op so the lane never skips the next queued modal.
        val c = controller()
        val first = c.alert(actions = listOf(NativeAlertAction("OK")))
        val second = c.alert(actions = listOf(NativeAlertAction("OK")))
        c.onModalResult(first, null) // first resolved → second promoted
        assertEquals(second, c.activeModal?.id)
        c.onModalResult(first, null) // stale id: must NOT advance past `second`
        assertEquals(second, c.activeModal?.id)
    }

    @Test
    fun dismissCurrentModal_invokes_onCancel_then_advances() {
        val c = controller()
        var cancelled = false
        val id = c.confirmationSheet(actions = listOf(NativeConfirmationAction("Delete")), onCancel = { cancelled = true })
        assertEquals(id, c.activeModal?.id)
        c.dismissCurrentModal()
        assertTrue(cancelled)
        assertNull(c.activeModal)
    }

    @Test
    fun second_modal_queues_then_promotes_in_order() {
        val c = controller()
        val a = c.alert(actions = listOf(NativeAlertAction("A")))
        val b = c.alert(actions = listOf(NativeAlertAction("B")))
        assertEquals(a, c.activeModal?.id) // first shows, second waits
        c.onModalResult(a, null)
        assertEquals(b, c.activeModal?.id) // second promoted
        c.onModalResult(b, null)
        assertNull(c.activeModal)
    }
}
