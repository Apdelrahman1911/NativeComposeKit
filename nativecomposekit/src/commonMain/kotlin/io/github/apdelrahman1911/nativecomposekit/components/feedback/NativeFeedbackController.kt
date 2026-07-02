package io.github.apdelrahman1911.nativecomposekit.components.feedback

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf

// region records (internal — the platform hosts pattern-match on these)

/** A non-blocking message in the FIFO transient lane (toast / snackbar / banner). */
internal sealed interface TransientRecord {
    val id: Long
    val status: NativeFeedbackStatus
    val duration: NativeFeedbackDuration
    val onDismiss: (() -> Unit)?
}

internal data class ToastRecord(
    override val id: Long,
    override val status: NativeFeedbackStatus,
    override val duration: NativeFeedbackDuration,
    override val onDismiss: (() -> Unit)?,
    val message: String,
    val position: NativeFeedbackPosition,
    val useSystemToast: Boolean,
) : TransientRecord

internal data class SnackbarRecord(
    override val id: Long,
    override val status: NativeFeedbackStatus,
    override val duration: NativeFeedbackDuration,
    override val onDismiss: (() -> Unit)?,
    val message: String,
    val actionLabel: String?,
    val onAction: (() -> Unit)?,
    val swipeToDismiss: Boolean,
) : TransientRecord

internal data class BannerRecord(
    override val id: Long,
    override val status: NativeFeedbackStatus,
    override val duration: NativeFeedbackDuration,
    override val onDismiss: (() -> Unit)?,
    val message: String,
    val title: String?,
    val position: NativeFeedbackPosition,
    val actionLabel: String?,
    val onAction: (() -> Unit)?,
    val dismissible: Boolean,
    val swipeToDismiss: Boolean,
) : TransientRecord

/** A blocking message in the modal lane (alert / confirmation sheet), runs parallel to the transient lane. */
internal sealed interface ModalRecord {
    val id: Long
    val title: String?
    val message: String?
    /** Invoked when the modal is dismissed WITHOUT choosing an action (scrim/back/cancel gesture). */
    val onCancel: (() -> Unit)?
}

internal data class AlertRecord(
    override val id: Long,
    override val title: String?,
    override val message: String?,
    override val onCancel: (() -> Unit)?,
    val actions: List<NativeAlertAction>,
    val iosPresentation: NativePresentation,
) : ModalRecord

internal data class SheetRecord(
    override val id: Long,
    override val title: String?,
    override val message: String?,
    override val onCancel: (() -> Unit)?,
    val actions: List<NativeConfirmationAction>,
    val iosPresentation: NativePresentation,
) : ModalRecord

/** Auto-dismiss delay in ms, or null for [NativeFeedbackDuration.Indefinite] (host shows no timer). */
internal fun NativeFeedbackDuration.toMillisOrNull(): Long? = when (this) {
    NativeFeedbackDuration.Short -> 2_000L
    NativeFeedbackDuration.Long -> 3_500L
    NativeFeedbackDuration.Indefinite -> null
}

// endregion

/**
 * Imperative entry point for posting feedback. Held above the UI (created by
 * [rememberNativeFeedbackController] and exposed via [LocalNativeFeedbackController] by
 * [NativeFeedbackHost]). Methods are **plain functions** — callable from any click lambda — and return a
 * `Long` id you can pass to [dismiss].
 *
 * It is a synchronous **queue state machine** and nothing more: it owns no coroutines, no timers, and no
 * Material state. The visible timing of each surface is owned by the platform host (Android
 * `Snackbar`/`delay`, iOS `NSTimer`), which reports back through the `internal on…` callbacks — so a
 * message is never timed twice. Two independent lanes:
 * - **Transient** (toast/snackbar/banner): one [activeTransient] at a time, FIFO [NativeQueueBehavior].
 * - **Modal** (alert/sheet): one [activeModal] at a time, overlays a transient.
 *
 * **Threading: main thread only.** The queues are plain unsynchronized state read by composition; call the
 * post/dismiss methods from UI callbacks (click lambdas, effects) — from a background coroutine, hop first:
 * `withContext(Dispatchers.Main) { controller.toast(…) }`.
 *
 * **Re-entrancy is safe:** every user callback (`onDismiss`/`onAction`/`onCancel`/alert actions) runs
 * *after* the lane has advanced, so a callback may post or dismiss again without corrupting the queue.
 */
@Stable
public class NativeFeedbackController internal constructor() {

    private var idCounter = 1L
    private fun newId(): Long = idCounter++

    // Transient lane
    private val transientQueue = ArrayDeque<TransientRecord>()
    internal var activeTransient by mutableStateOf<TransientRecord?>(null)
        private set

