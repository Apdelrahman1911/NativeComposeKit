package io.github.apdelrahman1911.nativecomposekit.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Native design tokens that sit ALONGSIDE MaterialTheme — the things M3 doesn't model directly
 * (per-size control heights, corner radii, the spacing scale, kit elevations). Part of [NativeKitTheme];
 * read via [NativeTheme.tokens]. Swap by providing a different instance to [LocalNativeTokens].
 *
 * Deliberately **not** a `data class`: fields can then be added in later releases without breaking
 * binary compatibility (a data class bakes every field into generated `copy`/`componentN`
 * signatures). Equality is by value; use [copy] to derive a variant. When a field is added, the
 * previous constructor and `copy` signatures are kept as hidden deprecated overloads.
 */
@Immutable
public class NativeTokens(
    public val buttonHeightSmall: Dp = 36.dp,
    public val buttonHeightMedium: Dp = 44.dp,
    public val buttonHeightLarge: Dp = 52.dp,
    public val buttonPadHSmall: Dp = 12.dp,
    public val buttonPadHMedium: Dp = 16.dp,
    public val buttonPadHLarge: Dp = 22.dp,
    public val buttonPadVSmall: Dp = 6.dp,
    public val buttonPadVMedium: Dp = 10.dp,
    public val buttonPadVLarge: Dp = 14.dp,
    public val cornerSmall: Dp = 8.dp,
    public val cornerMedium: Dp = 12.dp,
    /** Large container radius (dialogs, hero cards) — matches the default M3 `Shapes.large`. */
    public val cornerLarge: Dp = 16.dp,
    public val fieldMinHeight: Dp = 52.dp,
    public val spacingXs: Dp = 4.dp,
    public val spacingSm: Dp = 8.dp,
    public val spacingMd: Dp = 16.dp,
    public val spacingLg: Dp = 24.dp,
    /** Shadow of raised in-page surfaces ([io.github.apdelrahman1911.nativecomposekit.components.NativeCard]'s Elevated variant). */
    public val elevationRaised: Dp = 2.dp,
    /** Shadow of floating overlays ([io.github.apdelrahman1911.nativecomposekit.components.NativeDialog]). */
    public val elevationOverlay: Dp = 6.dp,
) {
    /** Returns a copy with the given tokens replaced. */
    public fun copy(
        buttonHeightSmall: Dp = this.buttonHeightSmall,
        buttonHeightMedium: Dp = this.buttonHeightMedium,
        buttonHeightLarge: Dp = this.buttonHeightLarge,
        buttonPadHSmall: Dp = this.buttonPadHSmall,
        buttonPadHMedium: Dp = this.buttonPadHMedium,
        buttonPadHLarge: Dp = this.buttonPadHLarge,
        buttonPadVSmall: Dp = this.buttonPadVSmall,
        buttonPadVMedium: Dp = this.buttonPadVMedium,
        buttonPadVLarge: Dp = this.buttonPadVLarge,
        cornerSmall: Dp = this.cornerSmall,
        cornerMedium: Dp = this.cornerMedium,
        cornerLarge: Dp = this.cornerLarge,
        fieldMinHeight: Dp = this.fieldMinHeight,
        spacingXs: Dp = this.spacingXs,
        spacingSm: Dp = this.spacingSm,
        spacingMd: Dp = this.spacingMd,
        spacingLg: Dp = this.spacingLg,
        elevationRaised: Dp = this.elevationRaised,
        elevationOverlay: Dp = this.elevationOverlay,
    ): NativeTokens = NativeTokens(
        buttonHeightSmall, buttonHeightMedium, buttonHeightLarge,
        buttonPadHSmall, buttonPadHMedium, buttonPadHLarge,
        buttonPadVSmall, buttonPadVMedium, buttonPadVLarge,
        cornerSmall, cornerMedium, cornerLarge, fieldMinHeight,
        spacingXs, spacingSm, spacingMd, spacingLg,
        elevationRaised, elevationOverlay,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NativeTokens) return false
        return buttonHeightSmall == other.buttonHeightSmall &&
            buttonHeightMedium == other.buttonHeightMedium &&
            buttonHeightLarge == other.buttonHeightLarge &&
            buttonPadHSmall == other.buttonPadHSmall &&
            buttonPadHMedium == other.buttonPadHMedium &&
            buttonPadHLarge == other.buttonPadHLarge &&
            buttonPadVSmall == other.buttonPadVSmall &&
            buttonPadVMedium == other.buttonPadVMedium &&
            buttonPadVLarge == other.buttonPadVLarge &&
            cornerSmall == other.cornerSmall &&
            cornerMedium == other.cornerMedium &&
            cornerLarge == other.cornerLarge &&
            fieldMinHeight == other.fieldMinHeight &&
            spacingXs == other.spacingXs &&
            spacingSm == other.spacingSm &&
            spacingMd == other.spacingMd &&
            spacingLg == other.spacingLg &&
            elevationRaised == other.elevationRaised &&
            elevationOverlay == other.elevationOverlay
    }

    override fun hashCode(): Int {
        var result = buttonHeightSmall.hashCode()
        result = 31 * result + buttonHeightMedium.hashCode()
        result = 31 * result + buttonHeightLarge.hashCode()
        result = 31 * result + buttonPadHSmall.hashCode()
        result = 31 * result + buttonPadHMedium.hashCode()
        result = 31 * result + buttonPadHLarge.hashCode()
        result = 31 * result + buttonPadVSmall.hashCode()
        result = 31 * result + buttonPadVMedium.hashCode()
        result = 31 * result + buttonPadVLarge.hashCode()
        result = 31 * result + cornerSmall.hashCode()
        result = 31 * result + cornerMedium.hashCode()
        result = 31 * result + cornerLarge.hashCode()
        result = 31 * result + fieldMinHeight.hashCode()
        result = 31 * result + spacingXs.hashCode()
        result = 31 * result + spacingSm.hashCode()
        result = 31 * result + spacingMd.hashCode()
        result = 31 * result + spacingLg.hashCode()
        result = 31 * result + elevationRaised.hashCode()
        result = 31 * result + elevationOverlay.hashCode()
        return result
    }
}

/** The default token set — a single shared instance so default-parameter call sites stay skippable. */
internal val DefaultNativeTokens: NativeTokens = NativeTokens()

public val LocalNativeTokens: ProvidableCompositionLocal<NativeTokens> = staticCompositionLocalOf { DefaultNativeTokens }

/** Ergonomic accessor: `NativeTheme.tokens.spacingMd`, `NativeTheme.statusColors.success`. */
public object NativeTheme {
    public val tokens: NativeTokens
        @Composable
        @ReadOnlyComposable
        get() = LocalNativeTokens.current

    /** Semantic status colors (success/warning/info) for feedback surfaces; Error reuses scheme.error. */
    public val statusColors: NativeStatusColors
        @Composable
        @ReadOnlyComposable
        get() = LocalNativeStatusColors.current

    /** The kit's user-facing strings table (labels + accessibility descriptions). */
    public val strings: NativeStrings
        @Composable
        @ReadOnlyComposable
        get() = LocalNativeStrings.current

    /** Runtime platform capabilities the kit adapts to (reduce motion, …). */
    public val capabilities: NativeCapabilities
        @Composable
        @ReadOnlyComposable
        get() = LocalNativeCapabilities.current
}
