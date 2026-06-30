package io.github.apdelrahman1911.nativecomposekit.theme

/** No-op on Android: there's no native shell here, and the Compose side already follows
 * [NativeAppearance.darkOverride] via [NativeAppearanceScope]. */
internal actual fun applyPlatformColorScheme(dark: Boolean) {
    // intentionally empty
}

/** No-op on Android: Compose draws the whole surface (no native window/bars to theme). */
internal actual fun applyNativeShellChrome(dark: Boolean) {
    // intentionally empty
}
