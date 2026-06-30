package io.github.apdelrahman1911.nativecomposekit.showcase.categories

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.apdelrahman1911.nativecomposekit.components.NativeCheckbox
import io.github.apdelrahman1911.nativecomposekit.components.NativeRadioGroup
import io.github.apdelrahman1911.nativecomposekit.components.NativeRating
import io.github.apdelrahman1911.nativecomposekit.components.NativeSegmentedControl
import io.github.apdelrahman1911.nativecomposekit.components.NativeSelectionStyle
import io.github.apdelrahman1911.nativecomposekit.components.NativeSlider
import io.github.apdelrahman1911.nativecomposekit.components.NativeStepper
import io.github.apdelrahman1911.nativecomposekit.components.NativeText
import io.github.apdelrahman1911.nativecomposekit.components.NativeToggle
import io.github.apdelrahman1911.nativecomposekit.components.feedback.LocalNativeFeedbackController
import io.github.apdelrahman1911.nativecomposekit.components.feedback.NativeFeedbackStatus
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeTextStyle
import io.github.apdelrahman1911.nativecomposekit.showcase.ExampleLabel
import io.github.apdelrahman1911.nativecomposekit.showcase.Note
import io.github.apdelrahman1911.nativecomposekit.showcase.ShowcaseScreen
import io.github.apdelrahman1911.nativecomposekit.showcase.ShowcaseSection
import io.github.apdelrahman1911.nativecomposekit.showcase.WhenToUse

/**
 * Selection controls: ways to pick a value. Each delegates to the most native control where one exists
 * (UISwitch / UISegmentedControl / UISlider / UIStepper on iOS) and falls back to a branded Compose
 * control where the platform has no native equivalent (checkbox, radio group, star rating).
 */
@Composable
fun SelectionShowcase() = ShowcaseScreen(
    intro = "Controls for picking a value — booleans, one-of-many, a continuous amount, a discrete count, " +
        "a star score. Pick by the shape of the value, not by looks: a toggle is one boolean, a segmented " +
        "control is a few fixed options, a slider is continuous, a stepper is a small integer count.",
) {
    ToggleSection()
    CheckboxSection()
    RadioGroupSection()
    SegmentedSection()
    SliderSection()
    StepperSection()
    RatingSection()
}

// ---------------------------------------------------------------------------
// NativeToggle — one boolean per setting row.
// ---------------------------------------------------------------------------

@Composable
private fun ToggleSection() {
    var notifications by remember { mutableStateOf(true) }
    var sounds by remember { mutableStateOf(false) }
    var background by remember { mutableStateOf(true) }

    ShowcaseSection(
        title = "Toggle",
        description = "An on/off switch bound to a boolean. A real UISwitch on iOS, a Material Switch on Android. " +
            "The native control is fixed-size, so a settings row pairs a label on the left with the toggle on the right.",
    ) {
        SettingRow("Push notifications", notifications) { notifications = it }
        SettingRow("In-app sounds", sounds) { sounds = it }
        SettingRow("Background refresh", background) { background = it }

        ExampleLabel("Disabled (locked by a parent setting)")
        SettingRow("Background refresh", false, enabled = false, onChange = {})

        WhenToUse(
            "A single on/off setting: notifications, dark mode, background refresh.",
            "Settings rows where each line is one independent boolean.",
        )
        Note(
            "Reach for a toggle only for a true on/off setting. For choosing several items from a list use a " +
                "checkbox; for one of a few fixed options use a segmented control.",
        )
    }
}

/** A label-left / toggle-right settings row. The native switch sizes itself, so the label takes the slack. */
@Composable
private fun SettingRow(
    label: String,
    checked: Boolean,
    enabled: Boolean = true,
    onChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NativeText(label, style = NativeTextStyle.Body)
        NativeToggle(checked = checked, onCheckedChange = onChange, enabled = enabled)
    }
}

