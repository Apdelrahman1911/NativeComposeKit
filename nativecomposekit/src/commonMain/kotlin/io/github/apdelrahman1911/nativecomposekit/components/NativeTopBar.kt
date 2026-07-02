package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import io.github.apdelrahman1911.nativecomposekit.theme.LocalNativeStrings

/**
 * A themed top app bar, **decoupled from navigation** — drop it into any screen (or [NativeScaffold.topBar])
 * without going through a navigation host's chrome. **Compose-drawn on both platforms**
 * (the native iOS nav bar belongs to the native chrome shell; this is the in-content/Android chrome). Colors come
 * from NativeKitTheme (`surface` container, `onSurface` title).
 *
 * [navigationIcon] is a plain Compose [ImageVector] (Compose-drawn chrome — no SF-Symbol slot); pair it with
 * [onNavigationClick] and a [navigationContentDescription] — when omitted, the localized "Back" from
 * [io.github.apdelrahman1911.nativecomposekit.theme.NativeStrings] names the control (never unnamed).
 * [containerColor]/[contentColor] restyle the bar (transparent hero bars, primary-tinted bars);
 * [windowInsets] overrides the default status-bar insets (pass `WindowInsets(0)` inside a sheet).
 * [actions] is a trailing `RowScope` slot — put [NativeIconButton]s there. [centerTitle] follows the iOS
 * centered-title convention; leave it off for the Android leading-aligned look. (Collapse-on-scroll is
 * deferred: Material's `TopAppBarScrollBehavior` is still experimental, and the kit keeps experimental types
 * out of its public signatures.)
 *
 * `NativeTopBar("Library", actions = { NativeIconButton(addIcon, onAdd, contentDescription = "Add") })`
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun NativeTopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    navigationIcon: ImageVector? = null,
    onNavigationClick: (() -> Unit)? = null,
    navigationContentDescription: String? = null,
    centerTitle: Boolean = false,
    containerColor: Color? = null,
    contentColor: Color? = null,
    windowInsets: WindowInsets? = null,
    testTag: String? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val scheme = MaterialTheme.colorScheme
    val type = MaterialTheme.typography
    val resolvedContainer = containerColor ?: scheme.surface
    val resolvedContent = contentColor ?: scheme.onSurface
    val secondary = if (contentColor != null) contentColor.copy(alpha = 0.72f) else scheme.onSurfaceVariant
    val colors = TopAppBarDefaults.topAppBarColors(
        containerColor = resolvedContainer,
        scrolledContainerColor = resolvedContainer,
        titleContentColor = resolvedContent,
        navigationIconContentColor = resolvedContent,
        actionIconContentColor = secondary,
    )
    val insets = windowInsets ?: TopAppBarDefaults.windowInsets

    val titleSlot: @Composable () -> Unit = {
        Column {
            // The screen title is the page's primary heading — expose it for rotor/heading navigation
            // (the kit marks section headers the same way).
            Text(
                title,
                style = type.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.nativeHeading(),
            )
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = type.labelMedium,
                    color = secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }

    // An icon-only navigation control MUST have an accessible name: fall back to the localized "Back"
    // (the overwhelmingly common case) rather than shipping an unnamed button when the caller omits it.
    val navName = navigationContentDescription ?: LocalNativeStrings.current.back
    val navSlot: @Composable () -> Unit = {
        if (navigationIcon != null && onNavigationClick != null) {
            IconButton(onClick = onNavigationClick) {
                Icon(navigationIcon, contentDescription = navName)
            }
        }
    }

    var m = modifier
    testTag?.let { m = m.testTag(it) }

    if (centerTitle) {
        CenterAlignedTopAppBar(
            title = titleSlot,
            modifier = m,
            navigationIcon = navSlot,
            actions = actions,
            windowInsets = insets,
            colors = colors,
        )
    } else {
        TopAppBar(
            title = titleSlot,
            modifier = m,
            navigationIcon = navSlot,
            actions = actions,
            windowInsets = insets,
            colors = colors,
        )
    }
}
