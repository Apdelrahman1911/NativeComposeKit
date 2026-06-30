package com.ukkera.brandkit.app

import com.ukkera.brandkit.navigation.BrandRoute

/**
 * The app's navigation destinations. The library never knows these cases — it only uses [BrandRoute.id]. The id
 * encodes any arguments (e.g. the chapter) so a route round-trips through the SwiftUI `[String]` path.
 */
sealed interface AppRoute : BrandRoute {
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

    /** Root of the Catalog tab (the full component catalog). */
    data object CatalogRoot : AppRoute {
        override val id = "catalog"
    }

    /** Debug sheet: the bare-vs-card Liquid Glass interop stress test (see docs/interop-backdrop-audit.md). */
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
}
