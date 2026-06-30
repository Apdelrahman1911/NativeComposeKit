package io.github.apdelrahman1911.nativecomposekit.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.apdelrahman1911.nativecomposekit.components.NativeAvatar
import io.github.apdelrahman1911.nativecomposekit.components.NativeBadge
import io.github.apdelrahman1911.nativecomposekit.components.NativeBadgedBox
import io.github.apdelrahman1911.nativecomposekit.components.NativeButton
import io.github.apdelrahman1911.nativecomposekit.components.NativeCard
import io.github.apdelrahman1911.nativecomposekit.components.NativeCardVariant
import io.github.apdelrahman1911.nativecomposekit.components.NativeCheckbox
import io.github.apdelrahman1911.nativecomposekit.components.NativeChip
import io.github.apdelrahman1911.nativecomposekit.components.NativeChipStyle
import io.github.apdelrahman1911.nativecomposekit.components.NativeColorWell
import io.github.apdelrahman1911.nativecomposekit.components.NativeEmptyState
import io.github.apdelrahman1911.nativecomposekit.components.NativeIconButton
import io.github.apdelrahman1911.nativecomposekit.components.NativeListItem
import io.github.apdelrahman1911.nativecomposekit.components.NativeOtpField
import io.github.apdelrahman1911.nativecomposekit.components.NativePageControl
import io.github.apdelrahman1911.nativecomposekit.components.NativeProgressIndicator
import io.github.apdelrahman1911.nativecomposekit.components.NativeProgressKind
import io.github.apdelrahman1911.nativecomposekit.components.NativeRadioGroup
import io.github.apdelrahman1911.nativecomposekit.components.NativeRating
import io.github.apdelrahman1911.nativecomposekit.components.NativeSearchBar
import io.github.apdelrahman1911.nativecomposekit.components.NativeSegmentedControl
import io.github.apdelrahman1911.nativecomposekit.components.NativeSelectionStyle
import io.github.apdelrahman1911.nativecomposekit.components.NativeSkeleton
import io.github.apdelrahman1911.nativecomposekit.components.NativeSlider
import io.github.apdelrahman1911.nativecomposekit.components.NativeStepper
import io.github.apdelrahman1911.nativecomposekit.components.NativeSwipeAction
import io.github.apdelrahman1911.nativecomposekit.components.NativeText
import io.github.apdelrahman1911.nativecomposekit.components.NativeToggle
import io.github.apdelrahman1911.nativecomposekit.components.feedback.NativeFeedbackStatus
import io.github.apdelrahman1911.nativecomposekit.components.feedback.NativeInlineStatus
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeIcon
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeTextStyle

/**
 * Debug regression harness for the **surface-adaptation** bug class (the `NativeListItem` swipe reveal, the
 * invisible-skeleton-on-a-card, the native-control backing/light-dark probe, etc.): it renders each
 * surface-dependent component **twice — directly on the page surface and inside a Filled [NativeCard]** (which
 * publishes `surfaceVariant`). A component that hardcodes a surface color instead of reading
 * `LocalNativeSurface` will visibly differ (or vanish) between the two columns. Pushed from Settings →
 * "Component surface matrix"; debug-only, not part of the manga flow. The bare-on-glass native-control matrix
 * is covered separately by [GlassInteropTestScreen].
 *
 * Coverage: every surface-sensitive component is here. `NativeDatePicker` is intentionally omitted — its
 * Android renderer is a full inline calendar (rendering it twice makes the screen unusable) and its
 * surface/light-dark probe is iOS-only; it is exercised in the catalog + [GlassInteropTestScreen] instead.
 */
