package io.github.apdelrahman1911.nativecomposekit.components.internal

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.lerp

/**
 * Pure surface-adaptation helpers (no Compose runtime, so they're unit-testable). They centralize the rule
 * established in the hardening pass — see `docs/design-system-rules.md`: a surface-relative fill/border must
 * derive from the surrounding surface (the published `LocalNativeSurface`), never hardcode `surface`/
 * `surfaceVariant`, so a component stays correct on the page, inside a Filled card, and in dark mode.
 */

/**
 * Skeleton base + shimmer-highlight tones for a block sitting on [container], each nudged toward [onSurface] so
 * the block keeps a visible contrast on any surface — including a Filled card whose container is itself
 * `surfaceVariant` (a fixed `surfaceVariant` base would vanish there). [highlight] is the stronger of the two.
 */
internal fun skeletonColors(container: Color, onSurface: Color): Pair<Color, Color> =
    lerp(container, onSurface, 0.10f) to lerp(container, onSurface, 0.20f)

/**
 * The opaque fill for content that sits on a surface: the [published] surface when known (`isSpecified`), else
 * [fallback]. Used for the `NativeListItem` swipe foreground (must be opaque or the reveal shows at rest) and the
 * outlined `NativeInlineStatus` interior (matches the surface it's embedded in). The result is always a concrete,
 * opaque color — never `Color.Unspecified`.
 */
internal fun resolveSurfaceFill(published: Color, fallback: Color): Color =
    if (published.isSpecified) published else fallback

/**
 * Chip border tone — the M3 `outline` boundary role (visible on any surface, unlike the default faint
 * `outlineVariant` which disappears on a `surfaceVariant` card), dimmed to 38% when [enabled] is false.
 */
internal fun chipBorderColor(outline: Color, enabled: Boolean): Color =
    outline.copy(alpha = if (enabled) 1f else 0.38f)
