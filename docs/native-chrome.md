# The iOS native-chrome shell — containers, insets, and scroll-under-glass

How the reference iOS shell hosts Compose screens inside **real navigation containers** — a
`UITabBarController`, one `UINavigationController` per tab, one view controller per stack entry — and the
inset contract your screens follow so content scrolls **under** the Liquid Glass tab bar instead of being
clipped by it. `docs/navigation.md` covers the chrome *contract and the ownership protocol* (state out /
intents in / ratified pops); this page covers the *layout integration*. Reference implementation:
`iosApp/iosApp/Native/NativeNavShell.swift` + `composeApp/src/iosMain/…/app/navigation/NativeNavChrome.ios.kt`
+ `composeApp/src/commonMain/…/app/NativeContentInsets.kt`.

## The shape

```
UIWindow (SwiftUI host: NativeNavShell(root:).ignoresSafeArea())    ← edge-to-edge, ALL regions
└─ NativeShellViewController : UITabBarController                   ← real Liquid Glass UITabBar
   ├─ UINavigationController (tab "catalog")                        ← real bar, real interactive pop
   │  ├─ RouteHostController(entry "catalog")                       ← one VC per stack entry
   │  │  └─ ComposeUIViewController                                 ← that entry's screen
   │  └─ RouteHostController(entry "showcase/buttons") …
   ├─ UINavigationController (tab "library") …
   └─ UINavigationController (tab "settings") …
```

Each `RouteHostController` sets **`edgesForExtendedLayout = [.bottom]`**:

- **Top bar:** the container lays the screen out *below* the navigation bar (compact inline title; content
  is not designed to scroll under the top bar).
- **Bottom bar:** the screen extends *behind* the tab bar, and UIKit reports the overlap as the view
  controller's bottom **safe-area inset** — scrolled content passes behind the glass and refracts through
  it, and screens reserve just enough bottom padding that the *resting* end of the content clears the bar.

That one line replaces the previous shell's hand-plumbed geometry (manual bar constraints +
`viewDidLayoutSubviews` recomputing `additionalSafeAreaInsets`): real containers publish every inset
themselves, including through trait changes and rotations.

## The three layers of the inset contract

**1. The SwiftUI host must ignore the safe area entirely.**

```swift
// iosApp/iosApp/iOSApp.swift
NativeNavShell(root: root).ignoresSafeArea()
```

`.ignoresSafeArea(.keyboard)` (the common template default) is **not** enough: SwiftUI would inset the whole
shell below the status bar and above the home indicator, and nothing can extend behind the bars. Ignore all
regions and the shell owns the full screen. (The keyboard region stays ignored too — keyboard avoidance is
Compose's job, below.)

**2. UIKit provides the bottom inset — nothing to compute.**

With `edgesForExtendedLayout = [.bottom]`, the tab-bar overlap arrives as the host controller's bottom
safe-area inset and flows into Compose as `WindowInsets.safeDrawing`. There is no shell code to keep in sync.

**3. The per-entry Compose host publishes the inset; every scrollable screen consumes it.**

```kotlin
// NativeNavChrome.ios.kt — contentViewController(entryId), the host for ONE stack entry
nativeContentHostViewController {
    val bottomInset = WindowInsets.safeDrawing.asPaddingValues().calculateBottomPadding()
    CompositionLocalProvider(LocalNativeContentBottomInset provides bottomInset) {
        graph.Content(route)
    }
}
```

`LocalNativeContentBottomInset` lives in the **sample app** (`app/NativeContentInsets.kt`), not the library
— the shell is a reference implementation, and the local is one line to copy. It defaults to `0.dp`, which
is also the correct Android value: the Material `NativeNavHost` reserves space for its `NavigationBar`
through Scaffold padding, so shared screens read the local and stay correct on both platforms without a
platform check.

## What a screen does with the inset

The inset must land **inside each scroll container** (extending the scrollable content), never as outer
padding — outer padding would shrink the viewport and re-introduce the clip.

- **Scrolling screen that hosts text fields** — fold it into the keyboard padding (`ShowcaseUi.kt`):

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

## The chrome projection the containers render

The shell renders whatever `NativeChromeState` says, reconciled level-triggered on every emission:
`backStacksByTab` becomes the per-tab view-controller stacks (one `RouteHostController` per
`NativeChromeEntry`, its `navigationItem.title` from the entry — UIKit derives the back button from the
previous entry), `tabs` the `UITabBarItem`s, `actions` the top entry's right bar items, `sheetId` a native
`UISheetPresentationController` whose content comes from `chrome.sheetViewController()`. User actions come
back as intents — including `backCommitted(tabId, entryId)`, the after-the-fact ratification of a pop
UIKit's own back button / interactive edge swipe already performed. The full protocol, and why it cannot
recreate the historical dual-ownership loop, is in [`docs/navigation.md`](navigation.md).

## Pitfalls (each one cost a debugging session)

- **`.ignoresSafeArea(.keyboard)` instead of `.ignoresSafeArea()`** — content silently clips at the bars and
  no child-controller inset fixes it. The host's safe-area mode is the lever.
- **Adding the bottom inset *and* keyboard padding** — they stack, and fields float a bar-height above the
  keyboard. Use `nativeImePadding(minBottom = …)`, which takes the max.
- **Sharing one view controller between two stack entries** (or caching hosts by route id across tabs — the
  same id may live on two tabs) — a `UIViewController` can only sit in one navigation stack; sharing is how
  native stacks corrupt. One fresh host per entry, per tab.
- **Registering a Compose `BackHandler` (or hosting `NavDisplay`) inside shell-hosted route content** — CMP's
  edge recognizer arms and defeats UIKit's interactive pop. In the native shell, the platform owns back.
- **Testing scroll positions on the simulator** — `rememberScrollState()` is saveable and restores across app
  relaunches, so a relaunched app may not start at the top; that's state restoration, not an inset bug.
