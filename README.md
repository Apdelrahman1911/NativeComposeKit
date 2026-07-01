# NativeComposeKit

A Compose Multiplatform UI kit for Android and iOS. You call one shared component API from
`commonMain`, and each platform renders with the most native widget available: Jetpack Compose
Material 3 on Android, real UIKit controls on iOS. A `NativeToggle` is a `Switch` on Android and a
`UISwitch` on iOS; a `NativeSegmentedControl` is a `UISegmentedControl` on iOS. The shared code stays
the same — the rendering doesn't pretend.

![Android catalog — native Material 3 rendering](docs/android-catalog.png)

> The shared catalog running on Android with Material 3. The same `commonMain` code renders with real
> UIKit controls on iOS.

## The idea

One shared Compose/KMP API → the most-native renderer per platform.

Cross-platform UI usually means one of two things: draw everything yourself so it looks identical
everywhere (and slightly off on every platform), or maintain two native codebases. This kit takes a
third path. Each component is a single `@Composable` in `commonMain` that resolves its styling from
the theme and then hands off to a platform renderer through `expect`/`actual`:

```
NativeButton(…)                       // commonMain: the shared API + theme resolution
  └─ expect PlatformNativeButton(style, …)
       ├─ androidMain → Material 3 Button                 (NativeButton.android.kt)
       └─ iosMain     → UIButton via Compose UIKitView    (NativeButton.ios.kt)
```

Android users get Material 3 with its motion, ripple, and dynamic color. iOS users get the system
controls they already know — correct sizing, haptics, context menus, and accessibility — instead of
look-alikes. A handful of components have no native counterpart on one side (there's no UIKit radio
button or checkbox); those are documented exceptions that stay Compose-drawn on both platforms.

## Platforms

| Target | Renderer |
|---|---|
| Android | Jetpack Compose Material 3 |
| iOS (arm64, simulator-arm64) | UIKit controls hosted in Compose via `UIKitView` |
| Shared | Kotlin Multiplatform + Compose Multiplatform (`commonMain`) |

iOS ships `iosArm64` and `iosSimulatorArm64`. `iosX64` is intentionally dropped — Compose
Multiplatform 1.11 no longer supports the Apple x86_64 target.

## Toolchain

| Tool | Version |
|---|---|
| Kotlin | 2.3.21 |
| Compose Multiplatform | 1.11.0 |
| Android Gradle Plugin | 8.13.0 |
| Gradle | 8.13 |
| compileSdk / targetSdk | 36 |
| minSdk | 26 |
| iOS deployment target | 15.0 |

These are pinned in `gradle/libs.versions.toml`. The library module (`:nativecomposekit`) depends only on
Compose artifacts — no third-party runtime dependencies.

## Modules

```
:nativecomposekit     the UI kit (published surface; ABI-locked with binary-compatibility-validator)
:composeApp   the sample catalog app that exercises every component on both platforms
iosApp        thin SwiftUI host that loads the shared Compose UI
```

## Setup

NativeComposeKit isn't on Maven Central yet (see [development notes](#development-notes)). For now,
consume `:nativecomposekit` as a source module.

Clone it next to your project and pull the module into your `settings.gradle.kts`:

```kotlin
// settings.gradle.kts
includeBuild("../NativeComposeKit") // or vendor :nativecomposekit into your own tree
```

Then depend on it from a Compose Multiplatform module:

```kotlin
// build.gradle.kts (commonMain)
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("io.github.apdelrahman1911.nativecomposekit:nativecomposekit") // coordinates TBD; see development notes
        }
    }
}
```

`org.jetbrains.compose.experimental.uikit.enabled=true` must be set in `gradle.properties` for the
iOS UIKit interop to compile.

## Usage

Every component is a `Native*` composable from `io.github.apdelrahman1911.nativecomposekit.components`. Wrap
your UI in `AppTheme` once, then call components directly:

```kotlin
import io.github.apdelrahman1911.nativecomposekit.components.*
import io.github.apdelrahman1911.nativecomposekit.theme.AppTheme

@Composable
fun SignInForm(onSignIn: (String) -> Unit) {
    var email by remember { mutableStateOf("") }

    NativeScaffold(topBar = { NativeTopBar(title = "Sign in") }) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            NativeText("Welcome back", style = NativeTextStyle.Title)
            NativeTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                placeholder = "you@example.com",
            )
            NativeButton(
                text = "Continue",
                onClick = { onSignIn(email) },
                fullWidth = true,
            )
        }
    }
}
```

On Android this renders Material 3 controls; on iOS the button, text, and field are real `UIButton`,
`UILabel`, and `UITextField` instances.

## Theming

