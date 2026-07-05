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

**The kit's choice:** `NativeDialog` removes the flash with two levers. Its body is composed with
`LocalNativeSurface = Unspecified`, so `NativeText` takes its Compose-`Text` path and creates no
interop region for text at all. Its action controls are given overlay placement, so they composite
above the opaque card with no cut-out hole — the card's own pixels show from the first frame. Dialogs
don't scroll, so overlay's drift trade-off doesn't apply here.

Upstream: [CMP-10400](https://youtrack.jetbrains.com/issue/CMP-10400). Related: CMP-7509, CMP-8114.

## 4. Deferred interop transactions can drop mutations (ghost / doubled / stale controls)

Every UIKit-side interop mutation — inserting a view, **every frame/position update**, the final
removal, and the `onRelease` callback — is queued into an internal CMP transaction
(`UIKitInteropMutableTransaction`) and executed only when the next rendered Compose frame is actually
**presented**. A transaction retrieved for a frame that never presents (a dropped frame or display-link
hiccup — easy to hit during layout storms: an appearance flip, `AnimatedVisibility` churn inside lazy
items) is discarded without re-queueing, while Compose's own bookkeeping has already moved on. A
per-holder rect cache compounds it: a lost position update is deduped against a rect the view never
received, so it is never re-sent.

The failure is placement-asymmetric. With cut-out placement a leaked view hides behind the opaque
canvas — invisible. With the kit's **overlay** placement (`scrollSafeInteropProperties`) the identical
loss is fully visible: **ghost controls** hanging in the window after their row collapsed, **doubled
controls** when a lazy item is recreated (old removal lost + new instance inserted), **stale positions**
after rows shift. Reproduced deterministically on the sample app's **Settings → Developer → "Interop
churn test"** screen by flipping light/dark appearance while it auto-cycles.

**Kit mitigation (`InteropDisposeFailSafe`, applied beside every `UIKitView` the kit hosts):** a
`DisposableEffect` detaches the factory root synchronously at node disposal. `onDispose` runs in the
composition apply pass — it does not ride the losable transaction — so the control leaves the window the
moment its node is disposed, whatever happens to the queued container cleanup. This kills the
*permanent* artifact classes (ghosts, doubles) deterministically. Lost *position* updates of
still-composed views remain a CMP-level issue with no clean external fix: they self-correct on the next
layout change of that node (next animation frame, scroll, or churn cycle), so they can only appear as a
transient lag under extreme frame pressure, not as a settled corruption. Worth an upstream report —
overlay placement is experimental in CMP 1.10/1.11.

## Practical guidance

- Don't put many menu-bearing buttons in a long scroll if the post-open drift would be noticeable;
  prefer a non-menu action or present the menu from a fixed bar.
- For sheet-style content use `NativeSheet` (a real `UISheetPresentationController`) rather than
  stacking interop controls in an ad-hoc scrolling container.
- Give content-sized interop controls an explicit width — see the usage notes in the
  [README](../README.md#usage-notes) and the component reference.

## Deferred: Liquid Glass refraction vs the opaque `pinFilling` backing

The pinFilling-backed leaf controls (toggle / slider / segmented / search / date picker / color well)
sit on an **opaque, surface-colored backing** so their transparent pixels never reveal the interop host
backdrop. On an iOS 26 device that opacity also blocks the Liquid Glass **refraction** under the control
when the control sits on a material surface. Deferred pending visual tuning on real iOS 26 hardware:
candidate directions are a translucent/material backing, or gating the backing on solid surfaces only
(`LocalNativeSurface` already distinguishes them). Referenced from `docs/design-system-rules.md` §Open.

## Interop views do not animate with Compose transitions (shared-canvas rule)

A `UIKitView`-backed control is a real UIView composited beside the Compose canvas (cut-out) or above it
(overlay). Compose enter/exit transitions animate **Compose pixels only** — a `graphicsLayer` alpha/fade on
an ancestor never reaches the native view, so during an animated content change the outgoing content's
native controls stay fully opaque until the transition finishes and the outgoing composition is disposed
(seen as: the Library tab's `UISegmentedControl` floating over the incoming screen during an animated tab
switch). Slides are tolerable (interop views track node position, with the documented one-frame lag);
fades/scales are not.

**Scope: this bites wherever multiple screens share ONE Compose canvas** — Android's `NativeNavHost`
(NavDisplay inside the Material shell) and the iOS pure-Compose fallback (`MainViewController()`), plus any
in-screen container like `NativeContentState`. The rule there: a container that animates BETWEEN contents
which may hold interop-backed controls must **swap instantly** (the sample's tab switches do — also the
native `UITabBarController` convention) or use **full-width symmetric slides** (the sample's push/pop — the
incoming edge abuts the outgoing edge, so the previous screen's controls are never over the other screen);
never fade or parallax. `NativeContentState`'s cross-fade is acceptable because its loading/empty/error
placeholders are Compose-drawn; if your `content` holds interop controls, they will snap rather than fade.

**The iOS native shell is exempt — structurally.** There every stack entry is its own `UIViewController`
(see [`docs/navigation.md`](navigation.md)), and a screen's interop views live inside that controller's own
view hierarchy, so UIKit's transitions move them **with** their screen — the authentic parallax + dim of the
interactive pop is safe, and the cross-screen "floating control" class cannot occur between screens. The
in-screen guidance (fades inside one screen's content) still applies.
