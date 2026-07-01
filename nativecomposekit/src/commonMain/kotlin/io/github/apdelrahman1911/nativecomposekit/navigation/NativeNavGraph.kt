package io.github.apdelrahman1911.nativecomposekit.navigation

import androidx.compose.runtime.Composable
import kotlin.reflect.KClass

/**
 * Maps each [NativeRoute] to the shared `@Composable` that renders it. Built with the [nativeNavGraph] DSL and
 * consumed by `NativeNavHost` (the Compose renderer on both platforms). Platform-neutral commonMain.
 *
 * Routes are matched by their **runtime class** (registered via the reified [NativeNavGraphBuilder.screen]), not
 * by id string — so any arguments carried in the route data class are available to the screen. (Id strings are
 * only the Swift-path projection / `AnimatedContent` key.)
 */
public class NativeNavGraph internal constructor(
    internal val screens: Map<KClass<out NativeRoute>, @Composable (NativeRoute) -> Unit>,
) {
    @Composable
    internal fun Content(route: NativeRoute) {
        val screen = screens[route::class]
            ?: error("No screen registered in the NativeNavGraph for route ${route::class.simpleName} (id='${route.id}')")
        screen(route)
    }
}

public class NativeNavGraphBuilder internal constructor() {
    @PublishedApi
    internal val screens: MutableMap<KClass<out NativeRoute>, @Composable (NativeRoute) -> Unit> = mutableMapOf()

    /** Register the screen for route type [R]. The lambda receives the typed route (args included). */
    public inline fun <reified R : NativeRoute> screen(noinline content: @Composable (R) -> Unit) {
        @Suppress("UNCHECKED_CAST")
        screens[R::class] = { route -> content(route as R) }
    }

    internal fun build(): NativeNavGraph = NativeNavGraph(screens.toMap())
}

/** Declare the route→screen registry: `nativeNavGraph { screen<MangaDetail> { MangaDetailScreen(it.mangaId) }; … }`. */
public fun nativeNavGraph(build: NativeNavGraphBuilder.() -> Unit): NativeNavGraph =
    NativeNavGraphBuilder().apply(build).build()
