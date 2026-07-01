package io.github.apdelrahman1911.nativecomposekit.app.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable

@Composable
internal actual fun NativeBackHandler(enabled: Boolean, onBack: () -> Unit) {
    BackHandler(enabled = enabled, onBack = onBack)
}
