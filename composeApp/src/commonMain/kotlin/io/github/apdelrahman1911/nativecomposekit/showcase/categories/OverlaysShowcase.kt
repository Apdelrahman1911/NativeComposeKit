package io.github.apdelrahman1911.nativecomposekit.showcase.categories

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.apdelrahman1911.nativecomposekit.components.NativeButton
import io.github.apdelrahman1911.nativecomposekit.components.NativeDialog
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeDialogColors
import io.github.apdelrahman1911.nativecomposekit.components.NativePopover
import io.github.apdelrahman1911.nativecomposekit.components.NativeSegmentedControl
import io.github.apdelrahman1911.nativecomposekit.components.NativeSheet
import io.github.apdelrahman1911.nativecomposekit.components.NativeSheetDetent
import io.github.apdelrahman1911.nativecomposekit.components.NativeText
import io.github.apdelrahman1911.nativecomposekit.components.NativeTextField
import io.github.apdelrahman1911.nativecomposekit.components.NativeToggle
import io.github.apdelrahman1911.nativecomposekit.components.rememberNativeShare
import io.github.apdelrahman1911.nativecomposekit.components.feedback.LocalNativeFeedbackController
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeButtonSize
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeButtonVariant
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeTextStyle
import io.github.apdelrahman1911.nativecomposekit.showcase.ExampleLabel
import io.github.apdelrahman1911.nativecomposekit.showcase.Note
import io.github.apdelrahman1911.nativecomposekit.showcase.ShowcaseScreen
import io.github.apdelrahman1911.nativecomposekit.showcase.ShowcaseSection
import io.github.apdelrahman1911.nativecomposekit.showcase.WhenToUse

/**
 * Showcase: things that present above the current screen. Each example is driven by hoisted visibility
 * state and a trigger button — the way you'd wire these in a real screen.
 */
@Composable
fun OverlaysShowcase() = ShowcaseScreen(
    intro = "Surfaces that present over the current screen. Pick by shape and intent: a plain text + " +
        "buttons confirmation is an alert; custom centered content is a dialog; a detented panel is a sheet; " +
        "a small contextual panel is a popover; handing content to the OS is the share sheet.",
) {
    DialogSection()
    SheetSection()
    PopoverSection()
    ShareSection()
}

// ----- NativeDialog: a confirmation with a destructive action -----

@Composable
private fun DialogSection() {
    ShowcaseSection(
        title = "NativeDialog",
        description = "Custom centered content in a Compose Dialog. Reach for it when the native paths " +
            "don't fit; for plain text + buttons prefer feedback.alert (a real UIAlertController on iOS).",
    ) {
        var confirmOpen by remember { mutableStateOf(false) }

        ExampleLabel("Destructive confirmation")
        NativeButton(
            "Delete project",
            onClick = { confirmOpen = true },
            variant = NativeButtonVariant.Destructive,
        )

        if (confirmOpen) {
            NativeDialog(
                onDismissRequest = { confirmOpen = false },
                title = "Delete project?",
                actions = {
                    NativeButton(
                        "Cancel",
                        onClick = { confirmOpen = false },
                        variant = NativeButtonVariant.Tertiary,
                        size = NativeButtonSize.Small,
                    )
                    NativeButton(
                        "Delete",
                        onClick = { confirmOpen = false },
                        variant = NativeButtonVariant.Destructive,
                        size = NativeButtonSize.Small,
                    )
                },
            ) {
                NativeText(
                    "This removes the project and its 14 files for everyone. This can't be undone.",
                    style = NativeTextStyle.Body,
                )
            }
        }

        // Same component, a totally different look — icon + title slots, custom tonal colors, a rounder shape,
        // and centered layout. Nothing about the dialog is locked to one visual design.
        var styledOpen by remember { mutableStateOf(false) }
        ExampleLabel("Fully customized — icon, colors, shape, centered")
        NativeButton("What's new", onClick = { styledOpen = true }, variant = NativeButtonVariant.Secondary)
        if (styledOpen) {
            NativeDialog(
                onDismissRequest = { styledOpen = false },
                icon = { NativeText("✨", style = NativeTextStyle.Display) },
                title = { NativeText("What's new", style = NativeTextStyle.Title) },
                colorsOverride = NativeDialogColors(
                    container = MaterialTheme.colorScheme.secondaryContainer,
                    content = MaterialTheme.colorScheme.onSecondaryContainer,
                    title = MaterialTheme.colorScheme.onSecondaryContainer,
                ),
                cornerRadius = 28.dp,
                horizontalAlignment = Alignment.CenterHorizontally,
                actions = {
                    NativeButton("Got it", onClick = { styledOpen = false }, size = NativeButtonSize.Small)
                },
            ) {
                NativeText(
                    "Reader themes, faster sync, and swipe-to-archive.",
                    style = NativeTextStyle.Body,
                    align = TextAlign.Center,
                )
            }
        }

        WhenToUse(
            "Custom centered content the native paths don't cover — a short form, a list, an image.",
            "A confirmation that pairs a message with a clear default and a destructive choice.",
            "Any product's dialog look — icon/title/actions slots, colors, shape, elevation, alignment are yours.",
            "For a plain message + buttons, use feedback.alert instead (native UIAlertController).",
        )
    }
}

// ----- NativeSheet: a detented filter form -----

