# The iOS native-chrome shell — bars, insets, and scroll-under-glass

How the reference iOS shell lays out a real `UINavigationBar` (top), a Liquid Glass `UITabBar` (bottom),
and the Compose content between/behind them — and the exact inset contract your screens must follow so
content scrolls **under** the glass tab bar instead of being clipped by it. `docs/navigation.md` covers the
nav-agnostic chrome *contract* (state out / intents in); this page covers the *layout integration* you can't
reconstruct from the contract alone. Reference implementation: `iosApp/iosApp/Native/NativeNavShell.swift` +
`composeApp/src/iosMain/…/MainViewController.kt` + `composeApp/src/commonMain/…/app/NativeContentInsets.kt`.

## The shape

```
UIWindow (SwiftUI host: NativeNavShell(root:).ignoresSafeArea())   ← edge-to-edge, ALL regions
└─ NativeShellViewController
   ├─ content (ComposeUIViewController)   z-order: BOTTOM — fills from below the nav bar to the
   │                                      very bottom of the screen, BEHIND the tab bar
   ├─ UINavigationBar                     pinned to the top safe-area edge; content starts below it
   └─ UITabBar                            pinned to the screen bottom; OVERLAYS the content
```

- **Top bar:** content is constrained *below* it (`content.top = navBar.bottom`). A compact inline title;
  no scroll-under at the top.
- **Bottom bar:** content is constrained *past* it (`content.bottom = view.bottom`). The tab bar sits on top
  of the content in the z-order, so scrolled content passes behind the glass and refracts through it.
  Screens then reserve just enough bottom padding that the *resting* end of the content clears the bar.

## The three layers of the inset contract

**1. The SwiftUI host must ignore the safe area entirely.**

```swift
// iosApp/iosApp/iOSApp.swift
NativeNavShell(root: root).ignoresSafeArea()
```

`.ignoresSafeArea(.keyboard)` (the common template default) is **not** enough: SwiftUI then insets the whole
shell to the top/bottom safe areas, the hosted Compose content lays out below the bars, and — the confusing
part — `additionalSafeAreaInsets` on the child controller appears to do nothing. Only the Compose *background*
fills behind the bars; the content clips. Ignore all regions and the shell owns the full screen. (The keyboard
region stays ignored too — keyboard avoidance is Compose's job, below.)

**2. The shell converts the tab bar's overhang into a safe-area inset on the content controller.**

```swift
// NativeNavShell.swift — viewDidLayoutSubviews
let bottom = max(0, tabBar.frame.height - view.safeAreaInsets.bottom)
content.additionalSafeAreaInsets = UIEdgeInsets(top: 0, left: 0, bottom: bottom, right: 0)
```

The tab bar's frame already extends into the home-indicator region, and the content controller inherits that
system inset — so subtract `view.safeAreaInsets.bottom` or the inset is double-counted. (The nav bar needs no
inset: content is constrained below it.) `additionalSafeAreaInsets` flows into Compose as
`WindowInsets.safeDrawing`.

**3. The Compose host publishes the inset; every scrollable screen consumes it.**

```kotlin
// MainViewController.kt — inside the content ComposeUIViewController
val bottomInset = WindowInsets.safeDrawing.asPaddingValues().calculateBottomPadding()
CompositionLocalProvider(LocalNativeContentBottomInset provides bottomInset) { … }
```

`LocalNativeContentTopInset`/`LocalNativeContentBottomInset` live in the **sample app**
(`app/NativeContentInsets.kt`), not the library — the shell is a reference implementation, and the local is
one line to copy. It defaults to `0.dp`, which is also the correct Android value: the Material `NativeNavHost`
reserves space for its `NavigationBar` through Scaffold padding, so shared screens read the local and stay
correct on both platforms without a platform check.

## What a screen does with the inset

The inset must land **inside each scroll container** (extending the scrollable content), never as outer
padding — outer padding would shrink the viewport and re-introduce the clip. One top-level pad can't do this;
each screen applies it where its scroll lives.

- **Scrolling screen that hosts text fields** — fold it into the keyboard padding
  (`ShowcaseUi.kt`):

  ```kotlin
  Column(
      Modifier.verticalScroll(rememberScrollState())
          .nativeImePadding(minBottom = LocalNativeContentBottomInset.current)
          .padding(16.dp),
  ) { … }
  ```

  `nativeImePadding(minBottom = …)` pads by the **larger** of the keyboard extent and `minBottom` — never
  their sum. While the keyboard is up it covers the tab bar, so the keyboard extent alone is right; while
  it's down, `minBottom` keeps the last content clear of the bar.

- **Scrolling screen without fields** — plain extra bottom padding inside the scroll:

  ```kotlin
  Modifier.verticalScroll(…).padding(start = 16.dp, top = 16.dp, end = 16.dp,
      bottom = 16.dp + LocalNativeContentBottomInset.current)
  ```

- **Fixed header + lazy list** (Library grid, Reader) — keep the header where it is and put the inset in the
  *list's* `contentPadding`:

  ```kotlin
  LazyVerticalGrid(…, contentPadding = PaddingValues(…, bottom = 16.dp + bottomInset))
  ```

Missing the inset on one screen has exactly one symptom: that screen's last rows rest hidden behind the tab
bar.

## The chrome projection the bars render

The shell draws whatever `NativeChromeState` says — all seven fields:
`title`, `backTitle` (the previous screen's title, which UIKit renders in the native back button),
`canGoBack`, `selectedTabId`, `tabs`, `actions`, `sheetId`. Bar taps go back as intents
(`backRequested()` / `tabSelected` / `actionTapped` / `dismissSheet`); the shell never owns or mutates a
navigation stack — that single-ownership rule is what ended the iOS push/pop loop (`docs/navigation.md`).
Sheets present natively from the shell via `chrome.sheetViewController()` in a
`UISheetPresentationController`; the navigator owns the sheet state.

## Pitfalls (each one cost a debugging session)

- **`.ignoresSafeArea(.keyboard)` instead of `.ignoresSafeArea()`** — content silently clips at the bars and
  no amount of child-controller inset fixes it. The host's safe-area mode is the lever.
- **Forgetting to subtract `view.safeAreaInsets.bottom`** from the tab-bar height — every screen gets ~34 pt
  of dead space above the bar.
- **Adding the bottom inset *and* keyboard padding** — they stack, and fields float a bar-height above the
  keyboard. Use `nativeImePadding(minBottom = …)`, which takes the max.
- **Letting a native container own navigation** — a `UINavigationController`/SwiftUI `NavigationStack`
  fights the Kotlin stack (history in `docs/navigation.md`). The bare `UINavigationBar` + `UITabBar` here are
  dumb projections by design.
- **Testing scroll positions on the simulator** — `rememberScrollState()` is saveable and restores across app
  relaunches, so a relaunched app may not start at the top; that's state restoration, not an inset bug.
