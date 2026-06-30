package io.github.apdelrahman1911.nativecomposekit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import io.github.apdelrahman1911.nativecomposekit.app.configureCoilImageLoader

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureCoilImageLoader() // app-level image loader (Coil + Ktor/OkHttp); the kit stays dependency-free
        enableEdgeToEdge()
        setContent { App() }
    }
}
