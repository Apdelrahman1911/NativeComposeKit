package io.github.apdelrahman1911.nativecomposekit.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.apdelrahman1911.nativecomposekit.components.NativeButton
import io.github.apdelrahman1911.nativecomposekit.components.NativeCard
import io.github.apdelrahman1911.nativecomposekit.components.NativeCardVariant
import io.github.apdelrahman1911.nativecomposekit.components.NativeColorWell
import io.github.apdelrahman1911.nativecomposekit.components.NativeDatePicker
import io.github.apdelrahman1911.nativecomposekit.components.NativePageControl
import io.github.apdelrahman1911.nativecomposekit.components.NativeProgressIndicator
import io.github.apdelrahman1911.nativecomposekit.components.NativeProgressKind
import io.github.apdelrahman1911.nativecomposekit.components.NativeSegmentedControl
import io.github.apdelrahman1911.nativecomposekit.components.NativeSlider
import io.github.apdelrahman1911.nativecomposekit.components.NativeStepper
import io.github.apdelrahman1911.nativecomposekit.components.NativeText
import io.github.apdelrahman1911.nativecomposekit.components.NativeToggle
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeTextStyle

/** Independent state for one copy of the interop test controls (so the bare and solid copies don't mirror). */
private class GlassTestState {
    var toggle by mutableStateOf(true)
    var slider by mutableStateOf(0.5f)
    var stepper by mutableStateOf(2)
    var seg by mutableStateOf(0)
    var page by mutableStateOf(0)
    var color by mutableStateOf(Color(0xFF2DD4BF))
    var date by mutableStateOf<Long?>(null)
}

/**
 * **DEBUG stress test** (no production defaults change here). Renders UIKit-backed controls **directly on the
 * surface with no published `LocalNativeSurface`** (the bare path — on iOS this is a Liquid Glass sheet) and
 * again **inside a `NativeCard`** (which publishes its `surfaceVariant`). Compare the two: if the bare controls
 * are clean, bare-on-glass is allowed; if they show black/white rectangles, the rule is definitive — UIKit
 * controls must live in a solid surface. See `docs/interop-backdrop-audit.md`.
 */
@Composable
fun GlassInteropTestScreen() {
    val bare = remember { GlassTestState() }
    val solid = remember { GlassTestState() }
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        NativeText("Bare native controls on Liquid Glass — interop test", style = NativeTextStyle.Title)

        NativeText("Bare glass path: no solid surface published", style = NativeTextStyle.Label)
        InteropTestControls(bare)

        NativeText("Solid surface path: LocalNativeSurface published", style = NativeTextStyle.Label)
        NativeCard(variant = NativeCardVariant.Filled) { InteropTestControls(solid) }
    }
}

@Composable
private fun InteropTestControls(s: GlassTestState) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        LabeledRow("Toggle") { NativeToggle(checked = s.toggle, onCheckedChange = { s.toggle = it }) }
        NativeText("Slider", style = NativeTextStyle.Label)
        NativeSlider(value = s.slider, onValueChange = { s.slider = it }, modifier = Modifier.fillMaxWidth())
        LabeledRow("Stepper") { NativeStepper(value = s.stepper, onValueChange = { s.stepper = it }, min = 0, max = 10) }
        NativeText("Segmented", style = NativeTextStyle.Label)
        // fillMaxWidth: the iOS UISegmentedControl is a content-sized UIKitView and collapses to ~0 width without
        // an external width constraint (unlike the fixed-size Toggle/Stepper). Matches the Library-filter usage.
        NativeSegmentedControl(
            options = listOf("One", "Two", "Three"),
            selectedIndex = s.seg,
            onSelectedIndexChange = { s.seg = it },
            modifier = Modifier.fillMaxWidth(),
        )
        NativeText("Page dots (tap a dot, or Next)", style = NativeTextStyle.Label)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // weight(1f): the iOS UIPageControl is content-sized and collapses to ~0 width without a width
            // constraint; give it the row's free space so its dots render (the Next button keeps its size).
            NativePageControl(
                pageCount = 5,
                currentPage = s.page,
                onCurrentPageChange = { s.page = it },
                modifier = Modifier.weight(1f),
            )
            NativeButton(text = "Next", onClick = { s.page = (s.page + 1) % 5 })
        }
        LabeledRow("Spinner") { NativeProgressIndicator(kind = NativeProgressKind.Circular) }
        NativeText("Linear bar", style = NativeTextStyle.Label)
        NativeProgressIndicator(kind = NativeProgressKind.Linear, progress = s.slider, modifier = Modifier.fillMaxWidth())
        LabeledRow("Color well") { NativeColorWell(color = s.color, onColorChange = { s.color = it }) }
        LabeledRow("Date") { NativeDatePicker(selectedMillis = s.date, onSelectedMillisChange = { s.date = it }) }
    }
}

@Composable
private fun LabeledRow(label: String, control: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NativeText(label, style = NativeTextStyle.Label)
        control()
    }
}
