package io.github.apdelrahman1911.nativecomposekit.app

import io.github.apdelrahman1911.nativecomposekit.navigation.BrandTab

/** The app's top-level tabs (the SwiftUI/Material tab bar projects these). */
enum class AppTab(override val id: String) : BrandTab {
    Library("library"),
    Settings("settings"),
    Catalog("catalog"),
}
