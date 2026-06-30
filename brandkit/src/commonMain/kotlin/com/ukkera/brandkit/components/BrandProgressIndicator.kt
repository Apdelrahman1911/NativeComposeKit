package com.ukkera.brandkit.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.ukkera.brandkit.components.model.ResolvedProgressStyle

/** Progress shape. [Circular] for an in-place spinner/ring; [Linear] for a horizontal bar. */
public enum class BrandProgressKind { Circular, Linear }

/**
 * A progress indicator. Renders the most native control per platform: on **iOS** a real
 * `UIActivityIndicatorView` (indeterminate circular) or `UIProgressView` (determinate linear); on
 * **Android** a Material `CircularProgressIndicator`/`LinearProgressIndicator`. Pass [progress] in `0..1`
 * for determinate; `null` for indeterminate (a spinning circular / looping linear).
 *
 * iOS has no native control for the other two combinations (determinate **circular** and indeterminate
 * **linear**), so those fall back to the Compose-drawn Material indicator on iOS — see the docs. Size the
 * indicator with [modifier] (a linear bar should usually be `Modifier.fillMaxWidth()`).
 *
 * `BrandProgressIndicator()` (spinner) · `BrandProgressIndicator(kind = Linear, progress = downloaded)`
 */
@Composable
public fun BrandProgressIndicator(
    modifier: Modifier = Modifier,
    kind: BrandProgressKind = BrandProgressKind.Circular,
    progress: Float? = null,
    color: Color? = null,
    trackColor: Color? = null,
    contentDescription: String? = null,
    testTag: String? = null,
) {
    val scheme = MaterialTheme.colorScheme
    val style = ResolvedProgressStyle(
        indicator = color ?: scheme.primary,
        track = trackColor ?: scheme.surfaceVariant,
    )
    PlatformBrandProgressIndicator(
        modifier = modifier,
        kind = kind,
        progress = progress?.coerceIn(0f, 1f),
        style = style,
        contentDescription = contentDescription,
        testTag = testTag,
    )
}

/** Native progress renderer. Android → Material; iOS → `UIActivityIndicatorView`/`UIProgressView` (+ fallback). */
@Composable
internal expect fun PlatformBrandProgressIndicator(
    modifier: Modifier,
    kind: BrandProgressKind,
    progress: Float?,
    style: ResolvedProgressStyle,
    contentDescription: String?,
    testTag: String?,
)
