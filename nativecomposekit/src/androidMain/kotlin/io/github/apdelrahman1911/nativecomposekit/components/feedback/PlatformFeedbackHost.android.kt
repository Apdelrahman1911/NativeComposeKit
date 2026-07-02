package io.github.apdelrahman1911.nativecomposekit.components.feedback

import android.widget.Toast
import androidx.compose.animation.core.animate
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import io.github.apdelrahman1911.nativecomposekit.theme.LocalNativeStrings
import io.github.apdelrahman1911.nativecomposekit.theme.NativeTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Android feedback host: draws Material + brand-themed overlays over [content], driven by the
 * controller's snapshot state. The transient lane shows ONE of toast-HUD / Material snackbar / banner
 * at a time; the modal lane (alert dialog / bottom sheet) overlays it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal actual fun PlatformFeedbackHost(
    controller: NativeFeedbackController,
    content: @Composable () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    // The snackbar record Material is currently showing. Bound by the driver below (not read live from
    // activeTransient) so an outgoing snackbar keeps its own colors/swipe through its exit animation,
    // even after the controller has advanced to the next transient.
    var shownSnack by remember { mutableStateOf<SnackbarRecord?>(null) }

    Box(Modifier.fillMaxSize()) {
        content()

        // ---- Transient lane (one at a time) ----
        when (val t = controller.activeTransient) {
            is ToastRecord -> ToastHud(t, controller, onSystemToast = { msg ->
                // The system Toast has exactly two lengths; map Short → SHORT and everything else → LONG.
                val length = if (t.duration == NativeFeedbackDuration.Short) Toast.LENGTH_SHORT else Toast.LENGTH_LONG
                Toast.makeText(context, msg, length).show()
            })
            is BannerRecord -> FeedbackBanner(t, controller)
            is SnackbarRecord, null -> Unit // snackbar is rendered by the SnackbarHost below
        }

        // Material snackbar — host always present; a record drives it via the LaunchedEffect below.
        // Style + swipe come from the shown record (bound by the driver), so they stay stable for the
        // outgoing snackbar's exit animation instead of flickering to the next transient's style.
        val snackStyle = resolveFeedbackStyle(shownSnack?.status ?: NativeFeedbackStatus.Info, filled = true)
        val snackSwipe = shownSnack?.swipeToDismiss ?: false
        val snackKey = shownSnack?.id
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding(),
        ) { data ->
            val bar = @Composable {
                Snackbar(
                    snackbarData = data,
                    containerColor = snackStyle.background,
                    contentColor = snackStyle.content,
                    actionColor = snackStyle.iconTint,
                )
            }
            // Swipe down to dismiss (Material convention). data.dismiss() resolves showSnackbar with
            // Dismissed → the driver routes to onTransientTimeout (a plain dismiss, no action/Undo). Keyed
            // on the controller id so the drag offset resets per snackbar.
            if (snackSwipe) {
                key(snackKey) {
                    SwipeToDismissContainer(dismissUp = false, onDismiss = { data.dismiss() }) { bar() }
                }
            } else {
                bar()
            }
        }

        // ---- Modal lane (parallel) ----
        // key(m.id): back-to-back modals of the same kind must NOT reuse the previous one's composition
        // slot — a reused ModalBottomSheet keeps its (hidden) sheetState and never re-shows, stranding the
        // lane with an invisible active modal.
        when (val m = controller.activeModal) {
            is AlertRecord -> key(m.id) { FeedbackAlert(m, controller) }
            is SheetRecord -> key(m.id) { FeedbackSheet(m, controller, scope) }
            null -> Unit
        }
    }

    // Snackbar driver: Material owns its own timing AND returns the result.
    val snack = controller.activeTransient as? SnackbarRecord
    LaunchedEffect(snack?.id) {
        val s = snack ?: return@LaunchedEffect
        shownSnack = s // bind styling/swipe to the record actually being shown (survives the exit animation)
        val result = snackbarHostState.showSnackbar(
            message = s.message,
            actionLabel = s.actionLabel,
            withDismissAction = s.actionLabel == null && s.duration == NativeFeedbackDuration.Indefinite,
            duration = s.duration.toSnackbarDuration(),
        )
        when (result) {
            SnackbarResult.ActionPerformed -> controller.onTransientAction(s.id)
            SnackbarResult.Dismissed -> controller.onTransientTimeout(s.id)
        }
    }
}

