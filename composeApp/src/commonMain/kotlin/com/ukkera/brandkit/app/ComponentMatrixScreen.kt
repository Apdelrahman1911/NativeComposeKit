package com.ukkera.brandkit.app

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
import com.ukkera.brandkit.components.BrandAvatar
import com.ukkera.brandkit.components.BrandBadge
import com.ukkera.brandkit.components.BrandBadgedBox
import com.ukkera.brandkit.components.BrandButton
import com.ukkera.brandkit.components.BrandCard
import com.ukkera.brandkit.components.BrandCardVariant
import com.ukkera.brandkit.components.BrandCheckbox
import com.ukkera.brandkit.components.BrandChip
import com.ukkera.brandkit.components.BrandChipStyle
import com.ukkera.brandkit.components.BrandColorWell
import com.ukkera.brandkit.components.BrandEmptyState
import com.ukkera.brandkit.components.BrandIconButton
import com.ukkera.brandkit.components.BrandListItem
import com.ukkera.brandkit.components.BrandOtpField
import com.ukkera.brandkit.components.BrandPageControl
import com.ukkera.brandkit.components.BrandProgressIndicator
import com.ukkera.brandkit.components.BrandProgressKind
import com.ukkera.brandkit.components.BrandRadioGroup
import com.ukkera.brandkit.components.BrandRating
import com.ukkera.brandkit.components.BrandSearchBar
import com.ukkera.brandkit.components.BrandSegmentedControl
import com.ukkera.brandkit.components.BrandSelectionStyle
import com.ukkera.brandkit.components.BrandSkeleton
import com.ukkera.brandkit.components.BrandSlider
import com.ukkera.brandkit.components.BrandStepper
import com.ukkera.brandkit.components.BrandSwipeAction
import com.ukkera.brandkit.components.BrandText
import com.ukkera.brandkit.components.BrandToggle
import com.ukkera.brandkit.components.feedback.BrandFeedbackStatus
import com.ukkera.brandkit.components.feedback.BrandInlineStatus
import com.ukkera.brandkit.components.model.BrandIcon
import com.ukkera.brandkit.components.model.BrandTextStyle

/**
 * Debug regression harness for the **surface-adaptation** bug class (the `BrandListItem` swipe reveal, the
 * invisible-skeleton-on-a-card, the native-control backing/light-dark probe, etc.): it renders each
 * surface-dependent component **twice — directly on the page surface and inside a Filled [BrandCard]** (which
 * publishes `surfaceVariant`). A component that hardcodes a surface color instead of reading
 * `LocalBrandSurface` will visibly differ (or vanish) between the two columns. Pushed from Settings →
 * "Component surface matrix"; debug-only, not part of the manga flow. The bare-on-glass native-control matrix
 * is covered separately by [GlassInteropTestScreen].
 *
 * Coverage: every surface-sensitive component is here. `BrandDatePicker` is intentionally omitted — its
 * Android renderer is a full inline calendar (rendering it twice makes the screen unusable) and its
 * surface/light-dark probe is iOS-only; it is exercised in the catalog + [GlassInteropTestScreen] instead.
 */
