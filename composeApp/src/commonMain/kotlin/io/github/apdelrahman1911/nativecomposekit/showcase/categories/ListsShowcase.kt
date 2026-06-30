package io.github.apdelrahman1911.nativecomposekit.showcase.categories

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import io.github.apdelrahman1911.nativecomposekit.components.NativeAvatar
import io.github.apdelrahman1911.nativecomposekit.components.NativeAvatarSize
import io.github.apdelrahman1911.nativecomposekit.components.NativeListItem
import io.github.apdelrahman1911.nativecomposekit.components.NativeListSection
import io.github.apdelrahman1911.nativecomposekit.components.NativeListSectionStyle
import io.github.apdelrahman1911.nativecomposekit.components.NativeSwipeAction
import io.github.apdelrahman1911.nativecomposekit.components.NativeToggle
import io.github.apdelrahman1911.nativecomposekit.components.feedback.LocalNativeFeedbackController
import io.github.apdelrahman1911.nativecomposekit.showcase.ExampleLabel
import io.github.apdelrahman1911.nativecomposekit.showcase.Note
import io.github.apdelrahman1911.nativecomposekit.showcase.ShowcaseScreen
import io.github.apdelrahman1911.nativecomposekit.showcase.ShowcaseSection
import io.github.apdelrahman1911.nativecomposekit.showcase.WhenToUse

/**
 * Lists & Rows: NativeListSection groups NativeListItem rows under a header/footer and draws the separators
 * between them; the row itself carries the optional slots (leading, supporting text, trailing control or
 * value, chevron, swipe action). Together they cover settings screens, detail lists, and inbox-style rows.
 */
@Composable
fun ListsShowcase() = ShowcaseScreen(
    intro = "A list is two pieces: NativeListSection groups rows and owns the separators, and NativeListItem " +
        "is one row with optional leading/trailing slots, a chevron, and a swipe action. Compose-drawn on " +
        "both platforms — a row is layout, not a native leaf control.",
) {
    GroupedSettingsSection()
    SwipeAndLongPressSection()
    PlainStyleSection()

    Note(
        "Don't make a toggle row onClick-able too — the row stays unmerged so the toggle keeps its own focus, " +
            "and a second tap target competes with it. Tap-to-navigate rows (chevron) and control rows " +
            "(toggle / value) are different patterns; mix them within a section, not within a row.",
    )
}

@Composable
private fun GroupedSettingsSection() {
    val feedback = LocalNativeFeedbackController.current
    var alerts by remember { mutableStateOf(true) }
    var faceId by remember { mutableStateOf(false) }

    ShowcaseSection(
        title = "Grouped settings list",
        description = "The iOS inset-grouped look: a leading icon, supporting text, and a trailing value or " +
            "control per row. Navigable rows take onClick and get the auto-mirrored chevron for free.",
    ) {
        WhenToUse(
            "Settings, account, and preference screens with rows that share separators",
            "A row needs a value on the right (trailingText) or an inline control (trailing toggle)",
            "Tapping a row drills into a sub-screen — pass onClick for the chevron",
        )

        ExampleLabel("Header + footer, mixed value / toggle / navigable rows")
        NativeListSection(
            header = "GENERAL",
            footer = "Face ID unlocks the app without a passcode. Turn it off to require manual entry.",
            rows = listOf(
                {
                    NativeListItem(
                        headline = "Account",
                        supporting = "apple.id@icloud.com",
                        leading = { SettingIcon(Icons.Filled.Person) },
                        onClick = { feedback.toast("Open Account") },
                    )
                },
                {
                    // Value row: read-only trailing text + chevron into a picker sub-screen.
                    NativeListItem(
                        headline = "Appearance",
                        leading = { SettingIcon(Icons.Filled.Palette) },
                        trailingText = "Automatic",
                        onClick = { feedback.toast("Open Appearance") },
                    )
                },
                {
                    // Control row: trailing toggle, no onClick (the row stays unmerged so the toggle is focusable).
                    NativeListItem(
                        headline = "Notifications",
                        supporting = "Alerts, sounds, and badges",
                        leading = { SettingIcon(Icons.Filled.Notifications) },
                        trailing = { NativeToggle(checked = alerts, onCheckedChange = { alerts = it }) },
                    )
                },
                {
                    NativeListItem(
                        headline = "Unlock with Face ID",
                        leading = { SettingIcon(Icons.Filled.Lock) },
                        trailing = { NativeToggle(checked = faceId, onCheckedChange = { faceId = it }) },
                    )
                },
            ),
        )

        ExampleLabel("Disabled row (dimmed, non-tappable)")
        NativeListSection(
            rows = listOf(
                {
                    NativeListItem(
                        headline = "Restore Purchases",
                        supporting = "No purchases found on this Apple ID",
                        leading = { SettingIcon(Icons.Filled.Bookmark) },
                        enabled = false,
                        onClick = { feedback.toast("Restore") },
                    )
                },
            ),
        )
    }
}

