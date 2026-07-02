package io.github.apdelrahman1911.nativecomposekit.components

import platform.UIKit.UIApplication
import platform.UIKit.UIWindow
import platform.UIKit.endEditing

/**
 * Resigns whatever native field is first responder. The kit's iOS text inputs are real
 * `UITextField`/`UITextView`s whose focus lives in UIKit — clearing Compose focus does not reach them,
 * so the tap-to-dismiss modifier ends editing on the key window explicitly.
 */
internal actual fun platformEndEditing() {
    val app = UIApplication.sharedApplication
    val window = app.keyWindow ?: (app.windows.firstOrNull() as? UIWindow)
    window?.endEditing(true)
}
