package io.github.apdelrahman1911.nativecomposekit.showcase.categories

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.apdelrahman1911.nativecomposekit.components.NativeButton
import io.github.apdelrahman1911.nativecomposekit.components.NativeIconButton
import io.github.apdelrahman1911.nativecomposekit.components.NativeSplitButton
import io.github.apdelrahman1911.nativecomposekit.components.feedback.LocalNativeFeedbackController
import io.github.apdelrahman1911.nativecomposekit.components.feedback.NativeAlertAction
import io.github.apdelrahman1911.nativecomposekit.components.feedback.NativeAlertActionRole
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeButtonShape
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeButtonSize
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeButtonVariant
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeIcon
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeMenu
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeMenuItem
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeMenuItemRole
import io.github.apdelrahman1911.nativecomposekit.showcase.ExampleLabel
import io.github.apdelrahman1911.nativecomposekit.showcase.Note
import io.github.apdelrahman1911.nativecomposekit.showcase.ShowcaseScreen
import io.github.apdelrahman1911.nativecomposekit.showcase.ShowcaseSection
import io.github.apdelrahman1911.nativecomposekit.showcase.WhenToUse

/**
 * Buttons & Actions. Covers the three button components — NativeButton, NativeIconButton,
 * NativeSplitButton — and the NativeMenu they share. Each renders the most native control per
 * platform: Material 3 buttons on Android, UIButton via UIKit interop on iOS.
 */
