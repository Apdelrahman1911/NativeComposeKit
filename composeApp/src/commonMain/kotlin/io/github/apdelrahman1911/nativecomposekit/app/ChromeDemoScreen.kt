package io.github.apdelrahman1911.nativecomposekit.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import io.github.apdelrahman1911.nativecomposekit.components.NativeCard
import io.github.apdelrahman1911.nativecomposekit.components.NativeCardVariant
import io.github.apdelrahman1911.nativecomposekit.components.NativeText
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeTextStyle

/**
 * Debug demo for the navigation-chrome customization surfaces. The LIVE part is this screen's own
 * `NativeBarConfig` (see `appBarConfig`): the tab bar is hidden while it is pushed, and on iOS the top bar
 * carries this screen's own action (the sparkles button — tap it). The content documents the rest: how each
 * platform restyles its chrome INDEPENDENTLY, which is the design — Android stays Compose-native, iOS stays
 * UIKit-native, and neither is forced to match the other.
 */
@Composable
fun ChromeDemoScreen() {
    val bottomInset = LocalNativeContentBottomInset.current
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 16.dp + bottomInset),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        NativeText(
            "Live right now: this screen's NativeBarConfig hides the tab bar while it is on top" +
                " (pop back — it returns), and on iOS adds the screen's own bar action. Both hosts read" +
                " the SAME per-screen config; everything visual below is per-platform on purpose.",
            style = NativeTextStyle.Label,
        )

        NativeText("Shared: per-screen behavior", style = NativeTextStyle.Title)
        CodeCard(
            """
            // one lambda, used by both hosts (like appRouteTitle)
            fun appBarConfig(route: NativeRoute) = when (route) {
                is AppRoute.ChromeDemo -> NativeBarConfig(
                    hidesTabBar = true,
                    actions = listOf(NativeChromeAction("chrome-demo-action", "sparkles")),
                )
                else -> NativeBarConfig.Default
            }
            """.trimIndent(),
        )

        NativeText("Android: Compose slots + defaults", style = NativeTextStyle.Title)
        NativeText(
            "The Material host exposes topBar/bottomBar slots. Call NativeNavDefaults with parameters to" +
                " restyle the stock bars, or pass any composable to replace one. Actions stay composable" +
                " slots on Android — no icon-name lists.",
            style = NativeTextStyle.Label,
        )
        CodeCard(
            """
            NativeNavHost(
                navigator, graph, tabs,
                barConfig = ::appBarConfig,
                topBar = { state ->
                    NativeNavDefaults.TopBar(
                        state,
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = brand),
                        centeredTitle = true,
                    )
                },
                bottomBar = { state -> NativeNavDefaults.NavigationBar(state, containerColor = brand) },
            )
            """.trimIndent(),
        )

        NativeText("iOS: the shell style registry", style = NativeTextStyle.Title)
        NativeText(
            "The UIKit shell reads NativeShellStyle once at startup (register it before createNativeNavRoot)." +
                " Defaults are today's chrome. SystemMaterial re-exposes the documented mid-transition" +
                " darkening. Large titles are deliberately not offered (see docs/native-chrome.md).",
            style = NativeTextStyle.Label,
        )
        CodeCard(
            """
            applyNativeShellStyle(
                NativeShellStyle(
                    barBackground = NativeShellBarBackground.Custom,
                    customBarBackground = NativeShellColor(light = brandLight, dark = brandDark),
                    tint = NativeShellColor(light = accent, dark = accentDark),
                    tabItemSelected = NativeShellColor(light = accent, dark = accentDark),
                ),
            )
            """.trimIndent(),
        )
    }
}

@Composable
private fun CodeCard(code: String) {
    NativeCard(variant = NativeCardVariant.Filled, modifier = Modifier.fillMaxWidth()) {
        Text(
            code,
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
        )
    }
}