// ---------------------------------------------------------------------------
// NativeCheckbox — opt-in / multi-select, whole row is the tap target.
// ---------------------------------------------------------------------------

@Composable
private fun CheckboxSection() {
    var agreed by remember { mutableStateOf(false) }
    var marketing by remember { mutableStateOf(false) }

    ShowcaseSection(
        title = "Checkbox",
        description = "A labeled checkbox where the whole row is one merged tap target. Use it for an explicit " +
            "opt-in or for multi-select within a list.",
    ) {
        NativeCheckbox(
            checked = agreed,
            onCheckedChange = { agreed = it },
            label = "I agree to the Terms of Service",
        )
        NativeCheckbox(
            checked = marketing,
            onCheckedChange = { marketing = it },
            label = "Send me product updates",
        )

        ExampleLabel("Disabled (requires agreeing to the Terms first)")
        NativeCheckbox(
            checked = false,
            onCheckedChange = null,
            label = "Share anonymous usage data",
            enabled = false,
        )

        WhenToUse(
            "An explicit single opt-in: accepting terms, confirming a consent.",
            "Multi-select within a list — several boxes can be checked at once.",
        )
        Note(
            "Kept cross-platform as a documented exception: iOS has no native checkbox control, so this is a " +
                "branded Compose control on both platforms. For a single on/off setting prefer a toggle " +
                "(a native UISwitch on iOS).",
        )
    }
}

// ---------------------------------------------------------------------------
// NativeRadioGroup — exactly one of a visible list. Generic over the option type.
// ---------------------------------------------------------------------------

private enum class Plan(val label: String) {
    Free("Free — 1 device"),
    Plus("Plus — 5 devices"),
    Family("Family — up to 10 devices"),
}

@Composable
private fun RadioGroupSection() {
    var plan by remember { mutableStateOf(Plan.Plus) }
    var sort by remember { mutableStateOf("Newest") }

    ShowcaseSection(
        title = "Radio group",
        description = "Single-select over a list where every option stays on screen. Generic over the option " +
            "type T (here an enum), with a label mapping each option to its display string.",
    ) {
        ExampleLabel("Subscription plan (Radio style — Android idiom)")
        NativeRadioGroup(
            options = Plan.entries,
            selected = plan,
            onSelectedChange = { plan = it },
            label = { it.label },
        )

        ExampleLabel("Sort order (Checkmark style — iOS grouped-table idiom)")
        NativeRadioGroup(
            options = listOf("Newest", "Oldest", "Title A–Z"),
            selected = sort,
            onSelectedChange = { sort = it },
            style = NativeSelectionStyle.Checkmark,
        )

        WhenToUse(
            "One choice from a small visible set where seeing every option matters.",
            "Settings or forms where the options carry enough text to need their own row.",
        )
        Note(
            "Selection uses ==, so T needs a stable equals (enum, data class, or primitive). On iOS prefer the " +
                "Checkmark style; for a short, fixed option set a segmented control reads as more native.",
        )
    }
}

// ---------------------------------------------------------------------------
// NativeSegmentedControl — one of a few fixed options, inline. Needs a width.
// ---------------------------------------------------------------------------

@Composable
private fun SegmentedSection() {
    val filters = listOf("All", "Unread", "Starred")
    var filter by remember { mutableStateOf(0) }

    ShowcaseSection(
        title = "Segmented control",
        description = "A horizontal single-select across a few fixed options. A real UISegmentedControl on iOS, " +
            "a segmented button row on Android. Common as an inline filter at the top of a list.",
    ) {
        NativeSegmentedControl(
            options = filters,
            selectedIndex = filter,
            onSelectedIndexChange = { filter = it },
            modifier = Modifier.fillMaxWidth(),
            contentDescription = "Message filter",
        )
        NativeText("Showing: ${filters[filter]}", style = NativeTextStyle.Label)

        WhenToUse(
            "Switching between a small, fixed set of options — roughly five or fewer.",
            "An inline filter or view-mode switch that should stay on one line.",
        )
        Note(
            "The iOS control does not expose a reliable intrinsic width through interop, so without a width " +
                "constraint it collapses to about zero width and disappears. Always give it a width — " +
                "Modifier.fillMaxWidth() is the usual choice.",
        )
    }
}

