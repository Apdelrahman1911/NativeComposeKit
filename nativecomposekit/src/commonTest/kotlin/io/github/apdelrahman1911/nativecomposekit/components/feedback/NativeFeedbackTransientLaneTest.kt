package io.github.apdelrahman1911.nativecomposekit.components.feedback

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * The transient lane's state machine: FIFO advance, [NativeQueueBehavior], stale-id guards, and — most
 * importantly — the detach-then-notify contract: every user callback runs AFTER the lane has advanced, so
 * re-entrant posts/dismissals from inside a callback can't recurse, double-advance, or drop queued records.
 */
class NativeFeedbackTransientLaneTest {

    private fun controller() = NativeFeedbackController()

    @Test
    fun enqueue_advances_fifo_on_timeout() {
        val c = controller()
        val first = c.toast("one")
        val second = c.toast("two")

        assertEquals(first, c.activeTransient?.id)
        c.onTransientTimeout(first)
        assertEquals(second, c.activeTransient?.id)
        c.onTransientTimeout(second)
        assertNull(c.activeTransient)
    }

    @Test
    fun replaceCurrent_clears_the_queue_and_fires_onDismiss_once() {
        val c = controller()
        var dismissed = 0
        val first = c.toast("one", onDismiss = { dismissed++ })
        c.toast("queued") // will be cleared by ReplaceCurrent
        val replacement = c.toast("replacement", queue = NativeQueueBehavior.ReplaceCurrent)

        assertEquals(replacement, c.activeTransient?.id)
        assertEquals(1, dismissed)
        // The cleared queue entry is gone: finishing the replacement empties the lane.
        c.onTransientTimeout(replacement)
        assertNull(c.activeTransient)
        // Stale dismiss of the long-gone first toast is a no-op.
        c.dismiss(first)
        assertEquals(1, dismissed)
    }

    @Test
    fun replaceCurrent_posted_from_onDismiss_does_not_recurse() {
        val c = controller()
        var chained = 0
        // The regression this guards: onDismiss posting another ReplaceCurrent used to re-enter the branch
        // with the same record still active and recurse until stack overflow.
        c.toast(
            "one",
            onDismiss = {
                if (chained == 0) {
                    chained++
                    c.toast("from-callback", queue = NativeQueueBehavior.ReplaceCurrent)
                }
            },
        )
        c.toast("replacement", queue = NativeQueueBehavior.ReplaceCurrent)

        assertEquals(1, chained)
        assertNotNull(c.activeTransient) // a message is showing; no crash, no empty lane
    }

    @Test
    fun dropIfShowing_drops_while_anything_is_active_or_queued() {
        val c = controller()
        val first = c.toast("one")
        c.toast("dropped", queue = NativeQueueBehavior.DropIfShowing)

        assertEquals(first, c.activeTransient?.id)
        c.onTransientTimeout(first)
        assertNull(c.activeTransient) // the dropped toast never entered the queue
    }

    @Test
    fun stale_timeout_and_action_ids_are_ignored() {
        val c = controller()
        var dismissed = 0
        var acted = 0
        val id = c.snackbar("msg", actionLabel = "Undo", onAction = { acted++ }, onDismiss = { dismissed++ })

        c.onTransientTimeout(id + 999) // stale timer
        assertEquals(id, c.activeTransient?.id)
        c.onTransientAction(id + 999) // stale action
        assertEquals(0, acted)

        c.onTransientTimeout(id)
        assertEquals(1, dismissed)
        c.onTransientTimeout(id) // late duplicate timer after advance: no double dismiss
        assertEquals(1, dismissed)
    }

    @Test
    fun dismissing_a_queued_id_removes_it_silently() {
        val c = controller()
        var dismissed = 0
        val first = c.toast("one")
        val queued = c.toast("queued", onDismiss = { dismissed++ })

        c.dismiss(queued)
        assertEquals(0, dismissed) // queued removal runs no callback
        c.onTransientTimeout(first)
        assertNull(c.activeTransient) // the queued record is gone
    }

    @Test
    fun action_advances_first_so_a_reentrant_dismiss_is_a_noop() {
        val c = controller()
        var dismissed = 0
        var acted = 0
        var id = 0L
        id = c.snackbar(
            "msg",
            actionLabel = "Undo",
            onAction = {
                acted++
                c.dismiss(id) // used to pass the id guard → spurious onDismiss + double advance
            },
            onDismiss = { dismissed++ },
        )
        val second = c.toast("next")

        c.onTransientAction(id)
        assertEquals(1, acted)
        assertEquals(0, dismissed) // the action did NOT count as a dismiss
        assertEquals(second, c.activeTransient?.id) // the queued record was not dropped
    }

    @Test
    fun dismissCurrent_dismisses_and_advances() {
        val c = controller()
        var dismissed = 0
        c.banner("pinned", duration = NativeFeedbackDuration.Indefinite, onDismiss = { dismissed++ })
        val second = c.toast("next")

        c.dismissCurrent()
        assertEquals(1, dismissed)
        assertEquals(second, c.activeTransient?.id)
    }

    @Test
    fun clearAll_empties_both_lanes_without_callbacks() {
        val c = controller()
        var callbacks = 0
        c.toast("one", onDismiss = { callbacks++ })
        c.toast("two", onDismiss = { callbacks++ })
        c.alert(title = "t", actions = emptyList(), onCancel = { callbacks++ })

        c.clearAll()
        assertEquals(0, callbacks)
        assertNull(c.activeTransient)
        assertNull(c.activeModal)
    }

    @Test
    fun modal_action_advances_first_so_a_reentrant_dismiss_cannot_double_advance() {
        val c = controller()
        var cancelled = 0
        var firstId = 0L
        firstId = c.alert(
            title = "first",
            actions = emptyList(),
            onCancel = { cancelled++ },
        )
        val secondId = c.alert(title = "second", actions = emptyList())

        // The action re-entrantly dismisses the (already-finished) first modal — used to fire a spurious
        // onCancel and advance twice, silently dropping the second alert.
        c.onModalResult(firstId) { c.dismiss(firstId) }
        assertEquals(0, cancelled)
        assertEquals(secondId, c.activeModal?.id)
    }
}
