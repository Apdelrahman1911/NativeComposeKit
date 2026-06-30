package com.ukkera.brandkit.navigation

import androidx.compose.runtime.Composable

/** No-op on iOS: the production shell handles back via the native SwiftUI `NavigationStack` gesture, and
 * Compose-on-iOS (the iOS-15 fallback) has no system back button to intercept. */
@Composable
internal actual fun BrandBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // intentionally empty
}
