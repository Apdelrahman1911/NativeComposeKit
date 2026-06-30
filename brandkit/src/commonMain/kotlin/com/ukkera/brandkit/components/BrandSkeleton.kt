package com.ukkera.brandkit.components

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.ukkera.brandkit.components.internal.resolveSurfaceFill
import com.ukkera.brandkit.components.internal.skeletonColors
import com.ukkera.brandkit.theme.BrandTheme
import com.ukkera.brandkit.theme.LocalBrandCapabilities
import com.ukkera.brandkit.theme.LocalBrandSurface

/**
 * A loading-placeholder block with an animated shimmer sweep — for cover grids, list rows, and detail
 * screens before data arrives. **Compose-drawn on both platforms.** The **caller supplies the size**
 * via [modifier] (e.g. `Modifier.size(120.dp, 170.dp)` for a cover, `Modifier.fillMaxWidth().height(16.dp)`
 * for a text line); compose several to mock a card. Colors derive from the **surface it sits on** (the
 * published `LocalBrandSurface` nudged toward `onSurface`), so it stays visible on the page, inside a Filled
 * card, and in dark mode (a fixed `surfaceVariant` base would vanish on a `surfaceVariant` card); the sweep follows the layout
 * direction (reversed in RTL). The shimmer runs only while composed, so **show skeletons only while
 * actually loading** (swap to real content to stop them). [shimmer] **defaults to OFF when the OS
 * reduce-motion setting is on** (via [LocalBrandCapabilities]); pass `shimmer = false` for an always-static
 * placeholder, or `true` to force the animation.
 */
@Composable
public fun BrandSkeleton(
    modifier: Modifier = Modifier,
    shape: Shape? = null,
    shimmer: Boolean = !LocalBrandCapabilities.current.isReduceMotionEnabled,
) {
    val scheme = MaterialTheme.colorScheme
    val clipShape = shape ?: RoundedCornerShape(BrandTheme.tokens.cornerSmall)
    // Derive the block tone from the surface the skeleton actually sits on (the published LocalBrandSurface),
    // nudged toward onSurface — so it keeps a consistent, visible contrast on the page, inside a Filled card
    // (which is itself surfaceVariant — a fixed surfaceVariant base would vanish there), or in dark mode.
    // Falls back to surfaceVariant only when no surface is published (e.g. a Liquid Glass / unwrapped host).
    val container = resolveSurfaceFill(LocalBrandSurface.current, scheme.surfaceVariant)
    val (base, highlight) = skeletonColors(container, scheme.onSurface)

    if (!shimmer) {
        Box(modifier.clip(clipShape).background(base))
        return
    }

    var widthPx by remember { mutableStateOf(0f) }
    val transition = rememberInfiniteTransition(label = "skeleton")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 1200, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmer",
    )
    // Slide a [base → highlight → base] gradient one full width across, in the reading direction.
    val ltr = LocalLayoutDirection.current == LayoutDirection.Ltr
    val brush = if (widthPx <= 0f) {
        SolidColor(base)
    } else {
        val startX = if (ltr) -widthPx + progress * 2f * widthPx else widthPx - progress * 2f * widthPx
        Brush.linearGradient(
            colors = listOf(base, highlight, base),
            start = Offset(startX, 0f),
            end = Offset(startX + widthPx, 0f),
        )
    }

    Box(
        modifier
            .onSizeChanged { widthPx = it.width.toFloat() }
            .clip(clipShape)
            .background(brush),
    )
}
