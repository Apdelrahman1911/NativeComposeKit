package io.github.apdelrahman1911.nativecomposekit.app

import io.github.apdelrahman1911.nativecomposekit.app.navigation.NativeRoute

/**
 * The app's navigation destinations. The library never knows these cases — it only uses [NativeRoute.id]. The id
 * encodes any arguments (e.g. the chapter), so a route survives being projected to the native chrome as a
 * plain string id and mapped back.
 */
sealed interface AppRoute : NativeRoute {
    /** Root of the Library tab. */
    data object LibraryRoot : AppRoute {
        override val id = "library"
    }

    /** A pushed manga detail (cover, metadata, chapters). */
    data class MangaDetail(val mangaId: String) : AppRoute {
        override val id = "manga/$mangaId"
    }

    /** A pushed chapter reader. */
    data class Reader(val mangaId: String, val chapterId: String) : AppRoute {
        override val id = "reader/$mangaId/$chapterId"
    }

    /** Root of the Settings tab. */
    data object SettingsRoot : AppRoute {
        override val id = "settings"
    }

    /** Root of the Components tab (the showcase overview). */
    data object CatalogRoot : AppRoute {
        override val id = "catalog"
    }

    /** A pushed showcase category screen (e.g. `showcase/buttons`). [key] selects the category. */
    data class Showcase(val key: String) : AppRoute {
        override val id = "showcase/$key"
    }

    /** Debug sheet: the bare-vs-card Liquid Glass interop stress test (see docs/interop-notes.md). */
    data object GlassInteropTest : AppRoute {
        override val id = "debug/glass-interop"
    }

    /** Debug screen: the surface-adaptation regression matrix (page vs Filled card). Pushed from Settings. */
    data object ComponentMatrix : AppRoute {
        override val id = "debug/component-matrix"
    }

    /** Debug screen: raw-UIKitView interop reproductions (scroll clip/drift, UIMenu drift, Dialog first-frame). */
    data object InteropRepro : AppRoute {
        override val id = "debug/interop-repro"
    }

    /**
     * Debug screen: the navigation-chrome customization demo. Its `NativeBarConfig` (see `appBarConfig`)
     * hides the tab bar while pushed and, on iOS, carries a per-screen bar action; the screen's content
     * documents each platform's styling surface. Pushed from Settings.
     */
    data object ChromeDemo : AppRoute {
        override val id = "debug/chrome-demo"
    }
}