@Composable
private fun SheetSection() {
    ShowcaseSection(
        title = "NativeSheet",
        description = "A bottom sheet for a scoped, dismissible task. iOS renders a real " +
            "UISheetPresentationController with native detents, grabber, and swipe-to-dismiss.",
    ) {
        var sheetOpen by remember { mutableStateOf(false) }

        // Filter form state, hoisted out here so it survives the sheet opening/closing.
        var sort by remember { mutableStateOf(0) }
        var unreadOnly by remember { mutableStateOf(true) }
        var downloadedOnly by remember { mutableStateOf(false) }
        var query by remember { mutableStateOf("") }

        ExampleLabel("Detented panel holding a filter form")
        NativeButton(
            "Filters",
            onClick = { sheetOpen = true },
            variant = NativeButtonVariant.Secondary,
        )

        NativeSheet(
            visible = sheetOpen,
            onDismissRequest = { sheetOpen = false },
            detents = listOf(NativeSheetDetent.Medium, NativeSheetDetent.Large),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                NativeText("Filter chapters", style = NativeTextStyle.Title)

                NativeText("Sort", style = NativeTextStyle.Label)
                NativeSegmentedControl(
                    options = listOf("Newest", "Oldest", "A–Z"),
                    selectedIndex = sort,
                    onSelectedIndexChange = { sort = it },
                    modifier = Modifier.fillMaxWidth(),
                )

                NativeTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = "Contains",
                    placeholder = "Title keyword",
                )

                SettingsToggleRow(
                    title = "Unread only",
                    checked = unreadOnly,
                    onCheckedChange = { unreadOnly = it },
                )
                SettingsToggleRow(
                    title = "Downloaded only",
                    checked = downloadedOnly,
                    onCheckedChange = { downloadedOnly = it },
                )

                NativeButton(
                    "Apply",
                    onClick = { sheetOpen = false },
                    fullWidth = true,
                )
            }
        }

        WhenToUse(
            "A focused, dismissible task: filters, a short form, an action list.",
            "Compact-width iPhone panels — prefer a sheet here over a popover.",
            "Multiple detents (Medium + Large) when the content can start small and expand.",
        )

        Note(
            "iOS hosts the sheet content in a separate Compose composition that inherits no " +
                "CompositionLocals. The component re-provides the parent theme (colors, type, dark mode, RTL); " +
                "non-theme providers like LocalNativeFeedbackController are not resolvable inside, so capture a " +
                "reference before presenting if the content needs one.",
        )
    }
}

// ----- NativePopover: a small panel anchored to its trigger -----

@Composable
private fun PopoverSection() {
    ShowcaseSection(
        title = "NativePopover",
        description = "A transient panel floated next to its anchor. The anchor renders inline in layout " +
            "and the popover points at it.",
    ) {
        var infoOpen by remember { mutableStateOf(false) }

        ExampleLabel("Anchored to a button")
        // The trigger lives in the anchor slot, so the popover is positioned relative to it.
        NativePopover(
            visible = infoOpen,
            onDismissRequest = { infoOpen = false },
            anchor = {
                NativeButton(
                    "Storage info",
                    onClick = { infoOpen = true },
                    variant = NativeButtonVariant.Tertiary,
                )
            },
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                NativeText("On this device", style = NativeTextStyle.Label)
                NativeText("128 chapters cached · 1.4 GB", style = NativeTextStyle.Body)
                NativeText("Cleared automatically after 30 days.", style = NativeTextStyle.Label)
            }
        }

        WhenToUse(
            "A small contextual panel attached to a trigger — a quick detail or an overflow menu on iPad.",
            "A lightweight elevated surface anchored inline, dismissed by tapping outside.",
        )

        Note(
            "Phone vs tablet differ: on iPad / regular width this is a real UIPopoverPresentationController " +
                "(system material and arrow) anchored to the trigger's rect. On iPhone / compact width UIKit " +
                "would expand a native popover to full screen, so the kit draws a themed Compose popover there " +
                "(same as Android) and keeps it compact — prefer a sheet on iPhone when the panel is larger.",
        )
    }
}

// ----- NativeShareSheet: hand text/url to the system -----

@Composable
private fun ShareSection() {
    ShowcaseSection(
        title = "NativeShareSheet",
        description = "The system share UI, invoked from a click handler via a rememberNativeShare() handle. " +
            "Sharing is a one-shot action, so it's a handle you call rather than a placed composable.",
    ) {
        val share = rememberNativeShare()
        val feedback = LocalNativeFeedbackController.current

        ExampleLabel("Share text + URL")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NativeButton(
                "Share chapter",
                onClick = {
                    share.share(
                        text = "One Piece — Chapter 1043",
                        url = "https://example.com/one-piece/1043",
                    )
                },
                variant = NativeButtonVariant.Outline,
            )
            NativeButton(
                "Copy link",
                onClick = { feedback.toast("Link copied") },
                variant = NativeButtonVariant.Tertiary,
            )
        }

        WhenToUse(
            "Send text or a URL through the platform share sheet from a tap.",
            "Defer to the OS for the destination list (Messages, Mail, AirDrop, the clipboard).",
        )

        Note(
            "iOS presents a UIActivityViewController from the top-most view controller and anchors it as a " +
                "popover on iPad; Android opens an ACTION_SEND chooser. The sheet presents only when there's at " +
                "least one item to share.",
        )
    }
}

/** A settings-style row: a label on the left, a native toggle pinned to the trailing edge. */
@Composable
private fun SettingsToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        NativeText(title, style = NativeTextStyle.Body)
        NativeToggle(checked = checked, onCheckedChange = onCheckedChange)
    }
}
