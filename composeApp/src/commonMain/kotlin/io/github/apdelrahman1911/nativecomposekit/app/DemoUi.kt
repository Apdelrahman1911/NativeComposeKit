package io.github.apdelrahman1911.nativecomposekit.app

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import io.github.apdelrahman1911.nativecomposekit.components.NativeCard
import io.github.apdelrahman1911.nativecomposekit.components.NativeCardVariant
import io.github.apdelrahman1911.nativecomposekit.components.NativeText
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeTextStyle

/** Shared bits for the debug/demo screens (chrome demo, toolbar styles): monospace code cards + labels. */

@Composable
internal fun CodeCard(code: String) {
    NativeCard(variant = NativeCardVariant.Filled, modifier = Modifier.fillMaxWidth()) {
        Text(
            code,
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
        )
    }
}

@Composable
internal fun DemoSectionTitle(text: String) {
    NativeText(text, style = NativeTextStyle.Title)
}

@Composable
internal fun DemoNote(text: String) {
    NativeText(text, style = NativeTextStyle.Label)
}