/** Native-themed HUD pill (default) or the real system [Toast] (opt-in). */
@Composable
private fun ToastHud(
    record: ToastRecord,
    controller: NativeFeedbackController,
    onSystemToast: (String) -> Unit,
) {
    if (record.useSystemToast) {
        LaunchedEffect(record.id) {
            onSystemToast(record.message)
            // The OS toast cannot be held on screen: Indefinite degrades to the system LONG (~3.5 s), and the
            // lane advances in step with what the user actually sees (2 s / 3.5 s, matching SHORT/LONG).
            delay(record.duration.toMillisOrNull() ?: 3_500L)
            controller.onTransientTimeout(record.id)
        }
        return
    }

    val style = resolveFeedbackStyle(record.status, filled = true)
    val align = if (record.position == NativeFeedbackPosition.Top) Alignment.TopCenter else Alignment.BottomCenter
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = align) {
        Row(
            modifier = Modifier
                .let { if (record.position == NativeFeedbackPosition.Top) it.statusBarsPadding() else it.navigationBarsPadding() }
                .widthIn(max = 480.dp)
                .clip(RoundedCornerShape(style.cornerRadius))
                .background(style.background)
                .semantics(mergeDescendants = true) {
                    liveRegion = if (record.status == NativeFeedbackStatus.Error) LiveRegionMode.Assertive else LiveRegionMode.Polite
                }
                .padding(horizontal = style.insets.start, vertical = style.insets.top),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(NativeTheme.tokens.spacingSm),
        ) {
            Icon(record.status.defaultVector(), contentDescription = null, tint = style.iconTint, modifier = Modifier.size(20.dp))
            Text(record.message, style = style.textStyle)
        }
    }
    AutoDismiss(record.id, record.duration, controller)
}