    // Modal lane (parallel)
    private val modalQueue = ArrayDeque<ModalRecord>()
    internal var activeModal by mutableStateOf<ModalRecord?>(null)
        private set

    // region public API

    /** A small transient HUD message ("Copied", "Saved"). Lightweight, no action. */
    public fun toast(
        message: String,
        status: NativeFeedbackStatus = NativeFeedbackStatus.Info,
        duration: NativeFeedbackDuration = NativeFeedbackDuration.Short,
        position: NativeFeedbackPosition = NativeFeedbackPosition.Bottom,
        queue: NativeQueueBehavior = NativeQueueBehavior.Enqueue,
        onDismiss: (() -> Unit)? = null,
        android: NativeToastAndroidOptions = NativeToastAndroidOptions(),
    ): Long {
        val id = newId()
        enqueueTransient(
            ToastRecord(id, status, duration, onDismiss, message, position, android.useSystemToast),
            queue,
        )
        return id
    }

    /**
     * A bottom message with an optional action (e.g. "Item deleted — Undo"). Material Snackbar on Android.
     * [swipeToDismiss] (default true) lets the user swipe it away (a plain dismiss — no action/Undo).
     */
    public fun snackbar(
        message: String,
        status: NativeFeedbackStatus = NativeFeedbackStatus.Info,
        actionLabel: String? = null,
        onAction: (() -> Unit)? = null,
        duration: NativeFeedbackDuration =
            if (actionLabel == null) NativeFeedbackDuration.Short else NativeFeedbackDuration.Indefinite,
        queue: NativeQueueBehavior = NativeQueueBehavior.Enqueue,
        onDismiss: (() -> Unit)? = null,
        swipeToDismiss: Boolean = true,
    ): Long {
        val id = newId()
        enqueueTransient(
            SnackbarRecord(id, status, duration, onDismiss, message, actionLabel, onAction, swipeToDismiss),
            queue,
        )
        return id
    }

    /**
     * A prominent non-blocking message pinned top/bottom, with optional title, action and close button.
     * [swipeToDismiss] (default true) lets the user swipe it toward its edge — independent of [dismissible]
     * (the close button). A truly persistent banner sets both to false (only code can then dismiss it).
     */
    public fun banner(
        message: String,
        title: String? = null,
        status: NativeFeedbackStatus = NativeFeedbackStatus.Info,
        position: NativeFeedbackPosition = NativeFeedbackPosition.Top,
        actionLabel: String? = null,
        onAction: (() -> Unit)? = null,
        dismissible: Boolean = true,
        duration: NativeFeedbackDuration = NativeFeedbackDuration.Indefinite,
        queue: NativeQueueBehavior = NativeQueueBehavior.Enqueue,
        onDismiss: (() -> Unit)? = null,
        swipeToDismiss: Boolean = true,
    ): Long {
        val id = newId()
        enqueueTransient(
            BannerRecord(
                id, status, duration, onDismiss, message, title, position, actionLabel, onAction,
                dismissible, swipeToDismiss,
            ),
            queue,
        )
        return id
    }

    /** A blocking alert requiring a choice. Native `UIAlertController` on iOS by default; Material `AlertDialog` on Android. */
    public fun alert(
        title: String? = null,
        message: String? = null,
        actions: List<NativeAlertAction>,
        onCancel: (() -> Unit)? = null,
        ios: NativeAlertIosOptions = NativeAlertIosOptions(),
    ): Long {
        val id = newId()
        enqueueModal(AlertRecord(id, title, message, onCancel, actions, ios.presentation))
        return id
    }

    /** A blocking action sheet for choosing among actions. Native action sheet on iOS by default; `ModalBottomSheet` on Android. */
    public fun confirmationSheet(
        title: String? = null,
        message: String? = null,
        actions: List<NativeConfirmationAction>,
        onCancel: (() -> Unit)? = null,
        ios: NativeConfirmationSheetIosOptions = NativeConfirmationSheetIosOptions(),
    ): Long {
        val id = newId()
        enqueueModal(SheetRecord(id, title, message, onCancel, actions, ios.presentation))
        return id
    }

    /**
     * Dismiss a specific message by the id returned from a post call (works for either lane). Exact behavior
     * depends on where the message is: the **active transient** → its `onDismiss` runs and the lane advances;
     * the **active modal** → treated as a cancel (its `onCancel` runs) and the lane advances; still **queued**
     * → removed silently with no callback.
     */
    public fun dismiss(id: Long) {
        when {
            activeTransient?.id == id -> finishTransient(id, invokeDismiss = true)
            activeModal?.id == id -> dismissCurrentModal()
            else -> {
                transientQueue.removeAll { it.id == id }
                modalQueue.removeAll { it.id == id }
            }
        }
    }

