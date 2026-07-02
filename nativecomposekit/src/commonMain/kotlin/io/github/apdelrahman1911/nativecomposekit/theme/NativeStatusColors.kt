package io.github.apdelrahman1911.nativecomposekit.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Semantic status colors for feedback surfaces (success / warning / info). These sit ALONGSIDE
 * MaterialTheme the same way [NativeTokens] does — M3's `ColorScheme` only models `error`, so the
 * **Error** status reuses `MaterialTheme.colorScheme.error` / `onError` and is intentionally absent
 * here. Each status has a bold pair (`x`/`onX`, for high-emphasis fills like a filled banner) and a
 * tonal pair (`xContainer`/`onXContainer`, for softer surfaces). Read via [NativeTheme.statusColors];
 * swap by providing a different instance to [LocalNativeStatusColors].
 *
 * Build instances with [lightNativeStatusColors]/[darkNativeStatusColors] to override just the
 * statuses you rebrand and inherit the rest. Deliberately **not** a `data class` so fields (new
 * statuses) can be added later without breaking binary compatibility; equality is by value and
 * [copy] derives variants. When a field is added, the previous constructor and `copy` signatures
 * are kept as hidden deprecated overloads.
 */
@Immutable
public class NativeStatusColors(
    public val success: Color,
    public val onSuccess: Color,
    public val successContainer: Color,
    public val onSuccessContainer: Color,
    public val warning: Color,
    public val onWarning: Color,
    public val warningContainer: Color,
    public val onWarningContainer: Color,
    public val info: Color,
    public val onInfo: Color,
    public val infoContainer: Color,
    public val onInfoContainer: Color,
) {
    /** Returns a copy with the given colors replaced. */
    public fun copy(
        success: Color = this.success,
        onSuccess: Color = this.onSuccess,
        successContainer: Color = this.successContainer,
        onSuccessContainer: Color = this.onSuccessContainer,
        warning: Color = this.warning,
        onWarning: Color = this.onWarning,
        warningContainer: Color = this.warningContainer,
        onWarningContainer: Color = this.onWarningContainer,
        info: Color = this.info,
        onInfo: Color = this.onInfo,
        infoContainer: Color = this.infoContainer,
        onInfoContainer: Color = this.onInfoContainer,
    ): NativeStatusColors = NativeStatusColors(
        success, onSuccess, successContainer, onSuccessContainer,
        warning, onWarning, warningContainer, onWarningContainer,
        info, onInfo, infoContainer, onInfoContainer,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NativeStatusColors) return false
        return success == other.success &&
            onSuccess == other.onSuccess &&
            successContainer == other.successContainer &&
            onSuccessContainer == other.onSuccessContainer &&
            warning == other.warning &&
            onWarning == other.onWarning &&
            warningContainer == other.warningContainer &&
            onWarningContainer == other.onWarningContainer &&
            info == other.info &&
            onInfo == other.onInfo &&
            infoContainer == other.infoContainer &&
            onInfoContainer == other.onInfoContainer
    }

    override fun hashCode(): Int {
        var result = success.hashCode()
        result = 31 * result + onSuccess.hashCode()
        result = 31 * result + successContainer.hashCode()
        result = 31 * result + onSuccessContainer.hashCode()
        result = 31 * result + warning.hashCode()
        result = 31 * result + onWarning.hashCode()
        result = 31 * result + warningContainer.hashCode()
        result = 31 * result + onWarningContainer.hashCode()
        result = 31 * result + info.hashCode()
        result = 31 * result + onInfo.hashCode()
        result = 31 * result + infoContainer.hashCode()
        result = 31 * result + onInfoContainer.hashCode()
        return result
    }
}

/**
 * The kit's light status palette with per-color overrides — the M3 `lightColorScheme(...)` pattern.
 * Pass only what you rebrand: `lightNativeStatusColors(success = MyGreen)`.
 */
public fun lightNativeStatusColors(
    success: Color = Color(0xFF2E7D32),
    onSuccess: Color = Color(0xFFFFFFFF),
    successContainer: Color = Color(0xFFB8F0BE),
    onSuccessContainer: Color = Color(0xFF0A3911),
    warning: Color = Color(0xFF9A6700),
    onWarning: Color = Color(0xFFFFFFFF),
    warningContainer: Color = Color(0xFFFFE7A3),
    onWarningContainer: Color = Color(0xFF3A2A00),
    info: Color = Color(0xFF1565C0),
    onInfo: Color = Color(0xFFFFFFFF),
    infoContainer: Color = Color(0xFFD2E4FF),
    onInfoContainer: Color = Color(0xFF001B3D),
): NativeStatusColors = NativeStatusColors(
    success, onSuccess, successContainer, onSuccessContainer,
    warning, onWarning, warningContainer, onWarningContainer,
    info, onInfo, infoContainer, onInfoContainer,
)

/**
 * The kit's dark status palette with per-color overrides (lighter foregrounds, deep containers —
 * the same logic as M3 dark schemes). Pass only what you rebrand.
 */
public fun darkNativeStatusColors(
    success: Color = Color(0xFF7BD88F),
    onSuccess: Color = Color(0xFF06390F),
    successContainer: Color = Color(0xFF1B5E20),
    onSuccessContainer: Color = Color(0xFFB8F0BE),
    warning: Color = Color(0xFFFFC24B),
    onWarning: Color = Color(0xFF3A2A00),
    warningContainer: Color = Color(0xFF6B4E00),
    onWarningContainer: Color = Color(0xFFFFE7A3),
    info: Color = Color(0xFF9FC9FF),
    onInfo: Color = Color(0xFF00315F),
    infoContainer: Color = Color(0xFF0E4A8A),
    onInfoContainer: Color = Color(0xFFD2E4FF),
): NativeStatusColors = NativeStatusColors(
    success, onSuccess, successContainer, onSuccessContainer,
    warning, onWarning, warningContainer, onWarningContainer,
    info, onInfo, infoContainer, onInfoContainer,
)

/** Light-theme status palette, tuned to read clearly against the teal brand surfaces. */
internal val LightStatusColors: NativeStatusColors = lightNativeStatusColors()

/** Dark-theme status palette (lighter foregrounds, deep containers — same logic as M3 dark schemes). */
internal val DarkStatusColors: NativeStatusColors = darkNativeStatusColors()

/** Provided by [AppTheme]; reading it outside an [AppTheme] is a programming error. */
public val LocalNativeStatusColors: ProvidableCompositionLocal<NativeStatusColors> = staticCompositionLocalOf<NativeStatusColors> {
    error("NativeStatusColors not provided — wrap your content in AppTheme { … }")
}
