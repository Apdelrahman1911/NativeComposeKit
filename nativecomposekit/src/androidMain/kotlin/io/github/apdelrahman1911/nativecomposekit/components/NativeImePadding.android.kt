package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Android: the platform `imePadding()` already accounts for the IME (and its suggestion strip). When [minBottom]
 * is set, pad by the LARGER of the IME inset and [minBottom] (never their sum).
 */
@Composable
public actual fun Modifier.nativeImePadding(minBottom: Dp): Modifier =
    if (minBottom <= 0.dp) {
        imePadding()
    } else {
        padding(bottom = maxOf(WindowInsets.ime.asPaddingValues().calculateBottomPadding(), minBottom))
    }
