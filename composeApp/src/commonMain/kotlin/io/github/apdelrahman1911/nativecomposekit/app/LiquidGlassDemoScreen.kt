package io.github.apdelrahman1911.nativecomposekit.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.apdelrahman1911.nativecomposekit.components.NativeButton
import io.github.apdelrahman1911.nativecomposekit.components.NativeText
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeButtonIosBackground
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeButtonIosOptions
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeButtonVariant
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeTextStyle
import kotlinx.coroutines.delay

/**
 * Debug screen: **Liquid Glass buttons over live content**. Colorful stripes auto-scroll beneath two
 * floating [NativeButton]s using `ios = NativeButtonIosOptions(background = Glass / ProminentGlass)` —
 * on an iOS 26 device/simulator the capsules visibly refract the stripes moving under them and morph
 * on press (tap them; the toggle row is the pause switch). On Android and pre-26 iOS the same code
 * renders the buttons' regular variant styling — which is exactly the documented fallback contract.
 */
@Composable
fun LiquidGlassDemoScreen(autoRun: Boolean = true) {
    val stripes = listOf(
        Color(0xFFE57373), Color(0xFFFFB74D), Color(0xFFFFF176), Color(0xFFAED581),
        Color(0xFF4DB6AC), Color(0xFF4FC3F7), Color(0xFF7986CB), Color(0xFFBA68C8),
    )
    var scrolling by remember { mutableStateOf(autoRun) }
    val listState = rememberLazyListState()
    LaunchedEffect(scrolling) {
        while (scrolling) {
            listState.animateScrollToItem(24)
            delay(400)
            listState.animateScrollToItem(0)
            delay(400)
        }
    }
    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 12.dp),
        ) {
            items(count = 32) { i ->
                Box(
                    Modifier.fillMaxWidth().height(72.dp).padding(horizontal = 12.dp, vertical = 4.dp)
                        .background(stripes[i % stripes.size]),
                    contentAlignment = Alignment.Center,
                ) {
                    NativeText("Stripe ${i + 1}", style = NativeTextStyle.Label)
                }
            }
        }
        Column(
            // Clear the floating tab bar: the content inset local carries its height on iOS.
            modifier = Modifier.align(Alignment.BottomCenter)
                .padding(bottom = 24.dp + LocalNativeContentBottomInset.current),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            NativeButton(
                text = if (scrolling) "Glass — tap to pause" else "Glass — tap to scroll",
                onClick = { scrolling = !scrolling },
                variant = NativeButtonVariant.Secondary,
                ios = NativeButtonIosOptions(background = NativeButtonIosBackground.Glass),
            )
            NativeButton(
                text = "Prominent glass",
                onClick = { },
                variant = NativeButtonVariant.Primary,
                ios = NativeButtonIosOptions(background = NativeButtonIosBackground.ProminentGlass),
            )
        }
    }
}
