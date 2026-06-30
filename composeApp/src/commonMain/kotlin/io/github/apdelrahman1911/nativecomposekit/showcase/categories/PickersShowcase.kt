package io.github.apdelrahman1911.nativecomposekit.showcase.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.apdelrahman1911.nativecomposekit.components.NativeButton
import io.github.apdelrahman1911.nativecomposekit.components.NativeCard
import io.github.apdelrahman1911.nativecomposekit.components.NativeCardVariant
import io.github.apdelrahman1911.nativecomposekit.components.NativeColorWell
import io.github.apdelrahman1911.nativecomposekit.components.NativeDatePicker
import io.github.apdelrahman1911.nativecomposekit.components.NativePageControl
import io.github.apdelrahman1911.nativecomposekit.components.NativeText
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeButtonColors
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeButtonSize
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeButtonVariant
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeTextStyle
import io.github.apdelrahman1911.nativecomposekit.showcase.ExampleLabel
import io.github.apdelrahman1911.nativecomposekit.showcase.Note
import io.github.apdelrahman1911.nativecomposekit.showcase.ShowcaseScreen
import io.github.apdelrahman1911.nativecomposekit.showcase.ShowcaseSection
import io.github.apdelrahman1911.nativecomposekit.showcase.WhenToUse

private val onboardingPages = listOf("Track what you read", "Sync across devices", "Get release alerts")

@Composable
fun PickersShowcase() = ShowcaseScreen(
    intro = "Controls for picking a value — a calendar date, a color, or the active page in a pager. " +
        "Each renders the most native control per platform: UIDatePicker / UIColorWell / UIPageControl on " +
        "iOS, Material and branded Compose equivalents on Android.",
) {
    DatePickerSection()
    ColorWellSection()
    PageControlSection()
}

// ---------- NativeDatePicker ----------

@Composable
private fun DatePickerSection() {
    ShowcaseSection(
        title = "Date picker",
        description = "A single calendar date. The native iOS compact field expands into the system " +
            "calendar; Android shows the Material inline calendar.",
    ) {
        WhenToUse(
            "You need one calendar date and want the native picker on each platform.",
            "A booking, reminder, or filter form that captures a day.",
        )

        // A settings/form row: a labelled field whose trailing control is the picker, with the
        // chosen date echoed back below so the selection is always visible.
        var dueMillis by remember { mutableStateOf<Long?>(null) }
        ExampleLabel("In a form row")
        NativeCard(variant = NativeCardVariant.Outlined) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                NativeText("Due date", style = NativeTextStyle.Body)
                NativeDatePicker(
                    selectedMillis = dueMillis,
                    onSelectedMillisChange = { dueMillis = it },
                    contentDescription = "Due date",
                )
            }
            Spacer(Modifier.size(8.dp))
            NativeText(
                text = dueMillis?.let { "Selected: $it (UTC epoch ms)" } ?: "No date selected",
                style = NativeTextStyle.Label,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        ExampleLabel("Disabled")
        NativeDatePicker(
            selectedMillis = dueMillis,
            onSelectedMillisChange = { dueMillis = it },
            enabled = false,
        )

        Note(
            "selectedMillis is UTC epoch milliseconds at the start of the day — what Material's " +
                "DatePickerState emits and what the iOS renderer mirrors. Convert to the device zone at the " +
                "display layer; don't assume the value is local midnight. On Android the picker is " +
                "effectively uncontrolled after first composition.",
        )
    }
}

// ---------- NativeColorWell ----------

@Composable
private fun ColorWellSection() {
    ShowcaseSection(
        title = "Color well",
        description = "Pick a color. iOS opens the system UIColorWell (full spectrum, eyedropper, opacity); " +
            "Android opens a preset palette dialog.",
    ) {
        WhenToUse(
            "Theming or personalization — an accent, tag, or highlight color.",
            "You want the real iOS color picker and accept a preset palette on Android.",
        )

        var accent by remember { mutableStateOf(Color(0xFF1E88E5)) }
        ExampleLabel("Accent color")
        NativeCard(variant = NativeCardVariant.Outlined) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    NativeText("Highlight", style = NativeTextStyle.Body)
                    NativeText(
                        "Tap the well to change",
                        style = NativeTextStyle.Label,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // A swatch of the current selection, so the chosen color reads at a glance next to the well.
                    Swatch(accent)
                    NativeColorWell(
                        color = accent,
                        onColorChange = { accent = it },
                        contentDescription = "Accent color",
                    )
                }
            }
        }

        // A live preview that the picked color actually drives.
        ExampleLabel("Applied")
        NativeButton(
            text = "Save changes",
            onClick = {},
            colorsOverride = NativeButtonColors(container = accent, content = Color.White),
            fullWidth = true,
        )

        Note(
            "iOS has a real system color picker; Android does not, so the Android side opens a fixed " +
                "preset palette (opaque swatches). ios.supportsAlpha only applies on iOS.",
        )
    }
}

@Composable
private fun Swatch(color: Color) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(color),
    )
}

// ---------- NativePageControl ----------

@Composable
private fun PageControlSection() {
    ShowcaseSection(
        title = "Page control",
        description = "The row of dots for a carousel or onboarding flow. The pager owns the page; the " +
            "control indicates it (and can drive it via tap on iOS).",
    ) {
        WhenToUse(
            "A carousel, featured banner, or onboarding flow paired with a pager.",
            "You want an indicator, not the primary paging control.",
        )

        var page by remember { mutableStateOf(0) }
        val pageCount = onboardingPages.size

        ExampleLabel("Synced to a counter + Next button")
        NativeCard(variant = NativeCardVariant.Outlined) {
            // The "page" the dots track. In a real screen this would be a HorizontalPager.
            NativeText(onboardingPages[page], style = NativeTextStyle.Title)
            NativeText(
                "Step ${page + 1} of $pageCount",
                style = NativeTextStyle.Label,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.size(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // weight(1f) gives the control a width — it collapses to zero otherwise on iOS.
                NativePageControl(
                    pageCount = pageCount,
                    currentPage = page,
                    onCurrentPageChange = { page = it },
                    modifier = Modifier.weight(1f),
                )
                NativeButton(
                    text = if (page == pageCount - 1) "Done" else "Next",
                    onClick = { page = (page + 1) % pageCount },
                    variant = NativeButtonVariant.Tertiary,
                    size = NativeButtonSize.Small,
                )
            }
        }

        Note(
            "Give it a width — Modifier.weight(1f) in a Row or fillMaxWidth — or the iOS UIPageControl " +
                "collapses to ~0 width and becomes invisible. Pass onCurrentPageChange to allow tap-to-page " +
                "(matches UIPageControl); leave it null for a display-only indicator. iOS hides the dots for a " +
                "single page.",
        )
    }
}
