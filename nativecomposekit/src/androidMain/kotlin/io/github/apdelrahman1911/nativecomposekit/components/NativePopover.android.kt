package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize

/**
 * Android NativePopover — the shared Compose popover ([ComposeNativePopover]): an elevated themed [NativeCard] in a
 * `Popup` anchored to the [anchor]. Full Compose context preserved. [preferredSize] is iPad-native-only by
 * contract (the Compose panel sizes to its content), so it is not consumed here.
 */
@Composable
internal actual fun PlatformNativePopover(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier,
    alignment: Alignment,
    preferredSize: DpSize,
    testTag: String?,
    anchor: (@Composable () -> Unit)?,
    content: @Composable () -> Unit,
): Unit = ComposeNativePopover(visible, onDismissRequest, modifier, alignment, testTag, anchor, content)
