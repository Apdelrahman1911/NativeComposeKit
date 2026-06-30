package io.github.apdelrahman1911.nativecomposekit.navigation

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList

/**
 * Observable navigation state owned by [NativeNavigator] (the single source of truth). Everything Compose reads
 * goes through snapshot state, so both platforms' Compose layers recompose automatically; the iOS adapter
 * additionally projects this to SwiftUI via [NativeNavigator.observe].
 *
 * Each tab has its **own** back stack (`SnapshotStateList`, root at index 0), so push/pop in one tab does not
 * recompose another and switching tabs preserves each tab's depth.
 */
@Stable
public class NativeNavigationState internal constructor(
    public val tabs: List<NativeTab>,
    initialTab: NativeTab,
    rootRoutes: (NativeTab) -> NativeRoute,
) {
    public var selectedTab: NativeTab by mutableStateOf(initialTab)
        internal set

    internal val stacks: Map<String, SnapshotStateList<NativeRoute>> =
        tabs.associate { tab -> tab.id to mutableStateListOf(rootRoutes(tab)) }

    /** The route presented as a sheet over the current tab, or null. */
    public var sheet: NativeRoute? by mutableStateOf<NativeRoute?>(null)
        internal set

    internal fun stackFor(tab: NativeTab): SnapshotStateList<NativeRoute> = stacks.getValue(tab.id)

    /** The selected tab's back stack (root-first). */
    public fun currentStack(): List<NativeRoute> = stacks.getValue(selectedTab.id)

    /** The top (visible) route of the selected tab. */
    public fun top(): NativeRoute = currentStack().last()

    internal fun tabById(id: String): NativeTab? = tabs.firstOrNull { it.id == id }

    /** Resolve a route id back to its live route instance (searches every stack + the sheet). */
    internal fun routeById(id: String): NativeRoute? {
        stacks.values.forEach { stack -> stack.firstOrNull { it.id == id }?.let { return it } }
        return sheet?.takeIf { it.id == id }
    }
}
