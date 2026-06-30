package com.ukkera.brandkit.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Semantic status colors for feedback surfaces (success / warning / info). These sit ALONGSIDE
 * MaterialTheme the same way [BrandTokens] does — M3's `ColorScheme` only models `error`, so the
 * **Error** status reuses `MaterialTheme.colorScheme.error` / `onError` and is intentionally absent
 * here. Each status has a bold pair (`x`/`onX`, for high-emphasis fills like a filled banner) and a
 * tonal pair (`xContainer`/`onXContainer`, for softer surfaces). Read via [BrandTheme.statusColors];
 * swap by providing a different instance to [LocalBrandStatusColors].
 */
@Immutable
public data class BrandStatusColors(
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
    val info: Color,
    val onInfo: Color,
    val infoContainer: Color,
    val onInfoContainer: Color,
)

/** Light-theme status palette, tuned to read clearly against the teal brand surfaces. */
internal val LightStatusColors = BrandStatusColors(
    success = Color(0xFF2E7D32),
    onSuccess = Color(0xFFFFFFFF),
    successContainer = Color(0xFFB8F0BE),
    onSuccessContainer = Color(0xFF0A3911),
    warning = Color(0xFF9A6700),
    onWarning = Color(0xFFFFFFFF),
    warningContainer = Color(0xFFFFE7A3),
    onWarningContainer = Color(0xFF3A2A00),
    info = Color(0xFF1565C0),
    onInfo = Color(0xFFFFFFFF),
    infoContainer = Color(0xFFD2E4FF),
    onInfoContainer = Color(0xFF001B3D),
)

/** Dark-theme status palette (lighter foregrounds, deep containers — same logic as M3 dark schemes). */
internal val DarkStatusColors = BrandStatusColors(
    success = Color(0xFF7BD88F),
    onSuccess = Color(0xFF06390F),
    successContainer = Color(0xFF1B5E20),
    onSuccessContainer = Color(0xFFB8F0BE),
    warning = Color(0xFFFFC24B),
    onWarning = Color(0xFF3A2A00),
    warningContainer = Color(0xFF6B4E00),
    onWarningContainer = Color(0xFFFFE7A3),
    info = Color(0xFF9FC9FF),
    onInfo = Color(0xFF00315F),
    infoContainer = Color(0xFF0E4A8A),
    onInfoContainer = Color(0xFFD2E4FF),
)

/** Provided by [AppTheme]; reading it outside an [AppTheme] is a programming error. */
public val LocalBrandStatusColors: ProvidableCompositionLocal<BrandStatusColors> = staticCompositionLocalOf<BrandStatusColors> {
    error("BrandStatusColors not provided — wrap your content in AppTheme { … }")
}
