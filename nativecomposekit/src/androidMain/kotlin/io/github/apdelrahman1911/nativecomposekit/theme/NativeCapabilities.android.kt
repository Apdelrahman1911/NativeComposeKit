package io.github.apdelrahman1911.nativecomposekit.theme

import android.database.ContentObserver
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

/**
 * Android has no dedicated reduce-motion flag before API 34; the platform-wide proxy used by accessibility
 * tooling is `ANIMATOR_DURATION_SCALE == 0` — the user's "Remove animations" / "Animator duration scale: off"
 * setting. Observed live via a `ContentObserver`, so flipping the setting mid-session updates every
 * composition (matching the iOS actual, which observes `UIAccessibilityReduceMotionStatusDidChangeNotification`).
 */
@Composable
internal actual fun rememberReduceMotion(): Boolean {
    val context = LocalContext.current
    fun read(): Boolean =
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f

    var reduceMotion by remember(context) { mutableStateOf(read()) }
    DisposableEffect(context) {
        val observer = object : ContentObserver(null) {
            override fun onChange(selfChange: Boolean) {
                reduceMotion = read()
            }
        }
        context.contentResolver.registerContentObserver(
            Settings.Global.getUriFor(Settings.Global.ANIMATOR_DURATION_SCALE),
            false,
            observer,
        )
        onDispose { context.contentResolver.unregisterContentObserver(observer) }
    }
    return reduceMotion
}
