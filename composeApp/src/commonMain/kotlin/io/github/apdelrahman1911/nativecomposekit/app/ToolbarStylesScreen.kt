package io.github.apdelrahman1911.nativecomposekit.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.apdelrahman1911.nativecomposekit.app.navigation.NativeNavBarItem
import io.github.apdelrahman1911.nativecomposekit.app.navigation.NativeNavBottomBarState
import io.github.apdelrahman1911.nativecomposekit.app.navigation.NativeNavDefaults
import io.github.apdelrahman1911.nativecomposekit.app.navigation.NativeNavTopBarState

/**
 * Debug catalog: every toolbar/navigation-bar style a consumer can reach TODAY, with honest "not
 * supported" labels where a knob doesn't exist. Two kinds of exhibits:
 * - **Material bars render live inline** — they are ordinary composables ([NativeNavDefaults] + the
 *   [NativeNavHost] slot states), so each variant below is the real thing, built with the exact public API.
 * - **iOS bars render as real embedded `UINavigationBar`/`UITabBar` previews** (non-interactive) showing
 *   what the shell style registry maps to; on Android those exhibits show a labeled placeholder.
 * Nothing here changes the app's shipped chrome — the live app bars around this screen stay default.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolbarStylesScreen(onOpenImmersive: () -> Unit = {}) {
    val bottomInset = LocalNativeContentBottomInset.current
    val crimson = Color(0.75f, 0.20f, 0.30f)

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 16.dp + bottomInset),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        DemoNote(
            "Every exhibit uses the public customization surface. Material bars are LIVE composables;" +
                " iOS exhibits are real embedded UIKit bars (previews — the app's actual chrome is styled" +
                " once at startup). The app's own bars around this screen stay default on purpose.",
        )

        // 1 — Default ------------------------------------------------------------------------------
        DemoSectionTitle("1. Default toolbar")
        DemoNote("The shipped look on both platforms: compact title, back arrow when pushed, no restyle.")
        Exhibit("Android host default (live)") {
            NativeNavDefaults.TopBar(topState("Default title"), windowInsets = WindowInsets(0))
        }
        Exhibit("iOS shell default (native preview)") {
            IosNavBarPreview(title = "Default title", showsBack = true)
        }

        // 2 — Tinted -------------------------------------------------------------------------------
        DemoSectionTitle("2. Tinted toolbar")
        Exhibit("Android: NativeNavDefaults.TopBar(colors = …) (live)") {
            NativeNavDefaults.TopBar(
                topState("Tinted title"),
                windowInsets = WindowInsets(0),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        }
        Exhibit("iOS: NativeShellStyle(tint, customBarBackground) (native preview)") {
            IosNavBarPreview(
                title = "Tinted title",
                background = IosPreviewBackground.Custom,
                customBackground = Color(0.98f, 0.92f, 0.93f),
                tint = crimson,
                showsBack = true,
                actionSymbols = listOf("square.and.arrow.up"),
            )
        }

        // 3 — Actions ------------------------------------------------------------------------------
        DemoSectionTitle("3. Toolbar with actions")
        Exhibit("Android: actions are a free composable slot (live: icon, text, disabled, destructive)") {
            NativeNavDefaults.TopBar(
                windowInsets = WindowInsets(0),
                state = topState("Actions") {
                    IconButton(onClick = {}) { Icon(Icons.Filled.Share, contentDescription = "Share") }
                    TextButton(onClick = {}) { Text("Edit") }
                    IconButton(onClick = {}, enabled = false) {
                        Icon(Icons.Filled.Favorite, contentDescription = "Disabled")
                    }
                    IconButton(onClick = {}) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                },
            )
        }
        Exhibit("iOS: per-screen/tab actions via NativeChromeAction (native preview: one + many)") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                IosNavBarPreview(title = "One action", actionSymbols = listOf("square.and.arrow.up"))
                IosNavBarPreview(title = "Many actions", actionSymbols = listOf("square.and.arrow.up", "trash", "star"))
            }
        }
        DemoNote(
            "Not supported on iOS today (documented limitation): NativeChromeAction is icon-only" +
                " (SF Symbol name) — text actions, disabled state, and destructive styling are not in the" +
                " contract. On Android all of those work because actions are composables.",
        )

        // 4 — Per-screen config ---------------------------------------------------------------------
        DemoSectionTitle("4. Per-screen toolbar config (live)")
        DemoNote(
            "NativeBarConfig travels per screen through the shared contract: per-screen title" +
                " (appRouteTitle), per-screen actions, and bar visibility — and it resets automatically on" +
                " pop because the config belongs to the entry, not the bar. The 'Navigation chrome demo'" +
                " screen (Settings → Developer) shows a hidden TAB bar + a per-screen action; the button" +
                " below pushes a screen that hides BOTH bars.",
        )
        Button(onClick = onOpenImmersive) { Text("Open immersive demo (hides both bars)") }

        // 5 — Transparent / glass -------------------------------------------------------------------
        DemoSectionTitle("5. Transparent / glass backgrounds")
        Exhibit("Android: containerColor = Transparent over content (live)") {
            Box(
                Modifier.fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(Color(0xFF7E57C2), Color(0xFF26A69A)))),
            ) {
                NativeNavDefaults.TopBar(
                    topState("Transparent"),
                    windowInsets = WindowInsets(0),
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                    ),
                )
            }
        }
        Exhibit("iOS: NativeShellBarBackground.SystemMaterial (native preview — real blur)") {
            Box(
                Modifier.fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(Color(0xFF7E57C2), Color(0xFF26A69A)))),
            ) {
                IosNavBarPreview(title = "System material", background = IosPreviewBackground.Material)
            }
        }
        DemoNote(
            "iOS trade-off (documented): the app's REAL bars default to opaque because a translucent bar" +
                " samples UIKit's dimmed transition container mid-swipe. SystemMaterial is an explicit opt-in.",
        )

        // 6 — Elevation / hairline ------------------------------------------------------------------
        DemoSectionTitle("6. Elevation / shadow / hairline")
        DemoNote(
            "Android: Material 3 top bars are FLAT by design; scrolledContainerColor gives the tonal" +
                " scroll change, and a drop shadow is possible through a custom slot (section 9). iOS: UIKit" +
                " bars have no elevation concept — the knob is the bottom hairline (off by default).",
        )
        Exhibit("iOS: showsHairline = true (native preview)") {
            IosNavBarPreview(title = "With hairline", hairline = true)
        }

        // 7 — Title alignment -----------------------------------------------------------------------
        DemoSectionTitle("7. Title alignment and style")
        Exhibit("Android: start-aligned (default, live)") {
            NativeNavDefaults.TopBar(topState("Start-aligned"), windowInsets = WindowInsets(0))
        }
        Exhibit("Android: centeredTitle = true (live)") {
            NativeNavDefaults.TopBar(topState("Centered"), centeredTitle = true, windowInsets = WindowInsets(0))
        }
        DemoNote(
            "Subtitles: not offered by the defaults on either platform — on Android use a custom slot" +
                " (section 9 shows one with a subtitle); on iOS a native subtitle is not supported. iOS titles" +
                " are system-managed (centered, compact); a custom titleFont is supported via NativeShellStyle." +
                " Large titles are deliberately deferred (interactive swipe-back constraint — see" +
                " docs/native-chrome.md).",
        )

        // 8 — Tab bars ------------------------------------------------------------------------------
        DemoSectionTitle("8. Bottom / tab bar variants")
        Exhibit("Android: default NavigationBar (live)") {
            NativeNavDefaults.NavigationBar(bottomState(), windowInsets = WindowInsets(0))
        }
        Exhibit("Android: tinted container + item colors (live)") {
            NativeNavDefaults.NavigationBar(
                bottomState(),
                windowInsets = WindowInsets(0),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                itemColors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                    indicatorColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            )
        }
        Exhibit("iOS: default UITabBar (native preview)") {
            IosTabBarPreview()
        }
        Exhibit("iOS: tabItemSelected / tabItemUnselected (native preview)") {
            IosTabBarPreview(selectedColor = crimson, unselectedColor = Color(0.45f, 0.45f, 0.50f))
        }
        DemoNote(
            "Hiding the tab bar per screen: section 4 (both platforms). Custom tab-bar SHAPE/background:" +
                " Android = replace the bottomBar slot with any composable; iOS = not supported (the system" +
                " bar's shape is owned by UIKit).",
        )

        // 9 — Fully custom Android slot -------------------------------------------------------------
        DemoSectionTitle("9. Fully custom Android toolbar (slot replacement, live)")
        Exhibit("topBar = { state -> MyBrandBar(state) } — any composable, same state object") {
            FancyDemoBar(topState("Custom slot"))
        }
        CodeCard(
            """
            NativeNavHost(
                navigator, graph, tabs,
                topBar = { state -> MyBrandBar(state) }, // full replacement
            )
            """.trimIndent(),
        )

        // 10 — iOS shell style: the whole surface ---------------------------------------------------
        DemoSectionTitle("10. iOS shell style — the full registry")
        DemoNote(
            "Everything the iOS shell reads today (applied once at startup, before createNativeNavRoot):" +
                " bar background (Themed / SystemMaterial / Custom), control tint, tab item colors, inline" +
                " title font, hairline. Per-screen behavior (visibility + actions) comes from NativeBarConfig" +
                " instead. Large titles: deferred.",
        )
        CodeCard(
            """
            applyNativeShellStyle(
                NativeShellStyle(
                    barBackground = NativeShellBarBackground.Custom,
                    customBarBackground = NativeShellColor(light, dark),
                    tint = NativeShellColor(light, dark),
                    tabItemSelected = NativeShellColor(light, dark),
                    tabItemUnselected = NativeShellColor(light, dark),
                    titleFont = UIFont.boldSystemFontOfSize(17.0),
                    showsHairline = false,
                ),
            )
            """.trimIndent(),
        )
    }
}

/** The immersive per-screen demo: BOTH bars hidden by this screen's [appBarConfig] entry. */
@Composable
fun ImmersiveDemoScreen(onBack: () -> Unit = {}) {
    Box(
        modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0xFF1A1A2E), Color(0xFF16213E), Color(0xFF0F3460))),
        ),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Immersive", color = Color.White, style = MaterialTheme.typography.headlineLarge)
            Text(
                "NativeBarConfig(hidesTopBar = true, hidesTabBar = true)\n" +
                    "Both bars are hidden while this entry is on top and return on pop.\n" +
                    "Swipe back / system back still work — or:",
                color = Color(0xFFB0BEC5),
                style = MaterialTheme.typography.bodyMedium,
            )
            Button(onClick = onBack) { Text("Go back") }
        }
    }
}

