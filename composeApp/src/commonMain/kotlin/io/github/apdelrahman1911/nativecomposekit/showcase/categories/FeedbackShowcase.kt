package io.github.apdelrahman1911.nativecomposekit.showcase.categories

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.apdelrahman1911.nativecomposekit.components.NativeBadge
import io.github.apdelrahman1911.nativecomposekit.components.NativeBadgedBox
import io.github.apdelrahman1911.nativecomposekit.components.NativeButton
import io.github.apdelrahman1911.nativecomposekit.components.NativeProgressIndicator
import io.github.apdelrahman1911.nativecomposekit.components.NativeProgressKind
import io.github.apdelrahman1911.nativecomposekit.components.NativeText
import io.github.apdelrahman1911.nativecomposekit.components.feedback.LocalNativeFeedbackController
import io.github.apdelrahman1911.nativecomposekit.components.feedback.NativeAlertAction
import io.github.apdelrahman1911.nativecomposekit.components.feedback.NativeAlertActionRole
import io.github.apdelrahman1911.nativecomposekit.components.feedback.NativeFeedbackPosition
import io.github.apdelrahman1911.nativecomposekit.components.feedback.NativeFeedbackStatus
import io.github.apdelrahman1911.nativecomposekit.components.feedback.NativeInlineStatus
import io.github.apdelrahman1911.nativecomposekit.components.feedback.NativeSheetAction
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeButtonSize
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeButtonVariant
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeTextStyle
import io.github.apdelrahman1911.nativecomposekit.showcase.ExampleLabel
import io.github.apdelrahman1911.nativecomposekit.showcase.Note
import io.github.apdelrahman1911.nativecomposekit.showcase.ShowcaseScreen
import io.github.apdelrahman1911.nativecomposekit.showcase.ShowcaseSection
import io.github.apdelrahman1911.nativecomposekit.showcase.WhenToUse

