package io.github.apdelrahman1911.nativecomposekit.components.feedback

import androidx.compose.runtime.Immutable
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeIcon

/**
 * Semantic status of a feedback message — drives its color (and the default leading icon). [Error]
 * reuses `MaterialTheme.colorScheme.error`; the others come from
 * [io.github.apdelrahman1911.nativecomposekit.theme.NativeStatusColors].
 */
public enum class NativeFeedbackStatus { Info, Success, Warning, Error }

/** Where a transient (toast/banner) appears on screen. Snackbar is always bottom (platform convention). */
public enum class NativeFeedbackPosition { Top, Bottom }

/**
 * How long a transient stays before auto-dismissing.
 * - [Short] ≈ 2 s, [Long] ≈ 3.5 s.
 * - [Indefinite]: no timer — stays until dismissed by the user (close button / action) or by code
 *   ([NativeFeedbackController.dismiss]/[NativeFeedbackController.dismissCurrent]). Banners default to this.
 */
public enum class NativeFeedbackDuration { Short, Long, Indefinite }

/**
 * What happens when a transient is posted while one is already showing (the transient lane is FIFO,
 * one-at-a-time).
 * - [Enqueue] (default): wait in line and play in turn.
 * - [ReplaceCurrent]: clear the queue and replace whatever is showing now.
 * - [DropIfShowing]: ignore this message if anything is showing or queued (de-dupe rapid fires).
 */
public enum class NativeQueueBehavior { Enqueue, ReplaceCurrent, DropIfShowing }

/**
 * Role of an alert/sheet action.
 * - [Default]: normal action.
 * - [Cancel]: the dismissive choice — iOS lays it out specially (`UIAlertActionStyleCancel`); on
 *   Android it maps to the dialog's `dismissButton`. At most one per alert.
 * - [Destructive]: irreversible action — rendered red (iOS `UIAlertActionStyleDestructive` / Material error).
 *
 * Separate from `NativeMenuItemRole` because menus have no Cancel concept.
 */
public enum class NativeAlertActionRole { Default, Cancel, Destructive }

/** iOS presentation strategy for alert/sheet: the real system control, or a brand-themed custom overlay. */
public enum class NativePresentation { Native, Branded }

/**
 * One button in a [NativeFeedbackController.alert]. [onClick] runs after the alert dismisses.
 */
@Immutable
public data class NativeAlertAction(
    val label: String,
    val onClick: () -> Unit = {},
    val role: NativeAlertActionRole = NativeAlertActionRole.Default,
)

/**
 * One row in a [NativeFeedbackController.confirmationSheet]. [icon] shows a leading glyph (Android uses
 * [NativeIcon.androidImageVector]; iOS uses [NativeIcon.sfSymbolName]).
 */
@Immutable
public data class NativeSheetAction(
    val label: String,
    val onClick: () -> Unit = {},
    val role: NativeAlertActionRole = NativeAlertActionRole.Default,
    val icon: NativeIcon? = null,
)

/** Android-only knobs for [NativeFeedbackController.toast]. */
@Immutable
public data class NativeToastAndroidOptions(
    /**
     * Use the real `android.widget.Toast` instead of the brand-themed Compose HUD. True system toast
     * (renders even outside the app) but unstyleable on Android 12+ and ignores the brand theme/dark mode.
     */
    val useSystemToast: Boolean = false,
)

/** iOS-only knobs for [NativeFeedbackController.alert]. */
@Immutable
public data class NativeAlertIosOptions(
    /** [NativePresentation.Native] = real `UIAlertController`; [NativePresentation.Branded] = themed overlay. */
    val presentation: NativePresentation = NativePresentation.Native,
)

/**
 * iOS-only knobs for [NativeFeedbackController.confirmationSheet]. Named for the *confirmation sheet* (not the
 * presentational [io.github.apdelrahman1911.nativecomposekit.components.NativeSheet]) so the kit can add a `NativeSheetIosOptions` for
 * that component without a name clash.
 */
@Immutable
public data class NativeConfirmationSheetIosOptions(
    /** [NativePresentation.Native] = real action-sheet `UIAlertController`; [NativePresentation.Branded] = themed overlay. */
    val presentation: NativePresentation = NativePresentation.Native,
)
