package com.ukkera.brandkit.navigation

import androidx.compose.runtime.Stable

/**
 * The **single source of truth** for navigation (architecture.md §7). Renderers (the Android `BrandNavHost`,
 * the iOS SwiftUI shell) are projections that render this state and report user actions back as intents — there
 * is never a second independent stack. Mirrors the `@Stable` + intent-method shape of
 * `components/feedback/BrandFeedbackController`.
 *
 * Intents mutate [state] (snapshot state → Compose recomposes) and then notify [observe] subscribers (→ the
 * SwiftUI projection updates). All mutation flows through these methods, so the observer fires deterministically
 * even when no Compose composition is driving the navigator (the iOS case).
 */
@Stable
public class BrandNavigator internal constructor(public val state: BrandNavigationState) {

    private val observers = mutableListOf<(BrandNavSnapshot) -> Unit>()

    // ---- Intent API (architecture.md §7) ----

    /** Push [route] onto the selected tab's stack. */
    public fun push(route: BrandRoute) {
        state.stackFor(state.selectedTab).add(route)
        notifyObservers()
    }

    /** Pop the top of the selected tab's stack. Returns false (and does nothing) if already at the root. */
    public fun pop(): Boolean {
        val stack = state.stackFor(state.selectedTab)
        if (stack.size <= 1) return false
        stack.removeAt(stack.lastIndex)
        notifyObservers()
        return true
    }

    /** Pop the selected tab's stack back to its root. */
    public fun popToRoot() {
        val stack = state.stackFor(state.selectedTab)
        if (stack.size <= 1) return
        while (stack.size > 1) stack.removeAt(stack.lastIndex)
        notifyObservers()
    }

    /** Select a tab (tab-bar tap). No-op if already selected. */
    public fun selectTab(tab: BrandTab) {
        if (state.selectedTab.id == tab.id) return
        state.selectedTab = tab
        notifyObservers()
    }

    /** Present [route] as a sheet over the current content. */
    public fun presentSheet(route: BrandRoute) {
        state.sheet = route
        notifyObservers()
    }

    /** Dismiss the current sheet, if any. */
    public fun dismissSheet() {
        if (state.sheet == null) return
        state.sheet = null
        notifyObservers()
    }

    /** Replace [tab]'s entire stack (deep-link / cross-tab entry). [routes] must be non-empty (root-first). */
    public fun replaceStack(tab: BrandTab, routes: List<BrandRoute>) {
        require(routes.isNotEmpty()) { "replaceStack requires a non-empty stack (at least a root route)" }
        val stack = state.stackFor(tab)
        stack.clear()
        stack.addAll(routes)
        notifyObservers()
    }

    /** Replace the selected tab's stack. */
    public fun replaceStack(routes: List<BrandRoute>): Unit = replaceStack(state.selectedTab, routes)

    // ---- Observation: the SwiftUI bridge subscribes here ----

    /** An immutable, ObjC-friendly projection of the whole nav state (all ids are plain strings). */
    public fun snapshot(): BrandNavSnapshot = BrandNavSnapshot(
        selectedTabId = state.selectedTab.id,
        tabIds = state.tabs.map { it.id },
        stackIdsByTab = state.tabs.associate { tab -> tab.id to state.stackFor(tab).map { it.id } },
        sheetId = state.sheet?.id,
    )

    /**
     * Subscribe to state changes. [onChange] fires once immediately with the current snapshot, then after every
     * intent. Returns a cancellable; call [BrandNavCancellable.cancel] to stop (and break any retain cycle).
     */
    public fun observe(onChange: (BrandNavSnapshot) -> Unit): BrandNavCancellable {
        observers.add(onChange)
        onChange(snapshot())
        return BrandNavCancellable { observers.remove(onChange) }
    }

    private fun notifyObservers() {
        if (observers.isEmpty()) return
        val s = snapshot()
        observers.toList().forEach { it(s) }
    }

    // ---- Report-back helpers (string-keyed; used by the iOS bridge, same module) ----

    internal fun selectTabId(tabId: String) {
        state.tabById(tabId)?.let { selectTab(it) }
    }

    /**
     * Make [tabId]'s stack match [routeIds] (root-first) — the authoritative path SwiftUI reports after a
     * back-swipe / multi-pop. Idempotent: equal ids → no-op (prevents echo ping-pong). Only ids already live
     * in the stack are honored (pushes are Kotlin-driven via [push], never reported up from SwiftUI).
     */
    internal fun reconcileStack(tabId: String, routeIds: List<String>) {
        val tab = state.tabById(tabId) ?: return
        val stack = state.stackFor(tab)
        if (stack.map { it.id } == routeIds) return // idempotent no-op
        val byId = stack.associateBy { it.id }
        val resolved = routeIds.mapNotNull { byId[it] }
        if (resolved.isEmpty() || resolved.size != routeIds.size) return // unknown id → ignore (shouldn't happen)
        stack.clear()
        stack.addAll(resolved)
        notifyObservers()
    }
}

/** Immutable projection of [BrandNavigator] state, safe to hand across the Kotlin↔Swift boundary. */
public data class BrandNavSnapshot(
    val selectedTabId: String,
    val tabIds: List<String>,
    /** tabId → [routeId...] root-first. */
    val stackIdsByTab: Map<String, List<String>>,
    val sheetId: String?,
)

/** Handle returned by [BrandNavigator.observe]; [cancel] removes the subscription. */
public fun interface BrandNavCancellable {
    public fun cancel()
}
