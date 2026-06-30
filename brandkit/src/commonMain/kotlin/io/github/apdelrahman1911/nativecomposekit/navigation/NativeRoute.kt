package io.github.apdelrahman1911.nativecomposekit.navigation

/**
 * A navigation destination. The **app** declares its own sealed hierarchy of routes; the library never knows
 * the concrete cases — it only relies on [id].
 *
 * [id] must be **stable and unique per logical destination** — encode any arguments into it (e.g.
 * `"detail/$chapter"`), exactly as the spike already does with chapter ids. It is the projection used at the
 * Swift boundary (SwiftUI's `NavigationStack` path is a `[String]`) and the `AnimatedContent` content key on
 * Android. Two route values are the "same destination instance" iff their ids are equal.
 */
public interface NativeRoute {
    public val id: String
}

/** A top-level tab. The app declares its tabs (often an `enum`); [id] is the stable key used everywhere. */
public interface NativeTab {
    public val id: String
}
