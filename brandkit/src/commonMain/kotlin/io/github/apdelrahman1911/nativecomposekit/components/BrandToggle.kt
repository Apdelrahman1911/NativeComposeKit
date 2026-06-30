package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.apdelrahman1911.nativecomposekit.components.internal.resolveSurfaceFill
import io.github.apdelrahman1911.nativecomposekit.components.model.ResolvedToggleStyle
import io.github.apdelrahman1911.nativecomposekit.theme.LocalBrandSurface

/**
 * Brand on/off toggle. Colors come from AppTheme; renders the most native control per platform —
 * a Material 3 `Switch` on Android, a real `UISwitch` on iOS. Bound to [checked]; not locked to one
 * use case (enabled state, a11y label, testTag are all overridable).
 *
 * Pass `onCheckedChange = null` for a **read-only** display toggle (full color, no interaction) — matches
 * [BrandCheckbox]'s nullable convention (the kit's "nullable callback = optionally interactive" idiom).
 */
@Composable
public fun BrandToggle(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentDescription: String? = null,
    testTag: String? = null,
) {
    val scheme = MaterialTheme.colorScheme
    val style = ResolvedToggleStyle(
        trackOnColor = scheme.primary,
        thumbColor = scheme.onPrimary,
        // Drive the iOS control's light/dark from the surface it actually sits on (card/page/glass), not the page.
        surface = resolveSurfaceFill(LocalBrandSurface.current, scheme.background),
    )
    PlatformBrandToggle(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        enabled = enabled,
        style = style,
        contentDescription = contentDescription,
        testTag = testTag,
    )
}

/** Native toggle renderer. Android → Material 3 `Switch`; iOS → `UISwitch` via `UIKitView`. */
@Composable
internal expect fun PlatformBrandToggle(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier,
    enabled: Boolean,
    style: ResolvedToggleStyle,
    contentDescription: String?,
    testTag: String?,
)
