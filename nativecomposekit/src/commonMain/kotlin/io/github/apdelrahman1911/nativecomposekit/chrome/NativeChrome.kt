package io.github.apdelrahman1911.nativecomposekit.chrome

import androidx.compose.runtime.Immutable

/**
 * A tab as the native `UITabBar` should render it. Compares by value (shells may de-duplicate emissions);
 * not a `data class` so consumer-constructed call sites survive future field additions binary-compatibly.
 */
@Immutable
public class NativeChromeTab(
    public val id: String,
    public val title: String,
    public val sfSymbol: String,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NativeChromeTab) return false
        return id == other.id && title == other.title && sfSymbol == other.sfSymbol
    }

    override fun hashCode(): Int = (id.hashCode() * 31 + title.hashCode()) * 31 + sfSymbol.hashCode()
}

/** A top-bar action (e.g. a "+"), rendered as a native `UIBarButtonItem`. Compares by value. */
@Immutable
public class NativeChromeAction(
    public val id: String,
    public val sfSymbol: String,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NativeChromeAction) return false
        return id == other.id && sfSymbol == other.sfSymbol
    }

    override fun hashCode(): Int = id.hashCode() * 31 + sfSymbol.hashCode()
}

/**
 * Per-screen chrome BEHAVIOR for one back-stack entry — the platform-neutral knobs that must travel with
 * the entry itself (appearance stays per-platform: Compose parameters/slots on a Material host, the iOS
 * shell style registry on UIKit). Everything defaults to today's behavior; a config is pure display data
 * like the rest of the projection. Compares by value; not a `data class` so fields stay appendable
 * binary-compatibly.
 *
 * - [hidesTopBar] / [hidesTabBar] — hide the navigation/tab bar while this entry is on top (immersive
 *   readers, full-bleed media). Renderers animate the change with their platform's own transition.
 * - [prefersLargeTitle] — opt this entry into a large navigation title where the platform supports it
 *   (the iOS shell; requires the shell style's large-title opt-in). NOTE: with Compose content there is no
 *   `UIScrollView` under the bar, so a large title does NOT collapse on scroll like native UIKit lists —
 *   it stays large while the entry is on top. Platforms without the concept ignore it.
 * - [actions] — this entry's OWN top-bar actions (rendered as native bar button items by the iOS shell),
 *   replacing the tab-scoped actions while the entry is on top. Empty = no per-screen actions.
 */
@Immutable
public class NativeBarConfig(
    public val hidesTopBar: Boolean = false,
    public val hidesTabBar: Boolean = false,
    public val prefersLargeTitle: Boolean = false,
    public val actions: List<NativeChromeAction> = emptyList(),
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NativeBarConfig) return false
        return hidesTopBar == other.hidesTopBar &&
            hidesTabBar == other.hidesTabBar &&
            prefersLargeTitle == other.prefersLargeTitle &&
            actions == other.actions
    }

    override fun hashCode(): Int {
        var result = hidesTopBar.hashCode()
        result = 31 * result + hidesTabBar.hashCode()
        result = 31 * result + prefersLargeTitle.hashCode()
        result = 31 * result + actions.hashCode()
        return result
    }

    public companion object {
        /** Today's behavior: bars visible, compact title, no per-screen actions. */
        public val Default: NativeBarConfig = NativeBarConfig()
    }
}

/**
 * One back-stack entry as chrome may render it: a stable [id] (unique within its stack — renderers key on it),
 * the [title] shown while that entry is on screen, and the entry's per-screen [bar] behavior. This is still
 * **display data, not a navigable stack**: a shell may build one screen/bar item per entry and animate between
 * them, but the only way it can change navigation is the intent methods — it never mutates these lists.
 * Compares by value.
 */
@Immutable
public class NativeChromeEntry(
    public val id: String,
    public val title: String,
    public val bar: NativeBarConfig = NativeBarConfig.Default,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NativeChromeEntry) return false
        return id == other.id && title == other.title && bar == other.bar
    }

    override fun hashCode(): Int = (id.hashCode() * 31 + title.hashCode()) * 31 + bar.hashCode()
}

/**
 * An immutable projection the native iOS chrome renders. It is pure display data and is never authoritative:
 * [backStacksByTab] lists each tab's entries so a stack-rendering shell (real `UINavigationController`s) can
 * build one screen per entry, but they are value snapshots — the only way any shell changes navigation is the
 * intent methods, and the source of truth stays your navigation system. [sheetId] only tells the shell whether
 * a sheet should be presented; the sheet's Compose content comes from the iOS
 * `NativeChromeSource.sheetViewController`. The shell treats one sheet at a time: to switch sheets, emit
 * `sheetId = null` first, then the new id.
 *
 * Adapters construct this on every navigation change, so it is deliberately **not** a `data class`: fields
 * ([backTitle] and [backStacksByTab] were appended this way) can be added without breaking compiled adapters.
 * Compares by value so shells can skip no-op emissions. `observe` callbacks may deliver on any thread —
 * marshal to the main thread before touching UIKit.
 */
