package com.ukkera.brandkit.components.feedback

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
    val status: BrandFeedbackStatus
    val duration: BrandFeedbackDuration
    val onDismiss: (() -> Unit)?
}

internal data class ToastRecord(
    override val id: Long,
    override val status: BrandFeedbackStatus,
    override val duration: BrandFeedbackDuration,
    override val onDismiss: (() -> Unit)?,
    val message: String,
    val position: BrandFeedbackPosition,
    val useSystemToast: Boolean,
) : TransientRecord

internal data class SnackbarRecord(
    override val id: Long,
    override val status: BrandFeedbackStatus,
    override val duration: BrandFeedbackDuration,
    override val onDismiss: (() -> Unit)?,
    val message: String,
    val actionLabel: String?,
    val onAction: (() -> Unit)?,
    val swipeToDismiss: Boolean,
) : TransientRecord

internal data class BannerRecord(
    override val id: Long,
    override val status: BrandFeedbackStatus,
    override val duration: BrandFeedbackDuration,
    override val onDismiss: (() -> Unit)?,
    val message: String,
    val title: String?,
    val position: BrandFeedbackPosition,
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
    val actions: List<BrandAlertAction>,
    val iosPresentation: BrandPresentation,
) : ModalRecord

internal data class SheetRecord(
    override val id: Long,
    override val title: String?,
    override val message: String?,
    override val onCancel: (() -> Unit)?,
    val actions: List<BrandSheetAction>,
    val iosPresentation: BrandPresentation,
) : ModalRecord

/** Auto-dismiss delay in ms, or null for [BrandFeedbackDuration.Indefinite] (host shows no timer). */
internal fun BrandFeedbackDuration.toMillisOrNull(): Long? = when (this) {
    BrandFeedbackDuration.Short -> 2_000L
    BrandFeedbackDuration.Long -> 3_500L
    BrandFeedbackDuration.Indefinite -> null
}

// endregion

/**
 * Imperative entry point for posting feedback. Held above the UI (created by
 * [rememberBrandFeedbackController] and exposed via [LocalBrandFeedbackController] by
 * [BrandFeedbackHost]). Methods are **plain functions** — callable from any click lambda — and return a
 * `Long` id you can pass to [dismiss].
 *
 * It is a synchronous **queue state machine** and nothing more: it owns no coroutines, no timers, and no
 * Material state. The visible timing of each surface is owned by the platform host (Android
 * `Snackbar`/`delay`, iOS `NSTimer`), which reports back through the `internal on…` callbacks — so a
 * message is never timed twice. Two independent lanes:
 * - **Transient** (toast/snackbar/banner): one [activeTransient] at a time, FIFO [BrandQueueBehavior].
 * - **Modal** (alert/sheet): one [activeModal] at a time, overlays a transient.
 */
@Stable
public class BrandFeedbackController internal constructor() {

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
        status: BrandFeedbackStatus = BrandFeedbackStatus.Info,
        duration: BrandFeedbackDuration = BrandFeedbackDuration.Short,
        position: BrandFeedbackPosition = BrandFeedbackPosition.Bottom,
        queue: BrandQueueBehavior = BrandQueueBehavior.Enqueue,
        onDismiss: (() -> Unit)? = null,
        android: BrandToastAndroidOptions = BrandToastAndroidOptions(),
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
        status: BrandFeedbackStatus = BrandFeedbackStatus.Info,
        actionLabel: String? = null,
        onAction: (() -> Unit)? = null,
        duration: BrandFeedbackDuration =
            if (actionLabel == null) BrandFeedbackDuration.Short else BrandFeedbackDuration.Indefinite,
        queue: BrandQueueBehavior = BrandQueueBehavior.Enqueue,
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
        status: BrandFeedbackStatus = BrandFeedbackStatus.Info,
        position: BrandFeedbackPosition = BrandFeedbackPosition.Top,
        actionLabel: String? = null,
        onAction: (() -> Unit)? = null,
        dismissible: Boolean = true,
        duration: BrandFeedbackDuration = BrandFeedbackDuration.Indefinite,
        queue: BrandQueueBehavior = BrandQueueBehavior.Enqueue,
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
        actions: List<BrandAlertAction>,
        onCancel: (() -> Unit)? = null,
        ios: BrandAlertIosOptions = BrandAlertIosOptions(),
    ): Long {
        val id = newId()
        enqueueModal(AlertRecord(id, title, message, onCancel, actions, ios.presentation))
        return id
    }

    /** A blocking action sheet for choosing among actions. Native action sheet on iOS by default; `ModalBottomSheet` on Android. */
    public fun confirmationSheet(
        title: String? = null,
        message: String? = null,
        actions: List<BrandSheetAction>,
        onCancel: (() -> Unit)? = null,
        ios: BrandConfirmationSheetIosOptions = BrandConfirmationSheetIosOptions(),
    ): Long {
        val id = newId()
        enqueueModal(SheetRecord(id, title, message, onCancel, actions, ios.presentation))
        return id
    }

    /** Dismiss a specific message by the id returned from a post call (works for either lane). */
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
        cur.onCancel?.invoke()
        activeModal = modalQueue.removeFirstOrNull()
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

    /** The action button of the active transient ([id]) was tapped: run [SnackbarRecord.onAction]/[BannerRecord.onAction], then advance. */
    internal fun onTransientAction(id: Long) {
        val cur = activeTransient ?: return
        if (cur.id != id) return
        when (cur) {
            is SnackbarRecord -> cur.onAction?.invoke()
            is BannerRecord -> cur.onAction?.invoke()
            is ToastRecord -> {}
        }
        activeTransient = null
        promoteTransient()
    }

    /** An action of the active modal ([id]) was chosen: run [action], then advance the modal lane. */
    internal fun onModalResult(id: Long, action: (() -> Unit)?) {
        if (activeModal?.id != id) return
        action?.invoke()
        activeModal = modalQueue.removeFirstOrNull()
    }

    // endregion

    // region queue mechanics

    private fun enqueueTransient(record: TransientRecord, behavior: BrandQueueBehavior) {
        when (behavior) {
            BrandQueueBehavior.DropIfShowing ->
                if (activeTransient != null || transientQueue.isNotEmpty()) return
            BrandQueueBehavior.ReplaceCurrent -> {
                transientQueue.clear()
                val cur = activeTransient
                if (cur != null) {
                    cur.onDismiss?.invoke()
                    activeTransient = null
                }
            }
            BrandQueueBehavior.Enqueue -> {}
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
        if (invokeDismiss) cur.onDismiss?.invoke()
        activeTransient = null
        promoteTransient()
    }

    private fun enqueueModal(record: ModalRecord) {
        modalQueue.addLast(record)
        if (activeModal == null) activeModal = modalQueue.removeFirstOrNull()
    }

    // endregion
}

/** Remembers a [BrandFeedbackController] across recompositions. Pass it to [BrandFeedbackHost]. */
@Composable
public fun rememberBrandFeedbackController(): BrandFeedbackController = remember { BrandFeedbackController() }

/** The ambient controller, provided by [BrandFeedbackHost]. Access with `LocalBrandFeedbackController.current`. */
public val LocalBrandFeedbackController: ProvidableCompositionLocal<BrandFeedbackController> = staticCompositionLocalOf<BrandFeedbackController> {
    error("No BrandFeedbackController — wrap your content in BrandFeedbackHost { … }")
}
