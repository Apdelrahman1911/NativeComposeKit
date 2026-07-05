package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Collapses/expands [content] with the **platform-safe** animation for content that contains native
 * controls — use this instead of wrapping `Native*` components in `AnimatedVisibility`.
 *
 * On **Android** it is a real `AnimatedVisibility` (expand/shrink) — Compose owns every pixel there, so
 * the full transition is safe.
 *
 * On **iOS** the kit's controls are real UIKit views synchronized with the Compose canvas through an
 * interop transaction queue, and animating their *visibility* floods that queue with per-frame
 * insert/remove/clip actions — on physical devices the UIKit side then falls visibly out of sync
 * with the Compose layout: controls lag their collapsing row, draw outside their container, and
 * catch up only after a delay (and under continuous churn the backlog stops draining; a Compose
 * Multiplatform engine limitation — see docs/interop-notes.md §4). So on iOS this composable
 * animates the **container's size** while the
 * content itself enters and leaves in a single clean step, and `NativeText` inside renders through
 * Compose (skipping the interop label whose canvas cut-out would flash dark while the queue fills it).
 * Native controls still pop in a frame or two after the expansion starts — the accepted trade for a
 * pipeline that never corrupts.
 */
@Composable
public expect fun NativeCollapsible(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
)
