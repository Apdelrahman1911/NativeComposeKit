package com.ukkera.brandkit.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/**
 * Create a [BrandNavigator] with one root route per tab.
 *
 * Use [rememberBrandNavigator] from a Compose composition (Android `BrandNavHost`); use [createBrandNavigator]
 * to build one **outside** composition (iOS, where several hosted `ComposeUIViewController`s must share a single
 * navigator instance held by the SwiftUI shell).
 */
@Composable
public fun rememberBrandNavigator(
    tabs: List<BrandTab>,
    initialTab: BrandTab = tabs.first(),
    rootRoutes: (BrandTab) -> BrandRoute,
): BrandNavigator = remember { createBrandNavigator(tabs, initialTab, rootRoutes) }

public fun createBrandNavigator(
    tabs: List<BrandTab>,
    initialTab: BrandTab = tabs.first(),
    rootRoutes: (BrandTab) -> BrandRoute,
): BrandNavigator = BrandNavigator(BrandNavigationState(tabs, initialTab, rootRoutes))
