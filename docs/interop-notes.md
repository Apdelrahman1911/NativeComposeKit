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

## 4. Deferred interop transactions: delayed/out-of-sync mutations under animated visibility

Every UIKit-side interop mutation — inserting a view, **every frame/position update**, the final
removal, and the `onRelease` callback — is queued into an internal CMP transaction
(`UIKitInteropMutableTransaction`) and executed only when the next rendered Compose frame is
**presented**. Two failure shapes follow, both verified against the CMP 1.11 sources and on real
hardware (iPhone 17, Debug AND Release — the simulator only shows the milder form under forced frame
pressure):

- **Delay / desync (the dominant device behavior).** Animating the *visibility* of interop views
  (`AnimatedVisibility` enter/exit around native controls) floods the queue with per-frame
  insert/remove/clip actions and the UIKit side falls visibly behind the Compose layout: controls
  lag their collapsing row, get drawn **outside the container they belong to**, appear late on
  expand, and clear only after a delayed catch-up. Under a continuous cycle the backlog does not
  drain while the churn runs, so the desync looks stuck until it stops. The identical insert/remove
  rate WITHOUT animation (plain `if` gating) stays frame-accurate indefinitely on the same device.
- **Outright loss (narrower window).** A transaction retrieved for a frame that never presents is
  discarded without re-queueing, while Compose's bookkeeping has already moved on; a per-holder rect
  cache then dedupes the never-delivered update so it is never re-sent. Visible as ghost/doubled
  controls with the kit's overlay placement (reproduced on the simulator with light/dark flips
  during churn).

Reproduced hands-free on the sample app's **Settings → Developer → "Interop churn test"** screen —
the pathological `AnimatedVisibility` flavor is kept behind its off-by-default "Reproduce the wedge"
toggle. An upstream report with a self-contained repro lives in
[`docs/upstream/cmp-interop-transaction-lag.md`](upstream/cmp-interop-transaction-lag.md); the root
fix (re-queue on skipped presents, no dedup of undelivered rects) is engine-level.

**Kit mitigations (shipped):**

- `InteropDisposeFailSafe` (beside every kit `UIKitView`): a `DisposableEffect` detaches the factory
  root synchronously at node disposal — `onDispose` runs in the composition apply pass and does not
  ride the queue, so the *removal* delay is bounded to zero and lost-removal ghosts/doubles cannot
  occur.
- `InteropPositionHeal` (same coverage): `onGloballyPositioned` re-derives the holder's expected
  frames and — only on the **trailing edge**, ≥120ms after the last placement — corrects the actual
  frames when they diverge, healing lost settle-positions. (Correcting mid-animation was tested and
  rejected: out-of-band writes visibly fight the CATransaction-synchronized frame updates.)
- Neither can bound the *insert* side — a view whose insert action is still queued has nothing user
  code can reach. Hence the hard rules below.

**Hard rules for app code (iOS):**

1. **Never wrap native controls in `AnimatedVisibility`** (or any per-frame animated clip). Use
   [`NativeCollapsible`] — a real `AnimatedVisibility` on Android, and on iOS a container-size
   animation with one-step gating.
2. Conditionally inserted native controls always appear a frame or two late (the queue is
   asynchronous by design); a design that animates their *entrance* will never be frame-perfect.
   For a fully native feel, prefer disclosure by navigation (push / `NativeSheet`) or keep controls
   permanently composed and dim/disable them.
3. Bare `NativeText` on a solid surface inside a collapsible region briefly shows its cut-out hole
   (dark backdrop) while the fill action is queued. `NativeCollapsible` switches `NativeText` inside
   it to Compose rendering automatically; `NativeListItem` text is always Compose-drawn and safe.

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
