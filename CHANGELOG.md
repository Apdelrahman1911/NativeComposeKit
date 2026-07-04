# Changelog

All notable changes to NativeComposeKit are documented here. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and the project adheres to
[Semantic Versioning](https://semver.org/spec/v2.0.0.html) (pre-1.0: minor bumps may contain breaking
changes; the public surface is ABI-locked per release via binary-compatibility-validator).

## [Unreleased]

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

[0.1.0]: https://github.com/Apdelrahman1911/NativeComposeKit/releases/tag/v0.1.0