/**
 * "Feedback & Status" — progress indicators, the imperative feedback controller (toast / snackbar /
 * banner / alert / confirmation sheet), in-flow inline status, and count badges.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FeedbackShowcase() = ShowcaseScreen(
    intro = "Tell the user what's happening: progress while work runs, transient and modal feedback posted " +
        "through one controller, in-flow status inside forms, and badges for unread counts.",
) {
    val feedback = LocalNativeFeedbackController.current

    // region Triggers — every surface posted through the controller
    ShowcaseSection(
        title = "Feedback controller",
        description = "Read LocalNativeFeedbackController.current, then call toast/snackbar/banner/alert/" +
            "confirmationSheet from any click lambda. Transient messages (toast/snackbar/banner) share one " +
            "FIFO lane; alert and confirmationSheet block on a parallel modal lane.",
    ) {
        ExampleLabel("Transient — auto-dismissing")
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            NativeButton("Toast", { feedback.toast("Link copied") })
            NativeButton(
                "Saved",
                { feedback.toast("Settings saved", status = NativeFeedbackStatus.Success) },
                variant = NativeButtonVariant.Secondary,
            )
            NativeButton(
                "Undo",
                {
                    feedback.snackbar(
                        "Message archived",
                        actionLabel = "Undo",
                        onAction = { feedback.toast("Restored", status = NativeFeedbackStatus.Success) },
                    )
                },
                variant = NativeButtonVariant.Outline,
            )
            NativeButton(
                "Banner",
                {
                    feedback.banner(
                        "Changes will sync when you reconnect.",
                        title = "You're offline",
                        status = NativeFeedbackStatus.Warning,
                        position = NativeFeedbackPosition.Top,
                        actionLabel = "Retry",
                        onAction = { feedback.toast("Retrying…") },
                    )
                },
                variant = NativeButtonVariant.Outline,
            )
        }

        ExampleLabel("Modal — blocks until the user chooses")
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            NativeButton(
                "Discard…",
                {
                    feedback.alert(
                        title = "Discard draft?",
                        message = "Your unsaved edits will be lost.",
                        actions = listOf(
                            NativeAlertAction(
                                "Discard",
                                { feedback.toast("Draft discarded") },
                                role = NativeAlertActionRole.Destructive,
                            ),
                            NativeAlertAction("Keep editing", role = NativeAlertActionRole.Cancel),
                        ),
                    )
                },
                variant = NativeButtonVariant.Destructive,
            )
            NativeButton(
                "Export…",
                {
                    feedback.confirmationSheet(
                        title = "Export report",
                        actions = listOf(
                            NativeSheetAction("PDF", { feedback.toast("Exporting PDF") }),
                            NativeSheetAction("CSV", { feedback.toast("Exporting CSV") }),
                            NativeSheetAction(
                                "Delete report",
                                { feedback.toast("Report deleted") },
                                role = NativeAlertActionRole.Destructive,
                            ),
                            NativeSheetAction("Cancel", role = NativeAlertActionRole.Cancel),
                        ),
                    )
                },
                variant = NativeButtonVariant.Secondary,
            )
        }

        WhenToUse(
            "toast — confirm a quick, low-stakes action with no follow-up (\"Copied\", \"Saved\").",
            "snackbar — a transient message that carries one action, most often Undo.",
            "banner — a status that must persist and stay visible, with an optional action and close button.",
            "alert — a yes/no decision the user must make before continuing.",
            "confirmationSheet — a short list of actions tied to one subject (Export, Delete, Rename).",
        )

        Note(
            "alert and confirmationSheet are native by default — a real UIAlertController on iOS, a Material " +
                "AlertDialog / ModalBottomSheet on Android. A Destructive-role action renders red; at most one " +
                "Cancel-role action per modal. The controller stores a NativeFeedbackStatus, never resolved colors.",
        )
    }
    // endregion

    // region Progress
    ShowcaseSection(
        title = "Progress",
        description = "NativeProgressIndicator. Pass progress in 0..1 for determinate; null for an " +
            "indeterminate spinner / looping bar. kind picks the shape.",
    ) {
        ExampleLabel("Indeterminate — work of unknown length")
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            NativeProgressIndicator()
            NativeText("Refreshing feed…", style = NativeTextStyle.Label)
        }
        NativeProgressIndicator(
            kind = NativeProgressKind.Linear,
            modifier = Modifier.fillMaxWidth(),
        )

        var progress by remember { mutableStateOf(0.35f) }
        ExampleLabel("Determinate — a measurable download")
        NativeText("Downloading ${(progress * 100).toInt()}%", style = NativeTextStyle.Label)
        NativeProgressIndicator(
            kind = NativeProgressKind.Linear,
            progress = progress,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            NativeProgressIndicator(kind = NativeProgressKind.Circular, progress = progress)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NativeButton(
                    "-10%",
                    { progress = (progress - 0.1f).coerceIn(0f, 1f) },
                    variant = NativeButtonVariant.Outline,
                    size = NativeButtonSize.Small,
                )
                NativeButton(
                    "+10%",
                    { progress = (progress + 0.1f).coerceIn(0f, 1f) },
                    variant = NativeButtonVariant.Outline,
                    size = NativeButtonSize.Small,
                )
            }
        }

        Note(
            "iOS is fully native only for indeterminate circular (UIActivityIndicatorView) and determinate " +
                "linear (UIProgressView). The other two combinations — determinate circular and indeterminate " +
                "linear — have no native iOS control and fall back to the Compose-drawn Material indicator.",
        )
    }
    // endregion

    // region Inline status inside a form
    ShowcaseSection(
        title = "Inline status",
        description = "NativeInlineStatus lives in the layout flow — field validation, a sync hint, an " +
            "error with a Retry — not a floating overlay, so it is not posted through the controller. Drive " +
            "its visibility from your own state.",
    ) {
        var showError by remember { mutableStateOf(true) }

        ExampleLabel("Info / Success / Warning / Error")
        NativeInlineStatus(
            "Saved locally — will sync when you're back online.",
            status = NativeFeedbackStatus.Info,
        )
        NativeInlineStatus(
            "Profile updated.",
            title = "Done",
            status = NativeFeedbackStatus.Success,
        )
        NativeInlineStatus(
            "Your free trial ends in 3 days.",
            status = NativeFeedbackStatus.Warning,
            filled = false,
            actionLabel = "Upgrade",
            onAction = { feedback.toast("Opening upgrade…") },
        )
        if (showError) {
            NativeInlineStatus(
                "We couldn't reach the server. Check your connection and try again.",
                title = "Sign-in failed",
                status = NativeFeedbackStatus.Error,
                actionLabel = "Retry",
                onAction = { feedback.toast("Retrying…", status = NativeFeedbackStatus.Info) },
                onDismiss = { showError = false },
            )
        } else {
            NativeButton(
                "Reset error state",
                { showError = true },
                variant = NativeButtonVariant.Tertiary,
                size = NativeButtonSize.Small,
            )
        }

        Note(
            "Inline status is Compose-drawn on both platforms (no UIKit interop) so it sizes to its content " +
                "in a column. It announces to screen readers when it appears — Error interrupts (Assertive), " +
                "the rest are queued (Polite).",
        )
    }
    // endregion

    // region Badges
    ShowcaseSection(
        title = "Badges",
        description = "NativeBadge is a count or dot overlay; NativeBadgedBox anchors it to the top-end " +
            "corner of any content. A non-positive count renders nothing — the usual \"no badge when zero\".",
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(28.dp),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                NativeBadgedBox(badge = { NativeBadge(count = 3, contentDescription = "3 unread") }) {
                    Icon(
                        Icons.Outlined.Notifications,
                        contentDescription = "Notifications",
                        modifier = Modifier.size(28.dp),
                    )
                }
                ExampleLabel("Count")
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                NativeBadgedBox(
                    badge = {
                        NativeBadge(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        )
                    },
                ) {
                    Icon(
                        Icons.Outlined.Notifications,
                        contentDescription = "Notifications",
                        modifier = Modifier.size(28.dp),
                    )
                }
                ExampleLabel("Dot")
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                NativeBadgedBox(badge = { NativeBadge(count = 128, contentDescription = "128 unread") }) {
                    Icon(
                        Icons.Outlined.Notifications,
                        contentDescription = "Notifications",
                        modifier = Modifier.size(28.dp),
                    )
                }
                ExampleLabel("Capped 99+")
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            NativeText("Standalone count pill:", style = NativeTextStyle.Body)
            Spacer(Modifier.width(8.dp))
            NativeBadge(count = 12)
        }

        Note(
            "NativeBadge is Compose-drawn on both platforms (a styled overlay, not a native leaf control). It " +
                "defaults to the unread red (error); pass containerColor/contentColor for a semantic badge. A " +
                "numbered badge announces its count — pass a contentDescription when the bare number is ambiguous.",
        )
    }
    // endregion
}
