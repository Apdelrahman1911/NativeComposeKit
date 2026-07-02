package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import io.github.apdelrahman1911.nativecomposekit.theme.NativeTheme

/**
 * A transient popover that floats a small surface near an [anchor].
 *
 * **Native-per-platform, phone-first (kit thesis).**
 * - **iOS, iPad / regular width** → a real `UIPopoverPresentationController` (system material + arrow), anchored
 *   to the [anchor]'s on-screen rect, hosting the Compose [content].
 * - **iOS, iPhone / compact width** → a **polished Compose popover** ([ComposeNativePopover]): a lightweight
 *   elevated [NativeCard] in a `Popup` anchored to the [anchor]. This avoids UIKit's full-screen popover
 *   adaptation at compact width (a tiny panel shouldn't take the whole screen on a phone). Pure Compose ⇒ no
 *   `UIKitView` interop hole ⇒ no black/white backdrop artifact; it uses brand surface/elevation/corner/spacing/
 *   typography and adapts to light/dark via the theme.
 * - **Android** → the same Compose popover.
 *
 * Shown while [visible]; dismisses on an outside tap via [onDismissRequest]. [anchor] (**additive, optional**)
 * is the trigger rendered inline in layout; the popover points at it. [alignment] is the **preferred edge and
 * gravity** relative to the anchor: on the Compose path its vertical half picks the side (Bottom/Center → below,
 * Top → above — flipped automatically when that side doesn't fit on screen) and its horizontal half the gravity
 * over the anchor (start/center/end, clamped into the window); on the iPad native path it's a hint (UIKit picks
 * the arrow direction). [preferredSize] sizes the **iPad native popover only** (its `preferredContentSize`;
 * content scrolls if larger) — the compact/Android Compose panel sizes to its content. [modifier]/[testTag]
 * apply to the surface. If no presenter/anchor resolves on the iPad native path, it falls back to a centred
 * presentation safely.
 *
 * `NativePopover(visible, { open = false }, anchor = { NativeIconButton(moreIcon, { open = true }, …) }) { … }`
 */
@Composable
public fun NativePopover(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.BottomCenter,
    preferredSize: DpSize = DpSize(300.dp, 320.dp),
    testTag: String? = null,
    anchor: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    PlatformNativePopover(visible, onDismissRequest, modifier, alignment, preferredSize, testTag, anchor, content)
}

@Composable
internal expect fun PlatformNativePopover(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier,
    alignment: Alignment,
    preferredSize: DpSize,
    testTag: String?,
    anchor: (@Composable () -> Unit)?,
    content: @Composable () -> Unit,
)

/**
 * Anchored placement for the Compose popover path. The stock `Popup(alignment)` aligns the panel *within*
 * the anchor's box — it overlaps the trigger it should point at — and never adapts at screen edges. This
 * provider places the panel beside the anchor instead: on [alignment]'s vertical side (below for
 * Bottom/Center, above for Top), flipping to the opposite side when the preferred one doesn't fit in the
 * window, with [alignment]'s horizontal half as the gravity over the anchor and the result clamped
 * horizontally into the window.
 */
private class AnchoredPopoverPositionProvider(
    private val alignment: Alignment,
    private val gapPx: Int,
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        // Recover the vertical bias by aligning a zero-size box in a 2×2 space (y: 0 = top, 1 = center,
        // 2 = bottom) — works for the standard alignments and any custom BiasAlignment.
        val preferAbove = alignment.align(IntSize.Zero, IntSize(2, 2), layoutDirection).y == 0

        // Horizontal gravity over the anchor's width (start/center/end), then clamp into the window.
        val alignedX = anchorBounds.left +
            alignment.align(
                IntSize(popupContentSize.width, 0),
                IntSize(anchorBounds.width, 0),
                layoutDirection,
            ).x
        val x = alignedX.coerceIn(0, (windowSize.width - popupContentSize.width).coerceAtLeast(0))

        val above = anchorBounds.top - gapPx - popupContentSize.height
        val below = anchorBounds.bottom + gapPx
        val fitsAbove = above >= 0
        val fitsBelow = below + popupContentSize.height <= windowSize.height
        val y = when {
            preferAbove -> if (fitsAbove) above else below
            fitsBelow -> below
            fitsAbove -> above
            else -> below // neither side fully fits; the clamp below keeps the panel on screen
        }
        return IntOffset(x, y.coerceIn(0, (windowSize.height - popupContentSize.height).coerceAtLeast(0)))
    }
}

/**
 * The shared Compose popover surface: an elevated themed [NativeCard] in a `Popup`, anchored to [anchor]. Used by
 * the **Android** renderer and by the **iOS** renderer at **compact width** (iPhone). Pure Compose — no
 * `UIKitView`, so no interop backdrop artifact — and the card draws with brand surface/elevation/corner/spacing/
 * typography (`NativeCard` reads `LocalNativeSurface`) so it looks native-appropriate in light and dark. Sizes
 * to its content; positioned by [AnchoredPopoverPositionProvider] (beside the anchor, edge-flipping).
 */
@Composable
internal fun ComposeNativePopover(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier,
    alignment: Alignment,
    testTag: String?,
    anchor: (@Composable () -> Unit)?,
    content: @Composable () -> Unit,
) {
    // Box wraps the anchor so the Popup positions relative to the anchor's bounds (a contextual popover), not the
    // whole parent. The anchor renders inline (in layout); the Popup is out-of-layout above it.
    Box {
        anchor?.invoke()
        if (visible) {
            val gapPx = with(LocalDensity.current) { NativeTheme.tokens.spacingXs.roundToPx() }
            val positionProvider = remember(alignment, gapPx) {
                AnchoredPopoverPositionProvider(alignment, gapPx)
            }
            Popup(
                popupPositionProvider = positionProvider,
                onDismissRequest = onDismissRequest,
                properties = PopupProperties(focusable = true),
            ) {
                NativeCard(
                    modifier = modifier,
                    variant = NativeCardVariant.Elevated,
                    contentPadding = PaddingValues(NativeTheme.tokens.spacingMd),
                    testTag = testTag,
                ) {
                    content()
                }
            }
        }
    }
}
