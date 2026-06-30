# Architecture & API principles

Status: **living design doc**. Captures the decisions behind the cross-platform UI system. The goal is
a shared Kotlin component system where each platform still feels **truly native**, with a control
surface rich enough that a developer rarely needs to abandon a `Native*` component and hand-roll a
platform-specific one.

> Scope note: this doc records direction and contracts. Per-component details live in the
> [components reference](components/README.md). A few sections marked _(target)_ describe intended
> direction rather than current behavior.

---

## 1. The three-tier hybrid architecture

We do **not** pick "UIKit vs SwiftUI" globally. Responsibilities are split by tier:

```
                 iOS                                   Android
 ┌───────────────────────────────────┐   ┌───────────────────────────────────┐
 │ TIER 1 — Native shell (SwiftUI)    │   │ TIER 1 — Native shell (Material)  │
 │  TabView / NavigationStack         │   │  Scaffold / Nav / TopAppBar        │
 │  .toolbar / .searchable / .sheet   │   │  ModalBottomSheet / Dialog / Menu  │
 │  .alert / Menu  (Liquid Glass)     │   │  (Material 3 / Material You)        │
 └───────────────┬───────────────────┘   └───────────────┬───────────────────┘
                 │ hosts per screen                       │ hosts per screen
 ┌───────────────▼───────────────────────────────────────▼───────────────────┐
 │ TIER 2 — Shared Compose content (commonMain)                               │
 │  screens, layout, forms, state, validation, the Native* component APIs      │
 └───────────────┬───────────────────────────────────────┬───────────────────┘
                 │ per-control interop                    │ real Compose Material
 ┌───────────────▼───────────────────┐   ┌────────────────▼──────────────────┐
 │ TIER 3 — Native leaf controls      │   │ TIER 3 — Material controls         │
 │  UIKit interop (default)           │   │  Switch / Slider / TextField / …   │
 │  SwiftUI interop (exception only)  │   │                                    │
 └────────────────────────────────────┘   └────────────────────────────────────┘
```

- **Shared = content + logic** (Tier 2): screens, forms, state, and the `Native*` component APIs.
- **Native per platform = chrome (Tier 1) and leaf controls (Tier 3).**

**The trade we consciously accept:** navigation/chrome is **native per platform, not shared** — only
*content* is shared. This is required for genuine native feel (real `NavigationStack`, `TabView`,
sheets, search, Liquid Glass) and is the correct trade for this product.

### Liquid Glass — precise wording
Compose may approximate some glass-like visuals in its own custom content, but it **cannot own real
iOS system chrome** (native `TabView`, `NavigationStack`, toolbar, search, sheets) or the
**system-applied Liquid Glass** treatment. Note also that even native controls embedded inside the
Compose Metal layer get their native styling but **not guaranteed glass background-refraction** across
the interop boundary (the effect samples real content behind it). For true iOS system chrome and the
latest iOS visual language, the shell/presentation layer must be **native SwiftUI**.

---

## 2. Tier 1 — what is native SwiftUI on iOS

Everything that is system chrome or a system presentation:

- App shell / `@main App`, scene setup.
- `TabView` (incl. iOS 26 floating/glass tab bar), `NavigationStack` (native push/pop, back-swipe,
  large titles), `.toolbar` / `.toolbarBackground`.
- `.searchable` (native search + scopes + suggestions).
- `.sheet` / `.fullScreenCover` / detents, `.popover`.
- `.alert` / `.confirmationDialog`, `Menu` / context menus.
- System pickers presented modally (share, photo/file/contact).
- Liquid Glass structure (free when the shell is native + built against the iOS 26 SDK).

Compose content is hosted inside these via `ComposeUIViewController` per screen.

---

## 3. Tier 3 — UIKit interop (default) vs SwiftUI interop (exception)

**Default = UIKit interop in Kotlin/Native** (what we have): lightest interop, the most configurable
knobs, no Swift bridge, and it still inherits native iOS 26 control styling.

| Stays UIKit interop | Why |
|---|---|
| Button, Toggle, Slider, SegmentedControl, Stepper | system controls; UIKit exposes the most theming knobs; lightest |
| ProgressIndicator (`UIActivityIndicatorView`/`UIProgressView`), SearchBar (`UISearchBar`), PageControl (`UIPageControl`) | real system controls; native look/behavior; lightest (added in the v2 wave) |
| Text (`UILabel`) | non-interactive display (see §6) |
| TextField (`UITextField` / `UITextView`) | UIKit exposes clear button, keyboard appearance, input/accessory views, precise delegate control |
| `WKWebView`, `MKMapView`, camera/AVCapture preview | need deep delegate / gesture / overlay control; no SwiftUI advantage |
| `UIDatePicker` / `UIPickerView` | fully capable; avoids the Swift bridge |

