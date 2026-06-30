package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.viewinterop.UIKitInteropInteractionMode
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeInteropTouch
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeKeyboardType
import io.github.apdelrahman1911.nativecomposekit.theme.LocalNativeSurface
import platform.Foundation.setValue
import platform.UIKit.NSLayoutConstraint
import platform.UIKit.NSTextAlignment
import platform.UIKit.NSTextAlignmentCenter
import platform.UIKit.NSTextAlignmentLeft
import platform.UIKit.NSTextAlignmentNatural
import platform.UIKit.NSTextAlignmentRight
import platform.UIKit.UIColor
import platform.UIKit.UIFont
import platform.UIKit.UIFontMetrics
import platform.UIKit.UIFontWeightBold
import platform.UIKit.UIFontWeightMedium
import platform.UIKit.UIFontWeightRegular
import platform.UIKit.UIFontWeightSemibold
import platform.UIKit.UIKeyboardType
import platform.UIKit.UIKeyboardTypeDecimalPad
import platform.UIKit.UIKeyboardTypeDefault
import platform.UIKit.UIKeyboardTypeEmailAddress
import platform.UIKit.UIKeyboardTypeNumberPad
import platform.UIKit.UIApplication
import platform.UIKit.UIKeyboardTypePhonePad
import platform.UIKit.UIView
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.CoreGraphics.CGFloatVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value

/**
 * Small bridges that turn the resolved (theme-derived) Compose values into their native UIKit
 * equivalents. The Native* iOS renderers read ONLY these — they never hardcode colors/sizes.
 */

internal fun Color.toUIColor(): UIColor = UIColor(
    red = red.toDouble(),
    green = green.toDouble(),
    blue = blue.toDouble(),
    alpha = alpha.toDouble(),
)

/** Decomposes a [UIColor] (e.g. a `UIColorWell.selectedColor`) back into a Compose [Color]. */
@OptIn(ExperimentalForeignApi::class)
internal fun UIColor.toComposeColor(): Color = memScoped {
    val r = alloc<CGFloatVar>()
    val g = alloc<CGFloatVar>()
    val b = alloc<CGFloatVar>()
    val a = alloc<CGFloatVar>()
    getRed(r.ptr, green = g.ptr, blue = b.ptr, alpha = a.ptr)
    Color(red = r.value.toFloat(), green = g.value.toFloat(), blue = b.value.toFloat(), alpha = a.value.toFloat())
}

private fun FontWeight?.toUIFontWeight(): Double = when ((this ?: FontWeight.Normal).weight) {
    in Int.MIN_VALUE..499 -> UIFontWeightRegular
    in 500..599 -> UIFontWeightMedium
    in 600..699 -> UIFontWeightSemibold
    else -> UIFontWeightBold
}

/**
 * Builds the native font from the resolved [TextStyle]. The point size is **scaled for the user's Dynamic
 * Type setting** via [UIFontMetrics] (HIG: text should honor the accessibility text size). Controls that set
 * this font should also set `adjustsFontForContentSizeCategory = true` so they re-scale live when the user
 * changes the setting.
 */
internal fun TextStyle.toUIFont(): UIFont {
    val raw = fontSize.value
    val size = if (raw.isNaN() || raw <= 0f) 17.0 else raw.toDouble()
    val base = UIFont.systemFontOfSize(fontSize = size, weight = fontWeight.toUIFontWeight())
    return UIFontMetrics.defaultMetrics.scaledFontForFont(base)
}

internal fun TextAlign?.toNSTextAlignment(): NSTextAlignment = when (this) {
    TextAlign.Center -> NSTextAlignmentCenter
    TextAlign.Left, TextAlign.Start -> NSTextAlignmentLeft
    TextAlign.Right, TextAlign.End -> NSTextAlignmentRight
    else -> NSTextAlignmentNatural
}

internal fun NativeKeyboardType.toUIKeyboardType(): UIKeyboardType = when (this) {
    NativeKeyboardType.Text -> UIKeyboardTypeDefault
    NativeKeyboardType.Email -> UIKeyboardTypeEmailAddress
    NativeKeyboardType.Number -> UIKeyboardTypeNumberPad
    NativeKeyboardType.Phone -> UIKeyboardTypePhonePad
    NativeKeyboardType.Decimal -> UIKeyboardTypeDecimalPad
}

/**
 * Maps the public [NativeInteropTouch] to Compose's (experimental) `UIKitInteropProperties`, so the
 * touch strategy for a native view embedded in a Compose scroll stays a stable public choice. Shared by
 * every Native* iOS renderer (see architecture.md §6).
 *
 * [overlay] selects `placedAsOverlay` (default cut-out). It is `true` only inside a [NativeDialog] (via
 * [LocalNativeInteropPlacement]) so a freshly mounted dialog scene doesn't flash its black host backdrop through
 * a not-yet-filled cut-out hole — see [LocalNativeInteropPlacement]. Scrolling screens keep cut-out (the default).
 */
