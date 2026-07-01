package io.github.apdelrahman1911.nativecomposekit.app.navigation.example

import io.github.apdelrahman1911.nativecomposekit.chrome.NativeChromeCancellable
import io.github.apdelrahman1911.nativecomposekit.chrome.NativeChromeState
import io.github.apdelrahman1911.nativecomposekit.chrome.NativeChromeStateSource
import io.github.apdelrahman1911.nativecomposekit.chrome.NativeChromeTab
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * A **second, deliberately different** reference navigator, here purely to prove [NativeChromeStateSource] is
 * genuinely navigation-agnostic. Unlike the sample's `NativeNavigator` (per-tab `SnapshotStateList` mutated in
 * place + a hand-rolled observer list), this holds a single immutable state value in a `StateFlow`, uses one flat
 * back stack, and resets to the tab root on tab switch. If the chrome contract can project this too, it does not
 * assume the navigator's shape. This lives in `commonTest` — the whole projection is unit-tested in shared code.
 */
private data class MiniDest(val id: String, val title: String)
private data class MiniNav(val tabId: String, val stack: List<MiniDest>, val sheet: MiniDest?)

private class MiniRouter(initialTab: String, private val rootFor: (String) -> MiniDest) {
    private val _state = MutableStateFlow(MiniNav(initialTab, listOf(rootFor(initialTab)), sheet = null))
    val state: StateFlow<MiniNav> = _state
    fun push(dest: MiniDest) = _state.update { it.copy(stack = it.stack + dest) }
    fun pop() = _state.update { if (it.stack.size > 1) it.copy(stack = it.stack.dropLast(1)) else it }
    fun selectTab(tabId: String) =
        _state.update { if (it.tabId == tabId) it else MiniNav(tabId, listOf(rootFor(tabId)), it.sheet) }
    fun openSheet(dest: MiniDest) = _state.update { it.copy(sheet = dest) }
    fun closeSheet() = _state.update { it.copy(sheet = null) }
}

/**
 * The proof: a [NativeChromeStateSource] over [MiniRouter], written **entirely in shared code** (no iOS types).
 * A real consumer writes the same kind of adapter over Voyager / Decompose / Compose Navigation, then adds the
 * iOS `sheetViewController` to expose it as a `NativeChromeSource`. See docs/navigation.md.
 */
private class MiniChromeSource(
    private val router: MiniRouter,
    private val tabs: List<NativeChromeTab>,
    private val scope: CoroutineScope,
) : NativeChromeStateSource {
    override fun currentState(): NativeChromeState {
        val s = router.state.value
        return NativeChromeState(
            title = s.stack.last().title,
            canGoBack = s.stack.size > 1,
            selectedTabId = s.tabId,
            tabs = tabs,
            actions = emptyList(),
            sheetId = s.sheet?.id,
        )
    }

    override fun observe(onChange: (NativeChromeState) -> Unit): NativeChromeCancellable {
        // StateFlow delivers the current value on collect, then every subsequent one — matching the contract's
        // "fire once immediately, then after every change". A callback-based nav would wire its own observer here.
        val job = scope.launch { router.state.collect { onChange(currentState()) } }
        return NativeChromeCancellable { job.cancel() }
    }

    override fun backRequested() = router.pop()
    override fun tabSelected(tabId: String) = router.selectTab(tabId)
    override fun actionTapped(actionId: String) { /* this demo declares no top-bar actions */ }
    override fun dismissSheet() = router.closeSheet()
}

class MiniRouterChromeSourceTest {
    private val tabs = listOf(
        NativeChromeTab("home", "Home", "house"),
        NativeChromeTab("settings", "Settings", "gearshape"),
    )

    private fun source(scope: CoroutineScope): Pair<MiniRouter, MiniChromeSource> {
        val router = MiniRouter("home") { id -> MiniDest("$id/root", id.replaceFirstChar { it.uppercase() }) }
        return router to MiniChromeSource(router, tabs, scope)
    }

    @Test
    fun projects_initial_navigation_state() {
        val (_, src) = source(CoroutineScope(Dispatchers.Unconfined))
        val s = src.currentState()
        assertEquals("Home", s.title)
        assertFalse(s.canGoBack)
        assertEquals("home", s.selectedTabId)
        assertEquals(listOf("home", "settings"), s.tabs.map { it.id })
        assertNull(s.sheetId)
    }

    @Test
    fun back_intent_pops_and_updates_canGoBack() {
        val (router, src) = source(CoroutineScope(Dispatchers.Unconfined))
        router.push(MiniDest("home/detail", "Detail"))
        assertEquals("Detail", src.currentState().title)
        assertTrue(src.currentState().canGoBack)
        src.backRequested()
        assertEquals("Home", src.currentState().title)
        assertFalse(src.currentState().canGoBack)
        src.backRequested() // at the root: a no-op, the source never underflows
        assertEquals("Home", src.currentState().title)
    }

    @Test
    fun tab_intent_switches_selected_tab() {
        val (_, src) = source(CoroutineScope(Dispatchers.Unconfined))
        src.tabSelected("settings")
        val s = src.currentState()
        assertEquals("settings", s.selectedTabId)
        assertEquals("Settings", s.title)
        assertFalse(s.canGoBack)
    }

    @Test
    fun sheet_id_reflects_open_and_dismiss_intent() {
        val (router, src) = source(CoroutineScope(Dispatchers.Unconfined))
        router.openSheet(MiniDest("edit", "Edit"))
        assertEquals("edit", src.currentState().sheetId)
        src.dismissSheet()
        assertNull(src.currentState().sheetId)
    }

    @Test
    fun observe_fires_initial_then_on_each_change_then_stops_on_cancel() {
        val (router, src) = source(CoroutineScope(Dispatchers.Unconfined))
        val seen = mutableListOf<NativeChromeState>()
        val cancellable = src.observe { seen.add(it) }
        assertEquals(1, seen.size) // current value delivered immediately
        router.push(MiniDest("home/detail", "Detail"))
        assertEquals(2, seen.size)
        assertEquals("Detail", seen.last().title)
        cancellable.cancel()
        router.push(MiniDest("home/more", "More"))
        assertEquals(2, seen.size) // no deliveries after cancel
    }
}
