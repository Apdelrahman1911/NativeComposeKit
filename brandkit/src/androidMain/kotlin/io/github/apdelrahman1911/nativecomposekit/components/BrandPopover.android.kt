package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * Android BrandPopover — the shared Compose popover ([ComposeBrandPopover]): an elevated themed [BrandCard] in a
 * `Popup` anchored to the [anchor]. Full Compose context preserved.
 */
@Composable
internal actual fun PlatformBrandPopover(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier,
    alignment: Alignment,
    testTag: String?,
    anchor: (@Composable () -> Unit)?,
    content: @Composable () -> Unit,
) = ComposeBrandPopover(visible, onDismissRequest, modifier, alignment, testTag, anchor, content)
