package io.github.apdelrahman1911.nativecomposekit.components.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp

/** Content padding as four edge values — becomes PaddingValues on Android / insets on iOS. */
@Immutable
internal data class NativeInsets(val start: Dp, val top: Dp, val end: Dp, val bottom: Dp)

/**
 * Caller-override colors for the button family. Compares by value; not a `data class` so fields can
 * be added binary-compatibly — use [copy] to derive variants.
 */
@Immutable
public class NativeButtonColors(
    public val container: Color,
    public val content: Color,
    public val border: Color = Color.Unspecified, // Unspecified == no border
) {
    /** Returns a copy with the given colors replaced. */
    public fun copy(
        container: Color = this.container,
        content: Color = this.content,
        border: Color = this.border,
    ): NativeButtonColors = NativeButtonColors(container, content, border)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NativeButtonColors) return false
        return container == other.container && content == other.content && border == other.border
    }

    override fun hashCode(): Int =
        (container.hashCode() * 31 + content.hashCode()) * 31 + border.hashCode()
}

/**
 * Fully resolved button styling handed to the platform renderer. The renderer reads ONLY these
 * values (already pulled from AppTheme) — it hardcodes nothing.
 */
@Immutable
internal data class ResolvedButtonStyle(
    val variant: NativeButtonVariant,
    val colors: NativeButtonColors,
    val height: Dp,
    val insets: NativeInsets,
    val cornerRadius: Dp,
    /** Gap between a leading/trailing icon and the label (from NativeTokens.spacingSm). */
    val iconSpacing: Dp,
    val textStyle: TextStyle,
)

/**
 * Caller-override colors for text fields. Compares by value; not a `data class` so fields can be
 * added binary-compatibly — use [copy] to derive variants.
 */
@Immutable
public class NativeFieldColors(
    public val text: Color,
    public val placeholder: Color,
    public val container: Color,
    public val border: Color,
    public val focusedBorder: Color,
    public val errorBorder: Color,
    public val label: Color,
    public val helper: Color,
    public val error: Color,
    public val cursor: Color,
) {
    /** Returns a copy with the given colors replaced. */
    public fun copy(
        text: Color = this.text,
        placeholder: Color = this.placeholder,
        container: Color = this.container,
        border: Color = this.border,
        focusedBorder: Color = this.focusedBorder,
        errorBorder: Color = this.errorBorder,
        label: Color = this.label,
        helper: Color = this.helper,
        error: Color = this.error,
        cursor: Color = this.cursor,
    ): NativeFieldColors = NativeFieldColors(
        text, placeholder, container, border, focusedBorder, errorBorder, label, helper, error, cursor,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NativeFieldColors) return false
        return text == other.text &&
            placeholder == other.placeholder &&
            container == other.container &&
            border == other.border &&
            focusedBorder == other.focusedBorder &&
            errorBorder == other.errorBorder &&
            label == other.label &&
            helper == other.helper &&
            error == other.error &&
            cursor == other.cursor
    }

    override fun hashCode(): Int {
        var result = text.hashCode()
        result = 31 * result + placeholder.hashCode()
        result = 31 * result + container.hashCode()
        result = 31 * result + border.hashCode()
        result = 31 * result + focusedBorder.hashCode()
        result = 31 * result + errorBorder.hashCode()
        result = 31 * result + label.hashCode()
        result = 31 * result + helper.hashCode()
        result = 31 * result + error.hashCode()
        result = 31 * result + cursor.hashCode()
        return result
    }
}

/**
 * Colors for [io.github.apdelrahman1911.nativecomposekit.components.NativeDialog]. Defaults resolve from the
 * theme (`surface`/`onSurface`); pass a [copy] to restyle the dialog's surface, body content, and title.
 * [border] is `Unspecified` for no border. Compares by value; not a `data class` so fields can be added
 * binary-compatibly.
 */
