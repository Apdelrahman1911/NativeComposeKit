package io.github.apdelrahman1911.nativecomposekit.navigation

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList

/**
 * Observable navigation state owned by [BrandNavigator] (the single source of truth). Everything Compose reads
 * goes through snapshot state, so both platforms' Compose layers recompose automatically; the iOS adapter
 * additionally projects this to SwiftUI via [BrandNavigator.observe].
 *
 * Each tab has its **own** back stack (`SnapshotStateList`, root at index 0), so push/pop in one tab does not
 * recompose another and switching tabs preserves each tab's depth.
 */
@Stable
public class BrandNavigationState internal constructor(
    public val tabs: List<BrandTab>,
    initialTab: BrandTab,
    rootRoutes: (BrandTab) -> BrandRoute,
) {
    public var selectedTab: BrandTab by mutableStateOf(initialTab)
        internal set

    internal val stacks: Map<String, SnapshotStateList<BrandRoute>> =
        tabs.associate { tab -> tab.id to mutableStateListOf(rootRoutes(tab)) }

    /** The route presented as a sheet over the current tab, or null. */
    public var sheet: BrandRoute? by mutableStateOf<BrandRoute?>(null)
        internal set

    internal fun stackFor(tab: BrandTab): SnapshotStateList<BrandRoute> = stacks.getValue(tab.id)

    /** The selected tab's back stack (root-first). */
    public fun currentStack(): List<BrandRoute> = stacks.getValue(selectedTab.id)

    /** The top (visible) route of the selected tab. */
    public fun top(): BrandRoute = currentStack().last()

    internal fun tabById(id: String): BrandTab? = tabs.firstOrNull { it.id == id }

    /** Resolve a route id back to its live route instance (searches every stack + the sheet). */
    internal fun routeById(id: String): BrandRoute? {
        stacks.values.forEach { stack -> stack.firstOrNull { it.id == id }?.let { return it } }
        return sheet?.takeIf { it.id == id }
    }
}
