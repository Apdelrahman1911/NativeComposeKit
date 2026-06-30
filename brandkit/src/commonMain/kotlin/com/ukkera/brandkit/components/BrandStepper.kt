package com.ukkera.brandkit.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ukkera.brandkit.components.internal.resolveSurfaceFill
import com.ukkera.brandkit.components.model.ResolvedStepperStyle
import com.ukkera.brandkit.theme.LocalBrandSurface

/**
 * Brand stepper — an integer in [min]..[max] adjusted by [step]. Renders the most native control per
 * platform — a real `UIStepper` (the native -/+ control) on iOS, a Material -/+ row on Android.
 */
@Composable
public fun BrandStepper(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    min: Int = 0,
    max: Int = 100,
    step: Int = 1,
    enabled: Boolean = true,
    contentDescription: String? = null,
    testTag: String? = null,
) {
    val scheme = MaterialTheme.colorScheme
    // Drive the iOS control's light/dark from the surface it actually sits on (card/page/glass), not the page.
    val style = ResolvedStepperStyle(
        tint = scheme.primary,
        surface = resolveSurfaceFill(LocalBrandSurface.current, scheme.background),
    )
    PlatformBrandStepper(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        min = min,
        max = max,
        step = step,
        enabled = enabled,
        style = style,
        contentDescription = contentDescription,
        testTag = testTag,
    )
}

/** Native stepper renderer. Android → Material -/+ row; iOS → `UIStepper` via `UIKitView`. */
@Composable
internal expect fun PlatformBrandStepper(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier,
    min: Int,
    max: Int,
    step: Int,
    enabled: Boolean,
    style: ResolvedStepperStyle,
    contentDescription: String?,
    testTag: String?,
)
