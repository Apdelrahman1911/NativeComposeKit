# iosApp (SwiftUI host)

A thin SwiftUI app that hosts the shared Compose `App()` via `ComposeUIViewController`. The
`.xcodeproj` is **generated** from `project.yml` with [XcodeGen](https://github.com/yonsoo/XcodeGen)
(it is git-ignored), so there is no hand-authored project file to drift.

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
