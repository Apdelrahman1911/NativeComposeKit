package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import io.github.apdelrahman1911.nativecomposekit.components.model.ResolvedProgressStyle

@Composable
internal actual fun PlatformNativeProgressIndicator(
    modifier: Modifier,
    kind: NativeProgressKind,
    progress: Float?,
    style: ResolvedProgressStyle,
    contentDescription: String?,
    testTag: String?,
) {
    var m = modifier
    testTag?.let { m = m.testTag(it) }
    contentDescription?.let { cd -> m = m.semantics { this.contentDescription = cd } }

    when (kind) {
        NativeProgressKind.Circular ->
            if (progress != null) {
                CircularProgressIndicator(progress = { progress }, modifier = m, color = style.indicator, trackColor = style.track)
            } else {
                CircularProgressIndicator(modifier = m, color = style.indicator, trackColor = style.track)
            }
        NativeProgressKind.Linear ->
            if (progress != null) {
                LinearProgressIndicator(progress = { progress }, modifier = m, color = style.indicator, trackColor = style.track)
            } else {
                LinearProgressIndicator(modifier = m, color = style.indicator, trackColor = style.track)
            }
    }
}
