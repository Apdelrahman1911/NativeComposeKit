package io.github.apdelrahman1911.nativecomposekit.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.backhandler.BackHandler

/**
 * iOS: the system back affordance is the left-edge swipe, which Compose Multiplatform routes through
 * [androidx.compose.ui.backhandler.BackHandler]. Wiring it here lets the pure-Compose [NativeNavContent] pop the
 * Kotlin-owned stack on an edge-swipe — with no UIKit/SwiftUI navigation container owning a second stack.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal actual fun NativeBackHandler(enabled: Boolean, onBack: () -> Unit) {
    BackHandler(enabled = enabled, onBack = onBack)
}