// ---- exhibit plumbing -------------------------------------------------------------------------

/** A labeled, clipped frame around one live bar exhibit. */
@Composable
private fun Exhibit(label: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))) { content() }
    }
}

private fun topState(
    title: String,
    actions: @Composable RowScope.() -> Unit = {},
) = NativeNavTopBarState(
    route = AppRoute.ToolbarStyles,
    title = title,
    canPop = true,
    onBack = {},
    actions = actions,
)

private fun bottomState() = NativeNavBottomBarState(
    tabs = listOf(
        NativeNavBarItem(AppTab.Catalog, "Components", Icons.Filled.GridView),
        NativeNavBarItem(AppTab.Library, "Library", Icons.AutoMirrored.Filled.List),
        NativeNavBarItem(AppTab.Settings, "Settings", Icons.Filled.Settings),
    ),
    selectedTabId = AppTab.Catalog.id,
    onSelectTab = {},
)

/** Section 9's "deep replacement" example: gradient brand bar with avatar, subtitle, and a pill action. */
@Composable
private fun FancyDemoBar(state: NativeNavTopBarState) {
    Row(
        modifier = Modifier.fillMaxWidth().height(64.dp)
            .background(Brush.horizontalGradient(listOf(Color(0xFF512DA8), Color(0xFF00897B))))
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (state.canPop) {
            IconButton(onClick = state.onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
        }
        Box(Modifier.size(32.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.9f)))
        Spacer(Modifier.size(10.dp))
        Column(Modifier.weight(1f)) {
            Text(state.title, color = Color.White, fontWeight = FontWeight.Bold)
            Text("Subtitle — anything goes in a slot", color = Color.White.copy(alpha = 0.75f), style = MaterialTheme.typography.labelSmall)
        }
        Box(
            Modifier.clip(RoundedCornerShape(50)).background(Color.White.copy(alpha = 0.2f))
                .padding(horizontal = 12.dp, vertical = 6.dp),
        ) { Text("Action", color = Color.White, style = MaterialTheme.typography.labelMedium) }
    }
}
