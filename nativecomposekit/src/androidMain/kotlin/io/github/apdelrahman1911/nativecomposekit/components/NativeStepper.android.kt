package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.apdelrahman1911.nativecomposekit.components.model.ResolvedStepperStyle
import io.github.apdelrahman1911.nativecomposekit.theme.LocalNativeStrings

@Composable
internal actual fun PlatformNativeStepper(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier,
    min: Int,
    max: Int,
    step: Int,
    enabled: Boolean,
    style: ResolvedStepperStyle,
    contentDescription: String?,
    testTag: String?,
) {
    val m = if (testTag != null) modifier.testTag(testTag) else modifier
    val strings = LocalNativeStrings.current
    val name = contentDescription // capture to avoid shadowing the SemanticsPropertyReceiver property
    Row(modifier = m, verticalAlignment = Alignment.CenterVertically) {
        FilledTonalIconButton(
            onClick = { onValueChange((value - step).coerceAtLeast(min)) },
            enabled = enabled && value > min,
        ) {
            Icon(Icons.Default.Remove, contentDescription = strings.stepperDecrement)
        }
        Text(
            text = value.toString(),
            // The -/+ buttons stay individually operable (they are their own merge boundaries); the
            // value Text carries the control name + current value and announces changes (Polite).
            modifier = Modifier.widthIn(min = 40.dp).padding(horizontal = 8.dp).semantics {
                if (name != null) {
                    this.contentDescription = name
                    this.stateDescription = value.toString()
                }
                this.liveRegion = LiveRegionMode.Polite
            },
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
        )
        FilledTonalIconButton(
            onClick = { onValueChange((value + step).coerceAtMost(max)) },
            enabled = enabled && value < max,
        ) {
            Icon(Icons.Default.Add, contentDescription = strings.stepperIncrement)
        }
    }
}