**SwiftUI interop (via `UIHostingController` + `UIKitViewController`) only when there is no good UIKit
equivalent or it is a SwiftUI-only / iOS-26 component:**

| Justified SwiftUI interop |
|---|
| **Swift Charts** (no UIKit equivalent without hand-rolling) |
| **iOS-26 / Liquid-Glass-specific SwiftUI components** with no UIKit analog |
| _Toss-ups decided by testing:_ advanced multiline text, date pickers — **default to UIKit first** |

Rule: **default UIKit; reach for SwiftUI only for a real capability gap.** This keeps the
Kotlin↔Swift bridge surface minimal. The bridge (when needed) uses dependency inversion: Kotlin defines
an ObjC-compatible factory protocol, Swift implements + registers it at launch, the iOS `actual`
calls it and embeds the returned `UIViewController`.

---

## 4. Component API principles — rich, clean, production-grade

**The priority is a wide, powerful control surface, kept usable through progressive disclosure.** A
missing native/customization knob is the worst outcome — it makes developers abandon the component.
Under-powering is a bigger risk than a large-but-organized API.

Principles:
- **Expose every meaningful production knob.** If a platform offers a useful, safe customization, the
  Native component exposes it — directly, via a typed option object, or via `ios`/`android` options. If
  it is intentionally unsupported, that is **documented**, never silently omitted.
- **Simple by default, powerful when needed, rich for production, organized to stay usable.**
- **Surface layout:**
  - High-frequency props → top-level parameters.
  - Advanced cross-platform clusters → typed immutable option objects (e.g. `NativeFieldInput`,
    `NativeFieldFocus`).
  - Platform-only knobs → `ios = …` / `android = …` typed option objects.
  - Native touch behavior → `touch: NativeInteropTouch` (see §6).
  - Every option has a **safe default**, so the short call still compiles.
- **Grouping organizes a rich surface; it never shrinks it.** Even "simple" components (Toggle,
  Stepper) carry the full surface (label, supporting text, size, style, colors, haptics, touch,
  testTag, ios/android) — they just don't *require* any of it.

### Inclusion rule (bounds "expose everything")
Expose a knob if **(1)** it is meaningful for real production customization, **(2)** it is safe to
support (cross-platform or as a platform option), and **(3)** it has a sensible default. Anything
failing these is documented as intentionally unsupported, with the reason. This prevents both
under-powering and literal-every-property bloat.

### Hard KMP constraint that shapes "expose every knob"
A **raw** escape hatch like `ios.configure { uiTextField -> … }` **cannot be invoked from shared
`commonMain` code** — commonMain cannot name platform types (`UITextField`, `UISwitch`). Therefore:

- **Richness is delivered as typed, common-typed options** (enums, `Color`, `Dp`, `Boolean`,
  callbacks). Almost every native knob can be modeled this way (`clearButtonMode`,
  `keyboardAppearance`, `returnKeyType`, switch style, …). **This is the primary, shared-callable path
  and can be made arbitrarily rich.**
- When a caller needs something not yet typed, the fix is to **add the typed option** (one field),
  not to push them to a raw lambda.
- A raw `configure { nativeView -> }` hook is only reachable from a **platform source set** (iosMain),
  so it remains a true last resort for the rare untyped knob — never the main customization path.

### Platform-difference strategy
1. **Shared props** where behavior aligns (identical semantics both platforms).
2. **Documented divergence** — each component's renderer differences are documented on its [components reference](components/README.md) page.
3. **Typed `ios` / `android` option objects** for knobs that don't generalize.
4. **Safe fallbacks** — an unsupported prop is a documented **no-op**, never a crash. Callers branch
   intentionally via `NativeCapabilities` (§5) when they care.

---

## 5. NativeCapabilities (added early)

A small, honest, runtime-truthful capability layer so we never pretend a feature exists:

```kotlin
object NativeCapabilities {
    val platformVersion: NativePlatformVersion          // os + version
    val isLiquidGlassAvailable: Boolean                // iOS >= 26 at runtime
    val supportsNativeSearch: Boolean
    val supportsSwiftCharts: Boolean
    val supportsMultilineNativeField: Boolean
    // grows as features land; each flag is a real check, not aspirational
}
```

Pairs with the "unsupported = documented no-op" rule.

---

## 6. Native touch interop — first-class, per component

Native controls embedded in a Compose scroll must cooperate with scroll gestures. We wrap Compose's
(experimental) `UIKitInteropProperties.interactionMode` behind our own stable enum so the public API
does not leak experimental Compose types and survives CMP version churn:

