package io.github.apdelrahman1911.nativecomposekit.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf

/**
 * Runtime accessibility / platform capabilities the kit adapts to, published via [LocalNativeCapabilities]
 * by [NativeAppearanceScope]. Kept tiny and extensible: v1 carries only [isReduceMotionEnabled]; add more
 * system-derived signals (e.g. share availability, OS version gates) here as they're wired.
 */
@Immutable
public data class NativeCapabilities(
    /**
     * The user asked the OS to minimize motion (iOS *Reduce Motion* / Android *Remove animations*).
     * Motion-heavy defaults (e.g. [io.github.apdelrahman1911.nativecomposekit.components.NativeSkeleton]'s shimmer) turn off when true.
     */
    val isReduceMotionEnabled: Boolean = false,
)

/**
 * The current [NativeCapabilities]. Defaults to everything-off until [NativeAppearanceScope] provides the
 * real, platform-read values, so components read a safe default outside a scope (and in previews/tests).
 */
public val LocalNativeCapabilities: ProvidableCompositionLocal<NativeCapabilities> = compositionLocalOf { NativeCapabilities() }

/** Reads the platform's reduce-motion preference, observing OS changes where the platform reports them. */
@Composable
internal expect fun rememberReduceMotion(): Boolean
