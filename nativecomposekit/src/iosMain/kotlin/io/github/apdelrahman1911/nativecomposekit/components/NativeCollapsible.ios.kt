package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds

/**
 * `true` inside [NativeCollapsible]: `NativeText` must render through Compose instead of an interop
 * `UILabel`. An interop label behind a canvas cut-out flashes the dark system backdrop for the frames
 * the interop transaction queue needs to fill the hole — invisible on a static screen, conspicuous
 * inside content that is animating open. Compose-drawn glyphs have no hole to flash.
 */
internal val LocalNativeTextPrefersCompose = compositionLocalOf { false }

// iOS must NOT animate interop views' visibility: per-frame insert/remove/clip actions make the UIKit
// side fall visibly behind the Compose layout on device (delayed/out-of-sync, backlog stops draining
// under continuous churn — docs/interop-notes.md §4). Animate the container's size; gate the content
// in one step; render text through Compose while inside.
@Composable
public actual fun NativeCollapsible(
    visible: Boolean,
    modifier: Modifier,
    content: @Composable () -> Unit,
) {
    // Clip so Compose-drawn content reveals progressively with the animated size (interop controls
    // composite above the canvas and ignore the clip — they appear at their final spots, the accepted
    // trade). Order matters: clip applies to the size animateContentSize is interpolating.
    Box(modifier.animateContentSize().clipToBounds()) {
        if (visible) {
            CompositionLocalProvider(LocalNativeTextPrefersCompose provides true) {
                content()
            }
        }
    }
}
