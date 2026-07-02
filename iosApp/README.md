# iosApp (native chrome host)

A thin Swift app around the shared Compose UI. The entry point (`iOSApp.swift`) creates the Kotlin
navigator with `MainViewControllerKt.createNativeNavRoot()` and hands it to `NativeNavShell`
(`Native/NativeNavShell.swift`) — real UIKit chrome (`UINavigationController` + `UITabBarController`)
that projects the Compose-owned navigation state and sends intents back. It never owns or reconciles
the stack; Kotlin stays the single source of truth. The shell ignores safe areas so Compose content
renders edge-to-edge and scrolls under the Liquid Glass bars. See
[`docs/native-chrome.md`](../docs/native-chrome.md) for the full pattern.

`ContentView.swift` is the kept pure-Compose fallback: it hosts
`MainViewControllerKt.MainViewController()` (Compose-drawn chrome, no UIKit bars) via
`UIViewControllerRepresentable`. The app target does not use it — point `iOSApp.swift` at
`ContentView()` to run without the native chrome shell.

The `.xcodeproj` is **generated** from `project.yml` with
[XcodeGen](https://github.com/yonaskolb/XcodeGen) (it is git-ignored), so there is no hand-authored
project file to drift.

## Run on a Mac

```bash
# 1. one-time: install the generator
brew install xcodegen

# 2. generate the Xcode project from project.yml
cd iosApp && xcodegen generate

# 3. open and run (⌘R) on a simulator
open iosApp.xcodeproj
```

`project.yml` wires a pre-build script that runs
`./gradlew :composeApp:embedAndSignAppleFrameworkForXcode`, so building the iOS app also links the
Kotlin/Compose framework automatically.

Build settings (app name, bundle id, signing team) live in `iosApp/iosApp/Configuration/Config.xcconfig`.
`TEAM_ID` is left blank — simulator builds work as-is; set it to run on a physical device.
