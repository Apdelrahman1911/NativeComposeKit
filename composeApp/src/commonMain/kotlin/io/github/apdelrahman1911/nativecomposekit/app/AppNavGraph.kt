package io.github.apdelrahman1911.nativecomposekit.app

import io.github.apdelrahman1911.nativecomposekit.app.navigation.NativeNavGraph
import io.github.apdelrahman1911.nativecomposekit.app.navigation.NativeNavigator
import io.github.apdelrahman1911.nativecomposekit.app.navigation.NativeRoute
import io.github.apdelrahman1911.nativecomposekit.app.navigation.NativeTab
import io.github.apdelrahman1911.nativecomposekit.app.navigation.nativeNavGraph
import io.github.apdelrahman1911.nativecomposekit.chrome.NativeBarConfig
import io.github.apdelrahman1911.nativecomposekit.chrome.NativeChromeAction
import io.github.apdelrahman1911.nativecomposekit.showcase.ShowcaseCategoryScreen
import io.github.apdelrahman1911.nativecomposekit.showcase.ShowcaseHomeScreen
import io.github.apdelrahman1911.nativecomposekit.showcase.showcaseTitle

/** Each tab's root route — passed to the navigator factory. */
fun appRootRoute(tab: NativeTab): NativeRoute = when (tab) {
    AppTab.Library -> AppRoute.LibraryRoot
    AppTab.Settings -> AppRoute.SettingsRoot
    AppTab.Catalog -> AppRoute.CatalogRoot
    else -> AppRoute.LibraryRoot
}

/**
 * Title shown in the chrome top app bar, rendered by `NativeNavHost` on both platforms.
 * One source of truth for both platforms.
 */
fun appRouteTitle(route: NativeRoute): String = when (route) {
    is AppRoute.LibraryRoot -> "Library"
    is AppRoute.MangaDetail -> MangaLibrary.byId(route.mangaId)?.title ?: "Details"
    is AppRoute.Reader -> MangaLibrary.chapter(route.mangaId, route.chapterId)
        ?.let { (_, chapter) -> "Chapter ${chapter.number}" } ?: "Reader"
    is AppRoute.SettingsRoot -> "Settings"
    is AppRoute.CatalogRoot -> "Components"
    is AppRoute.Showcase -> showcaseTitle(route.key)
    is AppRoute.GlassInteropTest -> "Interop test"
    is AppRoute.ComponentMatrix -> "Component matrix"
    is AppRoute.InteropRepro -> "iOS interop repro"
    is AppRoute.ChromeDemo -> "Chrome demo"
    is AppRoute.ToolbarStyles -> "Toolbar styles"
    is AppRoute.ImmersiveDemo -> "Immersive"
    else -> ""
}

/** The id of the chrome demo's per-screen bar action (handled in each platform's shell wiring). */
const val CHROME_DEMO_ACTION_ID = "chrome-demo-action"

/**
 * Per-screen chrome BEHAVIOR for both hosts — the Material `NativeNavHost` (via its `barConfig` param) and
 * the iOS shell (via `NativeNavChrome.barConfigForRoute`). One source of truth, exactly like [appRouteTitle].
 * The demo screen hides the tab bar while pushed and carries its own per-screen bar action (rendered by
 * the iOS shell; the Android default bar takes actions as composable slots instead and ignores this list).
 */
fun appBarConfig(route: NativeRoute): NativeBarConfig = when (route) {
    is AppRoute.ChromeDemo -> NativeBarConfig(
        hidesTabBar = true,
        actions = listOf(NativeChromeAction(CHROME_DEMO_ACTION_ID, "sparkles")),
    )
    // The immersive demo hides BOTH bars — pop via swipe-back / system back / the screen's own button.
    is AppRoute.ImmersiveDemo -> NativeBarConfig(hidesTopBar = true, hidesTabBar = true)
    else -> NativeBarConfig.Default
}

/**
 * The route→screen registry, shared by both platform adapters. Screen callbacks are wired to [navigator]
 * intents here, keeping the screens themselves navigator-agnostic.
 */
fun appNavGraph(navigator: NativeNavigator): NativeNavGraph = nativeNavGraph {
    screen<AppRoute.LibraryRoot> {
        LibraryScreen(onOpenManga = { mangaId -> navigator.push(AppRoute.MangaDetail(mangaId)) })
    }
    screen<AppRoute.MangaDetail> { route ->
        MangaDetailScreen(
            mangaId = route.mangaId,
            onOpenChapter = { chapterId -> navigator.push(AppRoute.Reader(route.mangaId, chapterId)) },
        )
    }
    screen<AppRoute.Reader> { route -> ReaderScreen(route.mangaId, route.chapterId) }
    screen<AppRoute.SettingsRoot> {
        SettingsScreen(
            onOpenComponentMatrix = { navigator.push(AppRoute.ComponentMatrix) },
            onOpenInteropRepro = { navigator.push(AppRoute.InteropRepro) },
            onOpenChromeDemo = { navigator.push(AppRoute.ChromeDemo) },
            onOpenToolbarStyles = { navigator.push(AppRoute.ToolbarStyles) },
        )
    }
    screen<AppRoute.ComponentMatrix> { ComponentMatrixScreen() }
    screen<AppRoute.ChromeDemo> { ChromeDemoScreen() }
    screen<AppRoute.ToolbarStyles> {
        ToolbarStylesScreen(onOpenImmersive = { navigator.push(AppRoute.ImmersiveDemo) })
    }
    screen<AppRoute.ImmersiveDemo> { ImmersiveDemoScreen(onBack = { navigator.pop() }) }
    screen<AppRoute.InteropRepro> { InteropReproScreen() }
    screen<AppRoute.CatalogRoot> {
        ShowcaseHomeScreen(onOpenCategory = { key -> navigator.push(AppRoute.Showcase(key)) })
    }
    screen<AppRoute.Showcase> { route -> ShowcaseCategoryScreen(route.key) }
    screen<AppRoute.GlassInteropTest> { GlassInteropTestScreen() }
}
