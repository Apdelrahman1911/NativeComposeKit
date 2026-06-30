package com.ukkera.brandkit.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ukkera.brandkit.components.BrandListItem
import com.ukkera.brandkit.components.BrandListSection
import com.ukkera.brandkit.components.BrandText
import com.ukkera.brandkit.components.BrandToggle
import com.ukkera.brandkit.components.model.BrandTextStyle

/**
 * Shared content screens (Tier 2) — plain Compose using `Brand*` components, navigator-agnostic (they take
 * callbacks; the graph wires those to `navigator.push(...)`). The Library / manga-detail / reader screens
 * live in `MangaScreens.kt`; this file holds the remaining simple tab screens.
 */

@Composable
fun SettingsScreen(onOpenComponentMatrix: () -> Unit = {}, onOpenInteropRepro: () -> Unit = {}) {
    var notify by remember { mutableStateOf(true) }
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        BrandText("Settings — Compose content in a native tab.", style = BrandTextStyle.Label)
        BrandListSection(
            header = "Reader",
            rows = listOf(
                {
                    BrandListItem(
                        "New-chapter alerts",
                        trailing = { BrandToggle(checked = notify, onCheckedChange = { notify = it }) },
                    )
                },
                { BrandListItem("Reading direction", trailingText = "L → R", onClick = {}) },
            ),
        )
        BrandListSection(
            header = "Developer",
            rows = listOf(
                {
                    BrandListItem(
                        "Component surface matrix",
                        supporting = "Regression harness: components on the page vs inside a Filled card",
                        onClick = onOpenComponentMatrix,
                    )
                },
                {
                    BrandListItem(
                        "iOS interop repro",
                        supporting = "Raw UIKitView scroll clip/drift, UIMenu drift, Dialog first-frame (iOS-only)",
                        onClick = onOpenInteropRepro,
                    )
                },
            ),
        )
    }
}
