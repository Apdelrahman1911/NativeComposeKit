package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Avatar diameter. Small = 32dp · Medium = 40dp · Large = 56dp. */
public enum class NativeAvatarSize { Small, Medium, Large }

/** Avatar outline. [Circle] for people; [Rounded] (squircle-ish) for series/content thumbnails. */
public enum class NativeAvatarShape { Circle, Rounded }

/**
 * A user/author/series avatar. **Compose-drawn on both platforms.** Renders the first available of:
 * [image] (a [Painter] the caller supplies — this kit ships no network image loader, so pass your own
 * loaded painter), [initials] (first two code points, uppercased — surrogate-pair safe), or [icon];
 * falling back to an empty tinted shape. [icon] is a plain Compose [ImageVector] (this Compose-drawn control
 * takes one directly — no SF-Symbol slot to drop). Colors default to `primaryContainer`/`onPrimaryContainer`.
 *
 * `NativeAvatar(initials = "JD")` · `NativeAvatar(image = painter, shape = Rounded, size = Large)`
 */
@Composable
public fun NativeAvatar(
    modifier: Modifier = Modifier,
    image: Painter? = null,
    initials: String? = null,
    icon: ImageVector? = null,
    size: NativeAvatarSize = NativeAvatarSize.Medium,
    shape: NativeAvatarShape = NativeAvatarShape.Circle,
    containerColor: Color? = null,
    contentColor: Color? = null,
    contentDescription: String? = null,
    testTag: String? = null,
) {
    val scheme = MaterialTheme.colorScheme
    val type = MaterialTheme.typography
    val dimen: Dp = when (size) {
        NativeAvatarSize.Small -> 32.dp
        NativeAvatarSize.Medium -> 40.dp
        NativeAvatarSize.Large -> 56.dp
    }
    val clipShape = when (shape) {
        NativeAvatarShape.Circle -> CircleShape
        NativeAvatarShape.Rounded -> RoundedCornerShape(dimen / 4)
    }
    val container = containerColor ?: scheme.primaryContainer
    val onContainer = contentColor ?: scheme.onPrimaryContainer
    val iconVec = icon
    val initialsText = initials?.trim()?.takeIf { it.isNotEmpty() }?.firstTwoCodePointsUpper()

    var m = modifier.size(dimen).clip(clipShape)
    if (image == null) m = m.background(container)
    testTag?.let { m = m.testTag(it) }
    // Merge so the avatar announces the caller's description as one node (not the raw initials "JD").
    contentDescription?.let { cd -> m = m.semantics(mergeDescendants = true) { this.contentDescription = cd } }

    Box(m, contentAlignment = Alignment.Center) {
        when {
            image != null -> Image(
                painter = image,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            !initialsText.isNullOrEmpty() -> Text(
                text = initialsText,
                color = onContainer,
                fontWeight = FontWeight.SemiBold,
                style = when (size) {
                    NativeAvatarSize.Small -> type.labelMedium
                    NativeAvatarSize.Medium -> type.titleSmall
                    NativeAvatarSize.Large -> type.titleMedium
                },
            )
            iconVec != null -> Icon(
                imageVector = iconVec,
                contentDescription = null,
                tint = onContainer,
                modifier = Modifier.size(dimen * 0.55f),
            )
        }
    }
}

/**
 * First two code points, uppercased — counts by code point (not UTF-16 unit) so an astral-plane character
 * (emoji / surrogate pair) yields one whole glyph instead of half a broken one.
 */
private fun String.firstTwoCodePointsUpper(): String {
    val sb = StringBuilder()
    var i = 0
    var taken = 0
    while (i < length && taken < 2) {
        val c = this[i]
        if (c.isHighSurrogate() && i + 1 < length && this[i + 1].isLowSurrogate()) {
            sb.append(c).append(this[i + 1])
            i += 2
        } else {
            sb.append(c)
            i += 1
        }
        taken++
    }
    return sb.toString().uppercase()
}