```kotlin
enum class NativeInteropTouch { Cooperative, NonCooperative, NonInteractive }
// Cooperative    -> UIKitInteropInteractionMode.Cooperative(delayMillis = default)  // UIScrollView-like delay
// NonCooperative -> UIKitInteropInteractionMode.NonCooperative
// NonInteractive -> interactionMode = null  (touches pass through to Compose)
```

(`UIKitInteropInteractionMode` is `@ExperimentalComposeUiApi`; `Cooperative` carries a `delayMillis`,
`NonCooperative` is a singleton, `null` = non-interactive. Verified against Compose MP 1.11.0.)

`NonInteractive` (touch) is **orthogonal** to the dark-mode backing/`overrideUserInterfaceStyle` work
already in place — they don't conflict.

### Recommended defaults (and where to expose an override)

| Component | Default | Rationale | Expose override? |
|---|---|---|---|
| Text (`UILabel`) | **NonInteractive** | a label must not steal scroll; lighter | → Cooperative only if `onClick`/`selectable` |
| Button / Toggle / Stepper / SegmentedControl | **Cooperative** | tap-based; vertical scroll still works | advanced only |
| Slider | **Cooperative** | vertical drag scrolls; horizontal drag moves the thumb (mirrors UIScrollView+slider) | **Yes** — `NonCooperative` for grab-priority |
| TextField | **Cooperative** | reliable tap-to-focus; vertical drag scrolls | tune delay; expose for edge cases |
| Map / WebView (future) | **NonCooperative** | own pan/zoom/scroll; Compose must not intercept | rarely changed; give fixed height |
| SwiftUI-hosted (future) | per-view | inherit the wrapped control's gesture needs | yes |

Principles: never use `NonCooperative` for simple tap controls (it would eat scroll); keep the
cooperative delay short so tap-to-focus isn't swallowed; map/web get fixed-height regions, not
unbounded nesting in a parent scroll.

---

## 7. Navigation — library-agnostic core, renderers as projections _(target)_

**`NativeNavigator` is the single source of truth.** Renderers are projections that render the shared
state and report user actions back as intents. We never keep three independent sources of truth
(NativeNavigator + Nav3 stack + SwiftUI `NavigationPath`).

```
NativeNavigationCore (library-owned, platform-agnostic)
  NativeNavigator (SoT), NativeNavigationState, typed routes, tabs, sheet/dialog state, deep links, intents
       │ projects to / receives intents from
       ├─ Android: Nav3 adapter   (default renderer; NavDisplay, entry decorators, scenes, saveState…)
       ├─ iOS:     SwiftUI TabView / NavigationStack adapter (path is a projection)
       └─ future:  Voyager / Decompose / custom adapters
```

Public API is intent-based and library-neutral:

```kotlin
navigator.push(AppRoute.Details(id))
navigator.pop()
navigator.selectTab(AppTab.Settings)
navigator.presentSheet(AppRoute.EditCustomer(id))
```

Rules:
- **Nav3 is the default Android adapter, not the core dependency.** No `NavController`/`NavDisplay`/
  `NavBackStack` types in the public API; Nav3's advanced features are exposed behind a dedicated
  `NativeNav3Shell(navigator, graph) { … }` adapter.
- Renderer actions → intents → `NativeNavigator`: Android back press → `pop()`; iOS back-swipe →
  `pop()`; tab tap → `selectTab(...)`; deep link → `replaceStack(...)`.
- **Module boundaries: packages now, modules later.** Adopt the layering as packages inside one
  navigation module; extract `:brand-navigation-nav3`, `:brand-navigation-ios-swiftui`, etc. only when
  a second adapter/consumer actually exists. (Five Gradle modules up front is premature.)

**Biggest risk in the whole system:** binding a native SwiftUI `NavigationStack` to an *external*
source of truth without a feedback loop (SwiftUI wants to own its `path`). This **must be de-risked
with a spike** before the abstraction is built on top of it. If sync proves fragile, the fallback is:
shared SoT owns tabs + deep-link entry + cross-tab routing, while intra-tab stacks are owned locally by
the renderer and report pops up. Nav3's developer-owned back stack makes the Android side natural;
Nav3 is still stabilizing, which reinforces keeping it a swappable adapter.

---

## 8. Migration phases & acceptance criteria

Sequence for shippable value; do not build all infrastructure up front. The navigation abstraction is
its own workstream and must not block component value; prove hosting before building `NativeNavigator`.

**Phase 1 — SwiftUI shell hosting spike (throwaway, kill-criterion).** Use the dumbest navigation (one
hardcoded tab + stack) to prove hosting only. _Done when:_ one tab works; one `NavigationStack` works;
a `ComposeUIViewController` screen is hosted; back-swipe works; toolbar/search/sheet can be shown
natively; dark/light works; RTL doesn't break; keyboard insets acceptable; no obvious lifecycle/memory
issue. **Do not build `NativeNavigator` in the spike.**

