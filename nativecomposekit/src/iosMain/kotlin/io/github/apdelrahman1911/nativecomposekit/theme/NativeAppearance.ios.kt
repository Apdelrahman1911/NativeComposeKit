package io.github.apdelrahman1911.nativecomposekit.theme

import platform.UIKit.UIApplication
import platform.UIKit.UIUserInterfaceStyle
import platform.UIKit.UIWindow

/**
 * Flips every window's interface style so the **native** chrome (the UIKit shell's nav/tab bars and any
 * native controls, including the keyboard) matches the app-wide dark/light choice. This also updates the
 * trait collection, so hosted Compose compositions see the change too (though they primarily follow
 * [NativeAppearance.darkOverride]).
 */
internal actual fun applyPlatformColorScheme(dark: Boolean?) {
    val style = when (dark) {
        true -> UIUserInterfaceStyle.UIUserInterfaceStyleDark
        false -> UIUserInterfaceStyle.UIUserInterfaceStyleLight
        null -> UIUserInterfaceStyle.UIUserInterfaceStyleUnspecified // clear the override: follow the system
    }
    UIApplication.sharedApplication.windows.forEach { (it as? UIWindow)?.overrideUserInterfaceStyle = style }
}
