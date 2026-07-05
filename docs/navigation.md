# Navigation — bring your own; the kit renders native chrome

> This page covers the **contract and the ownership model**. For the shell's *layout integration* — the
> edge-to-edge host and how content scrolls under the Liquid Glass tab bar — see
> [`docs/native-chrome.md`](native-chrome.md).

**NativeComposeKit is not a navigation framework.** It ships UI components and a **nav-agnostic native chrome
contract** — a real iOS `UITabBarController`, per-tab `UINavigationController`s, and a
`UISheetPresentationController` that any navigation system can drive. Your app owns its navigation stack
outright; the kit never owns one.

## The ownership model: a ratified projection

This design is deliberate, and its history matters. The kit originally tried to **co-own** the stack across
Swift and Kotlin — a native stack container reconciling two-way against the Kotlin stack — and that dual
ownership caused a push/pop loop on iOS (spurious native callbacks were written back into Kotlin as
navigation; every guard just relocated the loop). The fix made **Kotlin the single owner** and reduced native
chrome to a one-way projection onto bare bars. That was loop-proof but had one visible cost: a bare
`UINavigationBar` has no seekable transition, so during the interactive back swipe the bar could only snap at
the end instead of tracking the finger.

The current shell keeps single ownership and fixes the swipe by upgrading the projection from *bare bars* to
*real containers*, under a strict protocol — a **ratified projection**:

