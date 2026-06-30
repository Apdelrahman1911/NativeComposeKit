package com.ukkera.brandkit.app

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.network.ktor3.KtorNetworkFetcherFactory

/**
 * Registers the **app's** Coil singleton `ImageLoader` with the Ktor-3 network fetcher (the platform Ktor
 * engine — OkHttp on Android, Darwin on iOS — is pulled in per source set). Coil's network artifact is not
 * auto-registered on iOS, so we set the singleton explicitly; call this **once per process at each platform
 * entry point**, before any image composes.
 *
 * This is app-level on purpose: the kit (`components/`) carries no third-party dependency, so image
 * loading lives in the app, with Coil providing the memory cache + request de-duplication that a real cover
 * grid needs. The brand-specific cover treatment (gradient/title fallback) stays in `MangaScreens.MangaCover`.
 */
fun configureCoilImageLoader() {
    // `setSafe` is the non-composable, idempotent way to install the singleton factory at app startup
    // (`coil3.compose.setSingletonImageLoaderFactory` is @Composable and meant for inside a composition).
    SingletonImageLoader.setSafe { context: PlatformContext ->
        ImageLoader.Builder(context)
            .components { add(KtorNetworkFetcherFactory()) }
            .build()
    }
}
