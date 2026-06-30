package com.ukkera.brandkit.components

import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.ukkera.brandkit.components.model.ResolvedToggleStyle

@Composable
internal actual fun PlatformBrandToggle(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier,
    enabled: Boolean,
    style: ResolvedToggleStyle,
    contentDescription: String?,
    testTag: String?,
) {
    var m = modifier
    if (testTag != null) m = m.testTag(testTag)
    if (contentDescription != null) {
        val desc = contentDescription
        m = m.semantics { this.contentDescription = desc }
    }
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = m,
        enabled = enabled,
        colors = SwitchDefaults.colors(
            checkedThumbColor = style.thumbColor,
            checkedTrackColor = style.trackOnColor,
        ),
    )
}
