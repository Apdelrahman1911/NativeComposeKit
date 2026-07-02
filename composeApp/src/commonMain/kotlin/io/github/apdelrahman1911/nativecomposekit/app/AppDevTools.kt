package io.github.apdelrahman1911.nativecomposekit.app

/**
 * Gates the sample's developer-only surfaces (the component surface matrix, the interop repro screens,
 * the Library "+" glass-interop sheet) to debug binaries. Set once at startup by each platform host —
 * `BuildConfig.DEBUG` on Android, `Platform.isDebugBinary` on iOS — the same wiring as the diagnostic
 * log flags, so release builds of the sample ship without its regression harnesses.
 */
object AppDevTools {
    var enabled: Boolean = false
}