`AppTheme` is the single source of styling — Material's `ColorScheme`/`Typography`/`Shapes` plus a
small set of design tokens (spacing, radii, status colors). There is no separate token file.
Components read their defaults from the theme and expose per-call overrides as typed parameters.

```kotlin
AppTheme(
    darkTheme = isSystemInDarkTheme(),
    // override any slice; defaults cover the common case
    // lightColors = myLightScheme,
    // tokens = NativeTokens(spacingMd = 12.dp),
) {
    App()
}
```

Switching `darkTheme` updates both the Compose Material controls and the UIKit controls — the iOS
renderers set `overrideUserInterfaceStyle` from the luminance of the surface they sit on, so they
read correctly in light and dark on any background. See
[`docs/design-system-rules.md`](docs/design-system-rules.md) for the rules a component follows.

## Project structure

A typical app using the kit looks like:

```
app/
  commonMain/
    App.kt              wraps everything in AppTheme
    screens/            your screens, built from Native* components
  androidMain/          MainActivity → App()
  iosMain/              MainViewController → App()
iosApp/                 SwiftUI host (ComposeUIViewController)
```

Keep `AppTheme` at the root so every component resolves the same theme. Build screens out of
`Native*` components rather than reaching for raw Material or UIKit — that's what keeps a screen
native on both platforms from one code path. The `:composeApp` module in this repo is a working
example.

## Usage notes

A few things worth knowing up front; they save the most common surprises.

- **Give content-sized UIKit controls an explicit width.** Some iOS controls size to their content
  through `UIKitView` interop and collapse to zero width without a width constraint.
  - `NativeSegmentedControl` usually wants `Modifier.fillMaxWidth()` — segmented controls normally
    span the available width.
  - `NativePageControl` inside a `Row` wants `Modifier.weight(1f)` (or another explicit width) so its
    dots have room. Fixed-size controls like `NativeToggle` and `NativeStepper` don't need this.
- **Sheets:** use `NativeSheet` for sheet-style and mobile content. On iOS it's a real
  `UISheetPresentationController` with detents and a grab handle.
- **Popovers:** `NativePopover` adapts to the device. On iPhone / compact width it uses a lightweight
  Compose popover (a full-screen UIKit popover on a phone is the wrong UX); on iPad / regular width
  it uses a native `UIPopoverPresentationController` anchored to your `anchor`.
- **Alerts:** for plain text-and-buttons alerts use `feedback.alert` — it's a real `UIAlertController`
  on iOS. Reach for `NativeDialog` only when the modal needs custom Compose content (a form, a list,
  an image).
- **OTP entry:** for native iOS SMS autofill use `NativeTextField(contentType = OneTimeCode)`.
  `NativeOtpField` is the branded segmented-cell visual and does not provide system autofill.
- **Settings switches:** prefer `NativeToggle` over a checkbox on iOS — a switch is the platform
  idiom. `NativeCheckbox` exists for the cases that genuinely need a checkbox.

## iOS interop limitations

The kit hosts UIKit controls inside Compose via `UIKitView`. A few Compose Multiplatform 1.11
behaviors in this area are upstream and have no clean workaround through public API. The kit picks
the least-bad trade-off and documents it; details and upstream issue links are in
[`docs/interop-notes.md`](docs/interop-notes.md).

- **Scrolling:** a `UIKitView` inside a Compose scroll can either clip its edge (cut-out placement)
  or drift slightly during the gesture (overlay placement). Leaf controls use overlay — the drift is
  subtler than a clipped edge and settles the moment scrolling stops.
- **Menu buttons:** a `UIButton` with a `UIMenu` can drift from its row after its menu has been
  opened once, because the menu leaves a transform on a view the kit can't reach. The native menu is
  kept; the post-open drift is the accepted cost.
- **Dialogs/popups:** a freshly opened Compose `Dialog`/`Popup` can flash the host backdrop for one
  frame where a `UIKitView` will appear. `NativeDialog` avoids this by drawing its text through
  Compose and compositing its controls as an overlay, so the card's own pixels show from the first
  frame.

## Components

Full reference with every parameter, both renderers, and examples is in
[`docs/components/`](docs/components/README.md).

- **Text & input** — [`NativeText`](docs/components/text-and-input.md),
  [`NativeTextField`](docs/components/text-and-input.md),
  [`NativeSearchBar`](docs/components/text-and-input.md),
  [`NativeOtpField`](docs/components/text-and-input.md)
- **Buttons** — [`NativeButton`](docs/components/buttons.md),
  [`NativeIconButton`](docs/components/buttons.md),
  [`NativeSplitButton`](docs/components/buttons.md), `NativeMenu`
