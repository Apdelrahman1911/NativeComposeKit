# Draft upstream issue — file at https://github.com/JetBrains/compose-multiplatform/issues

Formatted for the Compose Multiplatform issue template. Copy the body below into a new issue. Where
you see `📎 ATTACH:`, drag the named file from `docs/upstream/media/` onto the issue so GitHub embeds
it at that spot.

---

**Title:** iOS: UIKit interop views lag many frames behind the Compose layout during animated visibility on physical devices (delayed insert/remove, views drawn outside their container; simulator unaffected)

## Bug Description

All UIKit-side interop mutations (insert, frame/position update, removal, and the `onRelease`
callback) are queued into `UIKitInteropMutableTransaction` and executed only when the next rendered
frame is presented. On a **physical device**, wrapping `UIKitView` content in `AnimatedVisibility`
inside lazy-list items makes the UIKit side fall visibly **out of sync with the Compose layout**:

- during the collapse/expand animation the native views lag several-to-many frames behind the
  Compose content — a control belonging to a collapsing row stays on screen well after its row has
  visually closed, and disappears only after a noticeable delay when the queued actions catch up;
- while lagging, a view can be drawn **outside the bounds of the container it visually belongs to**
  (its card/row has already shrunk or moved, the native view has not), which reads as broken UI;
- newly expanded rows' views appear late for the same reason (their insert action waits in the
  backlog — instrumentation showed inserts still unexecuted 2.5 s+ after composition);
- under a **continuous** animation cycle the backlog does not drain for as long as the churn
  continues, so the desync looks stuck until the cycling stops, after which delayed actions
  eventually execute;
- the Compose side (composition, layout, effects, `onGloballyPositioned`) runs normally throughout —
  the desync is entirely on the UIKit-action side.

With `placedAsOverlay = true` the lagging views are fully visible on top of the canvas. With the
default cut-out placement the same lag is mostly hidden (views sit behind the opaque canvas), but
empty cut-out holes flash the dark backdrop while their fill action waits in the queue.

Separately from the delay, there is a narrower **outright-loss** window, reproducible on the
simulator under frame pressure (flipping light/dark appearance mid-cycle): removals/position updates
that never execute at all, leaving ghost controls until some later layout change happens to move the
node. Two structural details make that loss unrecoverable from user code:

1. A transaction retrieved for a frame that is never presented is discarded without re-queueing
   (`UIKitInteropContainer.retrieveTransaction()` → `UIKitInteropTransaction.performTransaction()`),
   while the Kotlin-side bookkeeping (`interopViews` map) was already updated eagerly in
   `place`/`unplace`.
2. `UIKitInteropElementHolder.layoutAccordingTo` caches `currentClippedRect`/`currentUnclippedRect`
   at **schedule** time and early-returns on equality, so a lost position update is never re-sent —
   the view and the holder's cache disagree until the node's rect changes for some other reason.

## Performance Issue Description

- **What specific issue:** delayed / out-of-sync execution of queued interop transactions relative
  to the presented Compose frames (UIKit mutations landing many frames late; in a narrower window,
  not at all) — visible desynchronization rather than a recoverable frame drop.
- **Patterns / circumstances:** only on physical hardware (iPhone 17, 120 Hz ProMotion); both Debug
  and Release configurations; triggered specifically by *animated* visibility of interop views
  (per-frame clip/frame actions during `AnimatedVisibility` enter/exit). The identical insert/remove
  RATE without animation (plain `if` gating) stays frame-accurate indefinitely on the same device.
  On the simulator only the outright-loss form reproduces, and only under extra frame pressure
  (appearance flips mid-cycle).

## Affected Platforms

- [ ] Desktop (Windows, Linux, macOS)
- [x] iOS
- [ ] Android
- [ ] Web (K/Wasm) - Canvas based API
- [ ] Web (K/JS) - Canvas based API
- [ ] Web (K/JS) - HTML library

## Versions

- Compose Multiplatform version*: 1.11.0 (interop sources also diffed against compose-ui 1.11.1 — identical)
- Kotlin version*: 2.3.21
- OS (name, version, arch): device iOS 26.5.1 (arm64); built on macOS (arm64), Xcode 26.5
- Device (model or simulator for iOS issues): iPhone 17 (iPhone18,3) — reproduces; iPhone 17 Pro **simulator** on the same OS — only the milder loss form under appearance-flip pressure
- JDK (for desktop issues): n/a
- Browser (for Web issues): n/a

## Reproduction Steps

1. Build the sample code below into an iOS app and run it **on a physical device** (Debug or
   Release — both reproduce).
2. Let the auto-cycle run.
3. Observe: the switches visibly lag the collapse — they stay on screen after their rows have closed
   (drawn outside/over the collapsed cards), and disappear only after a delayed catch-up; newly
   expanded rows' switches appear late. While the cycle keeps running, the backlog keeps the UIKit
   side desynced continuously.
4. Replace the `AnimatedVisibility` with `if (expanded)` (everything else identical) and rerun —
   the native views stay frame-accurate with the layout.

No error is logged — the failure is silent visual desynchronization:

```
// no exception/log output; see "Profiling Data" below for the instrumented evidence
```

## Expected Behavior

UIKit interop mutations stay synchronized with the presented Compose frames — or degrade by at most
a frame or two under load, with the backlog draining promptly instead of growing while an animation
cycles. Actions are executed exactly once regardless of presentation timing (re-queued if a present
is skipped), and a position update that was never delivered is not deduplicated away by the holder's
rect cache. Animated visibility around interop content may degrade gracefully (views popping in
late) but must never leave views desynced or lost.

