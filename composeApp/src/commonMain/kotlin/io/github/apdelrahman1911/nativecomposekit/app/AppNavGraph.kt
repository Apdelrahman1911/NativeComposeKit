package io.github.apdelrahman1911.nativecomposekit.app

import io.github.apdelrahman1911.nativecomposekit.navigation.NativeNavGraph
import io.github.apdelrahman1911.nativecomposekit.navigation.NativeNavigator
import io.github.apdelrahman1911.nativecomposekit.navigation.NativeRoute
import io.github.apdelrahman1911.nativecomposekit.navigation.NativeTab
import io.github.apdelrahman1911.nativecomposekit.navigation.nativeNavGraph
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
    else -> ""
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
        )
    }
    screen<AppRoute.ComponentMatrix> { ComponentMatrixScreen() }
    screen<AppRoute.InteropRepro> { InteropReproScreen() }
    screen<AppRoute.CatalogRoot> {
        ShowcaseHomeScreen(onOpenCategory = { key -> navigator.push(AppRoute.Showcase(key)) })
    }
    screen<AppRoute.Showcase> { route -> ShowcaseCategoryScreen(route.key) }
    screen<AppRoute.GlassInteropTest> { GlassInteropTestScreen() }
}
