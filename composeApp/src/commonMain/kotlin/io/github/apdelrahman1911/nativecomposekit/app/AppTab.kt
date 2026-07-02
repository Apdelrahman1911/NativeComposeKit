package io.github.apdelrahman1911.nativecomposekit.app

import io.github.apdelrahman1911.nativecomposekit.app.navigation.NativeTab

/** The app's top-level tabs (the native `UITabBar` on iOS / Material navigation bar on Android project these). */
enum class AppTab(override val id: String) : NativeTab {
    Catalog("catalog"),
    Library("library"),
    Settings("settings"),
}
