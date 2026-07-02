# Design-system rules

The non-negotiable rules a `Native*` component must follow to belong in the kit. Read this before adding or
changing a core component; [`architecture.md`](architecture.md) covers the wider structure.

## 1. Theme is the only styling source
A component reads its defaults from **`NativeKitTheme`** (`MaterialTheme` + `NativeTokens`), resolves a plain
`Resolved*Style`, and the platform renderer reads **only** that style. **Nothing is hardcoded in a renderer**;
there is no token file (`design-tokens.json` is banned) — `NativeKitTheme` is the single source. Per-call overrides
are exposed as typed params, never by reaching around the theme.

## 2. Surface-adaptation rule (the `LocalNativeSurface` rule)
A component that paints a **surface-relative** fill or border (a background behind content, a reveal, a skeleton
block, an outline meant to read against the surface) **must derive that color from the published
`LocalNativeSurface`** — falling back to a theme constant only when it is `Unspecified` (a Liquid Glass / unwrapped
host). **Never hardcode `surface`/`surfaceVariant`/a fixed color** for these — it will look right on the page and
wrong (or invisible) inside a `NativeCard` (which publishes `surfaceVariant`) or on glass.

This is enforced by the pure helpers in `components/internal/SurfaceColors.kt` (`skeletonColors`,
`resolveSurfaceFill`, `chipBorderColor`) — unit-tested in `commonTest/components/SurfaceColorsTest.kt`. Worked
examples (all real bugs found via the manga flow, now fixed + guarded):
- **`NativeListItem`** swipe foreground → `resolveSurfaceFill(LocalNativeSurface, surface)` (else the colored
  "reveal" showed at rest).
- **`NativeSkeleton`** base/highlight → `skeletonColors(LocalNativeSurface-or-surfaceVariant, onSurface)` (a fixed
  `surfaceVariant` base vanished inside a `surfaceVariant` card).
- **`NativeInlineStatus`** outlined interior → `resolveSurfaceFill(LocalNativeSurface, surface)`.
- **`NativeChip`** border → the M3 `outline` role (the default `outlineVariant` disappeared on a card).
- **iOS native controls** → `interopBackingColor()` (`UiKitInterop.ios.kt`) pins the interop backing to
  `LocalNativeSurface`, clear on glass. See `docs/interop-notes.md`.

**Visual regression harness:** `app/ComponentMatrixScreen.kt` (Settings → "Component surface matrix") renders
each surface-sensitive component on the page vs inside a Filled card — the check that would have caught all of
the above. Run it (Android adb / iOS by hand) when touching a surface-sensitive component.

## 3. Real brand / design-system value — don't wrap a library for the sake of it
A core component must add genuine design-system value: native-per-platform rendering, theme-driven variants/
sizes/states, brand a11y defaults, or a cross-platform contract the app shouldn't re-implement. A component whose
*entire* value is "wraps a third-party library" does **not** belong in core. **Precedent:** `NativeImage` was
removed — it was an async-image-loader wrapper with no tokens/variants; apps use **Coil 3** directly and keep
brand-specific treatment (e.g. the manga gradient cover fallback) as **app-level** composables.

## 4. The kit stays third-party-dependency-free
`components/` (and `theme/`, `chrome/`) depend on **Compose-official artifacts only** — no third-party
libraries. App concerns that need one (image loading → Coil, networking → Ktor) live in the **app** module/layer,
never in the kit. This keeps the kit light for every future consumer and is why image loading was pushed out
(rule 3). When in doubt, a dependency belongs in the app.

## 5. Platform differences are explicit, never silent
Shared props where behavior aligns; a typed `ios`/`android` option object where it diverges; an honest
**documented no-op** where a platform can't support something (see `NativeCapabilities`). Every divergence is
recorded on the component's reference page. `NativeText` is the one sanctioned Compose-fallback (Compose `Text` on
material surfaces, native `UILabel` on solid) — and that exception is documented, not silent.

## What is intentionally NOT in the kit
- **`NativeImage` / image loading** → app-level via Coil 3 (rules 3 + 4).
- **Wheel / `UIPickerView` picker** → covered by `NativeSegmentedControl` / `NativeMenu` / `NativeRadioGroup` /
  `NativeDatePicker`; a standalone wheel would duplicate them.
- **System notifications** → needs OS permissions + plumbing; out of scope.
- **Native glass popover & real system Liquid Glass chrome** → owned by the native **shell** (the UIKit
  `UINavigationBar`/`UITabBar` chrome — see `docs/native-chrome.md`), not Compose content.
- **Time / datetime picker** → `NativeDatePicker` is date-only v1; a follow-up if needed.

### Native typography boundary (iOS solid-surface text)

On iOS solid surfaces, `NativeText` renders a real `UILabel` with a **system font built from the resolved
`TextStyle`'s size + weight** (Dynamic-Type-scaled). Custom `fontFamily`, `lineHeight`, `letterSpacing`,
and italic from an injected typography are **not** carried onto that native label (they render fully on
Android and on the Compose glass path). Weights map across the full 100–900 range. If exact typographic
fidelity on iOS matters more than native rendering, keep those texts on Compose-drawn surfaces.

## Open / deferred
- **iOS-26 Liquid Glass refraction vs the opaque `pinFilling` backing** on toggle/slider/segmented/search/
  datepicker — the opaque backing blocks the material's refraction; needs visual tuning on an iOS 26 device
  (translucent/material backing, or gate on a solid surface). Tracked in `docs/interop-notes.md`.
