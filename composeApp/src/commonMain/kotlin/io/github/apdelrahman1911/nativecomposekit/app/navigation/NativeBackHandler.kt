package io.github.apdelrahman1911.nativecomposekit.app.navigation

import androidx.compose.runtime.Composable

/**
 * Handle the platform back affordance while [enabled]. Android → the system/predictive back gesture
 * (`androidx.activity.compose.BackHandler`); iOS → the left-edge back-swipe, which Compose Multiplatform routes
 * through `androidx.compose.ui.backhandler.BackHandler`. Both pop the Kotlin-owned stack, so no native navigation
 * container owns a second stack.
 */
@Composable
internal expect fun NativeBackHandler(enabled: Boolean, onBack: () -> Unit)