    /** Dismiss whatever transient is showing now (e.g. an indefinite banner) and advance the queue. */
    public fun dismissCurrent() {
        activeTransient?.let { finishTransient(it.id, invokeDismiss = true) }
    }

    /** Dismiss the current modal as a cancel (runs its `onCancel`) and advance the modal queue. */
    public fun dismissCurrentModal() {
        val cur = activeModal ?: return
        // Advance BEFORE the callback so a re-entrant dismiss/post inside onCancel sees a settled lane
        // (and the id guards correctly reject stale references to the just-finished modal).
        activeModal = modalQueue.removeFirstOrNull()
        cur.onCancel?.invoke()
    }

    /** Hard reset — clears both lanes immediately without invoking callbacks. */
    public fun clearAll() {
        transientQueue.clear()
        activeTransient = null
        modalQueue.clear()
        activeModal = null
    }

    // endregion

    // region host callbacks (internal — called by PlatformFeedbackHost; all guarded by id)

    /** The host's timer fired for [id]: dismiss it (counts as an [onDismiss]) and advance. */
    internal fun onTransientTimeout(id: Long) = finishTransient(id, invokeDismiss = true)

    /** The action button of the active transient ([id]) was tapped: advance, then run [SnackbarRecord.onAction]/[BannerRecord.onAction]. */
    internal fun onTransientAction(id: Long) {
        val cur = activeTransient ?: return
        if (cur.id != id) return
        // Advance BEFORE the callback: an action that re-entrantly calls dismiss(id) would otherwise pass the
        // id guard, fire a spurious onDismiss, and advance a second time (dropping a queued record).
        activeTransient = null
        promoteTransient()
        when (cur) {
            is SnackbarRecord -> cur.onAction?.invoke()
            is BannerRecord -> cur.onAction?.invoke()
            is ToastRecord -> {}
        }
    }

    /** An action of the active modal ([id]) was chosen: advance the modal lane, then run [action]. */
    internal fun onModalResult(id: Long, action: (() -> Unit)?) {
        if (activeModal?.id != id) return
        // Advance BEFORE the callback (same rationale as onTransientAction: no spurious onCancel, no
        // double-advance when the action calls dismiss()).
        activeModal = modalQueue.removeFirstOrNull()
        action?.invoke()
    }

    // endregion

    // region queue mechanics

    private fun enqueueTransient(record: TransientRecord, behavior: NativeQueueBehavior) {
        when (behavior) {
            NativeQueueBehavior.DropIfShowing ->
                if (activeTransient != null || transientQueue.isNotEmpty()) return
            NativeQueueBehavior.ReplaceCurrent -> {
                transientQueue.clear()
                val cur = activeTransient
                if (cur != null) {
                    // Detach BEFORE the callback — an onDismiss that itself posts ReplaceCurrent would
                    // otherwise re-enter this branch with the same record still active and recurse forever.
                    activeTransient = null
                    cur.onDismiss?.invoke()
                }
            }
            NativeQueueBehavior.Enqueue -> {}
        }
        transientQueue.addLast(record)
        if (activeTransient == null) promoteTransient()
    }

    private fun promoteTransient() {
        activeTransient = transientQueue.removeFirstOrNull()
    }

    private fun finishTransient(id: Long, invokeDismiss: Boolean) {
        val cur = activeTransient ?: return
        if (cur.id != id) return
        // Advance BEFORE the callback so onDismiss observes a settled lane: a re-entrant dismiss of the same
        // id no-ops (guard above), and a post from inside onDismiss queues/promotes normally.
        activeTransient = null
        promoteTransient()
        if (invokeDismiss) cur.onDismiss?.invoke()
    }

    private fun enqueueModal(record: ModalRecord) {
        modalQueue.addLast(record)
        if (activeModal == null) activeModal = modalQueue.removeFirstOrNull()
    }

    // endregion
}

/** Remembers a [NativeFeedbackController] across recompositions. Pass it to [NativeFeedbackHost]. */
@Composable
public fun rememberNativeFeedbackController(): NativeFeedbackController = remember { NativeFeedbackController() }

/** The ambient controller, provided by [NativeFeedbackHost]. Access with `LocalNativeFeedbackController.current`. */
public val LocalNativeFeedbackController: ProvidableCompositionLocal<NativeFeedbackController> = staticCompositionLocalOf<NativeFeedbackController> {
    error("No NativeFeedbackController — wrap your content in NativeFeedbackHost { … }")
}
