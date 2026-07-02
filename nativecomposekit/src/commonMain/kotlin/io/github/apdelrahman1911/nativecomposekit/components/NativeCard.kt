package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.apdelrahman1911.nativecomposekit.theme.NativeTheme
import io.github.apdelrahman1911.nativecomposekit.theme.LocalNativeSurface

/**
 * Card surface style.
 * - [Filled]: a soft tonal surface (`surfaceVariant`) — the default content container.
 * - [Elevated]: the page surface raised with a shadow — use to lift a card off a busy background.
 * - [Outlined]: the page surface with a hairline outline — quietest separation, no shadow.
 */
public enum class NativeCardVariant { Filled, Elevated, Outlined }

/**
 * A themed container surface — the building block for manga covers, list/grid cells, and grouped
 * settings. **Compose-drawn on both platforms** (a card isn't a native leaf control on iOS; it's a styled
 * view either way — so this is a branded Compose component, not UIKit interop). Colors/shape/elevation
 * come from the theme; pass [onClick] to make the whole card a button (Material ripple, clipped to the
 * shape). Content inherits the resolved content color via `LocalContentColor`. [enabled] gates a tappable
 * card (dims content + drops elevation when false); it is a no-op for a card with no [onClick].
 *
 * Simple: `NativeCard { Text("Hi") }`. Tappable cover cell: `NativeCard(variant = Elevated, onClick = …) { … }`.
 */
@Composable
public fun NativeCard(
    modifier: Modifier = Modifier,
    variant: NativeCardVariant = NativeCardVariant.Filled,
    onClick: (() -> Unit)? = null,
    onClickLabel: String? = null,
    enabled: Boolean = true,
    cornerRadius: Dp? = null,
    contentPadding: PaddingValues? = null,
    containerColor: Color? = null,
    contentColor: Color? = null,
    contentDescription: String? = null,
    testTag: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(cornerRadius ?: NativeTheme.tokens.cornerMedium)

    val container: Color
    val fg: Color
    val elevation: Dp
    val outline: Color?
    when (variant) {
        NativeCardVariant.Filled -> {
            container = scheme.surfaceVariant; fg = scheme.onSurfaceVariant; elevation = 0.dp; outline = null
        }
        NativeCardVariant.Elevated -> {
            container = scheme.surface; fg = scheme.onSurface; elevation = NativeTheme.tokens.elevationRaised; outline = null
        }
        NativeCardVariant.Outlined -> {
            container = scheme.surface; fg = scheme.onSurface; elevation = 0.dp; outline = scheme.outlineVariant
        }
    }
    // A disabled tappable card dims its content and drops elevation (matches NativeButton's disabled tone).
    val isDisabled = onClick != null && !enabled
    val resolvedContainer = containerColor ?: container
    val resolvedContent = (contentColor ?: fg).let { if (isDisabled) it.copy(alpha = 0.38f) else it }
    val resolvedElevation = if (isDisabled) 0.dp else elevation
    val pad = contentPadding ?: PaddingValues(NativeTheme.tokens.spacingMd)

    // Shadow must be applied BEFORE the clip so it renders outside the rounded bounds.
    var box: Modifier = modifier
    if (resolvedElevation > 0.dp) box = box.shadow(resolvedElevation, shape)
    box = box.clip(shape).background(resolvedContainer)
    if (outline != null) box = box.border(1.dp, outline, shape)
    if (onClick != null) {
        box = box.clickable(enabled = enabled, onClickLabel = onClickLabel, role = Role.Button, onClick = onClick)
    }
    testTag?.let { box = box.testTag(it) }
    contentDescription?.let { cd -> box = box.semantics { this.contentDescription = cd } }

    Column(modifier = box.padding(pad)) {
        val columnScope = this
        // Publish the card's surface so a descendant iOS NativeText (a UILabel in a UIKitView) can fill its
        // label backing with the real color it sits on — otherwise the interop host backdrop shows as a box.
        CompositionLocalProvider(
            LocalContentColor provides resolvedContent,
            LocalNativeSurface provides resolvedContainer,
        ) {
            columnScope.content()
        }
    }
}
