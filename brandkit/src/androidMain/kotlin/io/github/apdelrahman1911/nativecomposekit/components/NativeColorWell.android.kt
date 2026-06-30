package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

// A basic preset palette (this is picker *data*, not theme styling — fixed colors are the point of a picker).
private val ColorWellPresets = listOf(
    Color(0xFFE53935), Color(0xFFD81B60), Color(0xFF8E24AA), Color(0xFF5E35B1),
    Color(0xFF3949AB), Color(0xFF1E88E5), Color(0xFF00ACC1), Color(0xFF00897B),
    Color(0xFF43A047), Color(0xFF7CB342), Color(0xFFFDD835), Color(0xFFFB8C00),
    Color(0xFF6D4C41), Color(0xFF757575), Color(0xFF000000), Color(0xFFFFFFFF),
)

/** Android color well: a swatch that opens a preset-color dialog (no system color picker on Android). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal actual fun PlatformNativeColorWell(
    color: Color,
    onColorChange: (Color) -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    supportsAlpha: Boolean,
    contentDescription: String?,
    testTag: String?,
) {
    val scheme = MaterialTheme.colorScheme
    var open by remember { mutableStateOf(false) }

    var m = modifier
    testTag?.let { m = m.testTag(it) }
    val cd = contentDescription ?: "Selected color"
    m = m.semantics { this.contentDescription = cd }

    var swatch = m.size(36.dp).clip(CircleShape).background(color).border(1.dp, scheme.outline, CircleShape)
    if (enabled) swatch = swatch.clickable { open = true }
    androidx.compose.foundation.layout.Box(swatch)

    if (open) {
        AlertDialog(
            onDismissRequest = { open = false },
            confirmButton = { TextButton(onClick = { open = false }) { Text("Done") } },
            title = { Text("Pick a color") },
            text = {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ColorWellPresets.forEach { c ->
                        val selected = c.toArgb() == color.toArgb()
                        androidx.compose.foundation.layout.Box(
                            Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(c)
                                .border(
                                    width = if (selected) 3.dp else 1.dp,
                                    color = if (selected) scheme.primary else scheme.outline,
                                    shape = CircleShape,
                                )
                                .clickable { onColorChange(c); open = false },
                        )
                    }
                }
            },
        )
    }
}
