package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Bottom padding that keeps content clear of the on-screen keyboard.
 *
 * Prefer this over `Modifier.imePadding()` around scrollable content that hosts [NativeTextField] on iOS. On
 * iOS it measures the **full keyboard frame** reported by the system — which includes the input-accessory bar
 * (the Done toolbar) and the QuickType/suggestions row — so that layer never overlaps content. `Modifier.ime`
 * insets can omit the accessory/suggestions height, leaving the keyboard's top bar covering the UI.
 *
 * On Android it delegates to `Modifier.imePadding()`.
 */
@Composable
public expect fun Modifier.nativeImePadding(): Modifier
