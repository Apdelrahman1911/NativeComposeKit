package io.github.apdelrahman1911.nativecomposekit.app

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.apdelrahman1911.nativecomposekit.components.NativeText

@Composable
actual fun InteropReproScreen() {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        NativeText(
            "These reproductions target iOS UIKitView interop (scroll clip/drift, UIMenu drift, " +
                "Dialog first-frame backdrop). Run the app on iOS to observe them.",
        )
    }
}
