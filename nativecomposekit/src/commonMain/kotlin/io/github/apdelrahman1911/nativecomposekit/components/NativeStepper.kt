package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.apdelrahman1911.nativecomposekit.components.internal.resolveSurfaceFill
import io.github.apdelrahman1911.nativecomposekit.components.model.ResolvedStepperStyle
import io.github.apdelrahman1911.nativecomposekit.theme.LocalNativeSurface

/**
 * Native stepper — an integer in [min]..[max] adjusted by [step]. Renders the most native control per
 * platform — a real `UIStepper` (the native -/+ control) on iOS, a Material -/+ row on Android.
 *
 * Requires `step > 0` and `min <= max`; both platforms fail fast with the same message (a bad config
 * would otherwise misbehave differently per platform).
 */
@Composable
public fun NativeStepper(
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
    validateStepperConfig(min = min, max = max, step = step)
    val scheme = MaterialTheme.colorScheme
    // Drive the iOS control's light/dark from the surface it actually sits on (card/page/glass), not the page.
    val style = ResolvedStepperStyle(
        tint = scheme.primary,
        surface = resolveSurfaceFill(LocalNativeSurface.current, scheme.background),
    )
    PlatformNativeStepper(
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

/**
 * Fails fast on a bad stepper config with one clear message on both platforms — the Android -/+ row's
 * `coerceIn` would otherwise throw an obscure coercion error on inverted bounds, and the iOS
 * `UIStepper` would silently adjust, hiding the same bug on one platform only.
 */
internal fun validateStepperConfig(min: Int, max: Int, step: Int) {
    require(step > 0) { "NativeStepper step must be > 0 (was $step)." }
    require(min <= max) { "NativeStepper bounds are inverted: min ($min) must be <= max ($max)." }
}

/**
 * The value after a -/+ tap: [current] moved by [delta] (±step), clamped into [min]..[max] — a partial
 * last step lands ON the bound rather than refusing to move. Pure so the arithmetic is unit-testable;
 * used by the Android -/+ row (the iOS `UIStepper` steps and clamps natively).
 */
internal fun stepperNextValue(current: Int, delta: Int, min: Int, max: Int): Int =
    (current + delta).coerceIn(min, max)

/** Native stepper renderer. Android → Material -/+ row; iOS → `UIStepper` via `UIKitView`. */
@Composable
internal expect fun PlatformNativeStepper(
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
