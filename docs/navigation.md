# Navigation — bring your own; the kit renders native chrome

> This page covers the **contract**. For the shell's *layout integration* — the edge-to-edge host, the
> safe-area/inset plumbing, and how content scrolls under the Liquid Glass tab bar — see
> [`docs/native-chrome.md`](native-chrome.md).

**NativeComposeKit is not a navigation framework.** It ships UI components and a **nav-agnostic native chrome
contract** — a real iOS `UINavigationBar`, a Liquid Glass `UITabBar`, and a `UISheetPresentationController` that
any navigation system can drive. Your app owns its navigation stack outright; the kit never reads, mutates, or
mirrors it.

This is deliberate. The kit originally tried to co-own the stack across Swift and Kotlin, and that dual ownership
was the root cause of a push/pop loop on iOS (a native stack container fighting the Kotlin stack — any
`NavigationStack`/`UINavigationController` reconciling against it just relocated the loop). The fix made
**Compose the single owner**; once that was true, the kit no longer needed
to own navigation at all. What remains valuable — and stays in the library — is the *native chrome*, exposed
through a small contract.

## The contract (library — `io.github.apdelrahman1911.nativecomposekit.chrome`)

The contract is split so the platform-neutral part lives in `commonMain` — you implement and **unit-test your
chrome projection in shared code**, and only the one iOS-specific piece lives in `iosMain`:

- **`NativeChromeState(title, backTitle, canGoBack, selectedTabId, tabs, actions, sheetId)`** *(commonMain)* — an
  immutable, one-way projection the bars draw. Carries **no** route stack; `backTitle` is the previous screen's
  title (UIKit renders it in the native back button where the system shows a label); `sheetId` only tells the
  shell whether to present a sheet. `NativeChromeTab(id, title, sfSymbol)` / `NativeChromeAction(id, sfSymbol)`
  describe the tabs + top-bar actions.
- **`NativeChromeStateSource`** *(commonMain)* — the nav-agnostic core: state out + intents in.
  - `currentState(): NativeChromeState`, `observe(onChange): NativeChromeCancellable` (fires once immediately, then
    on every change).
  - `backRequested()`, `tabSelected(tabId)`, `actionTapped(actionId)`, `dismissSheet()` — turn a bar tap into your
    own navigation intent.
- **`NativeChromeSource : NativeChromeStateSource`** *(iosMain)* — adds the one genuinely-iOS member,
  `sheetViewController(): UIViewController?` (the Compose content for the current sheet, or null). This is the type
  the iOS chrome shell consumes.
- **`nativeSheetHostViewController(content)`** *(iosMain)* — builds a transparent, fully-scoped Compose host for a
  native `UISheetPresentationController` (so the Liquid Glass material shows through). Presentation stays the shell's job.

Any implementation must be a **dumb projection**: it may read your navigation state and emit intents, but it must
never expose or mutate a stack. Your navigation library stays the single source of truth.

## Adapt your navigation

Bring any navigation system. The recipe is always the same: implement `NativeChromeStateSource` (shared, testable)
by projecting your nav's current destination into `NativeChromeState` and forwarding bar taps as your own intents;
then, on iOS, wrap it as a `NativeChromeSource` by adding the sheet content.

