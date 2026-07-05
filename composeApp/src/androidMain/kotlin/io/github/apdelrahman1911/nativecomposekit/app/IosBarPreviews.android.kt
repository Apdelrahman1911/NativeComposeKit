package io.github.apdelrahman1911.nativecomposekit.app

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// The native bar previews are genuinely iOS-only (they embed real UIKit bars); Android shows an honest
// placeholder so the catalog reads the same on both platforms.

@Composable
private fun IosOnlyPlaceholder(label: String, modifier: Modifier, heightDp: Int) {
    Box(
        modifier = modifier.fillMaxWidth().height(heightDp.dp)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
actual fun IosNavBarPreview(
    title: String,
    modifier: Modifier,
    background: IosPreviewBackground,
    customBackground: Color?,
    tint: Color?,
    actionSymbols: List<String>,
    showsBack: Boolean,
    hairline: Boolean,
) {
    IosOnlyPlaceholder("iOS-only preview (real UINavigationBar) — run the iOS app", modifier, heightDp = 44)
}

@Composable
actual fun IosTabBarPreview(
    modifier: Modifier,
    selectedColor: Color?,
    unselectedColor: Color?,
    tint: Color?,
) {
    IosOnlyPlaceholder("iOS-only preview (real UITabBar) — run the iOS app", modifier, heightDp = 49)
}