## Screenshots/video

Device, mid-cycle — the visible toggles/sliders/segmented controls are interop views lagging the
Compose layout: their rows have already collapsed, the views are drawn outside/over the collapsed
cards and disappear only after a delayed catch-up:

📎 ATTACH: `01-device-wedge-stranded-controls.jpeg`

Device, rows collapsed while the cycle continues — the lagging views are still present well after
their rows closed; they clear only once the queued actions catch up:

📎 ATTACH: `02-device-wedge-collapsed-pile.jpeg`

Simulator's outright-loss form (appearance flip during the cycle) — removals/positions that never
executed leave a ghost pile:

📎 ATTACH: `03-sim-unfixed-ghost-pile.png`

## Sample Code

Self-contained; auto-cycles visibility of UIKit switches in a lazy list:

```kotlin
@Composable
fun InteropChurnRepro() {
    var expanded by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) { while (true) { delay(650); expanded = !expanded } }
    LazyColumn(Modifier.fillMaxSize()) {
        items(8) { index ->
            AnimatedVisibility(expanded, enter = expandVertically(), exit = shrinkVertically()) {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("Row ${index + 1}")
                    UIKitView(
                        factory = { UISwitch().apply { on = index % 2 == 0 } },
                        modifier = Modifier.size(width = 51.dp, height = 31.dp),
                        properties = UIKitInteropProperties(placedAsOverlay = true),
                    )
                }
            }
        }
    }
}
```

## Profiling Data for Performance Issue

Instrumented on the device (walking the window hierarchy every 2.5 s and logging every
`InteropWrappingView`, plus a `DisposableEffect` per interop node):

- While the cycle runs, the wrapper set stays at the first expanded generation (same instances,
  same frames) across consecutive dumps spanning tens of seconds — including phases where the
  composed state has zero rows — i.e. the action backlog does not drain while animated churn
  continues. Once the cycling stops, delayed actions eventually execute and the views clear.
- Compose-side disposal streams at exactly the churn rate (~26 disposals/second, thousands of
  events, every disposed view still attached at dispose time) — composition and layout are healthy;
  the delay is entirely on the queued UIKit actions.
- Insert actions for newly created nodes were still unexecuted 2.5 s+ after composition
  (`view.superview == nil` at the deferred check).
- With plain `if` gating instead of `AnimatedVisibility` (same insert/remove rate, no per-frame
  animated clip/frame actions), the hierarchy dump oscillates exactly with the cycle (2 wrappers
  collapsed ↔ 18 expanded) indefinitely — no lag, no backlog.

## Additional Information

Mitigations attempted from user code, with results — included since they localize the defect:

**1. Synchronous detach at dispose** — bounds the *removal* delay to zero and kills the
outright-lost-removal ghosts, because `DisposableEffect.onDispose` runs in the composition apply
pass and does not ride the delayed queue:

```kotlin
// alongside the UIKitView, where `root` is the factory-returned view
DisposableEffect(root) {
    onDispose {
        root.hidden = true
        root.removeFromSuperview() // CMP's own deferred removal targets its wrapper; this is idempotent
    }
}
```

Result — the simulator's loss case becomes fully clean under the same appearance-flip stress:

📎 ATTACH: `04-sim-dispose-failsafe-clean.png`

…but it cannot bound the *insert* delay: a view whose insert action is still queued has nothing to
detach, so late appearance (and lagging positions of alive views) remain. No user-space equivalent
exists for the insert side.

**2. Out-of-band frame correction** — `onGloballyPositioned` fires on the Compose side for every
placement regardless of when the UIKit action lands, so the holder's expected frames can be
recomputed (same clipped/unclipped root-coordinates math) and written directly when the actual
frames diverge:

```kotlin
Modifier.onGloballyPositioned { coords ->
    val root = coords.findRootCoordinates()
    val clipped = root.localBoundingBoxOf(coords, clipBounds = true)      // wrapper frame (px)
    val unclipped = root.localBoundingBoxOf(coords, clipBounds = false)   // content-host frame (px)
    scheduleVerification(clipped, unclipped) // compare & setFrame if diverged
}
```

Two findings worth knowing upstream:

- Correcting **while a node is animating** visibly fights the CATransaction-synchronized writes
  (frames land out of band with the canvas) and makes things worse:

  📎 ATTACH: `05-sim-midflight-correction-fights-queue.png`

- Restricting corrections to the **trailing edge** (only ≥120 ms after the LAST placement, i.e. once
  the node has settled) heals lost settle-positions cleanly:

  📎 ATTACH: `06-sim-trailing-heal-settled-clean.png`

  …but it cannot help views whose insert is still queued (`superview == nil` — nothing to
  reposition), so it does not fix the device-side lag either.

**3. Usage-pattern workaround (works on device)** — never animate interop views' visibility; animate
the container's size and gate the content in one step:

```kotlin
// instead of: AnimatedVisibility(expanded) { ...UIKitView content... }
Box(Modifier.animateContentSize()) {
    if (expanded) {
        // ...UIKitView content...
    }
}
```

With this pattern the UIKit side stays frame-accurate indefinitely on the same device. This is what
we ship, but it caps animation quality: the native views necessarily appear a few frames after the
expansion starts, and any cut-out-placed interop (e.g. labels) briefly shows its dark empty hole
while the fill action waits in the queue.

Impact: any screen that conditionally shows native controls with animation (settings groups,
expandable list sections) shows visibly desynced/misplaced native views during and after the
animation on real devices — controls drawn outside their containers, late appearance, delayed
disappearance. Because the behavior needs physical hardware, it ships unnoticed past simulator-based
testing.