@Composable
fun ButtonsShowcase() = ShowcaseScreen(
    intro = "The button family. All three components share theme resolution, variants, sizes, and the " +
        "same NativeMenu type, so you move between them without relearning the API.",
) {
    val feedback = LocalNativeFeedbackController.current

    ShowcaseSection(
        title = "NativeButton",
        description = "A labeled action. Variant carries the emphasis; reserve Primary for the main action " +
            "and Destructive for irreversible ones.",
    ) {
        WhenToUse(
            "You need a standard labeled action.",
            "You want a pull-down menu trigger — set menu and a chevron is appended automatically.",
            "Reach for NativeIconButton instead when a glyph alone is clear, and NativeSplitButton when " +
                "one main action also offers related choices.",
        )

        ExampleLabel("Variants")
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            NativeButton("Primary", { feedback.toast("Primary") }, variant = NativeButtonVariant.Primary)
            NativeButton("Secondary", { feedback.toast("Secondary") }, variant = NativeButtonVariant.Secondary)
            NativeButton("Tertiary", { feedback.toast("Tertiary") }, variant = NativeButtonVariant.Tertiary)
            NativeButton("Outline", { feedback.toast("Outline") }, variant = NativeButtonVariant.Outline)
            NativeButton("Destructive", { feedback.toast("Destructive") }, variant = NativeButtonVariant.Destructive)
        }

        ExampleLabel("Sizes")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            NativeButton("Small", { feedback.toast("Small") }, size = NativeButtonSize.Small)
            NativeButton("Medium", { feedback.toast("Medium") }, size = NativeButtonSize.Medium)
            NativeButton("Large", { feedback.toast("Large") }, size = NativeButtonSize.Large)
        }

        ExampleLabel("Disabled & loading")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            NativeButton("Disabled", { feedback.toast("unreachable") }, enabled = false)
            NativeButton("Saving…", { }, loading = true)
        }

        ExampleLabel("Leading & trailing icons")
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            NativeButton(
                "Add item",
                { feedback.toast("Added") },
                leadingIcon = NativeIcon(Icons.Default.Add, sfSymbolName = "plus", contentDescription = "Add"),
            )
            NativeButton(
                "Continue",
                { feedback.toast("Continue") },
                variant = NativeButtonVariant.Outline,
                trailingIcon = NativeIcon(Icons.AutoMirrored.Filled.ArrowForward, sfSymbolName = "arrow.right"),
            )
            NativeButton(
                "Pill",
                { feedback.toast("Pill") },
                variant = NativeButtonVariant.Secondary,
                shape = NativeButtonShape.Pill,
            )
        }

        ExampleLabel("Full width — a primary form submit")
        NativeButton("Create account", { feedback.toast("Submitted") }, fullWidth = true)

        Note(
            "On iOS the visual height stays compact (Small ≈ 36pt) but the hit area is expanded to the " +
                "44pt HIG minimum; Android reserves a 48dp interactive target. Labels honor Dynamic Type.",
        )
    }

    ShowcaseSection(
        title = "Pull-down menu",
        description = "Attach a NativeMenu to a button to make it a menu trigger. Items carry an optional icon, " +
            "a selected checkmark, and a destructive role.",
    ) {
        var sort by remember { mutableStateOf("Name") }
        val sortMenu = NativeMenu(
            title = "Sort by",
            items = listOf(
                NativeMenuItem("Name", { sort = "Name" }, selected = sort == "Name"),
                NativeMenuItem("Date modified", { sort = "Date modified" }, selected = sort == "Date modified"),
                NativeMenuItem("Size", { sort = "Size" }, selected = sort == "Size"),
            ),
        )

        ExampleLabel("Single-select menu — the current choice shows a native checkmark")
        NativeButton("Sort: $sort", { }, variant = NativeButtonVariant.Outline, menu = sortMenu)

        Note(
            "A menu-bearing NativeButton presents the menu on tap. On iOS that suppresses the tap action, " +
                "so onClick may not fire — when you need an action and a menu on one control, use a split button.",
        )
    }

    ShowcaseSection(
        title = "NativeIconButton",
        description = "Icon-only, circular by default. contentDescription is required — there is no visible " +
            "label for assistive tech to read.",
    ) {
        var starred by remember { mutableStateOf(false) }
        val overflowMenu = NativeMenu(
            items = listOf(
                NativeMenuItem("Edit", { feedback.toast("Edit") }, icon = NativeIcon(Icons.Default.Edit, sfSymbolName = "pencil")),
                NativeMenuItem("Share", { feedback.toast("Share") }, icon = NativeIcon(Icons.Default.Share, sfSymbolName = "square.and.arrow.up")),
                NativeMenuItem(
                    "Delete",
                    { feedback.toast("Deleted") },
                    icon = NativeIcon(Icons.Default.Delete, sfSymbolName = "trash"),
                    role = NativeMenuItemRole.Destructive,
                ),
            ),
        )

        ExampleLabel("Toolbar actions, and an overflow trigger (menu opens on tap, no chevron)")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NativeIconButton(
                NativeIcon(Icons.Default.Add, sfSymbolName = "plus"),
                { feedback.toast("New") },
                contentDescription = "New item",
                variant = NativeButtonVariant.Primary,
            )
            NativeIconButton(
                NativeIcon(Icons.Default.Edit, sfSymbolName = "pencil"),
                { feedback.toast("Edit") },
                contentDescription = "Edit",
                variant = NativeButtonVariant.Outline,
            )
            NativeIconButton(
                NativeIcon(Icons.Default.Share, sfSymbolName = if (starred) "star.fill" else "star"),
                { starred = !starred; feedback.toast(if (starred) "Starred" else "Unstarred") },
                contentDescription = if (starred) "Remove star" else "Add star",
            )
            NativeIconButton(
                NativeIcon(Icons.Default.MoreVert, sfSymbolName = "ellipsis"),
                { },
                contentDescription = "More actions",
                menu = overflowMenu,
            )
        }
    }

    ShowcaseSection(
        title = "NativeSplitButton",
        description = "One main action plus a chevron for related choices. Unlike a menu-bearing button, the " +
            "primary tap always fires.",
    ) {
        val saveMenu = NativeMenu(
            items = listOf(
                NativeMenuItem("Save as draft", { feedback.toast("Saved as draft") }),
                NativeMenuItem("Save a copy", { feedback.toast("Saved a copy") }),
                NativeMenuItem(
                    "Discard",
                    { feedback.toast("Discarded") },
                    role = NativeMenuItemRole.Destructive,
                ),
            ),
        )

        ExampleLabel("Primary segment commits; chevron opens the alternatives")
        NativeSplitButton(
            text = "Save",
            onPrimaryClick = { feedback.toast("Saved") },
            menu = saveMenu,
            leadingIcon = NativeIcon(Icons.Default.Add, sfSymbolName = "plus"),
        )

        Note(
            "There is no single native split control on either platform — it is two segments sharing the " +
                "variant colors with a hairline divider and one rounded outer outline. Required menu, so the " +
                "chevron always has something to present.",
        )
    }

    ShowcaseSection(
        title = "Action row",
        description = "Pair a destructive confirm with a low-emphasis escape. The dismissive action stays " +
            "Tertiary/Outline so the eye lands on intent, not on the cancel.",
    ) {
        ExampleLabel("Destructive confirm + Cancel — wired to a native alert")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            NativeButton(
                "Cancel",
                { feedback.toast("Cancelled") },
                variant = NativeButtonVariant.Tertiary,
            )
            NativeButton(
                "Delete",
                {
                    feedback.alert(
                        title = "Delete project?",
                        message = "This permanently removes the project and its files. This can't be undone.",
                        actions = listOf(
                            NativeAlertAction("Cancel", role = NativeAlertActionRole.Cancel),
                            NativeAlertAction(
                                "Delete",
                                onClick = { feedback.toast("Project deleted") },
                                role = NativeAlertActionRole.Destructive,
                            ),
                        ),
                    )
                },
                variant = NativeButtonVariant.Destructive,
                leadingIcon = NativeIcon(Icons.Default.Delete, sfSymbolName = "trash", contentDescription = "Delete"),
            )
        }

        Note(
            "Destructive intent is carried twice: the red button variant, and the destructive role on the " +
                "alert's confirm action (UIAlertActionStyleDestructive on iOS, error-tinted on Android).",
        )
    }
}
