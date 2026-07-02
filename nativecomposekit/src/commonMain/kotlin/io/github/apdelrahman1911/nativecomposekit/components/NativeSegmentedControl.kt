package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.apdelrahman1911.nativecomposekit.components.internal.resolveSurfaceFill
import io.github.apdelrahman1911.nativecomposekit.components.model.ResolvedSegmentedStyle
import io.github.apdelrahman1911.nativecomposekit.theme.LocalNativeSurface

/**
 * Native segmented control — single selection across [options]. Colors/type come from NativeKitTheme;
 * renders the most native control per platform — a `UISegmentedControl` on iOS (a pure iOS idiom),
 * a `SingleChoiceSegmentedButtonRow` on Android.
 */
@Composable
public fun NativeSegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentDescription: String? = null,
    testTag: String? = null,
) {
    val scheme = MaterialTheme.colorScheme
    val style = ResolvedSegmentedStyle(
        selectedColor = scheme.primary,
        textColor = scheme.onSurface,
        selectedTextColor = scheme.onPrimary,
        // Drive the iOS control's light/dark from the surface it actually sits on (card/page/glass), not the page.
        surface = resolveSurfaceFill(LocalNativeSurface.current, scheme.background),
        textStyle = MaterialTheme.typography.labelLarge,
    )
    PlatformNativeSegmentedControl(
        options = options,
        selectedIndex = selectedIndex,
        onSelectedIndexChange = onSelectedIndexChange,
        modifier = modifier,
        enabled = enabled,
        style = style,
        contentDescription = contentDescription,
        testTag = testTag,
    )
}

/** Native segmented renderer. Android → `SingleChoiceSegmentedButtonRow`; iOS → `UISegmentedControl`. */
@Composable
internal expect fun PlatformNativeSegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    style: ResolvedSegmentedStyle,
    contentDescription: String?,
    testTag: String?,
)
