package io.github.apdelrahman1911.nativecomposekit.components.feedback

import androidx.compose.runtime.Immutable
import io.github.apdelrahman1911.nativecomposekit.components.model.BrandIcon

/**
 * Semantic status of a feedback message — drives its color (and the default leading icon). [Error]
 * reuses `MaterialTheme.colorScheme.error`; the others come from
 * [io.github.apdelrahman1911.nativecomposekit.theme.BrandStatusColors].
 */
public enum class BrandFeedbackStatus { Info, Success, Warning, Error }

/** Where a transient (toast/banner) appears on screen. Snackbar is always bottom (platform convention). */
public enum class BrandFeedbackPosition { Top, Bottom }

/**
 * How long a transient stays before auto-dismissing.
 * - [Short] ≈ 2 s, [Long] ≈ 3.5 s.
 * - [Indefinite]: no timer — stays until dismissed by the user (close button / action) or by code
 *   ([BrandFeedbackController.dismiss]/[BrandFeedbackController.dismissCurrent]). Banners default to this.
 */
public enum class BrandFeedbackDuration { Short, Long, Indefinite }

/**
 * What happens when a transient is posted while one is already showing (the transient lane is FIFO,
 * one-at-a-time).
 * - [Enqueue] (default): wait in line and play in turn.
 * - [ReplaceCurrent]: clear the queue and replace whatever is showing now.
 * - [DropIfShowing]: ignore this message if anything is showing or queued (de-dupe rapid fires).
 */
public enum class BrandQueueBehavior { Enqueue, ReplaceCurrent, DropIfShowing }

/**
 * Role of an alert/sheet action.
 * - [Default]: normal action.
 * - [Cancel]: the dismissive choice — iOS lays it out specially (`UIAlertActionStyleCancel`); on
 *   Android it maps to the dialog's `dismissButton`. At most one per alert.
 * - [Destructive]: irreversible action — rendered red (iOS `UIAlertActionStyleDestructive` / Material error).
 *
 * Separate from `BrandMenuItemRole` because menus have no Cancel concept.
 */
public enum class BrandAlertActionRole { Default, Cancel, Destructive }

/** iOS presentation strategy for alert/sheet: the real system control, or a brand-themed custom overlay. */
public enum class BrandPresentation { Native, Branded }

/**
 * One button in a [BrandFeedbackController.alert]. [onClick] runs after the alert dismisses.
 */
@Immutable
public data class BrandAlertAction(
    val label: String,
    val onClick: () -> Unit = {},
    val role: BrandAlertActionRole = BrandAlertActionRole.Default,
)

/**
 * One row in a [BrandFeedbackController.confirmationSheet]. [icon] shows a leading glyph (Android uses
 * [BrandIcon.androidImageVector]; iOS uses [BrandIcon.sfSymbolName]).
 */
@Immutable
public data class BrandSheetAction(
    val label: String,
    val onClick: () -> Unit = {},
    val role: BrandAlertActionRole = BrandAlertActionRole.Default,
    val icon: BrandIcon? = null,
)

/** Android-only knobs for [BrandFeedbackController.toast]. */
@Immutable
public data class BrandToastAndroidOptions(
    /**
     * Use the real `android.widget.Toast` instead of the brand-themed Compose HUD. True system toast
     * (renders even outside the app) but unstyleable on Android 12+ and ignores the brand theme/dark mode.
     */
    val useSystemToast: Boolean = false,
)

/** iOS-only knobs for [BrandFeedbackController.alert]. */
@Immutable
public data class BrandAlertIosOptions(
    /** [BrandPresentation.Native] = real `UIAlertController`; [BrandPresentation.Branded] = themed overlay. */
    val presentation: BrandPresentation = BrandPresentation.Native,
)

/**
 * iOS-only knobs for [BrandFeedbackController.confirmationSheet]. Named for the *confirmation sheet* (not the
 * presentational [io.github.apdelrahman1911.nativecomposekit.components.BrandSheet]) so the kit can add a `BrandSheetIosOptions` for
 * that component without a name clash.
 */
@Immutable
public data class BrandConfirmationSheetIosOptions(
    /** [BrandPresentation.Native] = real action-sheet `UIAlertController`; [BrandPresentation.Branded] = themed overlay. */
    val presentation: BrandPresentation = BrandPresentation.Native,
)
