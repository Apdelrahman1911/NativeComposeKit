package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.foundation.layout.imePadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** Android: the platform `imePadding()` already accounts for the IME (and its suggestion strip). */
@Composable
public actual fun Modifier.nativeImePadding(): Modifier = imePadding()
