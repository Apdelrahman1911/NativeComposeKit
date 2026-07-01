package io.github.apdelrahman1911.nativecomposekit.app

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.dp

/**
 * Bottom inset a hosting shell asks scrollable content to reserve, when the shell overlays a **translucent bottom
 * bar** (the iOS `UITabBar` / Liquid Glass toolbar) on top of the content. Scrollable content fills to the very
 * bottom — behind the bar — and adds this inset to its bottom content-padding so the last item clears the bar
 * while the scroll still renders BEHIND it (content scrolls under the glass). The iOS shell provides the bar
 * height; Android leaves it 0 (the Material `NativeNavHost` reserves space for its `NavigationBar` via Scaffold
 * padding).
 */
val LocalNativeContentBottomInset = compositionLocalOf { 0.dp }
