# Navigation — NativeNavigator

Library-agnostic navigation per `architecture.md` §7: **`NativeNavigator` is the single source of truth (SoT)**;
the platform renderers are projections that render the shared state and report user actions back as intents.
There is never a second independent stack (no `NavController`/SwiftUI `NavigationPath` owning state).

## Core (commonMain `io.github.apdelrahman1911.nativecomposekit.navigation`)
- **`NativeRoute { val id: String }`** / **`NativeTab { val id: String }`** — caller-defined contracts. The app
  declares a sealed `NativeRoute` hierarchy; the library only uses `id`, which must be **stable & unique per
  destination** (encode args, e.g. `"detail/$chapter"`). `id` is the projection used at the Swift boundary
  (SwiftUI path is `[String]`) and the `AnimatedContent` key on Android.
- **`NativeNavigator`** (`@Stable`) — the SoT. Intents: `push`, `pop(): Boolean`, `popToRoot`, `selectTab`,
  `presentSheet`, `dismissSheet`, `replaceStack(tab?, routes)`. State (`NativeNavigationState`): `selectedTab`,
  one `SnapshotStateList<NativeRoute>` **per tab** (independent → switching tabs preserves each tab's depth),
  and `sheet: NativeRoute?`. Projection for the Swift bridge: `snapshot(): NativeNavSnapshot` (all strings) +
  `observe(onChange): NativeNavCancellable` (fires initial + after every intent; observers are notified directly
  so it works **without a running composition** — the iOS case).
- **`nativeNavGraph { screen<R> { route -> … } }`** — maps a route → its shared `@Composable`, matched by
  **reified route type** (so data-class args reach the screen). Both adapters consume the same graph.
- **`rememberNativeNavigator(...)`** (in composition, Android) / **`createNativeNavigator(...)`** (outside
  composition, iOS — the shell holds one shared instance).
- Tests: `commonTest/navigation/NativeNavigatorTest`.

App wiring lives in `commonMain/app/`: `AppRoute`, `AppTab`, `appNavGraph(navigator)`, the sample data
(`MangaData.kt`), and the shared screens — the real manga flow `LibraryScreen`/`MangaDetailScreen`/`ReaderScreen`
(`MangaScreens.kt`) plus `SettingsScreen`/`GlassInteropTestScreen` (`AppScreens.kt`). `appRouteTitle` is the one
source of chrome titles for **both** platforms — Android reads it directly; iOS reads it via
`NativeNavBridge.title(forRouteId:)` (no Swift-side id parsing).

## Android adapter (`navigation/NativeNavHost.kt`, commonMain — Material is multiplatform)
`NativeNavHost(navigator, graph, tabs, title, actions)` renders the selected tab's **top** route in an
`AnimatedContent` (push/pop slide by stack-delta), with a `Scaffold` `TopAppBar` (back when `stack.size > 1` →
`pop()`, plus app `actions`), a Material `NavigationBar` → `selectTab`, and a `ModalBottomSheet` for
`state.sheet`. System/predictive back → `pop()` via the `NativeBackHandler` expect/actual (Android →
`androidx.activity.compose.BackHandler`; iOS → no-op). Nav3 is **not** a dependency; a future
`NativeNav3Shell(navigator, graph)` can replace this against the same public API.

## iOS adapter (`navigation/NativeNavBridge.ios.kt` + Swift `iosApp/iosApp/Native/`)
Native SwiftUI `TabView` + per-tab `NavigationStack` (`NativeShell`) **projecting** the SoT via `NativeNavBridge`
(string-only surface; Swift never sees `NativeRoute`). `NativeNavModel` (`ObservableObject`) mirrors the snapshot
into `@Published` state and reports gestures back. **Full bidirectional bind** (the architecture's flagged top
risk, validated here):
- **SoT → SwiftUI:** `observe` → `NativeNavModel` updates `selectedTab` / `pathByTab` (root-excluded tail).
- **SwiftUI → SoT:** `NavigationStack(path:)` setter → `reconcileStack` (idempotent), tab tap → `selectTab`.
  An `applyingFromKotlin` guard prevents a SoT-driven update echoing back as a user change.
- **Sheets are presented natively by the bridge**, not via SwiftUI `.sheet`: when `state.sheet` becomes
  non-null the bridge presents a **transparent** `ComposeUIViewController` in a real `UISheetPresentationController`
  (medium/large detents, grabber, Liquid Glass) from `topmostUIViewController()` — the proven `NativeSheet` path.
  Dismissal syncs back via the presentation delegate (`presentationControllerWillDismiss` clears state at the
  *start* of dismissal so the next present isn't dropped during the close animation).
- Each hosted route's `ComposeUIViewController` wraps content in `NativeAppearanceScope` **and**
  `NativeFeedbackHost` (each is its own composition, so it needs both providers).

If the full bind ever proves fragile on a device, the documented **hybrid fallback** (SoT owns tabs +
deep-link + cross-tab; SwiftUI owns intra-tab stacks, reporting pops up) is a drop-in with the **same public
API** — only the iOS binding depth changes.

## Testing
See `docs/navigation-test-plan.md` (exact Android adb commands + iOS manual checklist).
