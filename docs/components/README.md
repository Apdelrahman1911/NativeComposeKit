# Components reference

Every component in NativeComposeKit, grouped by category. Each page documents purpose, the Android
and iOS renderer, when to use it (and when not to), all public parameters with defaults, an example,
and any platform notes or known limitations.

For the rules a component follows and the shared-API-to-native-renderer model, see
[`design-system-rules.md`](../design-system-rules.md) and [`architecture.md`](../architecture.md).
iOS-specific interop behavior is in [`interop-notes.md`](../interop-notes.md).

## Categories

- [Text & input](text-and-input.md) — `NativeText`, `NativeTextField`, `NativeSearchBar`, `NativeOtpField`
- [Buttons](buttons.md) — `NativeButton`, `NativeIconButton`, `NativeSplitButton`, `NativeMenu`
- [Selection & sliders](selection-and-sliders.md) — `NativeToggle`, `NativeCheckbox`, `NativeRadioGroup`, `NativeSegmentedControl`, `NativeSlider`, `NativeStepper`, `NativeRating`
- [Pickers & pagination](pickers.md) — `NativeDatePicker`, `NativeColorWell`, `NativePageControl`, `NativePager`, load-more helpers
- [Overlays](overlays.md) — `NativeSheet`, `NativePopover`, `NativeDialog`, `NativeShareSheet`
- [Feedback & progress](feedback.md) — `NativeProgressIndicator`, alert / confirmation sheet / snackbar / toast / banner / inline status
- [Layout](layout.md) — `NativeCard`, `NativeScaffold`, `NativeTopBar`, `NativeListSection`, `NativeListItem`, `NativeDivider`
- [Display & state](display-and-state.md) — `NativeContentState`, `NativeSkeleton`, `NativeEmptyState`, `NativePullRefresh`, `NativeBadge`, `NativeAvatar`, `NativeChip`
- [Accessibility & focus](accessibility.md) — `nativeDismissKeyboardOnTap`, `nativeHeading`, `nativeAutoFocus`, focus handles / order / group

## Index

| Component | Page |
|---|---|
| `NativeAvatar` | [Display & state](display-and-state.md) |
| `NativeBadge` | [Display & state](display-and-state.md) |
| `NativeButton` | [Buttons](buttons.md) |
| `NativeCard` | [Layout](layout.md) |
| `NativeCheckbox` | [Selection & sliders](selection-and-sliders.md) |
| `NativeChip` | [Display & state](display-and-state.md) |
| `NativeColorWell` | [Pickers](pickers.md) |
| `NativeContentState` | [Display & state](display-and-state.md) |
| `NativeDatePicker` | [Pickers](pickers.md) |
| `NativeDialog` | [Overlays](overlays.md) |
| `NativeDialogColors` | [Overlays](overlays.md) |
| `NativeDivider` | [Layout](layout.md) |
| `NativeEmptyState` | [Display & state](display-and-state.md) |
| `NativeIconButton` | [Buttons](buttons.md) |
| `NativeListItem` | [Layout](layout.md) |
| `NativeListSection` | [Layout](layout.md) |
| `NativeLoadMoreEffect` | [Pickers](pickers.md) |
| `NativeMenu` | [Buttons](buttons.md) |
| `NativeOtpField` | [Text & input](text-and-input.md) |
| `NativePageControl` | [Pickers](pickers.md) |
| `NativePageLoadState` | [Pickers](pickers.md) |
| `NativePager` | [Pickers](pickers.md) |
| `NativePopover` | [Overlays](overlays.md) |
| `NativeProgressIndicator` | [Feedback & progress](feedback.md) |
| `NativePullRefresh` | [Display & state](display-and-state.md) |
| `NativeRadioGroup` | [Selection & sliders](selection-and-sliders.md) |
| `NativeRating` | [Selection & sliders](selection-and-sliders.md) |
| `NativeScaffold` | [Layout](layout.md) |
| `NativeSearchBar` | [Text & input](text-and-input.md) |
| `NativeSegmentedControl` | [Selection & sliders](selection-and-sliders.md) |
| `NativeShareSheet` | [Overlays](overlays.md) |
| `NativeSheet` | [Overlays](overlays.md) |
| `NativeSkeleton` | [Display & state](display-and-state.md) |
| `NativeSlider` | [Selection & sliders](selection-and-sliders.md) |
| `NativeStepper` | [Selection & sliders](selection-and-sliders.md) |
| `NativeText` | [Text & input](text-and-input.md) |
| `NativeTextField` | [Text & input](text-and-input.md) |
| `NativeToggle` | [Selection & sliders](selection-and-sliders.md) |
| `NativeTopBar` | [Layout](layout.md) |
| `NativeFocusHandle` | [Accessibility & focus](accessibility.md) |
| `nativeAutoFocus` | [Accessibility & focus](accessibility.md) |
| `nativeDismissKeyboardOnTap` | [Accessibility & focus](accessibility.md) |
| `nativeFocusGroup` | [Accessibility & focus](accessibility.md) |
| `nativeFocusOrder` | [Accessibility & focus](accessibility.md) |
| `nativeFocusTarget` | [Accessibility & focus](accessibility.md) |
| `nativeHeading` | [Accessibility & focus](accessibility.md) |
| `nativePaginationFooter` | [Pickers](pickers.md) |
| `rememberNativeFocusHandle` | [Accessibility & focus](accessibility.md) |
