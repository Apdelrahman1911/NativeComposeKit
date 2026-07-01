package io.github.apdelrahman1911.nativecomposekit.app.navigation

/**
 * A navigation destination. The **app** declares its own sealed hierarchy of routes; this reference navigator
 * only relies on [id].
 *
 * [id] must be **stable and unique per logical destination** — encode any arguments into it (e.g.
 * `"detail/$chapter"`). It is the `AnimatedContent` content key that identifies a destination on both platforms:
 * two route values are the "same destination instance" iff their ids are equal.
 */
interface NativeRoute {
    val id: String
}

/** A top-level tab. The app declares its tabs (often an `enum`); [id] is the stable key used everywhere. */
interface NativeTab {
    val id: String
}
