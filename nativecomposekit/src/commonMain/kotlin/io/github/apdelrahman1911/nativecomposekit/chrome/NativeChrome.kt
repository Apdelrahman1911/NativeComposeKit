package io.github.apdelrahman1911.nativecomposekit.chrome

import androidx.compose.runtime.Immutable

/** A tab as the native `UITabBar` should render it. */
@Immutable
public data class NativeChromeTab(val id: String, val title: String, val sfSymbol: String)

/** A top-bar action (e.g. a "+"), rendered as a native `UIBarButtonItem`. */
@Immutable
public data class NativeChromeAction(val id: String, val sfSymbol: String)

/**
 * An immutable projection the native iOS chrome (a `UINavigationBar` + `UITabBar`) renders. It is pure display
 * data — it carries **no** route stack and is never authoritative. [sheetId] only tells the shell whether a sheet
 * should be presented; the sheet's Compose content comes from the iOS `NativeChromeSource.sheetViewController`.
 */
@Immutable
public data class NativeChromeState(
    val title: String,
    val canGoBack: Boolean,
    val selectedTabId: String,
    val tabs: List<NativeChromeTab>,
    val actions: List<NativeChromeAction>,
    val sheetId: String?,
    /** The previous destination's title — shown as the native back-button label. Null at a tab root. */
    val backTitle: String? = null,
)

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
