package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.apdelrahman1911.nativecomposekit.theme.NativeTheme

/**
 * Visual style for a single-select group.
 * - [Radio]: a leading radio dot per row (Material / Android idiom).
 * - [Checkmark]: a trailing checkmark on the selected row (iOS grouped-table idiom).
 */
public enum class NativeSelectionStyle { Radio, Checkmark }

/**
 * A single-select group of [options] (generic [T]). Each row is a merged `Role.RadioButton` node inside a
 * `selectableGroup` (correct screen-reader grouping) with a ≥48dp target. [label] maps an option to its
 * display string.
 *
 * **Kit thesis — kept cross-platform as a documented exception.** iOS has **no native radio-group control**, so
 * there is nothing more-native to delegate to. The iOS-idiomatic single-select is already covered:
 * - [NativeSelectionStyle.Checkmark] renders the iOS grouped-table checkmark-list idiom (use this on iOS);
 * - for a small, fixed option set, prefer [NativeSegmentedControl] (a real `UISegmentedControl` on iOS).
 *
 * [NativeSelectionStyle.Radio] is the Material/Android dot idiom. Selection uses `==`, so [T] must have a stable
 * `equals` (a data class, enum, or primitive — not identity types like lambdas). [selected] = null is a valid
 * "nothing selected yet" state.
 *
 * `NativeRadioGroup(qualities, selected = quality, onSelectedChange = { quality = it }, label = { it.name })`
 */
@Composable
public fun <T> NativeRadioGroup(
    options: List<T>,
    selected: T?,
    onSelectedChange: (T) -> Unit,
    modifier: Modifier = Modifier,
    label: (T) -> String = { it.toString() },
    enabled: Boolean = true,
    style: NativeSelectionStyle = NativeSelectionStyle.Radio,
    testTag: String? = null,
) {
    val scheme = MaterialTheme.colorScheme
    var m = modifier.fillMaxWidth().selectableGroup()
    testTag?.let { m = m.testTag(it) }

    Column(m) {
        options.forEach { option ->
            val isSelected = option == selected
            val textColor = if (enabled) scheme.onSurface else scheme.onSurface.copy(alpha = 0.38f)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = isSelected,
                        enabled = enabled,
                        role = Role.RadioButton,
                        onClick = { onSelectedChange(option) },
                    )
                    .semantics(mergeDescendants = true) {}
                    .heightIn(min = 48.dp)
                    .padding(vertical = NativeTheme.tokens.spacingSm),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(NativeTheme.tokens.spacingSm),
            ) {
                when (style) {
                    NativeSelectionStyle.Radio -> {
                        RadioButton(selected = isSelected, onClick = null, enabled = enabled)
                        Text(label(option), color = textColor, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    }
                    NativeSelectionStyle.Checkmark -> {
                        Text(label(option), color = textColor, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                        if (isSelected) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }
}
