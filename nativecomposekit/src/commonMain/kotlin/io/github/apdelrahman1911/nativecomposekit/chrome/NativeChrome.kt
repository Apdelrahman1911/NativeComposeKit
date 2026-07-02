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
 * An immutable projection the native iOS chrome (a `UINavigationBar` + `UITabBar`) renders. It is pure display
 * data — it carries **no** route stack and is never authoritative. [sheetId] only tells the shell whether a sheet
 * should be presented; the sheet's Compose content comes from the iOS `NativeChromeSource.sheetViewController`.
 * The shell treats one sheet at a time: to switch sheets, emit `sheetId = null` first, then the new id.
 *
 * Adapters construct this on every navigation change, so it is deliberately **not** a `data class`: fields
 * ([backTitle] was one) can then be appended without breaking compiled adapters. Compares by value so
 * shells can skip no-op emissions. `observe` callbacks may deliver on any thread — marshal to the main
 * thread before touching UIKit.
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
            backTitle == other.backTitle
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + canGoBack.hashCode()
        result = 31 * result + selectedTabId.hashCode()
        result = 31 * result + tabs.hashCode()
        result = 31 * result + actions.hashCode()
        result = 31 * result + (sheetId?.hashCode() ?: 0)
        result = 31 * result + (backTitle?.hashCode() ?: 0)
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
 * An implementation must be a **dumb, one-way projection**: it may read your navigation state and turn bar taps
 * into your navigation's own intents, but it must never expose, mutate, or mirror a navigation stack — your
 * navigation system stays the single source of truth. On iOS the chrome shell consumes the fuller
 * `NativeChromeSource` (this, plus the sheet's Compose content); a consumer typically implements this base in
 * shared code and supplies the one iOS-specific piece there. The kit itself owns no navigation.
 */
public interface NativeChromeStateSource {
    /** The current chrome to display, computed fresh from your live navigation state. */
    public fun currentState(): NativeChromeState

    /** Subscribe to chrome changes; must fire once immediately with the current state, then after every change. */
    public fun observe(onChange: (NativeChromeState) -> Unit): NativeChromeCancellable

    /** The back affordance was tapped — perform your navigation's "back". */
    public fun backRequested()

    /** The tab with [tabId] was tapped. */
    public fun tabSelected(tabId: String)

    /** The top-bar action with [actionId] was tapped. */
    public fun actionTapped(actionId: String)

    /** A native sheet dismissal (swipe / tap-outside) happened — clear your sheet state. Must be idempotent. */
    public fun dismissSheet()
}
