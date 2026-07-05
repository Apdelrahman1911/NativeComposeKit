# Changelog

All notable changes to NativeComposeKit are documented here. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and the project adheres to
[Semantic Versioning](https://semver.org/spec/v2.0.0.html) (pre-1.0: minor bumps may contain breaking
changes; the public surface is ABI-locked per release via binary-compatibility-validator).

## [Unreleased]

### Added

- **`NativeCollapsible`** — the platform-safe way to collapse/expand content containing native
  controls (use instead of `AnimatedVisibility` around `Native*` components). A real
  `AnimatedVisibility` on Android; on iOS it animates the container's size while content enters and
  leaves in one clean step, and `NativeText` inside renders through Compose (no interop cut-out
  flash during the animation).
- **"Interop churn test" screen** in the sample app (Settings → Developer) — regression harness for
  the interop synchronization behaviors below, with the pathological `AnimatedVisibility` pattern
  reproducible behind an off-by-default toggle.

### Fixed

- **iOS: ghost / doubled / stale native controls after interop churn.** Compose Multiplatform queues
  every UIKit-side interop mutation (insert, position update, removal, `onRelease`) into a
  transaction executed only when the next rendered frame is presented; actions can execute with
  severe delay (visibly desynced controls during animated visibility) and, in a narrower window, be
  lost outright. Every kit-hosted `UIKitView` now detaches its native view **synchronously** at node
  disposal (removal delay bounded to zero — no lingering/doubled controls), and settled positions
  self-heal against the Compose-side layout truth. The remaining delay on the *insert* side is a
  Compose Multiplatform engine limitation, documented with hard usage rules in
  `docs/interop-notes.md` §4 and reported upstream (`docs/upstream/cmp-interop-transaction-lag.md`).

## [0.2.0] — 2026-07-05

### Added

- **Per-screen chrome behavior** — `NativeBarConfig` (`hidesTopBar`, `hidesTabBar`, per-screen
  `actions`) carried on `NativeChromeEntry.bar`, so any host can drive per-screen bar visibility and
  native toolbar actions through the existing chrome projection. Defaults unchanged; fully opt-in.
- **iOS shell style registry** — `NativeShellStyle` + `applyNativeShellStyle()` (iosMain): themed /
  system-material / custom bar backgrounds, global tint, tab-item selected/unselected colors, title
  font, and hairline visibility for the native `UINavigationBar`/`UITabBar` chrome, with
  `nativeShell…UIColor` resolvers for Swift shells.
- The sample app gained Compose-side bar slots + restylable defaults (`NativeNavDefaults`) and a
  "Navigation toolbar styles" catalog (Settings → Developer) demonstrating the full customization
  surface on both platforms, including documented limitations.

## [0.1.0] — 2026-07-04

First public release.

### Added

- **Component set** — one shared `Native*` API per component, rendered with Jetpack Compose Material 3 on
  Android and real UIKit controls on iOS: buttons (incl. icon/split/menu), text fields (single/multiline,
  search bar, OTP), selection controls (toggle, checkbox, radio, segmented, slider, stepper), pickers
  (date, color, page control + pager), cards/surfaces/dividers, list rows with swipe actions, dialogs,
  sheets, popovers, share, feedback (native alerts/action sheets, toasts, banners), progress/skeleton,
  empty/error content states, rating, avatar/badge, pagination + load-more, and focus utilities.
- **Theming** — `NativeKitTheme` with token-driven spacing/corners/typography, light/dark palettes that
  also reach native iOS shell chrome and Android system bars, Dynamic Type scaling on iOS, and
  reduce-motion awareness.
- **Nav-agnostic native chrome contract** (`…nativecomposekit.chrome`) — `NativeChromeStateSource` /
  `NativeChromeState` project any navigation system into real iOS chrome (`UITabBarController` + per-tab
  `UINavigationController`s + `UISheetPresentationController`), including full back-stack projection
  (`backStacksByTab`), commit-time pop ratification (`backCommitted`), and per-entry / sheet Compose
  content hosts. The kit itself owns no navigation.
- **iOS interop hardening** — scroll-safe overlay placement with theme-matched backings, VoiceOver
  exposure for interactive native controls (semantics mirrors for display-only ones), controls are never
  restyled or re-asserted while their own tap animation runs, and interop views stay hidden until their
  first valid placement.
- **Docs** — architecture, navigation ownership model, native-chrome layout/inset contract, interop
  behavior notes with upstream issue references, and per-component references with platform capability
  tables.

[0.2.0]: https://github.com/ukkkera/NativeComposeKit/releases/tag/v0.2.0
[0.1.0]: https://github.com/ukkkera/NativeComposeKit/releases/tag/v0.1.0
