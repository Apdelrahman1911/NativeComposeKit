package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

// Android is single-canvas Compose — the full visibility transition is safe, so the platform-idiomatic
// AnimatedVisibility is used directly.
@Composable
public actual fun NativeCollapsible(
    visible: Boolean,
    modifier: Modifier,
    content: @Composable () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = expandVertically(),
        exit = shrinkVertically(),
    ) {
        content()
    }
}
