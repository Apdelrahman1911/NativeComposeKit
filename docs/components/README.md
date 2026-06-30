# Components reference

Every component in NativeComposeKit, grouped by category. Each page documents purpose, the Android
and iOS renderer, when to use it (and when not to), all public parameters with defaults, an example,
and any platform notes or known limitations.

For the rules a component follows and the shared-API-to-native-renderer model, see
[`design-system-rules.md`](../design-system-rules.md) and [`architecture.md`](../architecture.md).
iOS-specific interop behavior is in [`interop-notes.md`](../interop-notes.md).

## Categories

- [Text & input](text-and-input.md) — `BrandText`, `BrandTextField`, `BrandSearchBar`, `BrandOtpField`
- [Buttons](buttons.md) — `BrandButton`, `BrandIconButton`, `BrandSplitButton`, `BrandMenu`
- [Selection & sliders](selection-and-sliders.md) — `BrandToggle`, `BrandCheckbox`, `BrandRadioGroup`, `BrandSegmentedControl`, `BrandSlider`, `BrandStepper`, `BrandRating`
- [Pickers](pickers.md) — `BrandDatePicker`, `BrandColorWell`, `BrandPageControl`
- [Overlays](overlays.md) — `BrandSheet`, `BrandPopover`, `BrandDialog`, `BrandShareSheet`
- [Feedback & progress](feedback.md) — `BrandProgressIndicator`, alert / confirmation sheet / snackbar / toast / banner / inline status
- [Layout](layout.md) — `BrandCard`, `BrandScaffold`, `BrandTopBar`, `BrandListSection`, `BrandListItem`, `BrandDivider`
- [Display & state](display-and-state.md) — `BrandContentState`, `BrandSkeleton`, `BrandEmptyState`, `BrandPullRefresh`, `BrandBadge`, `BrandAvatar`, `BrandChip`
- [Accessibility & focus](accessibility.md) — `brandDismissKeyboardOnTap`, `brandHeading`
- [Deprecated](deprecated.md) — `BrandTabBar`, `BrandTooltip`

## Index

| Component | Page |
|---|---|
| `BrandAvatar` | [Display & state](display-and-state.md) |
| `BrandBadge` | [Display & state](display-and-state.md) |
| `BrandButton` | [Buttons](buttons.md) |
| `BrandCard` | [Layout](layout.md) |
| `BrandCheckbox` | [Selection & sliders](selection-and-sliders.md) |
| `BrandChip` | [Display & state](display-and-state.md) |
| `BrandColorWell` | [Pickers](pickers.md) |
| `BrandContentState` | [Display & state](display-and-state.md) |
| `BrandDatePicker` | [Pickers](pickers.md) |
| `BrandDialog` | [Overlays](overlays.md) |
| `BrandDivider` | [Layout](layout.md) |
| `BrandEmptyState` | [Display & state](display-and-state.md) |
| `BrandIconButton` | [Buttons](buttons.md) |
| `BrandListItem` | [Layout](layout.md) |
| `BrandListSection` | [Layout](layout.md) |
| `BrandMenu` | [Buttons](buttons.md) |
| `BrandOtpField` | [Text & input](text-and-input.md) |
| `BrandPageControl` | [Pickers](pickers.md) |
| `BrandPopover` | [Overlays](overlays.md) |
| `BrandProgressIndicator` | [Feedback & progress](feedback.md) |
| `BrandPullRefresh` | [Display & state](display-and-state.md) |
| `BrandRadioGroup` | [Selection & sliders](selection-and-sliders.md) |
| `BrandRating` | [Selection & sliders](selection-and-sliders.md) |
| `BrandScaffold` | [Layout](layout.md) |
| `BrandSearchBar` | [Text & input](text-and-input.md) |
| `BrandSegmentedControl` | [Selection & sliders](selection-and-sliders.md) |
| `BrandShareSheet` | [Overlays](overlays.md) |
| `BrandSheet` | [Overlays](overlays.md) |
| `BrandSkeleton` | [Display & state](display-and-state.md) |
| `BrandSlider` | [Selection & sliders](selection-and-sliders.md) |
| `BrandStepper` | [Selection & sliders](selection-and-sliders.md) |
| `BrandText` | [Text & input](text-and-input.md) |
| `BrandTextField` | [Text & input](text-and-input.md) |
| `BrandToggle` | [Selection & sliders](selection-and-sliders.md) |
| `BrandTopBar` | [Layout](layout.md) |
| `brandDismissKeyboardOnTap` | [Accessibility & focus](accessibility.md) |
| `brandHeading` | [Accessibility & focus](accessibility.md) |
| `BrandTabBar` *(deprecated)* | [Deprecated](deprecated.md) |
| `BrandTooltip` *(deprecated)* | [Deprecated](deprecated.md) |