/** Full-width status banner pinned top/bottom, with title/message, optional action, and close button. */
@Composable
private fun FeedbackBanner(record: BannerRecord, controller: NativeFeedbackController) {
    val style = resolveFeedbackStyle(record.status, filled = true)
    val align = if (record.position == NativeFeedbackPosition.Top) Alignment.TopCenter else Alignment.BottomCenter
    // Swipe is independent of the close button: a banner can be swipe-only, button-only, or both. A truly
    // persistent banner sets dismissible = false AND swipeToDismiss = false (then only code can dismiss it).
    val swipeEnabled = record.swipeToDismiss

    val card = @Composable {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .let { if (record.position == NativeFeedbackPosition.Top) it.statusBarsPadding() else it.navigationBarsPadding() }
                .padding(NativeTheme.tokens.spacingSm)
                .clip(RoundedCornerShape(style.cornerRadius))
                .background(style.background)
                .semantics(mergeDescendants = true) {
                    liveRegion = if (record.status == NativeFeedbackStatus.Error) LiveRegionMode.Assertive else LiveRegionMode.Polite
                }
                .padding(start = style.insets.start, top = style.insets.top, end = style.insets.end, bottom = style.insets.bottom),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(NativeTheme.tokens.spacingSm),
        ) {
            Icon(record.status.defaultVector(), contentDescription = null, tint = style.iconTint, modifier = Modifier.size(22.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                if (record.title != null) Text(record.title, style = style.titleTextStyle)
                Text(record.message, style = style.textStyle)
                if (record.actionLabel != null) {
                    Text(
                        text = record.actionLabel,
                        style = style.textStyle.copy(fontWeight = FontWeight.SemiBold, color = style.iconTint),
                        modifier = Modifier.padding(top = 4.dp)
                            .clickable(role = Role.Button, onClickLabel = record.actionLabel) {
                                controller.onTransientAction(record.id)
                            },
                    )
                }
            }
            if (record.dismissible) {
                val dismissLabel = LocalNativeStrings.current.dismiss
                // ≥48dp labeled target around the 20dp glyph (matches NativeInlineStatus's dismiss).
                Box(
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .clickable(onClickLabel = dismissLabel, role = Role.Button) {
                            controller.dismiss(record.id)
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = dismissLabel,
                        tint = style.content,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }

    Box(Modifier.fillMaxSize(), contentAlignment = align) {
        // Swipe the banner toward its pinned edge to dismiss (same result as the close button → onDismiss).
        if (swipeEnabled) {
            key(record.id) {
                SwipeToDismissContainer(
                    dismissUp = record.position == NativeFeedbackPosition.Top,
                    onDismiss = { controller.dismiss(record.id) },
                    modifier = Modifier.fillMaxWidth(),
                ) { card() }
            }
        } else {
            card()
        }
    }
    AutoDismiss(record.id, record.duration, controller)
}

/**
 * Wraps [content] with vertical swipe-to-dismiss. Drag toward the dismiss edge ([dismissUp] = up, for a
 * top banner; otherwise down); releasing past a distance/velocity threshold runs [onDismiss] (which
 * advances the queue), otherwise it springs back. Pairs with — does not replace — the close button,
 * action, and timeout paths. Wrap call sites in `key(recordId)` so the offset resets per message.
 */
@Composable
private fun SwipeToDismissContainer(
    dismissUp: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val thresholdPx = with(LocalDensity.current) { 56.dp.toPx() }
    var offsetY by remember { mutableStateOf(0f) }
    val dragState = rememberDraggableState { delta ->
        // Allow motion only toward the dismiss edge; rubber-band the opposite way to a few px.
        offsetY = (offsetY + delta).let { if (dismissUp) it.coerceAtMost(8f) else it.coerceAtLeast(-8f) }
    }
    Box(
        modifier
            .offset { IntOffset(0, offsetY.roundToInt()) }
            .draggable(
                state = dragState,
                orientation = Orientation.Vertical,
                onDragStopped = { velocity ->
                    val passed = if (dismissUp) offsetY < -thresholdPx || velocity < -800f
                    else offsetY > thresholdPx || velocity > 800f
                    if (passed) onDismiss() else animate(offsetY, 0f) { value, _ -> offsetY = value }
                },
            ),
    ) { content() }
}

/** Material [AlertDialog]. Non-cancel actions become confirm buttons; a Cancel-role action is the dismiss button. */
@Composable
private fun FeedbackAlert(record: AlertRecord, controller: NativeFeedbackController) {
    val cancel = record.actions.firstOrNull { it.role == NativeAlertActionRole.Cancel }
    val others = record.actions.filter { it.role != NativeAlertActionRole.Cancel }
    AlertDialog(
        onDismissRequest = { controller.dismissCurrentModal() },
        title = record.title?.let { { Text(it) } },
        text = record.message?.let { { Text(it) } },
        confirmButton = {
            if (record.actions.isEmpty()) {
                // Parity with iOS, which always injects a confirm (strings.alertOk) so an alert without
                // explicit actions can still be closed by a button, not only by tapping outside.
                val ok = LocalNativeStrings.current.alertOk
                TextButton(onClick = { controller.onModalResult(record.id, null) }) {
                    Text(ok, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(NativeTheme.tokens.spacingXs)) {
                    others.forEach { action ->
                        TextButton(onClick = { controller.onModalResult(record.id, action.onClick) }) {
                            Text(
                                action.label,
                                color = if (action.role == NativeAlertActionRole.Destructive) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }
        },
        dismissButton = cancel?.let {
            { TextButton(onClick = { controller.onModalResult(record.id, it.onClick) }) { Text(it.label) } }
        },
    )
}

/** Material [ModalBottomSheet] of action rows; animates closed before running the chosen action. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FeedbackSheet(
    record: SheetRecord,
    controller: NativeFeedbackController,
    scope: CoroutineScope,
) {
    val sheetState = rememberModalBottomSheetState()
    fun choose(action: (() -> Unit)?) {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) controller.onModalResult(record.id, action)
        }
    }
    ModalBottomSheet(
        onDismissRequest = { controller.dismissCurrentModal() },
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = NativeTheme.tokens.spacingMd),
        ) {
            if (record.title != null || record.message != null) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = NativeTheme.tokens.spacingLg, vertical = NativeTheme.tokens.spacingSm),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    if (record.title != null) Text(record.title, style = MaterialTheme.typography.titleMedium)
                    if (record.message != null) Text(record.message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            record.actions.forEach { action ->
                val tint = when (action.role) {
                    NativeAlertActionRole.Destructive -> MaterialTheme.colorScheme.error
                    NativeAlertActionRole.Cancel -> MaterialTheme.colorScheme.onSurfaceVariant
                    NativeAlertActionRole.Default -> MaterialTheme.colorScheme.onSurface
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { choose(action.onClick) }
                        .padding(horizontal = NativeTheme.tokens.spacingLg, vertical = NativeTheme.tokens.spacingMd),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(NativeTheme.tokens.spacingMd),
                ) {
                    val icon = action.icon
                    val vec = icon?.androidImageVector
                    if (vec != null) Icon(vec, contentDescription = icon.contentDescription, tint = tint, modifier = Modifier.size(22.dp))
                    Text(action.label, style = MaterialTheme.typography.bodyLarge, color = tint, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

/** Schedules an auto-dismiss for a timed transient (no-op for [NativeFeedbackDuration.Indefinite]). */
@Composable
private fun AutoDismiss(id: Long, duration: NativeFeedbackDuration, controller: NativeFeedbackController) {
    val ms = duration.toMillisOrNull()
    LaunchedEffect(id, ms) {
        if (ms != null) {
            delay(ms)
            controller.onTransientTimeout(id)
        }
    }
}

private fun NativeFeedbackDuration.toSnackbarDuration(): SnackbarDuration = when (this) {
    NativeFeedbackDuration.Short -> SnackbarDuration.Short
    NativeFeedbackDuration.Long -> SnackbarDuration.Long
    NativeFeedbackDuration.Indefinite -> SnackbarDuration.Indefinite
}