**1. Project + intents (shared code).** Example over a `StateFlow`-based router — deliberately unlike the sample's
navigator, and unit-tested in `composeApp/src/commonTest/.../app/navigation/example/MiniRouterChromeSourceTest.kt`:

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
        )
    }
    // A StateFlow emits its current value on collect, then every change — exactly the observe contract.
    override fun observe(onChange: (NativeChromeState) -> Unit): NativeChromeCancellable {
        val job = scope.launch { router.state.collect { onChange(currentState()) } }
        return NativeChromeCancellable { job.cancel() }
    }
    override fun backRequested() = router.pop()
    override fun tabSelected(tabId: String) = router.select(tabId)
    override fun actionTapped(actionId: String) = router.onAction(actionId)
    override fun dismissSheet() = router.closeSheet()
}
```

**2. Add the iOS sheet (iOS code).** Expose it as a `NativeChromeSource`, e.g. by delegating the shared core:

```kotlin
class MyIosChromeSource(
    private val base: MyChromeSource,
    private val sheetContent: (sheetId: String) -> (@Composable () -> Unit)?,
) : NativeChromeSource, NativeChromeStateSource by base {
    override fun sheetViewController(): UIViewController? =
        base.currentState().sheetId?.let(sheetContent)?.let(::nativeSheetHostViewController)
}
```

Hand `MyIosChromeSource` to your iOS shell (see `NativeNavShell.swift` + `createNativeNavRoot()` for how the sample
wires the bars and presents the sheet). Nothing else changes — the same native chrome renders your navigation.

**Adapter sketches for common libraries** (each projects the same six `NativeChromeState` fields):

- **Voyager** — `currentState()` reads `tabNavigator.current` + `navigator.lastItem`/`navigator.size`;
  `backRequested()` → `navigator.pop()`; `tabSelected(id)` → `tabNavigator.current = tabFor(id)`. Bridge `observe`
  from a `snapshotFlow { navigator.lastItem }`.
- **Decompose** — project from the `Value<ChildStack<*, *>>`: `canGoBack = childStack.backStack.isNotEmpty()`,
  `backRequested()` → `navigation.pop()`. `observe` uses `value.subscribe { … }` and returns the unsubscribe
  handle as the `NativeChromeCancellable`.
- **Compose Navigation (`NavController`)** — `observe` collects `navController.currentBackStackEntryFlow`;
  `canGoBack = navController.previousBackStackEntry != null`; `backRequested()` → `navController.popBackStack()`;
  `tabSelected(id)` → `navController.navigate(id) { launchSingleTop = true }`.

The `observe` contract (fire once immediately, then on every change) is the only subtlety: most nav libraries
already expose their state as a `Flow`/observable, so bridge that and be sure to emit the current value first.

## Reference wiring (sample app — `composeApp/.../app/navigation/`)

The sample ships a small, self-contained navigator so the catalog has something to navigate — **reference wiring,
not library API.** A real consumer swaps it for its own navigation library (Nav3, Voyager, Decompose, …) and
writes the equivalent `NativeChromeSource` adapter.

- **`NativeNavigator`** (`@Stable`) — the sample's source of truth: `push` / `pop` / `popToRoot` / `selectTab` /
  `presentSheet` / `dismissSheet` / `replaceStack`, one `SnapshotStateList<NativeRoute>` **per tab** (switching tabs
  preserves each tab's depth), and `sheet: NativeRoute?`. `snapshot()` / `observe()` give an ObjC-friendly
  projection that works **without a running composition** (the iOS case). `push` is idempotent at the top (a
  repeated push of the current route is a no-op — absorbs double taps).
- **`NativeRoute { val id }` / `NativeTab { val id }`** — the app declares its own sealed route hierarchy; `id` is
  stable/unique per destination and is the `AnimatedContent` key.
- **`nativeNavGraph { screen<R> { route -> … } }`** — route → `@Composable`, matched by reified route type.
- **`NativeNavHost` / `NativeNavContent`** — the Compose renderer. `NativeNavHost` wraps `NativeNavContent` in
  Material chrome (Android); `NativeNavContent` is content-only (the iOS native-chrome shell wraps it instead).
- **`NativeNavChrome`** — adapts `NativeNavigator` into `NativeChromeSource`. This is the file to read as the
  reference for writing your own adapter.

## How the two platforms render

**Android** — `NativeNavHost(navigator, graph, tabs, title, actions)`: the selected tab's top route in an
`AnimatedContent` (push/pop slide by stack delta), a Material `TopAppBar` (back when depth > 1 → `pop()`), a
`NavigationBar` → `selectTab`, and a `ModalBottomSheet` for `state.sheet`. System/predictive back → `pop()`.

**iOS** — Compose owns the stack in **one** `ComposeUIViewController` rendering `NativeNavContent(renderSheet = false)`.
A Swift shell (`iosApp/iosApp/Native/NativeNavShell.swift`) renders the real native `UINavigationBar` + Liquid
Glass `UITabBar` and presents the native `UISheetPresentationController`, driven **one-way** by the
`NativeChromeSource` from `createNativeNavRoot()`. The left-edge back-swipe is Compose's
`androidx.compose.ui.backhandler.BackHandler` → `navigator.pop()`. No SwiftUI/UIKit container owns a second stack.

## Tests

- `composeApp/src/commonTest/.../app/navigation/NativeNavigatorTest` covers the reference navigator (per-tab
  stacks, push/pop/replace/sheet, `observe` delivery, idempotent top-push).
- `composeApp/src/commonTest/.../app/navigation/example/MiniRouterChromeSourceTest` proves the chrome contract is
  nav-agnostic: a second, `StateFlow`-based navigator implements `NativeChromeStateSource` and is driven through
  the interface (projection, intents, `observe`) — entirely in shared code.
