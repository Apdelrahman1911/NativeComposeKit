package io.github.apdelrahman1911.nativecomposekit.app.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

private enum class TestTab(override val id: String) : NativeTab { A("a"), B("b") }
private data class TestRoute(override val id: String) : NativeRoute

private fun navigator() = createNativeNavigator(
    tabs = listOf(TestTab.A, TestTab.B),
    initialTab = TestTab.A,
    rootRoutes = { tab -> TestRoute("${tab.id}-root") },
)

class NativeNavigatorTest {

    @Test
    fun starts_at_root_of_initial_tab() {
        val nav = navigator()
        assertEquals(TestTab.A, nav.state.selectedTab)
        assertEquals(1, nav.state.currentStack().size)
        assertEquals("a-root", nav.state.top().id)
    }

    @Test
    fun push_then_pop_depth_math() {
        val nav = navigator()
        nav.push(TestRoute("a-1"))
        nav.push(TestRoute("a-2"))
        assertEquals(3, nav.state.currentStack().size)
        assertEquals("a-2", nav.state.top().id)

        assertTrue(nav.pop())
        assertEquals("a-1", nav.state.top().id)
        assertTrue(nav.pop())
        assertEquals("a-root", nav.state.top().id)
        // at root: pop is a no-op and returns false
        assertFalse(nav.pop())
        assertEquals(1, nav.state.currentStack().size)
    }

    @Test
    fun popToRoot_clears_to_root() {
        val nav = navigator()
        nav.push(TestRoute("a-1")); nav.push(TestRoute("a-2")); nav.push(TestRoute("a-3"))
        nav.popToRoot()
        assertEquals(1, nav.state.currentStack().size)
        assertEquals("a-root", nav.state.top().id)
    }

    @Test
    fun per_tab_stacks_are_isolated_and_preserved() {
        val nav = navigator()
        nav.push(TestRoute("a-1"))           // tab A depth 2
        nav.selectTab(TestTab.B)
        assertEquals(1, nav.state.currentStack().size) // tab B untouched
        nav.push(TestRoute("b-1")); nav.push(TestRoute("b-2"))
        assertEquals(3, nav.state.currentStack().size) // tab B depth 3
        nav.selectTab(TestTab.A)
        assertEquals(2, nav.state.currentStack().size) // tab A preserved at depth 2
        assertEquals("a-1", nav.state.top().id)
    }

    @Test
    fun replaceStack_resets_a_tab() {
        val nav = navigator()
        nav.push(TestRoute("a-1"))
        nav.replaceStack(listOf(TestRoute("x"), TestRoute("y")))
        assertEquals(listOf("x", "y"), nav.state.currentStack().map { it.id })
        // cross-tab replace targets the named tab without selecting it
        nav.replaceStack(TestTab.B, listOf(TestRoute("b-root"), TestRoute("b-deep")))
        assertEquals(TestTab.A, nav.state.selectedTab)
        assertEquals(2, nav.state.stackFor(TestTab.B).size)
    }

    @Test
    fun sheet_present_and_dismiss() {
        val nav = navigator()
        assertNull(nav.snapshot().sheetId)
        nav.presentSheet(TestRoute("sheet/edit"))
        assertEquals("sheet/edit", nav.snapshot().sheetId)
        nav.dismissSheet()
        assertNull(nav.snapshot().sheetId)
    }

    @Test
    fun snapshot_mirrors_state() {
        val nav = navigator()
        nav.push(TestRoute("a-1"))
        nav.selectTab(TestTab.B)
        val snap = nav.snapshot()
        assertEquals("b", snap.selectedTabId)
        assertEquals(listOf("a", "b"), snap.tabIds)
        assertEquals(listOf("a-root", "a-1"), snap.stackIdsByTab["a"])
        assertEquals(listOf("b-root"), snap.stackIdsByTab["b"])
    }

    @Test
    fun observe_fires_initial_and_per_intent_then_cancel_stops() {
        val nav = navigator()
        val seen = mutableListOf<NativeNavSnapshot>()
        val handle = nav.observe { seen.add(it) }
        assertEquals(1, seen.size) // initial emission
        nav.push(TestRoute("a-1"))
        nav.selectTab(TestTab.B)
        assertEquals(3, seen.size)
        assertEquals("b", seen.last().selectedTabId)
        handle.cancel()
        nav.push(TestRoute("b-1"))
        assertEquals(3, seen.size) // no more deliveries after cancel
    }