**Phase 2 — Component API expansion (UIKit), one at a time** to the rich-but-clean standard. Order by
impact: **TextField → Button → Slider/Segmented/Stepper/Toggle → Text.** Each component's _done_
criteria:

- **TextField:** single-line `UITextField` still works; multiline works (chosen strategy); focus
  callbacks work; submit/IME actions work; clear button works; iOS `cornerRadius` works; Android
  aligned; `testTag` maps; scroll/touch tested in a vertical scroll; docs capability table written.
- **Button:** both leading+trailing icons render; trailing layout correct without the RTL hack;
  role/menu work on iOS; Material parity on Android; loading + disabled correct; `interactionMode`
  correct; testTag maps; docs written.
- **Slider:** continuous + discrete `steps`; `onValueChangeFinished`; track/thumb color overrides;
  vertical-scroll vs horizontal-drag verified; testTag maps; docs written.
- **SegmentedControl:** per-segment label/icon/enabled; selected/track/text color overrides; selection
  callback correct; docs written.
- **Toggle:** label/supporting text; size; colors (on/off track, thumb); haptics; iOS style
  (switch/checkbox/button) where available; docs written.
- **Stepper:** range/step/wrap; optional value display + formatter; tint; autorepeat (iOS); docs.
- **Text:** `interactionMode = NonInteractive`; `onClick`/`selectable`; lineHeight/minLines/letter
  spacing; no rich-text claim beyond what `UILabel` supports; docs.

**Phase 3 — Navigation core + adapters** (after the spike proves hosting): `NativeNavigator`, Nav3
adapter, SwiftUI adapter. Finalize the module split only when a second adapter exists.
> ✅ **BUILT** (device-verified both platforms). `navigation/NativeNavigator` is the SoT
> (`push`/`pop`/`popToRoot`/`selectTab`/`presentSheet`/`dismissSheet`/`replaceStack`, per-tab `SnapshotStateList`
> stacks, `snapshot()`/`observe()` projection). Android adapter = the custom Compose `NativeNavHost` (no Nav3 dep
> — Nav3 stays the deferred `NativeNav3Shell` adapter). iOS adapter = native SwiftUI `TabView`/`NavigationStack`
> (`NativeShell` + `NativeNavModel`) projecting the SoT via `NativeNavBridge`, with the **full bidirectional bind**
> (not the hybrid fallback): SoT → path via `observe`, back-swipe/tab → intents with an `applyingFromKotlin`
> reconciliation guard. Sheets are presented **natively by the bridge** (the proven `NativeSheet`
> `UISheetPresentationController` path), not SwiftUI `.sheet`. The Phase-1 spike files are retired. Tests:
> `commonTest/NativeNavigatorTest`. See `docs/navigation.md` + `docs/navigation-test-plan.md`.

**Phase 4 — SwiftUI-interop components as *new* components** (Swift Charts, glass-only): pilot one end
to end through the bridge (verify memory/lifecycle) before relying on it.

Do not touch yet: Android Material actuals (stable), the theme token system (stable), and the public
`expect` signatures (frozen — only `actual` bodies change).

---

## 9. Test matrix

| Check | How |
|---|---|
| Visual parity, light, dark, stable-state regression | **Automated** (simulator screenshot workflow) |
| `testTag` → `accessibilityIdentifier` present | **Automated** (small XCUITest / assertion) |
| Core interactions (toggle flips, slider min↔max, field type+submit) | **Automated** (a few XCUITests by id) |
| Memory leaks (esp. SwiftUI-hosted) | **Semi-automated** (Instruments / `XCTMemoryMetric`) |
| RTL/Arabic, Dynamic Type, VoiceOver, keyboard/focus/IME, scroll-touch in scroll, reduced motion | **Manual checklist** (per component) |
| Real-device matrix, performance in long forms | **Release QA** |

Be realistic: full automation of RTL/Dynamic Type/VoiceOver is a large investment. Prioritize
screenshot regression + a11y-id presence + a thin XCUITest happy-path; the rest is a structured manual
checklist shipped in the docs.

---

## 10. Decisions log / non-goals

- **Stay UIKit for low-level controls; SwiftUI only by exception** (Charts / glass-only).
- **Move app shell / navigation / presentations to native** (SwiftUI on iOS, Material on Android).
- **Shell/navigation is native per platform; only content is shared.**
- **Rich component APIs via typed options + `ios`/`android` blocks**, not minimal APIs and not raw
  view-mutation from shared code.
- **Navigation core is library-agnostic; Nav3 is the default Android adapter, not the core.**
- **No premature modularization** — packages now, Gradle modules when a second renderer exists.
- Non-goal (for now): a full SwiftUI rewrite of the basic controls (adds a bridge + hosting overhead
  for no visual gain and *less* theming control on several controls).