@Composable
fun ComponentMatrixScreen() {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        BrandText(
            "Each component is shown on the page surface, then inside a Filled card. " +
                "They should look consistent (and visible) in both.",
            style = BrandTextStyle.Label,
        )

        Demo("Skeleton (must stay visible on the card)") {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                BrandSkeleton(Modifier.size(64.dp, 96.dp))
                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BrandSkeleton(Modifier.fillMaxWidth().height(16.dp))
                    BrandSkeleton(Modifier.fillMaxWidth(0.6f).height(14.dp))
                }
            }
        }

        Demo("Inline status (filled + outlined)") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                BrandInlineStatus("Saved locally.", status = BrandFeedbackStatus.Success)
                BrandInlineStatus(
                    "Couldn't reach the server.",
                    status = BrandFeedbackStatus.Error,
                    filled = false,
                    onDismiss = {},
                )
            }
        }

        Demo("List item with swipe action (no reveal at rest)") {
            BrandListItem(
                "Chapter 18",
                supporting = "Swipe left to mark read",
                trailing = { BrandBadge(contentDescription = "Unread") },
                onClick = {},
                swipeAction = BrandSwipeAction(label = "Mark read", onAction = {}),
            )
        }

        Demo("Badge / Chip / Rating") {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                BrandBadgedBox(badge = { BrandBadge(count = 5) }) {
                    BrandChip("Tag", style = BrandChipStyle.Suggestion)
                }
                BrandRating(4.5f)
            }
        }

        // ---- Native interop controls (the backing + light/dark probe must follow the surface) ----

        Demo("Toggle (interactive + read-only)") {
            var on by remember { mutableStateOf(true) }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                BrandToggle(checked = on, onCheckedChange = { on = it }, contentDescription = "Notifications")
                // null callback = read-only display toggle (Batch 2): full color, non-interactive.
                BrandToggle(checked = true, onCheckedChange = null, contentDescription = "Read-only on")
            }
        }

        Demo("Checkbox + radio group") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                var hd by remember { mutableStateOf(true) }
                BrandCheckbox(checked = hd, onCheckedChange = { hd = it }, label = "Download in HD")
                val sorts = listOf("Latest", "A–Z")
                var sort by remember { mutableStateOf("Latest") }
                BrandRadioGroup(
                    options = sorts,
                    selected = sort,
                    onSelectedChange = { sort = it },
                    style = BrandSelectionStyle.Checkmark,
                )
            }
        }

        Demo("Slider + stepper") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                var v by remember { mutableStateOf(0.4f) }
                BrandSlider(value = v, onValueChange = { v = it }, contentDescription = "Brightness")
                var q by remember { mutableStateOf(2) }
                BrandStepper(value = q, onValueChange = { q = it }, contentDescription = "Quantity")
            }
        }

        Demo("Segmented control") {
            var seg by remember { mutableStateOf(0) }
            BrandSegmentedControl(
                options = listOf("Day", "Week", "Month"),
                selectedIndex = seg,
                onSelectedIndexChange = { seg = it },
                modifier = Modifier.fillMaxWidth(),
                contentDescription = "Period",
            )
        }

        Demo("Search bar") {
            var q by remember { mutableStateOf("") }
            BrandSearchBar(
                value = q,
                onValueChange = { q = it },
                placeholder = "Search…",
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Demo("Page control") {
            var page by remember { mutableStateOf(1) }
            BrandPageControl(pageCount = 5, currentPage = page, onCurrentPageChange = { page = it })
        }

        Demo("Color well") {
            var color by remember { mutableStateOf(Color(0xFF1E88E5)) }
            BrandColorWell(color = color, onColorChange = { color = it })
        }

        Demo("Progress (circular + linear)") {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                BrandProgressIndicator(kind = BrandProgressKind.Circular, progress = 0.6f)
                BrandProgressIndicator(kind = BrandProgressKind.Linear, progress = 0.6f, modifier = Modifier.width(120.dp))
            }
        }

        // ---- Compose-on-both components ----

        Demo("Avatar (initials + icon)") {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                BrandAvatar(initials = "JD")
                BrandAvatar(icon = Icons.Outlined.Image, contentDescription = "Placeholder")
            }
        }

        Demo("OTP field") {
            var code by remember { mutableStateOf("12") }
            BrandOtpField(value = code, onValueChange = { code = it }, length = 4, contentDescription = "One-time code")
        }

        Demo("Buttons (label + icon-only)") {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                BrandButton("Action", onClick = {})
                BrandIconButton(BrandIcon(Icons.Default.Add, sfSymbolName = "plus"), onClick = {}, contentDescription = "Add")
            }
        }

        Demo("Empty state") {
            BrandEmptyState(
                title = "Nothing here",
                message = "Add something to see it on this surface.",
                icon = Icons.Outlined.Image,
                actionLabel = "Add",
                onAction = {},
            )
        }
    }
}

/** Renders [content] on the page surface, then again inside a Filled [BrandCard] (publishes `surfaceVariant`). */
@Composable
private fun Demo(name: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        BrandText(name, style = BrandTextStyle.Title)
        BrandText("On page", style = BrandTextStyle.Label)
        content()
        BrandText("In Filled card", style = BrandTextStyle.Label)
        BrandCard(variant = BrandCardVariant.Filled) { content() }
    }
}