- **Selection & sliders** — [`NativeToggle`](docs/components/selection-and-sliders.md),
  [`NativeCheckbox`](docs/components/selection-and-sliders.md),
  [`NativeRadioGroup`](docs/components/selection-and-sliders.md),
  [`NativeSegmentedControl`](docs/components/selection-and-sliders.md),
  [`NativeSlider`](docs/components/selection-and-sliders.md),
  [`NativeStepper`](docs/components/selection-and-sliders.md),
  [`NativeRating`](docs/components/selection-and-sliders.md)
- **Pickers** — [`NativeDatePicker`](docs/components/pickers.md),
  [`NativeColorWell`](docs/components/pickers.md),
  [`NativePageControl`](docs/components/pickers.md)
- **Overlays** — [`NativeSheet`](docs/components/overlays.md),
  [`NativePopover`](docs/components/overlays.md),
  [`NativeDialog`](docs/components/overlays.md),
  [`NativeShareSheet`](docs/components/overlays.md)
- **Feedback & progress** — [`NativeProgressIndicator`](docs/components/feedback.md), alert /
  confirmation sheet / snackbar / toast / banner / inline status ([`feedback`](docs/components/feedback.md))
- **Layout** — [`NativeCard`](docs/components/layout.md),
  [`NativeScaffold`](docs/components/layout.md), [`NativeTopBar`](docs/components/layout.md),
  [`NativeListSection`](docs/components/layout.md), [`NativeListItem`](docs/components/layout.md),
  [`NativeDivider`](docs/components/layout.md)
- **Display & state** — [`NativeContentState`](docs/components/display-and-state.md),
  [`NativeSkeleton`](docs/components/display-and-state.md),
  [`NativeEmptyState`](docs/components/display-and-state.md),
  [`NativePullRefresh`](docs/components/display-and-state.md),
  [`NativeBadge`](docs/components/display-and-state.md),
  [`NativeAvatar`](docs/components/display-and-state.md),
  [`NativeChip`](docs/components/display-and-state.md)
- **Accessibility & focus** — [`nativeDismissKeyboardOnTap`, `nativeHeading`, `nativeAutoFocus`, focus handles / order / group](docs/components/accessibility.md)

## Documentation

- [Components reference](docs/components/README.md) — every component, parameter, and example
- [Architecture](docs/architecture.md) — how the shared API resolves to platform renderers
- [Design-system rules](docs/design-system-rules.md) — what a component must do to belong
- [Navigation](docs/navigation.md) — bring your own navigation; the kit's nav-agnostic native chrome contract
- [iOS interop notes](docs/interop-notes.md) — UIKit interop limitations and trade-offs

## Building

```bash
# Android
./gradlew :composeApp:assembleDebug
./gradlew :composeApp:installDebug        # to a running emulator/device

# iOS (Kotlin side — fast check before opening Xcode)
./gradlew :composeApp:compileKotlinIosSimulatorArm64

# iOS app
cd iosApp && xcodegen generate && open iosApp.xcodeproj
```

### Tests and checks

```bash
./gradlew :nativecomposekit:apiCheck              # fail if the public ABI changed without an apiDump
./gradlew :nativecomposekit:testDebugUnitTest     # unit + Robolectric Compose tests (no emulator)
./gradlew :composeApp:testDebugUnitTest
./gradlew check                           # everything
```

The library's public API is locked with
[binary-compatibility-validator](https://github.com/Kotlin/binary-compatibility-validator). After an
intentional API change, regenerate the baseline with `./gradlew :nativecomposekit:apiDump` and commit the
updated files under `nativecomposekit/api/`.

## Contributing

Contributions are welcome. Before opening a pull request:

- Read [`docs/design-system-rules.md`](docs/design-system-rules.md) — a new component must resolve
  its style from the theme, render natively per platform (or be a documented exception), and add real
  design-system value rather than wrap a library.
- Keep `:nativecomposekit` free of third-party runtime dependencies.
- Run `./gradlew check` and, for any public API change, `:nativecomposekit:apiDump`.
- Add the component to the sample catalog so it's exercised on both platforms.

## Development notes

- **Naming.** The Kotlin package is `io.github.apdelrahman1911.nativecomposekit`, the Android
  namespace is `io.github.apdelrahman1911.nativecomposekit.kit`, and the public components use the
  `Native*` prefix. The Gradle module is still named `:nativecomposekit` — an internal build name, not the
  published group, so there's no need to churn it.
- **Publishing.** Maven Central coordinates aren't set up yet; that's the next step toward consuming
  the kit as a normal dependency. The dependency line above is illustrative until then.

## License

NativeComposeKit is released under the [Apache License 2.0](LICENSE).