@Composable
private fun SwipeAndLongPressSection() {
    val feedback = LocalNativeFeedbackController.current
    // Real row state: a mutable list the swipe action removes from, so the row actually disappears.
    val people = remember {
        mutableStateListOf(
            Contact("Jin Dao", "Last seen 2m ago", "JD"),
            Contact("Aiko Kira", "Typing…", "AK"),
            Contact("Mateo Xu", "Last seen yesterday", "MX"),
        )
    }

    ShowcaseSection(
        title = "Avatars, swipe-to-delete & long-press",
        description = "Each row has an avatar in the leading slot. Swipe ← past the threshold fires the swipe " +
            "action and the row snaps back; remove the item from your list in onAction so it disappears. " +
            "Long-press surfaces a secondary action.",
    ) {
        WhenToUse(
            "Inbox / chat / contact rows where each item has a photo or initials",
            "A destructive or quick action belongs on a swipe rather than a visible button",
            "A secondary action fits a long-press (named via onLongClickLabel for screen readers)",
        )

        ExampleLabel("Swipe ← to delete · long-press to pin")
        if (people.isEmpty()) {
            NativeListSection(
                rows = listOf(
                    { NativeListItem(headline = "No contacts", supporting = "Everyone has been removed") },
                ),
            )
        } else {
            NativeListSection(
                header = "CONTACTS",
                rows = people.map { contact ->
                    {
                        NativeListItem(
                            headline = contact.name,
                            supporting = contact.status,
                            leading = { NativeAvatar(initials = contact.initials, size = NativeAvatarSize.Medium) },
                            onClick = { feedback.toast("Open ${contact.name}") },
                            onLongClick = { feedback.toast("Pinned ${contact.name}") },
                            onLongClickLabel = "Pin to top",
                            swipeAction = NativeSwipeAction(
                                label = "Delete",
                                icon = Icons.Filled.Delete,
                                destructive = true,
                                onAction = {
                                    val removed = contact
                                    people.remove(removed)
                                    feedback.snackbar(
                                        message = "Deleted ${removed.name}",
                                        actionLabel = "Undo",
                                        onAction = { if (removed !in people) people.add(removed) },
                                    )
                                },
                            ),
                        )
                    }
                },
            )
        }

        Note(
            "Swipe and long-press are gesture-only and invisible to screen readers, so the row exposes them as " +
                "custom accessibility actions — the swipe always, the long-press only when you pass " +
                "onLongClickLabel. destructive = true tints the reveal with the error color.",
        )
    }
}

@Composable
private fun PlainStyleSection() {
    val feedback = LocalNativeFeedbackController.current
    var selected by remember { mutableStateOf("Today") }
    val filters = listOf("Today", "Upcoming", "Done")

    ShowcaseSection(
        title = "Plain style",
        description = "Plain drops the inset card so rows run edge-to-edge — the flat Android-style list. Same " +
            "rows, no surrounding container. Here the overline plus a trailing checkmark mark the active filter.",
    ) {
        ExampleLabel("style = Plain, single-select rows")
        NativeListSection(
            style = NativeListSectionStyle.Plain,
            rows = filters.map { filter ->
                {
                    NativeListItem(
                        headline = filter,
                        overline = "FILTER",
                        trailingText = if (filter == selected) "✓" else null,
                        showChevron = false,
                        onClick = {
                            selected = filter
                            feedback.toast("Showing $filter")
                        },
                    )
                }
            },
        )
    }
}

/** A tinted leading glyph sized to sit in a NativeListItem's leading slot. */
@Composable
private fun SettingIcon(icon: ImageVector) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(24.dp),
    )
}

private data class Contact(val name: String, val status: String, val initials: String)
