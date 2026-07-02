package io.github.apdelrahman1911.nativecomposekit.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Native seed palette — defined ONCE here (the NativeKitTheme). Components never hardcode these;
// they read MaterialTheme.colorScheme + NativeTheme.tokens.
private val NativeTeal = Color(0xFF0D7C66)
private val NativeTealLight = Color(0xFF14A88C)
private val NativeTealDark = Color(0xFF095C4C)

internal val LightColors = lightColorScheme(
    primary = NativeTeal,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB8F2E4),
    onPrimaryContainer = Color(0xFF00201A),
    secondary = NativeTealLight,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCDEFE6),
    onSecondaryContainer = Color(0xFF052119),
    error = Color(0xFFB3261E),
    onError = Color.White,
)

internal val DarkColors = darkColorScheme(
    primary = NativeTealLight,
    onPrimary = Color(0xFF00382C),
    primaryContainer = NativeTealDark,
    onPrimaryContainer = Color(0xFFB8F2E4),
    secondary = Color(0xFF8FD8C8),
    onSecondary = Color(0xFF052119),
    secondaryContainer = Color(0xFF0C4A3C),
    onSecondaryContainer = Color(0xFFCDEFE6),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
)

/**
 * The **default** brand window/background color per mode — what a native host (the iOS UIKit shell) paints
 * before Compose has composed, and the fallback when no custom palette is injected. Once
 * `NativeAppearanceScope` runs, the shell color source (`nativeBackgroundUIColor` on iOS) reflects the
 * scope's actual (possibly injected) schemes instead. Exposed so there is no duplicated hex in Swift.
 */
public val nativeLightBackground: Color = LightColors.background
public val nativeDarkBackground: Color = DarkColors.background

internal val AppTypography = Typography()

internal val AppShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
)

/**
 * The single styling source for the kit. There is no token JSON — every default (colors, type,
 * shapes, plus the brand-specific [NativeTokens]) lives here, and the Native* components read from it
 * via [MaterialTheme] + [NativeTheme.tokens].
 *
 * **Reskinnable:** every styling input is an optional parameter defaulting to the brand defaults, so a host
 * (or a future consumer of an extracted `:nativecomposekit` module) can reskin the whole kit by passing its own
 * [lightColors]/[darkColors]/[typography]/[shapes]/[tokens]/status colors without forking the kit. The simple
 * call — `NativeKitTheme { … }` — is unchanged. An injected palette also reaches the **native iOS shell**:
 * `NativeAppearanceScope` registers both modes' `background` colors, so the window paint and the shell's
 * dynamic root color (`nativeBackgroundUIColor`) follow the injected scheme, not just the default brand.
 */
@Composable
public fun NativeKitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    lightColors: ColorScheme = LightColors,
    darkColors: ColorScheme = DarkColors,
    typography: Typography = AppTypography,
    shapes: Shapes = AppShapes,
    tokens: NativeTokens = DefaultNativeTokens,
    lightStatusColors: NativeStatusColors = LightStatusColors,
    darkStatusColors: NativeStatusColors = DarkStatusColors,
    strings: NativeStrings = DefaultNativeStrings,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalNativeTokens provides tokens,
        LocalNativeStatusColors provides if (darkTheme) darkStatusColors else lightStatusColors,
        LocalNativeStrings provides strings,
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) darkColors else lightColors,
            typography = typography,
            shapes = shapes,
            content = content,
        )
    }
}
