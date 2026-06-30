package io.github.apdelrahman1911.nativecomposekit.components

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
internal actual fun rememberPlatformShare(): (NativeShareContent) -> Unit {
    val context = LocalContext.current
    return remember(context) {
        { content ->
            val body = listOfNotNull(content.text, content.url).joinToString("\n")
            val send = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, body)
            }
            val chooser = Intent.createChooser(send, null).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // safe when launched from a non-Activity context
            }
            context.startActivity(chooser)
        }
    }
}