@Composable
fun ComponentMatrixScreen() {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        NativeText(
            "Each component is shown on the page surface, then inside a Filled card. " +
                "They should look consistent (and visible) in both.",
            style = NativeTextStyle.Label,
        )

        Demo("Skeleton (must stay visible on the card)") {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                NativeSkeleton(Modifier.size(64.dp, 96.dp))
                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    NativeSkeleton(Modifier.fillMaxWidth().height(16.dp))
                    NativeSkeleton(Modifier.fillMaxWidth(0.6f).height(14.dp))
                }
            }
        }

        Demo("Inline status (filled + outlined)") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                NativeInlineStatus("Saved locally.", status = NativeFeedbackStatus.Success)
                NativeInlineStatus(
                    "Couldn't reach the server.",
                    status = NativeFeedbackStatus.Error,
                    filled = false,
                    onDismiss = {},
                )
            }
        }

        Demo("List item with swipe action (no reveal at rest)") {
            NativeListItem(
                "Chapter 18",
                supporting = "Swipe left to mark read",
                trailing = { NativeBadge(contentDescription = "Unread") },
                onClick = {},
                swipeAction = NativeSwipeAction(label = "Mark read", onAction = {}),
            )
        }

        Demo("Badge / Chip / Rating") {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                NativeBadgedBox(badge = { NativeBadge(count = 5) }) {
                    NativeChip("Tag", style = NativeChipStyle.Suggestion)
                }
                NativeRating(4.5f)
            }
        }

        // ---- Native interop controls (the backing + light/dark probe must follow the surface) ----

        Demo("Toggle (interactive + read-only)") {
            var on by remember { mutableStateOf(true) }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                NativeToggle(checked = on, onCheckedChange = { on = it }, contentDescription = "Notifications")
                // null callback = read-only display toggle (Batch 2): full color, non-interactive.
                NativeToggle(checked = true, onCheckedChange = null, contentDescription = "Read-only on")
            }
        }

        Demo("Checkbox + radio group") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                var hd by remember { mutableStateOf(true) }
                NativeCheckbox(checked = hd, onCheckedChange = { hd = it }, label = "Download in HD")
                val sorts = listOf("Latest", "A–Z")
                var sort by remember { mutableStateOf("Latest") }
                NativeRadioGroup(
                    options = sorts,
                    selected = sort,
                    onSelectedChange = { sort = it },
                    style = NativeSelectionStyle.Checkmark,
                )
            }
        }

        Demo("Slider + stepper") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                var v by remember { mutableStateOf(0.4f) }
                NativeSlider(value = v, onValueChange = { v = it }, contentDescription = "Brightness")
                var q by remember { mutableStateOf(2) }
                NativeStepper(value = q, onValueChange = { q = it }, contentDescription = "Quantity")
            }
        }

        Demo("Segmented control") {
            var seg by remember { mutableStateOf(0) }
            NativeSegmentedControl(
                options = listOf("Day", "Week", "Month"),
                selectedIndex = seg,
                onSelectedIndexChange = { seg = it },
                modifier = Modifier.fillMaxWidth(),
                contentDescription = "Period",
            )
        }

        Demo("Search bar") {
            var q by remember { mutableStateOf("") }
            NativeSearchBar(
                value = q,
                onValueChange = { q = it },
                placeholder = "Search…",
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Demo("Page control") {
            var page by remember { mutableStateOf(1) }
            NativePageControl(pageCount = 5, currentPage = page, onCurrentPageChange = { page = it })
        }

        Demo("Color well") {
            var color by remember { mutableStateOf(Color(0xFF1E88E5)) }
            NativeColorWell(color = color, onColorChange = { color = it })
        }

        Demo("Progress (circular + linear)") {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                NativeProgressIndicator(kind = NativeProgressKind.Circular, progress = 0.6f)
                NativeProgressIndicator(kind = NativeProgressKind.Linear, progress = 0.6f, modifier = Modifier.width(120.dp))
            }
        }

        // ---- Compose-on-both components ----

        Demo("Avatar (initials + icon)") {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                NativeAvatar(initials = "JD")
                NativeAvatar(icon = Icons.Outlined.Image, contentDescription = "Placeholder")
            }
        }

        Demo("OTP field") {
            var code by remember { mutableStateOf("12") }
            NativeOtpField(value = code, onValueChange = { code = it }, length = 4, contentDescription = "One-time code")
        }

        Demo("Buttons (label + icon-only)") {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                NativeButton("Action", onClick = {})
                NativeIconButton(NativeIcon(Icons.Default.Add, sfSymbolName = "plus"), onClick = {}, contentDescription = "Add")
            }
        }

        Demo("Empty state") {
            NativeEmptyState(
                title = "Nothing here",
                message = "Add something to see it on this surface.",
                icon = Icons.Outlined.Image,
                actionLabel = "Add",
                onAction = {},
            )
        }
    }
}

/** Renders [content] on the page surface, then again inside a Filled [NativeCard] (publishes `surfaceVariant`). */
@Composable
private fun Demo(name: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        NativeText(name, style = NativeTextStyle.Title)
        NativeText("On page", style = NativeTextStyle.Label)
        content()
        NativeText("In Filled card", style = NativeTextStyle.Label)
        NativeCard(variant = NativeCardVariant.Filled) { content() }
    }
}