1. **Kotlin owns the stack.** Per-tab stacks live in your navigation state (the sample's `NativeNavigator`).
   The native side holds a **mirror**: one `UINavigationController` per tab, one view controller per stack
   entry, written ONLY by the shell's reconciler, always *from* Kotlin state.
2. **UIKit owns the visual transition.** The system's interactive pop runs **speculatively on the mirror** —
   that is what makes the bar title, back button, and content all track the finger, with the authentic
   parallax, dim, and cancel-bounce.
3. **Commit is ratified; cancel is silence.** While the finger is down, Kotlin is untouched. If the user
   cancels, UIKit restores the mirror — Kotlin never learns the gesture existed. If the user commits, the
   shell reports **one idempotent, tab-scoped intent**: `backCommitted(tabId, entryId)` — "the user landed on
   this entry" — which your navigation ratifies (the sample maps it to `NativeNavigator.popTo`). The
   resulting state emission re-syncs the mirror to a no-op.
4. **Pops are the only UIKit-initiated change, and they are intents, not stack writes.** Any other divergence
   of the mirror (grew, reordered) is resolved by re-asserting the projection. Tab selection is
   projection-first: the shell reports the tap and returns `false` — UIKit never switches itself.

Why this cannot recreate the old loop: the mirror **never** writes stack state into Kotlin (the old
`reconcileStack` direction is gone); the ratifying intent is idempotent and target-based, so a duplicate or
stale report converges instead of compounding; applying Kotlin state produces no intents (the settle-check
compares against what was applied); and the reconciler is **level-triggered** — every sync reads the current
state fresh and converges the mirror to it, so no queued stale snapshot can ever be applied. There are no
timing heuristics anywhere.

Two structural rules keep the mirror legal by construction:

- **One view controller per stack entry, never shared.** Route ids are unique per stack (the sample's
  navigator enforces this), and the same id on two different tabs still gets two controllers. The historical
  `UINavigationController` corruption came from hosting one shared content controller.
- **Route content must not register Compose back handlers** (`BackHandler` / `PredictiveBackHandler`) or host
  its own navigation renderer (`NavDisplay`) when rendered inside the native shell. Compose Multiplatform's
  edge-gesture recognizers arm only when a handler registers — and when armed they are designed to defeat
  every other recognizer, including UIKit's interactive pop. In the native shell, the platform owns back.

## The contract (library — `io.github.apdelrahman1911.nativecomposekit.chrome`)

Split so the platform-neutral part lives in `commonMain` — you implement and **unit-test your chrome
projection in shared code** — and only the iOS-specific pieces live in `iosMain`:

- **`NativeChromeState(title, canGoBack, selectedTabId, tabs, actions, sheetId, backTitle, backStacksByTab)`**
  *(commonMain)* — an immutable projection. `backStacksByTab` (tabId → root-first `NativeChromeEntry(id,
  title, bar)` list) is what a **stack-rendering** shell renders — one native screen per entry; it is still
  display data, not a navigable stack. Each entry's `bar` is its **`NativeBarConfig`** — the per-screen
  chrome behavior (hide the top/tab bar while on top, this screen's own bar actions); see
  docs/native-chrome.md § Customizing the chrome. Flat shells (a bare title bar) can ignore
  all of it and use `title`/`backTitle`.
  `NativeChromeTab(id, title, sfSymbol)` / `NativeChromeAction(id, sfSymbol)` describe tabs + top-bar actions;
  `sheetId` only says whether a sheet should be up.
- **`NativeChromeStateSource`** *(commonMain)* — the nav-agnostic core: state out + intents in.
  - `currentState(): NativeChromeState`, `observe(onChange): NativeChromeCancellable` (fires once immediately,
    then on every change).
  - Intents: `backRequested()` (a flat shell asking permission **before** anything moves),
    `backCommitted(tabId, entryId)` (a stack shell ratifying a pop the platform **already performed** — must be
    idempotent; default no-op), `tabSelected(tabId)`, `actionTapped(actionId)`, `dismissSheet()`.
- **`NativeChromeSource : NativeChromeStateSource`** *(iosMain)* — adds the Compose-content suppliers:
  `sheetViewController(): UIViewController?` and `contentViewController(entryId): UIViewController?` — a
  **fresh** host per new entry (the shell owns it for that entry's lifetime; never return a shared one).
- **`nativeContentHostViewController(content)` / `nativeSheetHostViewController(content)`** *(iosMain)* —
  standard hosts: opaque + kit-scoped for stack entries; transparent for native sheets (so the Liquid Glass
  material shows through).

## Adapt your navigation

The recipe: implement `NativeChromeStateSource` in shared code by projecting your nav's state, forward bar
taps as your own intents, then on iOS add the content suppliers. A `StateFlow`-based example (deliberately
unlike the sample's navigator, unit-tested in
`composeApp/src/commonTest/.../app/navigation/example/MiniRouterChromeSourceTest.kt`):

```kotlin
class MyChromeSource(
    private val router: MyRouter,             // your navigation, whatever its shape
    private val tabs: List<NativeChromeTab>,
    private val scope: CoroutineScope,
) : NativeChromeStateSource {
    override fun currentState(): NativeChromeState = router.state.value.let { s ->
        NativeChromeState(
            title = s.top.title,
            canGoBack = s.stack.size > 1,
            selectedTabId = s.tabId,
            tabs = tabs,
            actions = emptyList(),
            sheetId = s.sheet?.id,
            backStacksByTab = s.stacksByTab.mapValues { (_, stack) ->
                stack.map { NativeChromeEntry(it.id, it.title) }
            },
        )
    }
    // A StateFlow emits its current value on collect, then every change — exactly the observe contract.
    override fun observe(onChange: (NativeChromeState) -> Unit): NativeChromeCancellable {
        val job = scope.launch { router.state.collect { onChange(currentState()) } }
        return NativeChromeCancellable { job.cancel() }
    }
    override fun backRequested() = router.pop()
    override fun backCommitted(tabId: String, entryId: String) = router.popTo(tabId, entryId) // idempotent!
    override fun tabSelected(tabId: String) = router.select(tabId)
    override fun actionTapped(actionId: String) = router.onAction(actionId)
    override fun dismissSheet() = router.closeSheet()
}
```

Your `popTo(tabId, entryId)` must be: no-op when `entryId` is already the top (duplicate report), no-op when
it is absent (stale report — the shell re-syncs), tab-scoped (never touch another tab), and it must pop to
**exactly** that entry. Get those four properties right and the ratification protocol is safe for any router.

**Adapter sketches for common libraries** (same idea — project entries, ratify with the library's own pop):

- **Voyager** — entries from `navigator.items`; `backCommitted` → `navigator.popUntil { it.key == entryId }`.
- **Decompose** — entries from the `ChildStack`; `backCommitted` → `navigation.popTo(index of entryId)`.
- **Compose Navigation (`NavController`)** — entries from `currentBackStack`; `backCommitted` →
  `popBackStack(entryId, inclusive = false)`.

## Reference wiring (sample app — `composeApp/.../app/navigation/`)

The sample ships a small, self-contained navigator so the catalog has something to navigate — **reference
wiring, not library API.** A real consumer swaps it for its own library and writes the equivalent adapter.

- **`NativeNavigator`** (`@Stable`) — the sample's source of truth: `push` / `pop` / `popTo(tab, entryId)` /
  `popToRoot` / `selectTab` / `presentSheet` / `dismissSheet` / `replaceStack`, one
  `SnapshotStateList<NativeRoute>` **per tab**, and `sheet: NativeRoute?`. `snapshot()` / `observe()` work
  without a running composition (the iOS case). `push` is idempotent at the top and **fails fast on a
  duplicate id deeper in the stack** — ids are the entry identity every renderer keys on.
- **`NativeNavChrome`** — adapts `NativeNavigator` into `NativeChromeSource` (projection, ratification via
  `popTo`, per-entry content hosts, and per-screen chrome behavior via `barConfigForRoute` — the same
  lambda the Material host takes as `barConfig`). This is the file to read before writing your own adapter.
- **`nativeNavGraph { screen<R> { route -> … } }`** — route → `@Composable`, matched by reified route type.
- **`NativeNavHost` / `NativeNavContent`** — the **Compose-canvas** renderer used where the native shell
  isn't: all of Android, and the iOS pure-Compose fallback (`MainViewController()`).

## How the two platforms render

**Android** — `NativeNavHost(navigator, graph, …)`: Material chrome around `NativeNavContent`, which hosts
the selected tab's stack on **Navigation 3's `NavDisplay`** (`androidx.navigation3` runtime + the
multiplatform `org.jetbrains.androidx.navigation3` UI — app-level dependencies; the kit stays
dependency-free). NavDisplay owns transitions, per-entry saveable state, and the **predictive back preview**:
the manifest opts in with `android:enableOnBackInvokedCallback="true"`, so the system back gesture seeks the
pop and the previous screen peeks in as you swipe. Transitions are full-width symmetric slides and tab
switches swap instantly — both are interop-safety requirements on a shared Compose canvas (see
[`docs/interop-notes.md`](interop-notes.md)).

**iOS** — the native shell (`iosApp/iosApp/Native/NativeNavShell.swift` + `createNativeNavRoot()`): a real
`UITabBarController`, one `UINavigationController` per tab, one Compose screen per stack entry, run as the
ratified projection described above. The interactive back swipe, the bar-item cross-fade under the finger,
the parallax + dim, tab behavior (instant switch; re-tap pops to root via the navigator) — all of it is
stock UIKit driving the mirror, ratified into Kotlin only on commit. Because every screen is its own view
controller, its interop-backed native controls travel **with** it through transitions — the cross-screen
"ghost" class from the shared-canvas days cannot occur here. The pure-Compose fallback
(`MainViewController()`) still renders everything in one `ComposeUIViewController` with NavDisplay (and gets
Compose's own in-canvas predictive back).

## Tests

- `composeApp/src/commonTest/.../app/navigation/NativeNavigatorTest` — per-tab stacks, push/pop/replace/sheet,
  `observe` delivery, idempotent top-push, **unique-id enforcement**, and the `popTo` ratification properties
  (target truncation, idempotence, absent-id no-op, tab scoping).
- `composeApp/src/commonTest/.../app/navigation/example/MiniRouterChromeSourceTest` — proves the chrome
  contract is nav-agnostic: a second, `StateFlow`-based navigator drives it entirely in shared code.
- The shell's debug builds log a `NCK-Shell: DESYNC` tripwire if any native stack ever diverges from the
  projection outside a transition — the loud regression alarm for the historical bug class.
