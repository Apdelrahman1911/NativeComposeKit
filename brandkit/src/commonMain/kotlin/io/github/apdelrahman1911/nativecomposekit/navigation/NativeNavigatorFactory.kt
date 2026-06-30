package io.github.apdelrahman1911.nativecomposekit.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/**
 * Create a [NativeNavigator] with one root route per tab.
 *
 * Use [rememberNativeNavigator] from a Compose composition (Android `NativeNavHost`); use [createNativeNavigator]
 * to build one **outside** composition (iOS, where several hosted `ComposeUIViewController`s must share a single
 * navigator instance held by the SwiftUI shell).
 */
@Composable
public fun rememberNativeNavigator(
    tabs: List<NativeTab>,
    initialTab: NativeTab = tabs.first(),
    rootRoutes: (NativeTab) -> NativeRoute,
): NativeNavigator = remember { createNativeNavigator(tabs, initialTab, rootRoutes) }

public fun createNativeNavigator(
    tabs: List<NativeTab>,
    initialTab: NativeTab = tabs.first(),
    rootRoutes: (NativeTab) -> NativeRoute,
): NativeNavigator = NativeNavigator(NativeNavigationState(tabs, initialTab, rootRoutes))
