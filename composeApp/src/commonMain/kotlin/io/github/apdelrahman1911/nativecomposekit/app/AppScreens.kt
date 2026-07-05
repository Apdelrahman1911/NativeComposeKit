package io.github.apdelrahman1911.nativecomposekit.app

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
import io.github.apdelrahman1911.nativecomposekit.components.NativeListItem
import io.github.apdelrahman1911.nativecomposekit.components.NativeListSection
import io.github.apdelrahman1911.nativecomposekit.components.NativeText
import io.github.apdelrahman1911.nativecomposekit.components.NativeToggle
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeTextStyle

/**
 * Shared content screens (Tier 2) — plain Compose using `Native*` components, navigator-agnostic (they take
 * callbacks; the graph wires those to `navigator.push(...)`). The Library / manga-detail / reader screens
 * live in `MangaScreens.kt`; this file holds the remaining simple tab screens.
 */

@Composable
fun SettingsScreen(
    onOpenComponentMatrix: () -> Unit = {},
    onOpenInteropRepro: () -> Unit = {},
    onOpenInteropChurn: () -> Unit = {},
    onOpenLiquidGlass: () -> Unit = {},
    onOpenChromeDemo: () -> Unit = {},
    onOpenToolbarStyles: () -> Unit = {},
) {
    var notify by remember { mutableStateOf(true) }
    // Fill to the bottom behind the overlaying tab bar; end content clear of it (0 on Android).
    val bottomInset = LocalNativeContentBottomInset.current
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 16.dp + bottomInset),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        NativeText("Settings — Compose content in a native tab.", style = NativeTextStyle.Label)
        NativeListSection(
            header = "Reader",
            rows = listOf(
                {
                    NativeListItem(
                        "New-chapter alerts",
                        trailing = { NativeToggle(checked = notify, onCheckedChange = { notify = it }) },
                    )
                },
                { NativeListItem("Reading direction", trailingText = "L → R") }, // display-only value row
            ),
        )
        if (AppDevTools.enabled) NativeListSection(
            header = "Developer",
            rows = listOf(
                {
                    NativeListItem(
                        "Component surface matrix",
                        supporting = "Regression harness: components on the page vs inside a Filled card",
                        onClick = onOpenComponentMatrix,
                    )
                },
                {
                    NativeListItem(
                        "iOS interop repro",
                        supporting = "Raw UIKitView scroll clip/drift, UIMenu drift, Dialog first-frame (iOS-only)",
                        onClick = onOpenInteropRepro,
                    )
                },
                {
                    NativeListItem(
                        "Liquid glass buttons",
                        supporting = "Glass and prominent-glass NativeButtons refracting auto-scrolling content (iOS 26)",
                        onClick = onOpenLiquidGlass,
                    )
                },
                {
                    NativeListItem(
                        "Interop churn test",
                        supporting = "Auto-collapsing native controls in a lazy list — ghost/double regression harness",
                        onClick = onOpenInteropChurn,
                    )
                },
                {
                    NativeListItem(
                        "Navigation chrome demo",
                        supporting = "Per-screen NativeBarConfig live (hidden tab bar, per-screen action) + each platform's styling surface",
                        onClick = onOpenChromeDemo,
                    )
                },
                {
                    NativeListItem(
                        "Navigation toolbar styles",
                        supporting = "Catalog of toolbar/tab-bar variants: Material live previews, native iOS bar previews, per-screen demos",
                        onClick = onOpenToolbarStyles,
                    )
                },
            ),
        )
    }
}
