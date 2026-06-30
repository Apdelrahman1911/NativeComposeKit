package io.github.apdelrahman1911.nativecomposekit.navigation

import androidx.compose.runtime.Composable
import kotlin.reflect.KClass

/**
 * Maps each [BrandRoute] to the shared `@Composable` that renders it. Built with the [brandNavGraph] DSL and
 * consumed by **both** adapters (Android `BrandNavHost`, iOS `BrandNavBridge`). Platform-neutral commonMain.
 *
 * Routes are matched by their **runtime class** (registered via the reified [BrandNavGraphBuilder.screen]), not
 * by id string — so any arguments carried in the route data class are available to the screen. (Id strings are
 * only the Swift-path projection / `AnimatedContent` key.)
 */
public class BrandNavGraph internal constructor(
    internal val screens: Map<KClass<out BrandRoute>, @Composable (BrandRoute) -> Unit>,
) {
    @Composable
    internal fun Content(route: BrandRoute) {
        val screen = screens[route::class]
            ?: error("No screen registered in the BrandNavGraph for route ${route::class.simpleName} (id='${route.id}')")
        screen(route)
    }
}

public class BrandNavGraphBuilder internal constructor() {
    @PublishedApi
    internal val screens: MutableMap<KClass<out BrandRoute>, @Composable (BrandRoute) -> Unit> = mutableMapOf()

    /** Register the screen for route type [R]. The lambda receives the typed route (args included). */
    public inline fun <reified R : BrandRoute> screen(noinline content: @Composable (R) -> Unit) {
        @Suppress("UNCHECKED_CAST")
        screens[R::class] = { route -> content(route as R) }
    }

    internal fun build(): BrandNavGraph = BrandNavGraph(screens.toMap())
}

/** Declare the route→screen registry: `brandNavGraph { screen<MangaDetail> { MangaDetailScreen(it.mangaId) }; … }`. */
public fun brandNavGraph(build: BrandNavGraphBuilder.() -> Unit): BrandNavGraph =
    BrandNavGraphBuilder().apply(build).build()