    @Test
    fun push_is_idempotent_at_the_top() {
        // Regression: a fast double-tap (or a re-fired click as a screen re-appears) must not stack the same
        // route twice — pushing the route already on top is a no-op. Guards the historical duplicate-push.
        val nav = navigator()
        nav.push(TestRoute("a-1"))
        assertEquals(2, nav.state.currentStack().size)
        nav.push(TestRoute("a-1")) // same id already on top → ignored
        assertEquals(2, nav.state.currentStack().size)
        assertEquals("a-1", nav.state.top().id)
        nav.push(TestRoute("a-2")) // a different route still pushes
        assertEquals(3, nav.state.currentStack().size)
    }

    @Test
    fun reselecting_the_current_tab_pops_it_to_root() {
        // Platform convention on both OSes: a re-tap of the selected tab returns to that tab's start.
        val nav = navigator()
        nav.push(TestRoute("a-1"))
        nav.push(TestRoute("a-2"))
        assertEquals(3, nav.state.currentStack().size)
        nav.selectTab(TestTab.A)
        assertEquals(1, nav.state.currentStack().size)
        assertEquals("a-root", nav.state.top().id)
        // Re-tapping at the root stays a no-op.
        nav.selectTab(TestTab.A)
        assertEquals(1, nav.state.currentStack().size)
    }

    @Test
    fun selecting_a_foreign_tab_fails_fast_instead_of_corrupting_state() {
        val nav = navigator()
        val foreign = object : NativeTab { override val id: String = "not-mine" }
        val failed = runCatching { nav.selectTab(foreign) }
        assertTrue(failed.exceptionOrNull() is IllegalArgumentException)
        assertEquals(TestTab.A, nav.state.selectedTab) // untouched
    }

    @Test
    fun push_rejects_a_duplicate_deeper_in_the_stack() {
        // Route ids must stay unique per stack — every renderer keys entries on them (Navigation 3's
        // contentKey, the iOS shell's per-entry view controllers). Duplicate-of-top stays a benign no-op;
        // a duplicate DEEPER in the stack is a programming error and fails fast with state untouched.
        val nav = navigator()
        nav.push(TestRoute("a-1"))
        nav.push(TestRoute("a-2"))
        val failed = runCatching { nav.push(TestRoute("a-1")) }
        assertTrue(failed.exceptionOrNull() is IllegalArgumentException)
        assertEquals(listOf("a-root", "a-1", "a-2"), nav.state.currentStack().map { it.id })
    }

    @Test
    fun popTo_truncates_to_the_target_entry() {
        // The iOS shell reports a committed user pop as the entry the user LANDED ON (not a count) — a
        // long-press back-menu jump or a mid-gesture race must truncate to exactly that entry.
        val nav = navigator()
        nav.push(TestRoute("a-1")); nav.push(TestRoute("a-2")); nav.push(TestRoute("a-3"))
        nav.popTo(TestTab.A, "a-1")
        assertEquals(listOf("a-root", "a-1"), nav.state.currentStack().map { it.id })
    }

    @Test
    fun popTo_is_idempotent_and_ignores_absent_entries() {
        val nav = navigator()
        nav.push(TestRoute("a-1"))
        val seen = mutableListOf<NativeNavSnapshot>()
        nav.observe { seen.add(it) } // 1 initial emission
        nav.popTo(TestTab.A, "a-1")      // already on top → no-op, no emission (a duplicate didShow report)
        nav.popTo(TestTab.A, "ghost")    // not in the stack → no-op, no emission (projection re-asserts instead)
        assertEquals(1, seen.size)
        assertEquals(listOf("a-root", "a-1"), nav.state.currentStack().map { it.id })
    }

    @Test
    fun popTo_is_tab_scoped_and_never_reads_the_selected_tab() {
        // A pop committing on tab A's renderer while the user has switched to tab B must truncate A, not B.
        val nav = navigator()
        nav.push(TestRoute("a-1")); nav.push(TestRoute("a-2"))
        nav.selectTab(TestTab.B)
        nav.push(TestRoute("b-1"))
        nav.popTo(TestTab.A, "a-root")
        assertEquals(TestTab.B, nav.state.selectedTab)                       // selection untouched
        assertEquals(listOf("b-root", "b-1"), nav.state.currentStack().map { it.id }) // B untouched
        assertEquals(listOf("a-root"), nav.state.stackFor(TestTab.A).map { it.id })   // A truncated
    }
}
