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

    @Test
    fun dismiss_by_id_of_the_active_modal_cancels_it_and_promotes_the_queued_one() {
        val c = controller()
        var cancelled = 0
        val first = c.alert(title = "first", actions = listOf(NativeAlertAction("OK")), onCancel = { cancelled++ })
        val second = c.alert(title = "second", actions = listOf(NativeAlertAction("OK")))
        c.dismiss(first) // the active modal: treated as a cancel, not a silent queue removal
        assertEquals(1, cancelled)
        assertEquals(second, c.activeModal?.id)
    }

    @Test
    fun post_from_onCancel_is_accepted_and_promoted() {
        // The lane advances BEFORE onCancel runs, so a modal chained from inside the callback must land in a
        // settled lane — either promoted immediately (empty queue) or queued in order.
        val c = controller()
        var chained = -1L
        c.alert(
            title = "first",
            actions = listOf(NativeAlertAction("OK")),
            onCancel = { chained = c.alert(title = "chained", actions = listOf(NativeAlertAction("OK"))) },
        )
        c.dismissCurrentModal()
        assertEquals(chained, c.activeModal?.id)
        c.onModalResult(chained, null)
        assertNull(c.activeModal)
    }

    @Test
    fun post_from_a_modal_result_callback_is_accepted_and_promoted() {
        val c = controller()
        var chained = -1L
        val id = c.alert(actions = listOf(NativeAlertAction("OK")))
        c.onModalResult(id) {
            chained = c.confirmationSheet(actions = listOf(NativeConfirmationAction("Go")))
        }
        assertEquals(chained, c.activeModal?.id)
    }

    @Test
    fun modal_and_transient_lanes_are_independent() {
        val c = controller()
        val toastId = c.toast("busy")
        val alertId = c.alert(actions = listOf(NativeAlertAction("OK")))
        // One active record in EACH lane at the same time — a modal overlays a transient, never replaces it.
        assertEquals(toastId, c.activeTransient?.id)
        assertEquals(alertId, c.activeModal?.id)
        c.onModalResult(alertId, null)
        assertNull(c.activeModal)
        assertEquals(toastId, c.activeTransient?.id) // resolving one lane leaves the other untouched
        c.onTransientTimeout(toastId)
        assertNull(c.activeTransient)
    }
}
