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
 * Collapsing/expanding native leaf controls inside lazy items is the churn pattern the catalog's static
 * pages never exercised: every [AnimatedVisibility] cycle bursts insert/remove/frame mutations through
 * Compose Multiplatform's interop transaction queue, whose actions execute only when the next rendered
 * frame is presented — a frame skipped between retrieval and presentation loses them, and with the kit's
 * overlay placement a lost REMOVAL is a fully visible ghost/doubled control (see
 * `InteropDisposeFailSafe` in the kit and docs/interop-notes.md). This screen auto-cycles so the pattern
 * reproduces hands-free.
 *
 * PASS: rows appear/disappear cleanly, controls stay inside their cards, the count keeps climbing.
 * FAIL: a toggle/slider/segmented lingers after collapse, doubles up, or sits at a stale position.
 *
 * [autoRun] exists for the Android unit test, where an endlessly self-advancing animation clock would
 * never go idle.
 */
@Composable
fun InteropChurnScreen(autoRun: Boolean = true) {
    var running by remember { mutableStateOf(autoRun) }
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
            }
        }
        items(count = 8, key = { it }) { index ->
            AnimatedVisibility(visible = expanded, enter = expandVertically(), exit = shrinkVertically()) {
                NativeCard(variant = NativeCardVariant.Filled, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            NativeText("Row ${index + 1}", style = NativeTextStyle.Label, modifier = Modifier.fillMaxWidth(0.5f))
                            NativeToggle(
                                checked = index % 2 == 0,
                                onCheckedChange = null, // read-only: churn harness, not an input form
                                contentDescription = "Row ${index + 1} toggle",
                            )
                        }
                        NativeSlider(
                            value = level,
                            onValueChange = { level = it },
                            modifier = Modifier.fillMaxWidth(),
                            contentDescription = "Row ${index + 1} level",
                        )
                        NativeSegmentedControl(
                            options = listOf("One", "Two"),
                            selectedIndex = segment,
                            onSelectedIndexChange = { segment = it },
                            modifier = Modifier.fillMaxWidth(),
                            contentDescription = "Row ${index + 1} segments",
                        )
                    }
                }
            }
        }
    }
}
