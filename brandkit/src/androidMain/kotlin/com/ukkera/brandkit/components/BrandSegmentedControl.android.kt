package com.ukkera.brandkit.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.ukkera.brandkit.components.model.ResolvedSegmentedStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal actual fun PlatformBrandSegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    style: ResolvedSegmentedStyle,
    contentDescription: String?,
    testTag: String?,
) {
    var m = modifier
    testTag?.let { m = m.testTag(it) }
    // Names the group; each SegmentedButton stays its own selectable node (a merge boundary).
    contentDescription?.let { cd -> m = m.semantics { this.contentDescription = cd } }
    SingleChoiceSegmentedButtonRow(modifier = m) {
        options.forEachIndexed { index, label ->
            SegmentedButton(
                selected = index == selectedIndex,
                onClick = { onSelectedIndexChange(index) },
                enabled = enabled,
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                // Apply the resolved brand style instead of silently using Material defaults.
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = style.selectedColor,
                    activeContentColor = style.selectedTextColor,
                    inactiveContentColor = style.textColor,
                ),
            ) {
                Text(label, style = style.textStyle)
            }
        }
    }
}
