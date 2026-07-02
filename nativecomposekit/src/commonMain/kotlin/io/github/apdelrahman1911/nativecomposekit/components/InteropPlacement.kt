package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.runtime.staticCompositionLocalOf

/** How an iOS `UIKitView`-backed control composites: a punched cut-out (default) vs above the canvas (overlay). */
internal enum class NativeInteropPlacement { Cutout, Overlay }

/**
 * Lets a container request that the iOS `UIKitView`-backed controls inside it use OVERLAY placement
 * (`placedAsOverlay = true`) instead of the default cut-out.
 *
 * The only consumer today is [NativeDialog]. A `Dialog` mounts a fresh iOS scene; with cut-out placement the
 * transparent hole is punched the instant a control composes, but CMP defers inserting the native view to the
 * next presented frame — so for the first frame(s) the hole reveals the dialog's host backdrop (black in dark
 * mode), the reported "black flash" behind dialog buttons. Overlay placement punches NO hole: the native view
 * simply composites above the opaque, Compose-drawn dialog card once inserted, so the card's own pixels show in
 * the meantime and there is no flash.
 *
 * Placement trade in a SCROLL (see docs/interop-notes.md): a cut-out hole lags the Compose layer by a frame
 * and momentarily CLIPS the control's edge while scrolling; an overlay never clips but is not scroll-aware,
 * so it can lag/drift slightly during an active fling and snaps back at rest. The kit's chosen defaults:
 * the pinFilling-backed leaf controls use overlay (`scrollSafeInteropProperties` — the accepted subtle
 * drift beats the visible clip), while text/fields/buttons default here to [Cutout]. This local exists for
 * containers, like the dialog, whose situation inverts the trade.
 *
 * iOS-only effect; the Android renderers ignore it.
 */
internal val LocalNativeInteropPlacement = staticCompositionLocalOf { NativeInteropPlacement.Cutout }
