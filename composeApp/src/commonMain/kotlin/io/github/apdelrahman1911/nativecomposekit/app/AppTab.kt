package io.github.apdelrahman1911.nativecomposekit.app

import io.github.apdelrahman1911.nativecomposekit.app.navigation.NativeTab

/** The app's top-level tabs (the SwiftUI/Material tab bar projects these). */
enum class AppTab(override val id: String) : NativeTab {
    Catalog("catalog"),
    Library("library"),
    Settings("settings"),
}
