package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.apdelrahman1911.nativecomposekit.components.internal.resolveSurfaceFill
import io.github.apdelrahman1911.nativecomposekit.components.internal.sliderInactiveTrackColor
import io.github.apdelrahman1911.nativecomposekit.components.model.ResolvedSliderStyle
import io.github.apdelrahman1911.nativecomposekit.theme.LocalNativeSurface
import kotlin.math.round

/**
 * Native slider — a continuous value in [valueRange]. Colors come from NativeKitTheme; renders the most
 * native control per platform — a Material 3 `Slider` on Android, a real `UISlider` on iOS.
 *
 * [steps] > 0 makes the slider discrete: emitted values snap to the `steps + 2` evenly spaced stop
 * positions (Material's convention — [steps] values strictly between the endpoints, plus both endpoints)
 * on both platforms. [onValueChangeFinished] fires once when the drag gesture ends — commit expensive
 * work there instead of reacting to every drag frame.
 */
@Composable
public fun NativeSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    onValueChangeFinished: (() -> Unit)? = null,
    enabled: Boolean = true,
    contentDescription: String? = null,
    testTag: String? = null,
) {
    validateSliderRange(valueRange.start, valueRange.endInclusive)
    validateSliderSteps(steps)
    val scheme = MaterialTheme.colorScheme
    // Drive the iOS control's light/dark from the surface it actually sits on (card/page/glass), not the page.
    val surface = resolveSurfaceFill(LocalNativeSurface.current, scheme.background)
    val style = ResolvedSliderStyle(
        activeTrackColor = scheme.primary,
        // The remaining track must stay visible on ANY surface — a fixed surfaceVariant vanishes on a
        // Filled card whose container is itself surfaceVariant; derive it from the published surface.
        inactiveTrackColor = sliderInactiveTrackColor(surface, scheme.onSurface),
        thumbColor = scheme.primary,
        surface = surface,
    )
    PlatformNativeSlider(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        min = valueRange.start,
        max = valueRange.endInclusive,
        steps = steps,
        onValueChangeFinished = onValueChangeFinished,
        enabled = enabled,
        style = style,
        contentDescription = contentDescription,
        testTag = testTag,
    )
}

/**
 * Fails fast on an inverted range with one clear message on both platforms — Android's `coerceIn` would
 * otherwise throw an obscure coercion error and the iOS `UISlider` would silently adjust, hiding the
 * same bug on one platform only.
 */
internal fun validateSliderRange(start: Float, endInclusive: Float) {
    require(start <= endInclusive) {
        "NativeSlider valueRange is inverted: start ($start) must be <= endInclusive ($endInclusive)."
    }
}

/** Fails fast on a negative step count with one clear message on both platforms. */
internal fun validateSliderSteps(steps: Int) {
    require(steps >= 0) { "NativeSlider steps must be >= 0 (was $steps)." }
}

/**
 * Snaps [value] to the nearest of the `steps + 2` stop positions of a discrete slider. Android gets this
 * from the Material `Slider` itself; the iOS `UISlider` is continuous, so its value-changed handler snaps
 * the emitted value with this. Pure so the snap arithmetic is unit-testable.
 */
internal fun sliderSnappedValue(value: Float, min: Float, max: Float, steps: Int): Float {
    if (steps <= 0 || max <= min) return value
    val intervals = steps + 1
    val fraction = ((value - min) / (max - min)).coerceIn(0f, 1f)
    return min + round(fraction * intervals) / intervals * (max - min)
}

/** Native slider renderer. Android → Material 3 `Slider`; iOS → `UISlider` via `UIKitView`. */
@Composable
internal expect fun PlatformNativeSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier,
    min: Float,
    max: Float,
    steps: Int,
    onValueChangeFinished: (() -> Unit)?,
    enabled: Boolean,
    style: ResolvedSliderStyle,
    contentDescription: String?,
    testTag: String?,
)
