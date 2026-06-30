package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * Android NativePopover — the shared Compose popover ([ComposeNativePopover]): an elevated themed [NativeCard] in a
 * `Popup` anchored to the [anchor]. Full Compose context preserved.
 */
@Composable
internal actual fun PlatformNativePopover(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier,
    alignment: Alignment,
    testTag: String?,
    anchor: (@Composable () -> Unit)?,
    content: @Composable () -> Unit,
) = ComposeNativePopover(visible, onDismissRequest, modifier, alignment, testTag, anchor, content)
