package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import io.github.apdelrahman1911.nativecomposekit.theme.BrandTheme

/**
 * A transient popover that floats a small surface near an [anchor].
 *
 * **Native-per-platform, phone-first (kit thesis).**
 * - **iOS, iPad / regular width** → a real `UIPopoverPresentationController` (system material + arrow), anchored
 *   to the [anchor]'s on-screen rect, hosting the Compose [content].
 * - **iOS, iPhone / compact width** → a **polished Compose popover** ([ComposeBrandPopover]): a lightweight
 *   elevated [BrandCard] in a `Popup` anchored to the [anchor]. This avoids UIKit's full-screen popover
 *   adaptation at compact width (a tiny panel shouldn't take the whole screen on a phone). Pure Compose ⇒ no
 *   `UIKitView` interop hole ⇒ no black/white backdrop artifact; it uses brand surface/elevation/corner/spacing/
 *   typography and adapts to light/dark via the theme.
 * - **Android** → the same Compose popover.
 *
 * Shown while [visible]; dismisses on an outside tap via [onDismissRequest]. [anchor] (**additive, optional**)
 * is the trigger rendered inline in layout; the popover points at it. [alignment] positions the Compose popover
 * relative to the anchor; on the iPad native path it's a hint (UIKit picks the arrow direction). [modifier]/
 * [testTag] apply to the surface. If no presenter/anchor resolves on the iPad native path, it falls back to a
 * centred presentation safely.
 *
 * `BrandPopover(visible, { open = false }, anchor = { BrandIconButton(moreIcon, { open = true }, …) }) { … }`
 */
@Composable
public fun BrandPopover(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.BottomCenter,
    testTag: String? = null,
    anchor: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    PlatformBrandPopover(visible, onDismissRequest, modifier, alignment, testTag, anchor, content)
}

@Composable
internal expect fun PlatformBrandPopover(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier,
    alignment: Alignment,
    testTag: String?,
    anchor: (@Composable () -> Unit)?,
    content: @Composable () -> Unit,
)

/**
 * The shared Compose popover surface: an elevated themed [BrandCard] in a `Popup`, anchored to [anchor]. Used by
 * the **Android** renderer and by the **iOS** renderer at **compact width** (iPhone). Pure Compose — no
 * `UIKitView`, so no interop backdrop artifact — and the card draws with brand surface/elevation/corner/spacing/
 * typography (`BrandCard` reads `LocalBrandSurface`) so it looks native-appropriate in light and dark.
 */
@Composable
internal fun ComposeBrandPopover(
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
            Popup(
                alignment = alignment,
                onDismissRequest = onDismissRequest,
                properties = PopupProperties(focusable = true),
            ) {
                BrandCard(
                    modifier = modifier,
                    variant = BrandCardVariant.Elevated,
                    contentPadding = PaddingValues(BrandTheme.tokens.spacingMd),
                    testTag = testTag,
                ) {
                    content()
                }
            }
        }
    }
}