@Immutable
public class NativeDialogColors(
    public val container: Color,
    public val content: Color,
    public val title: Color,
    public val border: Color = Color.Unspecified,
) {
    /** Returns a copy with the given colors replaced. */
    public fun copy(
        container: Color = this.container,
        content: Color = this.content,
        title: Color = this.title,
        border: Color = this.border,
    ): NativeDialogColors = NativeDialogColors(container, content, title, border)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NativeDialogColors) return false
        return container == other.container &&
            content == other.content &&
            title == other.title &&
            border == other.border
    }

    override fun hashCode(): Int {
        var result = container.hashCode()
        result = 31 * result + content.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + border.hashCode()
        return result
    }
}

/** Fully resolved text-field styling handed to the platform renderer. */
@Immutable
internal data class ResolvedFieldStyle(
    val colors: NativeFieldColors,
    val cornerRadius: Dp,
    val minHeight: Dp,
    val textStyle: TextStyle,
)

/**
 * Each [surface] below is the page color the control sits on (from the theme). iOS paints it behind
 * the native control so the rounded/transparent native widget doesn't reveal the interop host
 * backdrop (a white/black box in dark mode). Android ignores [surface].
 */

/** Resolved toggle/switch styling. */
@Immutable
internal data class ResolvedToggleStyle(
    val trackOnColor: Color, // track when on (UISwitch onTintColor / Material checked track)
    val thumbColor: Color,
    val surface: Color,
)

/** Resolved segmented-control styling. */
@Immutable
internal data class ResolvedSegmentedStyle(
    val selectedColor: Color,     // selected segment background
    val textColor: Color,         // unselected label color
    val selectedTextColor: Color, // selected label color
    val surface: Color,
    val textStyle: TextStyle,
)

/** Resolved slider styling. */
@Immutable
internal data class ResolvedSliderStyle(
    val activeTrackColor: Color,   // filled (minimum) track
    val inactiveTrackColor: Color, // unfilled (maximum) track
    val thumbColor: Color,
    val surface: Color,
)

/** Resolved stepper styling. */
@Immutable
internal data class ResolvedStepperStyle(
    val tint: Color, // -/+ control tint
    val surface: Color,
)

/**
 * Fully resolved styling for a feedback surface (toast / snackbar / banner / inline status, and the
 * branded iOS alert/sheet overlay). Resolved from a [NativeFeedbackStatus] against
 * [io.github.apdelrahman1911.nativecomposekit.theme.NativeStatusColors] + the theme — the renderer reads ONLY these values.
 *
 * - [background]/[content] are the filled-surface pair (e.g. successContainer / onSuccessContainer).
 * - [iconTint]/[border] use the bolder status color so a leading glyph and outline read clearly,
 *   including the non-filled (outlined) inline variant where [background] is the page [surface].
 */
@Immutable
internal data class ResolvedFeedbackStyle(
    val background: Color,
    val content: Color,
    val border: Color,
    val iconTint: Color,
    val cornerRadius: Dp,
    val insets: NativeInsets,
    val textStyle: TextStyle,
    val titleTextStyle: TextStyle,
)

/**
 * Resolved progress-indicator styling. iOS `UIActivityIndicatorView`/`UIProgressView`; Android Material.
 * No `surface` backing: the spinner is transparent and the bar is opaque, so neither reveals the interop
 * host backdrop (unlike the rounded Toggle/Slider, which do need a backing).
 */
@Immutable
internal data class ResolvedProgressStyle(
    val indicator: Color, // the moving/filled part (UIProgressView progressTint / spinner color)
    val track: Color,     // the unfilled track (linear only on iOS)
)

/** Resolved search-field styling (iOS `UISearchBar`, Android Material search-styled field). */
@Immutable
internal data class ResolvedSearchStyle(
    val text: Color,
    val placeholder: Color,
    val container: Color, // Android field container (iOS uses the native search-field appearance)
    val tint: Color, // magnifier / clear / cancel / cursor tint
    val cornerRadius: Dp,
    val textStyle: TextStyle,
    /** Page color painted behind the rounded native `UISearchBar` on iOS (so its corners don't reveal the
     * interop host backdrop in dark mode); Android ignores it. */
    val surface: Color,
)

/** Resolved page-control (dots) styling (iOS `UIPageControl`, Android branded dots — no backing needed). */
@Immutable
internal data class ResolvedPageControlStyle(
    val current: Color,
    val inactive: Color,
)
