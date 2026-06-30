package com.ukkera.brandkit.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf

/**
 * Runtime accessibility / platform capabilities the kit adapts to, published via [LocalBrandCapabilities]
 * by [BrandAppearanceScope]. Kept tiny and extensible: v1 carries only [isReduceMotionEnabled]; add more
 * system-derived signals (e.g. share availability, OS version gates) here as they're wired.
 */
@Immutable
public data class BrandCapabilities(
    /**
     * The user asked the OS to minimize motion (iOS *Reduce Motion* / Android *Remove animations*).
     * Motion-heavy defaults (e.g. [com.ukkera.brandkit.components.BrandSkeleton]'s shimmer) turn off when true.
     */
    val isReduceMotionEnabled: Boolean = false,
)

/**
 * The current [BrandCapabilities]. Defaults to everything-off until [BrandAppearanceScope] provides the
 * real, platform-read values, so components read a safe default outside a scope (and in previews/tests).
 */
public val LocalBrandCapabilities: ProvidableCompositionLocal<BrandCapabilities> = compositionLocalOf { BrandCapabilities() }

/** Reads the platform's reduce-motion preference, observing OS changes where the platform reports them. */
@Composable
internal expect fun rememberReduceMotion(): Boolean
