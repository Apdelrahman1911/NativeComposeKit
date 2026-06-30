package io.github.apdelrahman1911.nativecomposekit.app

import androidx.compose.foundation.isSystemInDarkTheme
import io.github.apdelrahman1911.nativecomposekit.catalog.CatalogScreen
import io.github.apdelrahman1911.nativecomposekit.navigation.BrandNavGraph
import io.github.apdelrahman1911.nativecomposekit.navigation.BrandNavigator
import io.github.apdelrahman1911.nativecomposekit.navigation.BrandRoute
import io.github.apdelrahman1911.nativecomposekit.navigation.BrandTab
import io.github.apdelrahman1911.nativecomposekit.navigation.brandNavGraph
import io.github.apdelrahman1911.nativecomposekit.theme.BrandAppearance

/** Each tab's root route — passed to the navigator factory. */
fun appRootRoute(tab: BrandTab): BrandRoute = when (tab) {
    AppTab.Library -> AppRoute.LibraryRoot
    AppTab.Settings -> AppRoute.SettingsRoot
    AppTab.Catalog -> AppRoute.CatalogRoot
    else -> AppRoute.LibraryRoot
}

/**
 * Title shown in the chrome (the Android top app bar, and — via `BrandNavBridge.title` — the iOS nav bar).
 * One source of truth for both platforms.
 */
fun appRouteTitle(route: BrandRoute): String = when (route) {
    is AppRoute.LibraryRoot -> "Library"
    is AppRoute.MangaDetail -> MangaLibrary.byId(route.mangaId)?.title ?: "Details"
    is AppRoute.Reader -> MangaLibrary.chapter(route.mangaId, route.chapterId)
        ?.let { (_, chapter) -> "Chapter ${chapter.number}" } ?: "Reader"
    is AppRoute.SettingsRoot -> "Settings"
    is AppRoute.CatalogRoot -> "Catalog"
    is AppRoute.GlassInteropTest -> "Interop test"
    is AppRoute.ComponentMatrix -> "Component matrix"
    is AppRoute.InteropRepro -> "iOS interop repro"
    else -> ""
}

/**
 * The route→screen registry, shared by both platform adapters. Screen callbacks are wired to [navigator]
 * intents here, keeping the screens themselves navigator-agnostic.
 */
fun appNavGraph(navigator: BrandNavigator): BrandNavGraph = brandNavGraph {
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
        )
    }
    screen<AppRoute.ComponentMatrix> { ComponentMatrixScreen() }
    screen<AppRoute.InteropRepro> { InteropReproScreen() }
    screen<AppRoute.CatalogRoot> {
        val dark = BrandAppearance.darkOverride ?: isSystemInDarkTheme()
        CatalogScreen(
            dark = dark,
            onToggleDark = { BrandAppearance.setDark(it) },
            rtl = BrandAppearance.rtl,
            onToggleRtl = { BrandAppearance.rtl = it },
        )
    }
    screen<AppRoute.GlassInteropTest> { GlassInteropTestScreen() }
}
