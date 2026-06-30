package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.apdelrahman1911.nativecomposekit.theme.NativeTheme

/**
 * A centered empty / no-results state — empty library, no search matches, nothing downloaded.
 * **Compose-drawn on both platforms.** Shows an optional [icon] (a plain Compose [ImageVector] — this
 * Compose-drawn component takes one directly, with no SF-Symbol slot to drop; the glyph is decorative, the
 * [title] carries the accessible meaning), a [title], an optional [message], and an optional call-to-action:
 * a [NativeButton] appears only when BOTH [actionLabel] and [onAction] are given. Center it in the available
 * space with a `Box(Modifier.fillMaxSize(), contentAlignment = Center)`.
 *
 * `NativeEmptyState("No results", message = "Try a different search.", icon = Icons.Default.Search)`
 */
@Composable
public fun NativeEmptyState(
    title: String,
    modifier: Modifier = Modifier,
    message: String? = null,
    icon: ImageVector? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    contentDescription: String? = null,
    testTag: String? = null,
) {
    val scheme = MaterialTheme.colorScheme
    val type = MaterialTheme.typography

    var m = modifier.fillMaxWidth().padding(NativeTheme.tokens.spacingLg)
    testTag?.let { m = m.testTag(it) }
    contentDescription?.let { cd -> m = m.semantics { this.contentDescription = cd } }

    Column(
        modifier = m,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(NativeTheme.tokens.spacingSm),
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = scheme.onSurfaceVariant, modifier = Modifier.size(48.dp))
        }
        Text(title, style = type.titleMedium, color = scheme.onSurface, textAlign = TextAlign.Center)
        if (message != null) {
            Text(message, style = type.bodyMedium, color = scheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
        if (actionLabel != null && onAction != null) {
            NativeButton(
                text = actionLabel,
                onClick = onAction,
                modifier = Modifier.padding(top = NativeTheme.tokens.spacingSm),
            )
        }
    }
}
