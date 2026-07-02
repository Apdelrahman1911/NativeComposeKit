package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Android: the platform `imePadding()` already accounts for the IME (and its suggestion strip). When [minBottom]
 * is set, pad by the LARGER of the IME inset and [minBottom] (never their sum) — expressed as a `union` of the
 * IME insets with a fixed bottom inset, so both branches are layout-phase (no per-animation-frame recomposition)
 * and both **consume** the IME insets, keeping nested `imePadding()` children from double-padding.
 */
@Composable
public actual fun Modifier.nativeImePadding(minBottom: Dp): Modifier =
    if (minBottom <= 0.dp) {
        imePadding()
    } else {
        windowInsetsPadding(WindowInsets.ime.union(WindowInsets(bottom = minBottom)))
    }
