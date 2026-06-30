package com.ukkera.brandkit.app

import com.ukkera.brandkit.navigation.BrandTab

/** The app's top-level tabs (the SwiftUI/Material tab bar projects these). */
enum class AppTab(override val id: String) : BrandTab {
    Library("library"),
    Settings("settings"),
    Catalog("catalog"),
}
