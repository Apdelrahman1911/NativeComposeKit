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
 * How long a transient stays before auto-dismissing. Each surface maps it to its platform-idiomatic timing
 * (the lane always advances in step with what is actually shown):
 * - **Toast HUD / banner** (kit-drawn, both platforms): [Short] ≈ 2 s, [Long] ≈ 3.5 s; [Indefinite] = no
 *   timer — stays until dismissed by the user (close button / action) or by code
 *   ([NativeFeedbackController.dismiss]/[NativeFeedbackController.dismissCurrent]). Banners default to it.
 * - **Android system toast** (`android.useSystemToast`): the OS offers exactly two lengths — [Short] → SHORT
 *   (~2 s), [Long] → LONG (~3.5 s), and [Indefinite] **degrades to LONG** (the OS can't hold a toast).
 * - **Android snackbar**: Material timings — [Short] → ~4 s, [Long] → ~10 s, [Indefinite] → held (Material
 *   `SnackbarDuration`s; a snackbar with an action defaults to [Indefinite]).
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
 * Compared by identity (it holds a lambda); not a `data class` so fields stay addable binary-compatibly.
 */
@Immutable
public class NativeAlertAction(
    public val label: String,
    public val onClick: () -> Unit = {},
    public val role: NativeAlertActionRole = NativeAlertActionRole.Default,
)

/**
 * One row in a [NativeFeedbackController.confirmationSheet]. [icon] shows a leading glyph on Android
 * ([NativeIcon.androidImageVector]) and in the iOS **Branded** presentation ([NativeIcon.sfSymbolName]);
 * the iOS *Native* presentation (`UIAlertController`) has no public action-image API and drops it.
 * Named for the *confirmation sheet* (not the presentational
 * [io.github.apdelrahman1911.nativecomposekit.components.NativeSheet]), like [NativeConfirmationSheetIosOptions].
 */
@Immutable
public class NativeConfirmationAction(
    public val label: String,
    public val onClick: () -> Unit = {},
    public val role: NativeAlertActionRole = NativeAlertActionRole.Default,
    public val icon: NativeIcon? = null,
)

/** Android-only knobs for [NativeFeedbackController.toast]. Compares by value. */
@Immutable
public class NativeToastAndroidOptions(
    /**
     * Use the real `android.widget.Toast` instead of the brand-themed Compose HUD. True system toast
     * (renders even outside the app) but unstyleable on Android 12+ and ignores the brand theme/dark mode.
     */
    public val useSystemToast: Boolean = false,
) {
    override fun equals(other: Any?): Boolean =
        this === other || (other is NativeToastAndroidOptions && useSystemToast == other.useSystemToast)

    override fun hashCode(): Int = useSystemToast.hashCode()
}

/** iOS-only knobs for [NativeFeedbackController.alert]. Compares by value. */
@Immutable
public class NativeAlertIosOptions(
    /** [NativePresentation.Native] = real `UIAlertController`; [NativePresentation.Branded] = themed overlay. */
    public val presentation: NativePresentation = NativePresentation.Native,
) {
    override fun equals(other: Any?): Boolean =
        this === other || (other is NativeAlertIosOptions && presentation == other.presentation)

    override fun hashCode(): Int = presentation.hashCode()
}

/**
 * iOS-only knobs for [NativeFeedbackController.confirmationSheet]. Named for the *confirmation sheet* (not the
 * presentational [io.github.apdelrahman1911.nativecomposekit.components.NativeSheet]) so the kit can add a `NativeSheetIosOptions` for
 * that component without a name clash. Compares by value.
 */
@Immutable
public class NativeConfirmationSheetIosOptions(
    /** [NativePresentation.Native] = real action-sheet `UIAlertController`; [NativePresentation.Branded] = themed overlay. */
    public val presentation: NativePresentation = NativePresentation.Native,
) {
    override fun equals(other: Any?): Boolean =
        this === other || (other is NativeConfirmationSheetIosOptions && presentation == other.presentation)

    override fun hashCode(): Int = presentation.hashCode()
}
