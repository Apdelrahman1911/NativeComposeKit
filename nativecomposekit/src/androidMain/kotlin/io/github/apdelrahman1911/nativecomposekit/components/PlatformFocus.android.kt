package io.github.apdelrahman1911.nativecomposekit.components

/** No-op on Android: text fields are Compose-focused, so clearing Compose focus already hides the IME. */
internal actual fun platformEndEditing() {
    // intentionally empty
}
