package com.ukkera.brandkit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ukkera.brandkit.app.configureCoilImageLoader

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureCoilImageLoader() // app-level image loader (Coil + Ktor/OkHttp); the kit stays dependency-free
        enableEdgeToEdge()
        setContent { App() }
    }
}
