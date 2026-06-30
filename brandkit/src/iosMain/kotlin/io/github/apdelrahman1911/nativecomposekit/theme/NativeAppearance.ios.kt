package io.github.apdelrahman1911.nativecomposekit.theme

import platform.UIKit.UIApplication
import platform.UIKit.UIUserInterfaceStyle
import platform.UIKit.UIWindow

/**
 * Flips every window's interface style so the **native** chrome (the SwiftUI shell's tab bar / nav bar and
 * any native controls) matches the app-wide dark/light choice. This also updates the trait collection, so
 * hosted Compose compositions see the change too (though they primarily follow [NativeAppearance.darkOverride]).
 */
internal actual fun applyPlatformColorScheme(dark: Boolean) {
    val style = if (dark) UIUserInterfaceStyle.UIUserInterfaceStyleDark else UIUserInterfaceStyle.UIUserInterfaceStyleLight
    UIApplication.sharedApplication.windows.forEach { (it as? UIWindow)?.overrideUserInterfaceStyle = style }
}
