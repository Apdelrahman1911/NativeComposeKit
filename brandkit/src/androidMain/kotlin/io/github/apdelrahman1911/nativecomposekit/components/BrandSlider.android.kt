package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import io.github.apdelrahman1911.nativecomposekit.components.model.ResolvedSliderStyle

@Composable
internal actual fun PlatformBrandSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier,
    min: Float,
    max: Float,
    enabled: Boolean,
    style: ResolvedSliderStyle,
    contentDescription: String?,
    testTag: String?,
) {
    var m = modifier
    testTag?.let { m = m.testTag(it) }
    // Names the control without clobbering Material's built-in value/range/setProgress semantics
    // (co-located on the Slider node, mirroring BrandProgressIndicator).
    contentDescription?.let { cd -> m = m.semantics { this.contentDescription = cd } }
    Slider(
        value = value,
        onValueChange = onValueChange,
        modifier = m,
        enabled = enabled,
        valueRange = min..max,
        colors = SliderDefaults.colors(
            thumbColor = style.thumbColor,
            activeTrackColor = style.activeTrackColor,
            inactiveTrackColor = style.inactiveTrackColor,
        ),
    )
}
