package com.ukkera.brandkit.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ukkera.brandkit.components.internal.resolveSurfaceFill
import com.ukkera.brandkit.components.model.ResolvedSegmentedStyle
import com.ukkera.brandkit.theme.LocalBrandSurface

/**
 * Brand segmented control — single selection across [options]. Colors/type come from AppTheme;
 * renders the most native control per platform — a `UISegmentedControl` on iOS (a pure iOS idiom),
 * a `SingleChoiceSegmentedButtonRow` on Android.
 */
@Composable
public fun BrandSegmentedControl(
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
        surface = resolveSurfaceFill(LocalBrandSurface.current, scheme.background),
        textStyle = MaterialTheme.typography.labelLarge,
    )
    PlatformBrandSegmentedControl(
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
internal expect fun PlatformBrandSegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    style: ResolvedSegmentedStyle,
    contentDescription: String?,
    testTag: String?,
)
