package com.ukkera.brandkit.theme

import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Android has no dedicated reduce-motion flag before API 34; the platform-wide proxy used by accessibility
 * tooling is `ANIMATOR_DURATION_SCALE == 0` — the user's "Remove animations" / "Animator duration scale: off"
 * setting. Read at composition (the scale rarely changes mid-session); a `ContentObserver` for live updates
 * is a documented follow-up if needed.
 */
@Composable
internal actual fun rememberReduceMotion(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }
}