// ---------------------------------------------------------------------------
// NativeSlider — a continuous value with a live read-out.
// ---------------------------------------------------------------------------

@Composable
private fun SliderSection() {
    var volume by remember { mutableStateOf(0.6f) }

    ShowcaseSection(
        title = "Slider",
        description = "A continuous value within a float range. A real UISlider on iOS, a Material Slider on " +
            "Android. Pair it with a live read-out so the current value is always visible.",
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NativeText("Volume", style = NativeTextStyle.Body)
            NativeText("${(volume * 100).toInt()}%", style = NativeTextStyle.Label)
        }
        NativeSlider(
            value = volume,
            onValueChange = { volume = it },
            modifier = Modifier.fillMaxWidth(),
            contentDescription = "Volume",
        )

        WhenToUse(
            "Adjusting a continuous quantity: volume, brightness, reading position.",
            "Coarse, approximate input where the exact number is not critical.",
        )
        Note(
            "valueRange defaults to 0f..1f. For a discrete integer count where the exact value matters, use a " +
                "stepper instead.",
        )
    }
}

// ---------------------------------------------------------------------------
// NativeStepper — a small bounded integer count.
// ---------------------------------------------------------------------------

@Composable
private fun StepperSection() {
    var quantity by remember { mutableStateOf(1) }
    val feedback = LocalNativeFeedbackController.current

    ShowcaseSection(
        title = "Stepper",
        description = "An integer adjusted up or down by a fixed step within bounds. A real UIStepper on iOS, " +
            "a -/+ row on Android. Best for a small count where the exact value matters.",
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NativeText("Quantity: $quantity", style = NativeTextStyle.Body)
            NativeStepper(
                value = quantity,
                onValueChange = {
                    quantity = it
                    if (it == 10) feedback.toast("Max 10 per order", status = NativeFeedbackStatus.Warning)
                },
                min = 1,
                max = 10,
                contentDescription = "Quantity",
            )
        }

        WhenToUse(
            "A small integer count where the exact value matters: quantity, copies, guests.",
            "Bounded input — min/max keep the value in range without extra validation.",
        )
        Note("Fixed-size native control, so no width constraint is needed. For a continuous range, use a slider.")
    }
}

// ---------------------------------------------------------------------------
// NativeRating — display a score, or capture one.
// ---------------------------------------------------------------------------

@Composable
private fun RatingSection() {
    var stars by remember { mutableStateOf(0f) }

    ShowcaseSection(
        title = "Rating",
        description = "A star rating: read-only for displaying a score, or interactive when given a change " +
            "callback. Compose-drawn on both platforms — neither has a native star control.",
    ) {
        ExampleLabel("Read-only — average score (half stars allowed)")
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NativeRating(4.5f)
            NativeText("4.5 out of 5", style = NativeTextStyle.Label)
        }

        ExampleLabel("Interactive — tap to rate (whole stars only)")
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NativeRating(stars, onRatingChange = { stars = it })
            NativeText(
                if (stars == 0f) "Tap a star" else "You rated ${stars.toInt()}",
                style = NativeTextStyle.Label,
            )
        }

        ExampleLabel("Disabled — dimmed and read-only")
        NativeRating(3f, onRatingChange = { stars = it }, enabled = false)

        WhenToUse(
            "Showing an average or captured score from 0 to a max number of stars.",
            "Capturing a quick subjective rating from the user.",
        )
        Note(
            "Half-star glyphs are display-only: an interactive control only sets whole stars, so it never shows " +
                "a half it cannot produce. Disabled forces read-only regardless of onRatingChange.",
        )
    }
}
