package io.github.apdelrahman1911.nativecomposekit.components.feedback

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.apdelrahman1911.nativecomposekit.components.internal.resolveSurfaceFill
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeInsets
import io.github.apdelrahman1911.nativecomposekit.components.model.ResolvedFeedbackStyle
import io.github.apdelrahman1911.nativecomposekit.theme.LocalNativeStrings
import io.github.apdelrahman1911.nativecomposekit.theme.LocalNativeSurface
import io.github.apdelrahman1911.nativecomposekit.theme.NativeTheme

/**
 * An inline status message that lives **in the layout flow** (field validation, an empty-state note, a
 * "Saved locally" hint) — not a floating overlay, so it is NOT posted through [NativeFeedbackController].
 *
 * Unlike the other Native controls this is **Compose-drawn on both platforms** (no UIKit interop): it
 * must size to its content within a Compose column, exactly like `NativeTextField`'s label/helper text on
 * iOS. That keeps intrinsic sizing correct and still reads as native (system font, themed colors).
 *
 * @param filled `true` = soft tonal surface (status container color); `false` = outlined on the page surface.
 * @param icon a plain Compose [ImageVector] overriding the default status glyph (Compose-drawn — no SF-Symbol
 *   slot to drop); [showIcon] hides it entirely.
 * @param actionLabel/[onAction] an optional inline text action; [onDismiss] adds a trailing close button.
 */
@Composable
public fun NativeInlineStatus(
    text: String,
    status: NativeFeedbackStatus = NativeFeedbackStatus.Info,
    modifier: Modifier = Modifier,
    title: String? = null,
    icon: ImageVector? = null,
    showIcon: Boolean = true,
    filled: Boolean = true,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null,
    contentDescription: String? = null,
    testTag: String? = null,
) {
    val style = resolveFeedbackStyle(status, filled)
    val shape = RoundedCornerShape(style.cornerRadius)

    // Outlined sits *in* the layout, so its interior should match the surface it's embedded in (page or card)
    // — use the published LocalNativeSurface, falling back to the resolved surface. Filled keeps its tonal
    // container. (resolveFeedbackStyle is shared with the floating overlays, which keep scheme.surface.)
    val bgColor = if (filled) style.background else resolveSurfaceFill(LocalNativeSurface.current, style.background)
    var box = modifier.fillMaxWidth().clip(shape).background(bgColor)
    if (!filled) box = box.border(1.dp, style.border, shape)
    testTag?.let { box = box.testTag(it) }
    // Announce the status to screen readers when it appears/changes: errors interrupt (Assertive),
    // everything else is queued (Polite). Merge descendants so the live region carries the title/message
    // text (a bare live region with no text announces nothing); the action/dismiss stay separate nodes.
    box = box.semantics(mergeDescendants = true) {
        liveRegion = if (status == NativeFeedbackStatus.Error) LiveRegionMode.Assertive else LiveRegionMode.Polite
        if (contentDescription != null) this.contentDescription = contentDescription
    }

    Row(
        modifier = box.padding(
            start = style.insets.start,
            top = style.insets.top,
            end = style.insets.end,
            bottom = style.insets.bottom,
        ),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(NativeTheme.tokens.spacingSm),
    ) {
        if (showIcon) {
            Icon(
                imageVector = icon ?: status.defaultVector(),
                contentDescription = null,
                tint = style.iconTint,
                modifier = Modifier.size(20.dp),
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            if (title != null) {
                Text(title, style = style.titleTextStyle, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Text(text, style = style.textStyle, maxLines = 8, overflow = TextOverflow.Ellipsis)
            if (actionLabel != null && onAction != null) {
                Text(
                    text = actionLabel,
                    style = style.textStyle.copy(fontWeight = FontWeight.SemiBold, color = style.iconTint),
                    modifier = Modifier.padding(top = 4.dp)
                        .clickable(role = Role.Button, onClickLabel = actionLabel, onClick = onAction),
                )
            }
        }
        if (onDismiss != null) {
            val dismissLabel = LocalNativeStrings.current.dismiss
            // ≥48dp labeled target around the 20dp glyph (the glyph alone is below the a11y minimum).
            Box(
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .clickable(onClickLabel = dismissLabel, role = Role.Button, onClick = onDismiss),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = dismissLabel,
                    tint = style.content,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

/**
 * Resolves a [ResolvedFeedbackStyle] from [status] against [NativeTheme.statusColors] + the M3 scheme.
 * Shared by [NativeInlineStatus] and the Android overlay surfaces (banner/toast/snackbar). `Error` reuses
 * the Material `error`/`errorContainer` roles (M3 already models them).
 *
 * @param filled `true` = tonal container fill; `false` = page surface with a status outline.
 */
@Composable
internal fun resolveFeedbackStyle(
    status: NativeFeedbackStatus,
    filled: Boolean = true,
): ResolvedFeedbackStyle {
    val tokens = NativeTheme.tokens
    val scheme = MaterialTheme.colorScheme
    val sc = NativeTheme.statusColors

    // bold = high-emphasis (icon/border); container = soft tonal fill (+ its on-color for text).
    val bold: Color
    val container: Color
    val onContainer: Color
    when (status) {
        NativeFeedbackStatus.Success -> { bold = sc.success; container = sc.successContainer; onContainer = sc.onSuccessContainer }
        NativeFeedbackStatus.Warning -> { bold = sc.warning; container = sc.warningContainer; onContainer = sc.onWarningContainer }
        NativeFeedbackStatus.Info -> { bold = sc.info; container = sc.infoContainer; onContainer = sc.onInfoContainer }
        NativeFeedbackStatus.Error -> { bold = scheme.error; container = scheme.errorContainer; onContainer = scheme.onErrorContainer }
    }

    val background = if (filled) container else scheme.surface
    val content = if (filled) onContainer else scheme.onSurface
    val vPad = tokens.spacingSm + tokens.spacingXs // 12dp comfortable vertical padding

    return ResolvedFeedbackStyle(
        background = background,
        content = content,
        border = bold,
        iconTint = bold,
        cornerRadius = tokens.cornerMedium,
        insets = NativeInsets(start = tokens.spacingMd, top = vPad, end = tokens.spacingMd, bottom = vPad),
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = content),
        titleTextStyle = MaterialTheme.typography.titleSmall.copy(color = content, fontWeight = FontWeight.SemiBold),
    )
}

/** Default leading glyph per status (Material vector — used by inline status and the Android overlays). */
internal fun NativeFeedbackStatus.defaultVector(): ImageVector = when (this) {
    NativeFeedbackStatus.Info -> Icons.Outlined.Info
    NativeFeedbackStatus.Success -> Icons.Outlined.CheckCircle
    NativeFeedbackStatus.Warning -> Icons.Outlined.Warning
    NativeFeedbackStatus.Error -> Icons.Outlined.Error
}
