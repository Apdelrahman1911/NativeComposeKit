package io.github.apdelrahman1911.nativecomposekit.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * The user-facing strings the kit renders **on its own** — default labels, accessibility descriptions, and
 * the button titles of kit-provided affordances (retry buttons, alert fallbacks, clear/dismiss icons…).
 * Defaults are English. Localize app-wide by providing a translated instance — via [NativeKitTheme]'s `strings`
 * parameter, or directly:
 *
 * ```
 * CompositionLocalProvider(LocalNativeStrings provides NativeStrings(retry = "…", dismiss = "…")) { … }
 * ```
 *
 * Text you pass to a component yourself (titles, labels, placeholders, action labels) is never touched by
 * this table — it only covers what the kit would otherwise hardcode. Screen-reader-only strings matter as
 * much as the visible ones: they are what TalkBack/VoiceOver announce.
 *
 * Create your translated instance **once** (a top-level `val`, or `remember { NativeStrings(…) }`) and pass
 * that same instance — the table is compared by identity, so a fresh instance per recomposition would
 * re-invalidate everything under the static local. New entries are only ever **appended** with defaults, so
 * positional and named construction stay source-compatible across releases.
 */
@Immutable
public class NativeStrings(
    /** Label of kit-rendered retry buttons (the content-state error action, the pagination error footer). */
    public val retry: String = "Retry",
    /** Accessibility description of the content-state loading spinner. */
    public val loading: String = "Loading",
    /** Default title of the content-state empty placeholder. */
    public val emptyStateTitle: String = "Nothing here yet",
    /** Default title of the content-state error placeholder. */
    public val errorStateTitle: String = "Something went wrong",
    /** Accessibility action label of a chip's trailing remove affordance. */
    public val chipRemove: String = "Remove",
    /** Default placeholder of [io.github.apdelrahman1911.nativecomposekit.components.NativeSearchBar]. */
    public val searchPlaceholder: String = "Search",
    /** Accessibility label of the search bar's clear affordance (Android trailing icon). */
    public val searchClear: String = "Clear",
    /** "Done" — the iOS keyboard accessory button (when not overridden per-field) and picker dialogs. */
    public val done: String = "Done",
    /** Accessibility/action label of dismiss affordances (inline status, banners). */
    public val dismiss: String = "Dismiss",
    /** Title of the fallback confirm action injected when an alert declares no actions. */
    public val alertOk: String = "OK",
    /** Title of the cancel action injected when a confirmation sheet declares none. */
    public val alertCancel: String = "Cancel",
    /** Accessibility label of the checkmark on a selected menu item. */
    public val menuSelected: String = "Selected",
    /** Accessibility label of a split button's menu (chevron) segment. */
    public val splitButtonMore: String = "More",
    /** Accessibility label of a stepper's increment (+) control. */
    public val stepperIncrement: String = "Increment",
    /** Accessibility label of a stepper's decrement (−) control. */
    public val stepperDecrement: String = "Decrement",
    /** Title of the color-picker dialog (Android's NativeColorWell fallback picker). */
    public val colorPickerTitle: String = "Pick a color",
    /** Default accessibility description of the color-well swatch. */
    public val colorWellDescription: String = "Selected color",
    /** Accessibility description of an OTP field, given its digit count. */
    public val otpDescription: (length: Int) -> String = { length -> "Enter the $length-digit code" },
    /** Accessibility description of a rating display, given the formatted rating and the maximum. */
    public val ratingDescription: (rating: String, max: Int) -> String = { rating, max -> "Rating: $rating out of $max" },
    /** Accessibility action label of an individual rating star. */
    public val ratingSetDescription: (star: Int, max: Int) -> String = { star, max -> "Rate $star of $max" },
    /** Accessibility description of a page control, given the 1-based page and the page count. */
    public val pageDescription: (page: Int, pageCount: Int) -> String = { page, count -> "Page $page of $count" },
    /** Accessibility action label of a page-control dot, given the 1-based page it jumps to. */
    public val goToPage: (page: Int) -> String = { page -> "Go to page $page" },
    /** Accessibility label of a top bar's navigation (back) control when the caller provides none. */
    public val back: String = "Back",
    /** Accessibility pane title announced when a [io.github.apdelrahman1911.nativecomposekit.components.NativeDialog] with no plain-text title opens. */
    public val dialogPaneTitle: String = "Dialog",
    /** Accessibility action label that triggers a pull-to-refresh without the gesture. */
    public val refresh: String = "Refresh",
    /** Announced while a pull-to-refresh is in flight. */
    public val refreshing: String = "Refreshing",
    /** Spoken name of the Success feedback status (prefixed to screen-reader announcements). */
    public val statusSuccess: String = "Success",
    /** Spoken name of the Warning feedback status. */
    public val statusWarning: String = "Warning",
    /** Spoken name of the Info feedback status. */
    public val statusInfo: String = "Info",
    /** Spoken name of the Error feedback status. */
    public val statusError: String = "Error",
    /** Accessibility description of a preset color swatch, given its 1-based index and the swatch count. */
    public val colorSwatchDescription: (index: Int, count: Int) -> String = { index, count -> "Color $index of $count" },
)

/**
 * The default (English) strings table — a single shared instance so the default parameter of
 * [NativeKitTheme]/`NativeAppearanceScope` keeps one identity across recompositions (the table holds
 * lambdas, so identity is its only meaningful equality).
 */
internal val DefaultNativeStrings: NativeStrings = NativeStrings()

/**
 * The active [NativeStrings] table. Static: a language change replaces the whole table at the root, which
 * is exactly a recompose-everything event. Provided with English defaults; [NativeKitTheme] re-provides it from
 * its `strings` parameter.
 */
public val LocalNativeStrings: ProvidableCompositionLocal<NativeStrings> =
    staticCompositionLocalOf { DefaultNativeStrings }
