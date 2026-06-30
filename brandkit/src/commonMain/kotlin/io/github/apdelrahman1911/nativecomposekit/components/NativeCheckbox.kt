package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.apdelrahman1911.nativecomposekit.theme.NativeTheme

/**
 * A checkbox, optionally with a [label]. **Kept cross-platform as a documented exception (kit thesis):** iOS has
 * no native checkbox control (its idiomatic equivalents are a switch for settings or a checkmark list row for
 * selection), so there is nothing more-native to delegate to — this is a branded Compose control (themed by
 * `MaterialTheme`) on both platforms. For an on/off setting prefer [NativeToggle] (a native `UISwitch` on iOS);
 * use this for multi-select (e.g. selecting chapters to download).
 *
 * With a [label] the whole row is the toggle target — a single merged `Role.Checkbox` node, ≥48dp tall —
 * and the [label] becomes its accessible name unless a [contentDescription] override is given. Pass
 * `onCheckedChange = null` for a read-only checkbox.
 */
@Composable
public fun NativeCheckbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    label: String? = null,
    enabled: Boolean = true,
    contentDescription: String? = null,
    testTag: String? = null,
) {
    val scheme = MaterialTheme.colorScheme
    var m = modifier
    testTag?.let { m = m.testTag(it) }

    if (label == null) {
        contentDescription?.let { cd -> m = m.semantics { this.contentDescription = cd } }
        Checkbox(checked = checked, onCheckedChange = onCheckedChange, modifier = m, enabled = enabled)
    } else {
        val toggle = if (onCheckedChange != null) {
            m.toggleable(value = checked, enabled = enabled, role = Role.Checkbox, onValueChange = onCheckedChange)
        } else {
            m
        }
        Row(
            modifier = toggle
                .semantics(mergeDescendants = true) { if (contentDescription != null) this.contentDescription = contentDescription }
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .padding(vertical = NativeTheme.tokens.spacingSm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(NativeTheme.tokens.spacingSm),
        ) {
            // The row owns the toggle; the checkbox is decorative (onCheckedChange = null) so there is one
            // merged tap target / semantics node, not two competing ones.
            Checkbox(checked = checked, onCheckedChange = null, enabled = enabled)
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) scheme.onSurface else scheme.onSurface.copy(alpha = 0.38f),
            )
        }
    }
}
