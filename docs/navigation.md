# Navigation — bring your own; the kit renders native chrome

**NativeComposeKit is not a navigation framework.** It ships UI components and a **nav-agnostic native chrome
contract** — a real iOS `UINavigationBar`, a Liquid Glass `UITabBar`, and a `UISheetPresentationController` that
any navigation system can drive. Your app owns its navigation stack outright; the kit never reads, mutates, or
mirrors it.

This is deliberate. The kit originally tried to co-own the stack across Swift and Kotlin, and that dual ownership
was the root cause of a push/pop loop on iOS (a native container fighting the Kotlin stack — see
`docs/interop-notes.md`). The fix made **Compose the single owner**; once that was true, the kit no longer needed
to own navigation at all. What remains valuable — and stays in the library — is the *native chrome*, exposed
through a small contract.

## The contract (library — `io.github.apdelrahman1911.nativecomposekit.chrome`, iOS)

Implement `NativeChromeSource` over whatever navigation you already use; the native chrome shell renders it:

- **`NativeChromeState(title, canGoBack, selectedTabId, tabs, actions, sheetId)`** — an immutable, one-way
  projection the bars draw. Carries **no** route stack; `sheetId` only tells the shell whether to present a sheet.
  `NativeChromeTab(id, title, sfSymbol)` / `NativeChromeAction(id, sfSymbol)` describe the tab bar + top-bar actions.
- **`NativeChromeSource`** — state out + intents in, nothing else:
  - `currentState(): NativeChromeState`, `observe(onChange): NativeChromeCancellable` (fires once immediately, then
    on every change).
  - `backRequested()`, `tabSelected(tabId)`, `actionTapped(actionId)`, `dismissSheet()` — turn a bar tap into your
    own navigation intent.
  - `sheetViewController(): UIViewController?` — the Compose content for the current sheet, or null.
- **`nativeSheetHostViewController(content)`** — builds a transparent, fully-scoped Compose host suitable for a
  native `UISheetPresentationController` (so the Liquid Glass material shows through). Presentation stays the shell's job.

A `NativeChromeSource` implementation must be a **dumb projection**: it may read your navigation state and emit
intents, but it must never expose or mutate a stack. Your navigation library stays the single source of truth.

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

`composeApp/src/commonTest/.../app/navigation/NativeNavigatorTest` covers the reference navigator (per-tab stacks,
push/pop/replace/sheet, `observe` delivery, idempotent top-push).