@Immutable
public class NativeChromeState(
    public val title: String,
    public val canGoBack: Boolean,
    public val selectedTabId: String,
    public val tabs: List<NativeChromeTab>,
    public val actions: List<NativeChromeAction>,
    public val sheetId: String?,
    /** The previous destination's title — shown as the native back-button label. Null at a tab root. */
    public val backTitle: String? = null,
    /**
     * Every tab's back stack as chrome-renderable entries (tabId → root-first [NativeChromeEntry]s). Empty for
     * sources feeding a flat shell (a bare title bar needs only [title]/[backTitle]); a **stack-rendering**
     * shell (one native screen per entry, e.g. a `UINavigationController` mirror whose interactive pop makes
     * the bar and content track the finger) requires it. Still one-way display data: the shell renders these
     * and reports committed user pops via [NativeChromeStateSource.backCommitted] — it never mutates a stack.
     */
    public val backStacksByTab: Map<String, List<NativeChromeEntry>> = emptyMap(),
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NativeChromeState) return false
        return title == other.title &&
            canGoBack == other.canGoBack &&
            selectedTabId == other.selectedTabId &&
            tabs == other.tabs &&
            actions == other.actions &&
            sheetId == other.sheetId &&
            backTitle == other.backTitle &&
            backStacksByTab == other.backStacksByTab
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + canGoBack.hashCode()
        result = 31 * result + selectedTabId.hashCode()
        result = 31 * result + tabs.hashCode()
        result = 31 * result + actions.hashCode()
        result = 31 * result + (sheetId?.hashCode() ?: 0)
        result = 31 * result + (backTitle?.hashCode() ?: 0)
        result = 31 * result + backStacksByTab.hashCode()
        return result
    }
}

/** Handle returned by [NativeChromeStateSource.observe]; [cancel] stops delivery (and breaks any retain cycle). */
public fun interface NativeChromeCancellable {
    public fun cancel()
}

/**
 * The **nav-agnostic core** of the native-chrome contract — the part that is pure Kotlin and lives in `commonMain`,
 * so you can implement and unit-test your chrome projection in shared code. Bring your own navigation system and
 * project it here: emit a [NativeChromeState] out (title / back / tabs / actions, and whether a sheet is open) and
 * accept only intents in ([backRequested]/[tabSelected]/[actionTapped]/[dismissSheet]).
 *
 * An implementation must be a **ratified projection**: it may read your navigation state (including projecting
 * your stacks as display entries) and turn user actions into your navigation's own intents — but it never
 * exposes a mutable stack, and nothing on the native side ever writes one back. Even [backCommitted], the one
 * intent describing something the platform already did, is a request your navigation ratifies idempotently;
 * your navigation system stays the single source of truth. On iOS the chrome shell consumes the fuller
 * `NativeChromeSource` (this, plus the per-entry and sheet Compose content); a consumer typically implements
 * this base in shared code and supplies the iOS-specific pieces there. The kit itself owns no navigation.
 */
public interface NativeChromeStateSource {
    /** The current chrome to display, computed fresh from your live navigation state. */
    public fun currentState(): NativeChromeState

    /** Subscribe to chrome changes; must fire once immediately with the current state, then after every change. */
    public fun observe(onChange: (NativeChromeState) -> Unit): NativeChromeCancellable

    /** The back affordance was tapped — perform your navigation's "back". */
    public fun backRequested()

    /**
     * The shell **committed** a user pop on [tabId] and landed on [entryId] — ratify it: make that tab's stack
     * end at [entryId]. Only stack-rendering shells call this (a platform pop the user already watched finish,
     * e.g. `UINavigationController`'s interactive swipe/back button, is reported after the fact — unlike
     * [backRequested], which asks *permission* before anything moves). Implementations MUST be idempotent
     * (already there → no-op; unknown [entryId] → no-op) so a duplicate or stale report converges instead of
     * corrupting; they must never treat it as a stack write. The default no-op suits sources that only ever
     * feed flat shells — override it if your chrome projects [NativeChromeState.backStacksByTab].
     */
    public fun backCommitted(tabId: String, entryId: String) {}

    /** The tab with [tabId] was tapped. */
    public fun tabSelected(tabId: String)

    /** The top-bar action with [actionId] was tapped. */
    public fun actionTapped(actionId: String)

    /** A native sheet dismissal (swipe / tap-outside) happened — clear your sheet state. Must be idempotent. */
    public fun dismissSheet()
}
