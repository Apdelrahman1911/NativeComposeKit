package io.github.apdelrahman1911.nativecomposekit.navigation

import androidx.compose.runtime.Composable

/**
 * Handle the platform back affordance while [enabled]. Android → the system/predictive back gesture
 * (`androidx.activity.compose.BackHandler`); iOS → no-op (back is the native SwiftUI `NavigationStack`
 * gesture in the production shell, and Compose-on-iOS has no system back button).
 */
@Composable
internal expect fun BrandBackHandler(enabled: Boolean, onBack: () -> Unit)
