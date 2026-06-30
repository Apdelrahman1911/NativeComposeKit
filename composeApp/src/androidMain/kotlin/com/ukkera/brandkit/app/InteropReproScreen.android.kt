package com.ukkera.brandkit.app

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
actual fun InteropReproScreen() {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text(
            "These reproductions target iOS UIKitView interop (scroll clip/drift, UIMenu drift, " +
                "Dialog first-frame backdrop). Run the app on iOS to observe them.",
        )
    }
}
