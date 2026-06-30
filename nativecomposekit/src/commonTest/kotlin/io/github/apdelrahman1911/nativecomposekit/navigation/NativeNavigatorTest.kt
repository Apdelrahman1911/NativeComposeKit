package io.github.apdelrahman1911.nativecomposekit.navigation

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
    fun reconcileStack_is_idempotent_and_truncates_on_pop() {
        val nav = navigator()
        nav.push(TestRoute("a-1")); nav.push(TestRoute("a-2"))
        // same ids → no-op
        nav.reconcileStack("a", listOf("a-root", "a-1", "a-2"))
        assertEquals(3, nav.state.currentStack().size)
        // SwiftUI reports a back-swipe (shorter path) → truncate
        nav.reconcileStack("a", listOf("a-root", "a-1"))
        assertEquals(listOf("a-root", "a-1"), nav.state.currentStack().map { it.id })
        // unknown id → ignored
        nav.reconcileStack("a", listOf("a-root", "ghost"))
        assertEquals(listOf("a-root", "a-1"), nav.state.currentStack().map { it.id })
    }
}
