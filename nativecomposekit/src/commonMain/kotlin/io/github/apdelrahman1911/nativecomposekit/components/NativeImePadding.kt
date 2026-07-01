package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Bottom padding that keeps content clear of the on-screen keyboard.
 *
 * Prefer this over `Modifier.imePadding()` around scrollable content that hosts [NativeTextField] on iOS. On
 * iOS it measures the **full keyboard frame** reported by the system — which includes the input-accessory bar
 * (the Done toolbar) and the QuickType/suggestions row — so that layer never overlaps content. `Modifier.ime`
 * insets can omit the accessory/suggestions height, leaving the keyboard's top bar covering the UI.
 *
 * [minBottom] is a baseline bottom inset to keep even when the keyboard is down — e.g. the height of a
 * translucent bottom bar the content scrolls under. The padding is the **larger** of the keyboard extent and
 * [minBottom], never their sum: while the keyboard is up it covers the bar, so the keyboard extent alone is
 * correct; while it's down, [minBottom] keeps the last content clear of the bar.
 *
 * On Android it delegates to `Modifier.imePadding()` when [minBottom] is zero.
 */
@Composable
public expect fun Modifier.nativeImePadding(minBottom: Dp = 0.dp): Modifier
