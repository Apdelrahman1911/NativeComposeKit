package io.github.apdelrahman1911.nativecomposekit.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.apdelrahman1911.nativecomposekit.components.NativeCard
import io.github.apdelrahman1911.nativecomposekit.components.NativeCardVariant
import io.github.apdelrahman1911.nativecomposekit.components.NativeCollapsible
import io.github.apdelrahman1911.nativecomposekit.components.NativeListItem
import io.github.apdelrahman1911.nativecomposekit.components.NativeSegmentedControl
import io.github.apdelrahman1911.nativecomposekit.components.NativeSlider
import io.github.apdelrahman1911.nativecomposekit.components.NativeText
import io.github.apdelrahman1911.nativecomposekit.components.NativeToggle
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeTextStyle
import kotlinx.coroutines.delay

/**
 * Debug screen: interop **churn** regression harness (primarily iOS).
 *
 * Cycling native leaf controls in/out of lazy items stresses Compose Multiplatform's interop
 * transaction queue, whose actions execute only when a rendered frame is presented. Animating the
 * controls' VISIBILITY (`AnimatedVisibility`) makes the UIKit side fall visibly out of sync on real
 * devices — controls lag their collapsing row, draw outside their container, appear/disappear late,
 * and under continuous churn the backlog stops draining (see docs/interop-notes.md §4 and the
 * upstream report in docs/upstream/). That pathological flavor is kept behind the off-by-default
 * "Reproduce the wedge" toggle. The default rows exercise the SAFE patterns: [NativeCollapsible]
 * (animated container size, one-step gating, Compose-rendered text inside — the bare [NativeText]
 * in each row regression-proves that mode) and a plain `if` gate.
 *
 * PASS: rows appear/disappear cleanly and stay inside their cards; the count keeps climbing.
 * FAIL: a control lingers after collapse, doubles up, lags visibly, or sits at a stale position.
 *
 * [autoRun] exists for the Android unit test, where an endlessly self-advancing animation clock would
 * never go idle.
 */
@Composable
fun InteropChurnScreen(autoRun: Boolean = true) {
    var running by remember { mutableStateOf(autoRun) }
    var reproduceWedge by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(true) }
    var cycles by remember { mutableIntStateOf(0) }
    var level by remember { mutableStateOf(0.4f) }
    var segment by remember { mutableIntStateOf(0) }
    LaunchedEffect(running) {
        while (running) {
            delay(650)
            expanded = !expanded
            if (expanded) cycles++
        }
    }
    val bottomInset = LocalNativeContentBottomInset.current
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 16.dp + bottomInset),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "header") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DemoNote(
                    "Auto-collapsing rows of native controls stress the interop insert/remove/position " +
                        "queue. PASS: rows appear and disappear cleanly. FAIL: a control lingers as a " +
                        "ghost after collapse, doubles up, or sits at a stale position.",
                )
                NativeListItem(
                    "Auto-cycle",
                    supporting = "Completed cycles: $cycles",
                    trailing = {
                        NativeToggle(
                            checked = running,
                            onCheckedChange = { running = it },
                            contentDescription = "Auto-cycle",
                        )
                    },
                )
                NativeListItem(
                    "Reproduce the wedge",
                    supporting = "Wraps the first rows in AnimatedVisibility — WEDGES the iOS device interop queue (the documented CMP defect); relaunch to recover",
                    trailing = {
                        NativeToggle(
                            checked = reproduceWedge,
                            onCheckedChange = { reproduceWedge = it },
                            contentDescription = "Reproduce the wedge",
                        )
                    },
                )
            }
        }
        // Three churn flavors: NativeCollapsible (the kit's safe animated collapse — the pattern apps
        // should use), a plain `if` gate (instant, always safe), and — behind the header toggle — the
        // AnimatedVisibility killer that wedges the iOS device interop queue (kept for reproduction).
        items(count = 8, key = { it }) { index ->
            when {
                reproduceWedge && index < 2 -> {
                    AnimatedVisibility(visible = expanded, enter = expandVertically(), exit = shrinkVertically()) {
                        ChurnRow(index = index, flavor = "animated", level = level, onLevel = { level = it }, segment = segment, onSegment = { segment = it })
                    }
                }
                index < 4 -> {
                    NativeCollapsible(visible = expanded) {
                        ChurnRow(index = index, flavor = "collapsible", level = level, onLevel = { level = it }, segment = segment, onSegment = { segment = it })
                    }
                }
                expanded -> {
                    ChurnRow(index = index, flavor = "gated", level = level, onLevel = { level = it }, segment = segment, onSegment = { segment = it })
                }
            }
        }
    }
}

@Composable
private fun ChurnRow(
    index: Int,
    flavor: String,
    level: Float,
    onLevel: (Float) -> Unit,
    segment: Int,
    onSegment: (Int) -> Unit,
) {
    NativeCard(variant = NativeCardVariant.Filled, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Bare NativeText ON PURPOSE: inside NativeCollapsible it must render through Compose
                // (no interop cut-out), so a dark flash here would be a regression of that mode.
                NativeText(
                    "Row ${index + 1} · $flavor",
                    style = NativeTextStyle.Label,
                    modifier = Modifier.fillMaxWidth(0.5f),
                )
                NativeToggle(
                    checked = index % 2 == 0,
                    onCheckedChange = null, // read-only: churn harness, not an input form
                    contentDescription = "Row ${index + 1} toggle",
                )
            }
            NativeSlider(
                value = level,
                onValueChange = onLevel,
                modifier = Modifier.fillMaxWidth(),
                contentDescription = "Row ${index + 1} level",
            )
            NativeSegmentedControl(
                options = listOf("One", "Two"),
                selectedIndex = segment,
                onSelectedIndexChange = onSegment,
                modifier = Modifier.fillMaxWidth(),
                contentDescription = "Row ${index + 1} segments",
            )
        }
    }
}
