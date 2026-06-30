package com.ukkera.brandkit.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import androidx.compose.ui.window.Dialog
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGRectMake
import platform.UIKit.UIAction
import platform.UIKit.UIButton
import platform.UIKit.UIColor
import platform.UIKit.UIControlStateNormal
import platform.UIKit.UIMenu
import platform.UIKit.UISlider
import platform.UIKit.UIStepper
import platform.UIKit.UISwitch

private enum class Repro { Scroll, Menu, Dialog }

/**
 * iOS-only manual reproductions for the three confirmed `UIKitView` interop limitations (see
 * `docs/interop-notes.md`). Deliberately uses RAW `UIKitView` (not the Brand* wrappers) so each behavior is
 * isolated from the kit's own workarounds and matches the upstream issue snippets.
 */
@Composable
actual fun InteropReproScreen() {
    var which by remember { mutableStateOf(Repro.Scroll) }
    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(onClick = { which = Repro.Scroll }) { Text("1 · Scroll") }
            Button(onClick = { which = Repro.Menu }) { Text("2 · Menu") }
            Button(onClick = { which = Repro.Dialog }) { Text("3 · Dialog") }
        }
        when (which) {
            Repro.Scroll -> ScrollClipVsDriftRepro()
            Repro.Menu -> MenuDriftRepro()
            Repro.Dialog -> DialogFirstFrameRepro()
        }
    }
}

/** Issue 1 — scroll fast and compare cut-out (clips) vs overlay (drifts). */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun ScrollClipVsDriftRepro() {
    var overlay by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("placedAsOverlay = $overlay", modifier = Modifier.weight(1f))
            Switch(checked = overlay, onCheckedChange = { overlay = it })
        }
        Text(
            "Scroll fast. false → the control's edge clips against the green row; " +
                "true → the control lags/drifts, then snaps back when scrolling stops.",
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
        )
        // Re-mount the interop views when the placement flips so the new mode takes effect cleanly.
        key(overlay) {
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                repeat(40) { i ->
                    Row(
                        Modifier.fillMaxWidth().height(56.dp)
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .background(Color(0xFF2E7D32)), // green backing makes the edge obvious
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        UIKitView(
                            factory = {
                                when (i % 3) {
                                    0 -> UIStepper()
                                    1 -> UISwitch()
                                    else -> UISlider()
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(32.dp),
                            properties = UIKitInteropProperties(placedAsOverlay = overlay),
                        )
                    }
                }
            }
        }
    }
}

/** Issue 2 — scroll (clean), open a menu once, dismiss, scroll again → the button drifts. */
@OptIn(ExperimentalComposeUiApi::class, ExperimentalForeignApi::class, BetaInteropApi::class)
@Composable
private fun MenuDriftRepro() {
    Column(Modifier.fillMaxSize()) {
        Text(
            "Scroll (clean). Tap a button to open its native UIMenu, dismiss it, then scroll again — " +
                "the button now drifts from its row on every subsequent scroll.",
            modifier = Modifier.padding(12.dp),
        )
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            repeat(40) { i ->
                Row(
                    Modifier.fillMaxWidth().height(52.dp)
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .background(Color(0xFF1565C0)),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    UIKitView(
                        factory = {
                            UIButton(frame = CGRectMake(0.0, 0.0, 0.0, 0.0)).apply {
                                setTitle("Item $i  ▾", forState = UIControlStateNormal)
                                setTitleColor(UIColor.whiteColor(), forState = UIControlStateNormal)
                                val edit = UIAction.actionWithTitle(
                                    title = "Edit", image = null, identifier = null, handler = { _ -> },
                                )
                                val delete = UIAction.actionWithTitle(
                                    title = "Delete", image = null, identifier = null, handler = { _ -> },
                                )
                                menu = UIMenu.menuWithTitle("", listOf(edit, delete))
                                showsMenuAsPrimaryAction = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(40.dp),
                        properties = UIKitInteropProperties(placedAsOverlay = false), // cut-out (default)
                    )
                }
            }
        }
    }
}

/** Issue 3 — open in DARK mode and watch for a black flash behind the switch on the first frame. */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun DialogFirstFrameRepro() {
    var open by remember { mutableStateOf(false) }
    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            "Switch the device/simulator to DARK mode, then open the dialog and watch the area where the " +
                "native switch appears — it briefly shows the black system backdrop before the switch paints.",
        )
        Button(onClick = { open = true }) { Text("Open dialog") }
    }
    if (open) {
        Dialog(onDismissRequest = { open = false }) {
            // Opaque, Compose-drawn dialog card (light, so the black first-frame flash is obvious in dark mode).
            Surface(color = Color(0xFFF2F2F7)) {
                Column(
                    Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text("A native UISwitch in a freshly-mounted dialog scene:", color = Color.Black)
                    UIKitView(
                        factory = { UISwitch() },
                        modifier = Modifier.height(32.dp).width(64.dp),
                        properties = UIKitInteropProperties(placedAsOverlay = false), // cut-out (default)
                    )
                }
            }
        }
    }
}
