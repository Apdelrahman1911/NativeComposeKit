package io.github.apdelrahman1911.nativecomposekit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import io.github.apdelrahman1911.nativecomposekit.app.configureCoilImageLoader
import io.github.apdelrahman1911.nativecomposekit.app.navigation.NativeNavLog

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Demo diagnostics (navigation tracing, logcat tag "NCK-Nav") — debug builds only, never in release.
        NativeNavLog.enabled = BuildConfig.DEBUG
        io.github.apdelrahman1911.nativecomposekit.app.AppDevTools.enabled = BuildConfig.DEBUG
        configureCoilImageLoader() // app-level image loader (Coil + Ktor/OkHttp); the kit stays dependency-free
        enableEdgeToEdge()
        setContent { App() }
    }
}