@OptIn(ExperimentalComposeUiApi::class)
internal fun NativeInteropTouch.toInteropProperties(overlay: Boolean = false): UIKitInteropProperties = when (this) {
    NativeInteropTouch.Cooperative -> UIKitInteropProperties(interactionMode = UIKitInteropInteractionMode.Cooperative(), placedAsOverlay = overlay)
    NativeInteropTouch.NonCooperative -> UIKitInteropProperties(interactionMode = UIKitInteropInteractionMode.NonCooperative, placedAsOverlay = overlay)
    NativeInteropTouch.NonInteractive -> UIKitInteropProperties(interactionMode = null, placedAsOverlay = overlay)
}

/**
 * Interop properties for a **backed native leaf control placed inside a Compose scroll** (toggle, slider,
 * stepper, segmented, search bar, page control, color well, date picker — every `pinFilling`-backed control).
 *
 * Sets [UIKitInteropProperties.placedAsOverlay] = `true` so the native view + its theme-colored backing
 * composite **above** the Compose layer rather than through a punched cut-out hole. With the default cut-out
 * (`placedAsOverlay = false`) the hole lags the Compose layer by a frame while scrolling, so the backing's
 * leading/top edge is momentarily clipped — the reported "the switch / stepper background gets cut when I
 * scroll." An overlay has no hole to fall out of sync, so the control scrolls cleanly. [Cooperative] keeps the
 * scroll-vs-tap arbitration (Compose may claim a vertical drag; taps and short drags still reach the control).
 *
 * Safe for leaf controls: each backing is sized to the control's own bounds (`pinFilling` + the `UIKitView`
 * modifier), and nothing Compose-drawn is meant to paint over a control, so drawing above the canvas is correct.
 */
@OptIn(ExperimentalComposeUiApi::class)
internal fun scrollSafeInteropProperties(): UIKitInteropProperties =
    UIKitInteropProperties(interactionMode = UIKitInteropInteractionMode.Cooperative(), placedAsOverlay = true)

/**
 * Pins [child] to fill this view via Auto Layout and adds it as a subview. Used to place a native
 * control inside an opaque, theme-colored backing: the control keeps its rounded/native shape, and
 * its transparent pixels reveal the backing (the theme surface) instead of the interop host
 * backdrop — which would otherwise read as a white/black box in dark mode. Because the child is
 * pinned on all edges, the backing's Auto Layout fitting size also tracks the child's intrinsic size.
 */
/**
 * Sets the native `accessibilityIdentifier` (maps from `testTag`, for UI tests) via KVC. We use KVC
 * because casting a view to the bridged `UIAccessibilityIdentificationProtocol` compiles but throws a
 * `TypeCastException` at runtime in Kotlin/Native (the protocol conformance isn't visible to the cast).
 */
internal fun UIView.setAccessibilityId(id: String) {
    setValue(id, forKey = "accessibilityIdentifier")
}

internal fun UIView.pinFilling(child: UIView) {
    child.translatesAutoresizingMaskIntoConstraints = false
    addSubview(child)
    NSLayoutConstraint.activateConstraints(
        listOf(
            child.leadingAnchor.constraintEqualToAnchor(leadingAnchor),
            child.trailingAnchor.constraintEqualToAnchor(trailingAnchor),
            child.topAnchor.constraintEqualToAnchor(topAnchor),
            child.bottomAnchor.constraintEqualToAnchor(bottomAnchor),
        ),
    )
}

/**
 * The backing color for a native control's [pinFilling] wrapper, resolved **from context** — use this instead
 * of a hardcoded `style.surface` for every native UIKit control's backing:
 * - On a **known solid surface** (a parent published [LocalNativeSurface] — the page background, a `NativeCard`,
 *   etc.) → that exact surface color, so the opaque backing matches what's visually behind the control and the
 *   `UIKitView` interop region never exposes the host's system backdrop (the black/white rectangle).
 * - On a **material / Liquid Glass surface** ([LocalNativeSurface] is `Color.Unspecified`) → **clear**, so the
 *   native material shows through the control's transparent pixels instead of being covered by a solid box.
 *
 * (The control's own opaque pixels — a switch track, slider track, dots — still render either way; only the
 * surrounding interop region changes.) `@Composable` because it reads the [LocalNativeSurface] composition local.
 */
@Composable
internal fun interopBackingColor(): UIColor {
    val surface = LocalNativeSurface.current
    return if (surface.isSpecified) surface.toUIColor() else UIColor.clearColor()
}

/**
 * The top-most presented view controller — the correct thing to present a native sheet / popover / share
 * sheet from. Walks the presentation chain from the key window's root. Shared by the native-presentation
 * components (share, sheet, popover). (The feedback package keeps its own copy to stay self-contained.)
 */
@OptIn(ExperimentalForeignApi::class)
internal fun topmostUIViewController(): UIViewController? {
    val app = UIApplication.sharedApplication
    val window = app.keyWindow ?: (app.windows.firstOrNull() as? UIWindow)
    var vc: UIViewController? = window?.rootViewController
    while (true) {
        val presented = vc?.presentedViewController ?: break
        vc = presented
    }
    return vc
}
