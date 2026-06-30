# iOS interop notes

The iOS renderers host real UIKit controls inside Compose with `UIKitView`. That gives you genuine
native controls, but it also inherits a few Compose Multiplatform 1.11 behaviors that have no clean
fix through public API. This page records the three that matter, why they happen, and the trade-off
the kit makes for each. All three were reproduced on CMP 1.11.0 / Kotlin 2.3.21, iOS 26 (simulator
and a physical arm64 device), and reported upstream.

## 1. Scroll: clip vs. drift

A `UIKitView` inside a Compose scroll (`LazyColumn`, `verticalScroll`) can't be kept both unclipped
and perfectly in sync with the scroll. CMP positions the native view with `setFrame` scheduled into
the renderer's `CATransaction`; the scrolled pixels and the matching frame update travel different
present paths, so on a frame where they land a tick apart you see an artifact:

- `placedAsOverlay = false` (cut-out, the default) — the control's leading/top edge is briefly
  clipped during the gesture.
- `placedAsOverlay = true` (overlay) — no clipping, but the control lags slightly behind the
  surrounding rows and snaps back when scrolling stops.

There's no `UIScrollView`/`contentOffset` bridge exposed to fuse the two, and re-measure cadence
doesn't change it. **The kit's choice:** leaf controls (Toggle, Slider, Stepper, SegmentedControl,
SearchBar, PageControl, ColorWell, DatePicker) use overlay via `scrollSafeInteropProperties()` — the
drift is subtler than a hard clipped edge and settles instantly. Button-family controls stay cut-out.

Upstream: [CMP-10398](https://youtrack.jetbrains.com/issue/CMP-10398). Related: CMP-3525, CMP-9728,
CMP-3534.

## 2. Menu buttons: drift after first open

A `UIButton` with a native `UIMenu` (`showsMenuAsPrimaryAction`, or a `UIContextMenuInteraction`)
tracks the scroll correctly until its menu is presented once. After the menu has opened and
dismissed, the button drifts from its row on later scrolls. Presenting the menu leaves a residual
`transform` on a CMP-owned interop ancestor, and CMP's `setFrame` composes with it rather than
clearing it.

**The kit's choice:** keep the native `UIMenu` (it's the right iOS affordance) and accept the
post-open drift. Removing it would mean dropping the native menu for a Compose-drawn dropdown.

Upstream: [CMP-10399](https://youtrack.jetbrains.com/issue/CMP-10399). Related: CMP-10132.

## 3. Dialog / popup: first-frame backdrop

When a `UIKitView` first appears inside a freshly opened Compose `Dialog` or `Popup`, there's a
one-frame window where the interop region shows the host backdrop (black in dark mode) before the
native view is inserted. Compose-drawn content in the same dialog doesn't do this — it's specific to
the interop "hole" becoming visible a frame before the native view paints.

**The kit's choice:** `BrandDialog` removes the flash with two levers. Its body is composed with
`LocalBrandSurface = Unspecified`, so `BrandText` takes its Compose-`Text` path and creates no
interop region for text at all. Its action controls are given overlay placement, so they composite
above the opaque card with no cut-out hole — the card's own pixels show from the first frame. Dialogs
don't scroll, so overlay's drift trade-off doesn't apply here.

Upstream: [CMP-10400](https://youtrack.jetbrains.com/issue/CMP-10400). Related: CMP-7509, CMP-8114.

## Practical guidance

- Don't put many menu-bearing buttons in a long scroll if the post-open drift would be noticeable;
  prefer a non-menu action or present the menu from a fixed bar.
- For sheet-style content use `BrandSheet` (a real `UISheetPresentationController`) rather than
  stacking interop controls in an ad-hoc scrolling container.
- Give content-sized interop controls an explicit width — see the usage notes in the
  [README](../README.md#usage-notes) and the component reference.
