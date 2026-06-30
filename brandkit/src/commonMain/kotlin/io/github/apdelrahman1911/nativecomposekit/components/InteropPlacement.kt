package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.runtime.staticCompositionLocalOf

/** How an iOS `UIKitView`-backed control composites: a punched cut-out (default) vs above the canvas (overlay). */
internal enum class BrandInteropPlacement { Cutout, Overlay }

/**
 * Lets a container request that the iOS `UIKitView`-backed controls inside it use OVERLAY placement
 * (`placedAsOverlay = true`) instead of the default cut-out.
 *
 * The only consumer today is [BrandDialog]. A `Dialog` mounts a fresh iOS scene; with cut-out placement the
 * transparent hole is punched the instant a control composes, but CMP defers inserting the native view to the
 * next presented frame — so for the first frame(s) the hole reveals the dialog's host backdrop (black in dark
 * mode), the reported "black flash" behind dialog buttons. Overlay placement punches NO hole: the native view
 * simply composites above the opaque, Compose-drawn dialog card once inserted, so the card's own pixels show in
 * the meantime and there is no flash. Dialogs don't scroll, so overlay's only downside (it is not scroll-aware,
 * so it can drift during an active scroll) does not apply.
 *
 * Default [Cutout] everywhere else — cut-out is the correct placement for controls inside a Compose scroll.
 * iOS-only effect; the Android renderers ignore it.
 */
internal val LocalBrandInteropPlacement = staticCompositionLocalOf { BrandInteropPlacement.Cutout }
