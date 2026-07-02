package io.github.apdelrahman1911.nativecomposekit.app.navigation

import androidx.compose.runtime.Stable

/**
 * The single source of truth for the sample app's navigation. This is **reference wiring** that ships with the
 * sample, not part of the published kit — a real consumer brings its own navigation library and adapts it to the
 * kit's `NativeChromeSource` contract the same way [NativeNavChrome] does here.
 *
 * Renderers — the Compose `NativeNavHost` / `NativeNavContent`, and (on iOS) the native-chrome adapter — are
 * projections that render this state and report user actions back as intents; there is never a second independent
 * stack. It mirrors the `@Stable` + intent-method shape used by the kit's feedback controller.
 *
 * Intents mutate [state] (snapshot state → Compose recomposes) and then notify [observe] subscribers (→ the iOS
 * chrome projection updates). All mutation flows through these methods, so the observer fires deterministically
 * even when no Compose composition is driving the navigator (the iOS case).
 */
@Stable
class NativeNavigator internal constructor(val state: NativeNavigationState) {

    private val observers = mutableListOf<(NativeNavSnapshot) -> Unit>()

    // ---- Intent API ----

    /** Compact dump of every tab's stack for tracing. */
    private fun stacksDump(): String =
        state.tabs.joinToString("  ") { t -> "${t.id}=[${state.stackFor(t).joinToString(",") { it.id }}]" }

    /**
     * Push [route] onto the selected tab's stack. Idempotent at the top: pushing the route that is already the
     * top is a no-op. This both forbids an invalid duplicate-of-top stack and absorbs a repeated push of the same
     * screen (e.g. a double tap, or a re-fired click as a screen re-appears) instead of stacking it twice.
     */
    fun push(route: NativeRoute) {
        val stack = state.stackFor(state.selectedTab)
        if (stack.lastOrNull()?.id == route.id) {
            NativeNavLog.log { "push ignored (already on top) '${route.id}' sel=${state.selectedTab.id}" }
            return
        }
        stack.add(route)
        NativeNavLog.log { "push '${route.id}' -> sel=${state.selectedTab.id}  ${stacksDump()}" }
        notifyObservers()
    }

    /** Pop the top of the selected tab's stack. Returns false (and does nothing) if already at the root. */
    fun pop(): Boolean {
        val stack = state.stackFor(state.selectedTab)
        if (stack.size <= 1) {
            NativeNavLog.log { "pop ignored (at root) sel=${state.selectedTab.id}  ${stacksDump()}" }
            return false
        }
        val removed = stack.removeAt(stack.lastIndex)
        NativeNavLog.log { "pop '${removed.id}' -> sel=${state.selectedTab.id}  ${stacksDump()}" }
        notifyObservers()
        return true
    }

    /** Pop the selected tab's stack back to its root. */
    fun popToRoot() {
        val stack = state.stackFor(state.selectedTab)
        if (stack.size <= 1) return
        while (stack.size > 1) stack.removeAt(stack.lastIndex)
        notifyObservers()
    }

    /**
     * Select a tab (tab-bar tap). Re-selecting the already-selected tab pops it to its root — the platform
     * convention on both OSes (iOS tab bars and Android bottom navigation both treat a re-tap as
     * "take me back to the start of this tab"). [tab] must be one of the navigator's tabs.
     */
    fun selectTab(tab: NativeTab) {
        require(state.tabById(tab.id) != null) {
            "selectTab('${tab.id}') is not one of the navigator's tabs: ${state.tabs.map { it.id }}"
        }
        if (state.selectedTab.id == tab.id) {
            NativeNavLog.log { "selectTab '${tab.id}' re-selected -> popToRoot" }
            popToRoot()
            return
        }
        state.selectedTab = tab
        NativeNavLog.log { "selectTab '${tab.id}'  ${stacksDump()}" }
        notifyObservers()
    }

    /** Present [route] as a sheet over the current content. */
    fun presentSheet(route: NativeRoute) {
        state.sheet = route
        notifyObservers()
    }

    /** Dismiss the current sheet, if any. */
    fun dismissSheet() {
        if (state.sheet == null) return
        state.sheet = null
        notifyObservers()
    }

    /** Replace [tab]'s entire stack (deep-link / cross-tab entry). [routes] must be non-empty (root-first). */
    fun replaceStack(tab: NativeTab, routes: List<NativeRoute>) {
        require(routes.isNotEmpty()) { "replaceStack requires a non-empty stack (at least a root route)" }
        val stack = state.stackFor(tab)
        stack.clear()
        stack.addAll(routes)
        notifyObservers()
    }

    /** Replace the selected tab's stack. */
    fun replaceStack(routes: List<NativeRoute>): Unit = replaceStack(state.selectedTab, routes)

    // ---- Observation: the iOS native-chrome adapter subscribes here ----

    /** An immutable, ObjC-friendly projection of the whole nav state (all ids are plain strings). */
    fun snapshot(): NativeNavSnapshot = NativeNavSnapshot(
        selectedTabId = state.selectedTab.id,
        tabIds = state.tabs.map { it.id },
        stackIdsByTab = state.tabs.associate { tab -> tab.id to state.stackFor(tab).map { it.id } },
        sheetId = state.sheet?.id,
    )

    /**
     * Subscribe to state changes. [onChange] fires once immediately with the current snapshot, then after every
     * intent. Returns a cancellable; call [NativeNavCancellable.cancel] to stop (and break any retain cycle).
     */
    fun observe(onChange: (NativeNavSnapshot) -> Unit): NativeNavCancellable {
        observers.add(onChange)
        onChange(snapshot())
        return NativeNavCancellable { observers.remove(onChange) }
    }

    private fun notifyObservers() {
        if (observers.isEmpty()) return
        val s = snapshot()
        observers.toList().forEach { it(s) }
    }

}

/** Immutable projection of [NativeNavigator] state, safe to hand across the Kotlin↔Swift boundary. */
data class NativeNavSnapshot(
    val selectedTabId: String,
    val tabIds: List<String>,
    /** tabId → [routeId...] root-first. */
    val stackIdsByTab: Map<String, List<String>>,
    val sheetId: String?,
)

/** Handle returned by [NativeNavigator.observe]; [cancel] removes the subscription. */
fun interface NativeNavCancellable {
    fun cancel()
}
