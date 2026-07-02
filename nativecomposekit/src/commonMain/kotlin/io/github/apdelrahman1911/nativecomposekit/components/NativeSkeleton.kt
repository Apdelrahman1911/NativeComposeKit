package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.LayoutDirection
import io.github.apdelrahman1911.nativecomposekit.components.internal.resolveSurfaceFill
import io.github.apdelrahman1911.nativecomposekit.components.internal.skeletonColors
import io.github.apdelrahman1911.nativecomposekit.theme.NativeTheme
import io.github.apdelrahman1911.nativecomposekit.theme.LocalNativeCapabilities
import io.github.apdelrahman1911.nativecomposekit.theme.LocalNativeSurface

/**
 * A loading-placeholder block with an animated shimmer sweep — for cover grids, list rows, and detail
 * screens before data arrives. **Compose-drawn on both platforms.** The **caller supplies the size**
 * via [modifier] (e.g. `Modifier.size(120.dp, 170.dp)` for a cover, `Modifier.fillMaxWidth().height(16.dp)`
 * for a text line); compose several to mock a card. Colors derive from the **surface it sits on** (the
 * published `LocalNativeSurface` nudged toward `onSurface`), so it stays visible on the page, inside a Filled
 * card, and in dark mode (a fixed `surfaceVariant` base would vanish on a `surfaceVariant` card); the sweep follows the layout
 * direction (reversed in RTL). The shimmer runs only while composed, so **show skeletons only while
 * actually loading** (swap to real content to stop them). [shimmer] **defaults to OFF when the OS
 * reduce-motion setting is on** (via [LocalNativeCapabilities]); pass `shimmer = false` for an always-static
 * placeholder, or `true` to force the animation.
 */
@Composable
public fun NativeSkeleton(
    modifier: Modifier = Modifier,
    shape: Shape? = null,
    shimmer: Boolean = !LocalNativeCapabilities.current.isReduceMotionEnabled,
    testTag: String? = null,
) {
    val scheme = MaterialTheme.colorScheme
    val clipShape = shape ?: RoundedCornerShape(NativeTheme.tokens.cornerSmall)
    var m = modifier
    testTag?.let { m = m.testTag(it) }
    // Derive the block tone from the surface the skeleton actually sits on (the published LocalNativeSurface),
    // nudged toward onSurface — so it keeps a consistent, visible contrast on the page, inside a Filled card
    // (which is itself surfaceVariant — a fixed surfaceVariant base would vanish there), or in dark mode.
    // Falls back to surfaceVariant only when no surface is published (e.g. a Liquid Glass / unwrapped host).
    val container = resolveSurfaceFill(LocalNativeSurface.current, scheme.surfaceVariant)
    val (base, highlight) = skeletonColors(container, scheme.onSurface)

    if (!shimmer) {
        Box(m.clip(clipShape).background(base))
        return
    }

    val transition = rememberInfiniteTransition(label = "skeleton")
    val progress = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 1200, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmer",
    )
    val ltr = LocalLayoutDirection.current == LayoutDirection.Ltr

    // Read `progress` inside the draw phase (drawWithCache's onDrawBehind), NOT in composition — so the
    // shimmer animates by redrawing, without recomposing this composable ~60×/s for its whole loading life.
    // The gradient slides a [base → highlight → base] sweep one full width across in the reading direction.
    Box(
        m
            .clip(clipShape)
            .drawWithCache {
                val widthPx = size.width
                onDrawBehind {
                    if (widthPx <= 0f) {
                        drawRect(SolidColor(base))
                    } else {
                        val p = progress.value
                        val startX = if (ltr) -widthPx + p * 2f * widthPx else widthPx - p * 2f * widthPx
                        drawRect(
                            Brush.linearGradient(
                                colors = listOf(base, highlight, base),
                                start = Offset(startX, 0f),
                                end = Offset(startX + widthPx, 0f),
                            ),
                        )
                    }
                }
            },
    )
}
