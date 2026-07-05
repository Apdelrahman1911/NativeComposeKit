package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.findRootCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
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
import androidx.compose.ui.unit.LayoutDirection
import platform.UIKit.NSLayoutConstraint
import platform.UIKit.NSTextAlignment
import platform.UIKit.NSTextAlignmentCenter
import platform.UIKit.NSTextAlignmentJustified
import platform.UIKit.NSTextAlignmentLeft
import platform.UIKit.NSTextAlignmentNatural
import platform.UIKit.NSTextAlignmentRight
import platform.UIKit.UISemanticContentAttribute
import platform.UIKit.UISemanticContentAttributeForceLeftToRight
import platform.UIKit.UISemanticContentAttributeForceRightToLeft
import platform.UIKit.UIColor
import platform.UIKit.UIFont
import platform.UIKit.UIFontMetrics
import platform.UIKit.UIFontWeightBold
import platform.UIKit.UIFontWeightLight
import platform.UIKit.UIFontWeightMedium
import platform.UIKit.UIFontWeightRegular
import platform.UIKit.UIFontWeightSemibold
import platform.UIKit.UIFontWeightThin
import platform.UIKit.UIFontWeightUltraLight
import platform.UIKit.UIKeyboardType
import platform.UIKit.UIKeyboardTypeDecimalPad
import platform.UIKit.UIKeyboardTypeDefault
import platform.UIKit.UIKeyboardTypeEmailAddress
import platform.UIKit.UIKeyboardTypeNumberPad
import platform.UIKit.UIApplication
import platform.UIKit.UIKeyboardTypePhonePad
import platform.UIKit.UIKeyboardTypeURL
import platform.UIKit.UIView
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.CoreGraphics.CGFloatVar
import platform.CoreGraphics.CGRect
import platform.CoreGraphics.CGRectMake
import platform.darwin.DISPATCH_TIME_NOW
import platform.darwin.dispatch_after
import platform.darwin.dispatch_get_main_queue
import platform.darwin.dispatch_time
import kotlin.math.abs
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.useContents
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

/**
 * Decomposes a [UIColor] (e.g. a `UIColorWell.selectedColor`) back into a Compose [Color]. Components are
 * **clamped to 0..1**: on wide-gamut (Display P3) devices the system picker returns colors whose
 * extended-sRGB components fall outside that range, and Compose's sRGB `Color(Float…)` constructor throws
 * on them — an unclamped round-trip crashed mid-pick on vivid colors.
 */
@OptIn(ExperimentalForeignApi::class)
internal fun UIColor.toComposeColor(): Color = memScoped {
    val r = alloc<CGFloatVar>()
    val g = alloc<CGFloatVar>()
    val b = alloc<CGFloatVar>()
    val a = alloc<CGFloatVar>()
    getRed(r.ptr, green = g.ptr, blue = b.ptr, alpha = a.ptr)
    Color(
        red = r.value.toFloat().coerceIn(0f, 1f),
        green = g.value.toFloat().coerceIn(0f, 1f),
        blue = b.value.toFloat().coerceIn(0f, 1f),
        alpha = a.value.toFloat().coerceIn(0f, 1f),
    )
}

private fun FontWeight?.toUIFontWeight(): Double = when ((this ?: FontWeight.Normal).weight) {
    in Int.MIN_VALUE..149 -> UIFontWeightUltraLight
    in 150..249 -> UIFontWeightThin
    in 250..349 -> UIFontWeightLight
    in 350..499 -> UIFontWeightRegular
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

/**
 * Maps a Compose alignment to UIKit, resolving the direction-relative values against the **effective**
 * Compose [layoutDirection] (which includes a forced `NativeAppearance.setRtl` override — UIKit's own
 * "natural" only follows the locale). `Start`/null stay `Natural` because interop views also get
 * [toUISemanticContentAttribute] applied, which makes UIKit's natural alignment follow the same effective
 * direction; `End` has no natural counterpart, so it resolves to the physical edge.
 */
internal fun TextAlign?.toNSTextAlignment(layoutDirection: LayoutDirection): NSTextAlignment = when (this) {
    TextAlign.Center -> NSTextAlignmentCenter
    TextAlign.Justify -> NSTextAlignmentJustified
    TextAlign.Left -> NSTextAlignmentLeft
    TextAlign.Right -> NSTextAlignmentRight
    TextAlign.End -> if (layoutDirection == LayoutDirection.Rtl) NSTextAlignmentLeft else NSTextAlignmentRight
    else -> NSTextAlignmentNatural // null and Start: natural, resolved by the forced semantic attribute
}

/**
 * The UIKit counterpart of the effective Compose layout direction. Applied to interop views so a forced
 * app-wide RTL ([NativeAppearance.setRtl]) reaches native controls too — without it, UIKit resolves
 * direction from the app locale and ignores the Compose-side override.
 */
internal fun LayoutDirection.toUISemanticContentAttribute(): UISemanticContentAttribute = when (this) {
    LayoutDirection.Rtl -> UISemanticContentAttributeForceRightToLeft
    LayoutDirection.Ltr -> UISemanticContentAttributeForceLeftToRight
}

internal fun NativeKeyboardType.toUIKeyboardType(): UIKeyboardType = when (this) {
    NativeKeyboardType.Text -> UIKeyboardTypeDefault
    NativeKeyboardType.Email -> UIKeyboardTypeEmailAddress
    NativeKeyboardType.Number -> UIKeyboardTypeNumberPad
    NativeKeyboardType.Phone -> UIKeyboardTypePhonePad
    NativeKeyboardType.Decimal -> UIKeyboardTypeDecimalPad
    NativeKeyboardType.Url -> UIKeyboardTypeURL
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
internal fun NativeInteropTouch.toInteropProperties(
    overlay: Boolean = false,
    /**
     * Expose the native view itself to accessibility services instead of the (empty) Compose semantics of
     * the interop node. Without one of the two, an interop view is INVISIBLE to VoiceOver — Compose only
     * traverses the native element when this flag is set. `true` for interactive controls (they carry real
     * UIKit traits — adjustable sliders, editable fields); display-only views keep `false` and mirror
     * their content into `Modifier.semantics` instead, per Compose's own guidance.
     */
    nativeAccessibility: Boolean = false,
): UIKitInteropProperties = when (this) {
    NativeInteropTouch.Cooperative -> UIKitInteropProperties(interactionMode = UIKitInteropInteractionMode.Cooperative(), placedAsOverlay = overlay, isNativeAccessibilityEnabled = nativeAccessibility)
    NativeInteropTouch.NonCooperative -> UIKitInteropProperties(interactionMode = UIKitInteropInteractionMode.NonCooperative, placedAsOverlay = overlay, isNativeAccessibilityEnabled = nativeAccessibility)
    NativeInteropTouch.NonInteractive -> UIKitInteropProperties(interactionMode = null, placedAsOverlay = overlay, isNativeAccessibilityEnabled = nativeAccessibility)
}

/**
 * Interop properties for a **backed native leaf control placed inside a Compose scroll** (toggle, slider,
 * stepper, segmented, search bar, page control, color well, date picker — every `pinFilling`-backed control).
 *
 * Sets [UIKitInteropProperties.placedAsOverlay] = `true` so the native view + its theme-colored backing
 * composite **above** the Compose layer rather than through a punched cut-out hole. With the default cut-out
 * (`placedAsOverlay = false`) the hole lags the Compose layer by a frame while scrolling, so the backing's
 * leading/top edge is momentarily clipped — the reported "the switch / stepper background gets cut when I
 * scroll." An overlay has no hole to clip through; it is not scroll-aware either, so it can lag/drift
 * slightly during an active fling and snaps back at rest — the accepted trade (docs/interop-notes.md):
 * subtle drift beats a visible clip for these leaf controls. [Cooperative] keeps the
 * scroll-vs-tap arbitration (Compose may claim a vertical drag; taps and short drags still reach the control).
 *
 * Safe for leaf controls: each backing is sized to the control's own bounds (`pinFilling` + the `UIKitView`
 * modifier), and nothing Compose-drawn is meant to paint over a control, so drawing above the canvas is correct.
 */
@OptIn(ExperimentalComposeUiApi::class)
internal fun scrollSafeInteropProperties(
    /** See [toInteropProperties]. Defaults to true: the pinFilling-backed leaves are interactive controls. */
    nativeAccessibility: Boolean = true,
): UIKitInteropProperties =
    UIKitInteropProperties(
        interactionMode = UIKitInteropInteractionMode.Cooperative(),
        placedAsOverlay = true,
        isNativeAccessibilityEnabled = nativeAccessibility,
    )

/**
 * Dispose-time fail-safe for every interop view the kit hosts, closing a CMP 1.11 overlay-interop hazard:
 * ALL UIKit-side interop mutations — insertion, every frame/position update, the final removal, and even
 * the `onRelease` callback — are queued into an internal `UIKitInteropMutableTransaction` and executed
 * only when the next rendered Compose frame is actually presented. A transaction retrieved for a frame
 * that never presents (dropped frame / display-link hiccup — easy to hit during layout storms like an
 * appearance flip or AnimatedVisibility churn in a lazy list) is discarded WITHOUT re-queueing, while the
 * Kotlin-side bookkeeping has already moved on — so the queued `removeFromSuperview` never happens and a
 * per-holder rect cache means a lost position update is never re-sent either. With cut-out placement such
 * a leak hides behind the opaque canvas; with the kit's overlay placement ([scrollSafeInteropProperties])
 * it is fully visible: ghost controls hanging in the window, doubled controls when a lazy item is
 * recreated, stale positions after rows settle. Reproduced deterministically by the sample app's
 * "Interop churn" screen under an appearance flip.
 *
 * `DisposableEffect.onDispose` runs synchronously in the composition apply pass — it does NOT ride the
 * losable transaction — so detaching the factory [root] here guarantees the control leaves the window the
 * moment its node is disposed, whatever happens to the queued container cleanup. Idempotent against the
 * normal path: CMP's own deferred removal targets its wrapper view, not [root], and removing an already
 * removed view is a no-op. Lost *position* updates of still-composed views remain a CMP-level issue (they
 * self-correct on the next layout change); the permanent artifacts — ghosts and doubles — die here.
 *
 * Call it next to the `UIKitView` whose factory returns [root]. Must NOT be combined with `onReset`-based
 * view reuse (the kit uses none): a reused node's root would never be re-attached.
 */
@Composable
internal fun InteropDisposeFailSafe(root: UIView) {
    DisposableEffect(root) {
        onDispose {
            root.hidden = true
            root.removeFromSuperview()
        }
    }
}

/** Point-space rect snapshot for tolerance comparison (frames are UIKit points). */
private data class InteropRectPt(val x: Double, val y: Double, val w: Double, val h: Double)

@OptIn(ExperimentalForeignApi::class)
private fun CValue<CGRect>.toPt(): InteropRectPt =
    useContents { InteropRectPt(origin.x, origin.y, size.width, size.height) }

/** CMP rounds px rects before converting to points, so sub-point noise is expected — 1pt tolerance. */
private fun InteropRectPt.nearly(o: InteropRectPt): Boolean =
    abs(x - o.x) <= 1.0 && abs(y - o.y) <= 1.0 && abs(w - o.w) <= 1.0 && abs(h - o.h) <= 1.0

/**
 * Self-heal for interop POSITIONS — the second half of the transaction-loss hazard
 * [InteropDisposeFailSafe] documents (that one owns lost REMOVALS). A lost position/frame action leaves
 * the interop wrapper at a stale frame while CMP's holder cache believes the update was delivered, so it
 * is never re-sent: the control sits visibly misplaced until some later layout change happens to move
 * the node. On a 120 Hz device a churn animation (AnimatedVisibility collapse, appearance flip) drops
 * its LAST settle frame often enough to strand controls mid-screen — reproduced on an iPhone 17 where
 * the simulator stayed clean.
 *
 * Compose itself always knows the truth: `onGloballyPositioned` fires on the Compose side for every
 * placement regardless of whether the UIKit-side action survived. [onPositioned] records the expected
 * wrapper frames (the same clipped/unclipped root-coordinates math CMP's holder computes) and — one
 * coalesced [afterRecompositionWindow] later — compares them against the ACTUAL frames of the two
 * CMP-owned ancestors (`root.superview` = the unclipped content host, its superview = the clipping
 * wrapper), correcting both directly when they diverge. The normal path is a no-op comparison; a
 * correction writes exactly the values CMP's own next scheduled update would write, so the two writers
 * converge. Checks are generation-free but coalesced: whichever placement is LATEST when the pending
 * check fires is the one verified, and a placement arriving after a check re-arms a new one, so the
 * settle frame is always verified. A detached root (disposed node) skips — removal is the fail-safe's job.
 */
@OptIn(ExperimentalForeignApi::class)
internal class InteropPositionHeal(private val root: UIView) {
    private var generation = 0L

    fun onPositioned(group: CValue<CGRect>, host: CValue<CGRect>) {
        // TRAILING edge on purpose: every placement supersedes the previous check, so the verification
        // only ever runs ≥120ms after the LAST placement — i.e. once the node has settled. A check that
        // acts mid-animation writes frames out of band with the CATransaction that synchronizes interop
        // frames with the canvas, and visibly fights it (piled/trailing controls) — the settled frame is
        // the only one that both needs healing (nothing later will re-send it) and is safe to heal.
        val gen = ++generation
        afterRecompositionWindow {
            if (gen != generation) return@afterRecompositionWindow // superseded — a newer placement owns the check
            val hostView = root.superview ?: return@afterRecompositionWindow
            val groupView = hostView.superview ?: return@afterRecompositionWindow
            if (!groupView.frame.toPt().nearly(group.toPt()) || !hostView.frame.toPt().nearly(host.toPt())) {
                groupView.setFrame(group)
                hostView.setFrame(host)
            }
        }
    }
}

/**
 * The [Modifier] that feeds [InteropPositionHeal] — append to every kit `UIKitView`'s modifier chain
 * (alongside [InteropDisposeFailSafe]). Mirrors the holder's math: wrapper frame = clipped bounds in
 * root coordinates; content-host frame = the unclipped rect positioned relative to the clipped one, all
 * px→pt via density.
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
internal fun rememberInteropPositionHeal(root: UIView): Modifier {
    val density = LocalDensity.current.density.toDouble()
    val heal = remember(root) { InteropPositionHeal(root) }
    return Modifier.onGloballyPositioned { coords ->
        val rootCoords = coords.findRootCoordinates()
        val clipped = rootCoords.localBoundingBoxOf(coords, clipBounds = true)
        val unclipped = rootCoords.localBoundingBoxOf(coords, clipBounds = false)
        heal.onPositioned(
            group = CGRectMake(
                clipped.left / density,
                clipped.top / density,
                clipped.width / density,
                clipped.height / density,
            ),
            host = CGRectMake(
                (unclipped.left - clipped.left) / density,
                (unclipped.top - clipped.top) / density,
                unclipped.width / density,
                unclipped.height / density,
            ),
        )
    }
}

/**
 * Sets the native `accessibilityIdentifier` (maps from `testTag`, for UI tests) via KVC. We use KVC
 * because casting a view to the bridged `UIAccessibilityIdentificationProtocol` compiles but throws a
 * `TypeCastException` at runtime in Kotlin/Native (the protocol conformance isn't visible to the cast).
 */
internal fun UIView.setAccessibilityId(id: String) {
    setValue(id, forKey = "accessibilityIdentifier")
}

/**
 * The backing view every native leaf control is hosted in (created in each renderer, filled via
 * [pinFilling]). It starts **hidden** and stays hidden while its frame is DEGENERATE (zero area):
 * Compose only positions an interop view once its node has a real, non-empty placement — a node that
 * measures to zero (e.g. a slider given no width) leaves the native view parked unpositioned at the
 * content origin, and iOS 26 glass controls paint their lens OUTSIDE their bounds even at size zero
 * (a zero-sized `UISlider`'s thumb rendered as a stray accent-colored sliver at the screen's top-left).
 * Starting hidden also removes any first-frame flash at a default frame before the first placement
 * lands; the view unhides the moment a real frame arrives, so correctly-sized controls are visible
 * from their first placed frame.
 */
@OptIn(ExperimentalForeignApi::class)
internal class InteropBackingView : UIView(frame = platform.CoreGraphics.CGRectMake(0.0, 0.0, 0.0, 0.0)) {
    init {
        hidden = true // until the first valid placement
    }

    override fun layoutSubviews() {
        super.layoutSubviews()
        hidden = platform.CoreGraphics.CGRectIsEmpty(bounds)
    }
}

/**
 * Pins [child] to fill this view via Auto Layout and adds it as a subview. Used to place a native
 * control inside an opaque, theme-colored backing: the control keeps its rounded/native shape, and
 * its transparent pixels reveal the backing (the theme surface) instead of the interop host
 * backdrop — which would otherwise read as a white/black box in dark mode. Because the child is
 * pinned on all edges, the backing's Auto Layout fitting size also tracks the child's intrinsic size.
 * Idempotent: a `UIKitView` factory re-runs when its interop properties change, so re-pinning the
 * same remembered child must not stack duplicate constraints.
 */
internal fun UIView.pinFilling(child: UIView) {
    if (child.superview == this) return
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
 * Runs [block] on the main queue after the next recomposition has had time to land (~2 frames + margin).
 *
 * The controlled-rejection re-asserts in the toggle/slider/stepper handlers must not race the recomposition
 * an ACCEPTED change triggers: a plain `dispatch_async` tick reliably runs BEFORE the next Compose frame,
 * so the re-assert would act on a stale composed value — snapping the control backwards mid-animation and
 * then being snapped forward again by the late update (the reported mid-tap `UISwitch` thumb/track
 * ghosting). Callers pair this with an update-generation check: if composition responded inside the window,
 * its update is authoritative and the re-assert must do nothing. 120ms comfortably covers a dropped frame;
 * a genuine rejection's spring-back at that delay still reads as an immediate refusal.
 */
internal fun afterRecompositionWindow(block: () -> Unit) {
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, 120_000_000L), dispatch_get_main_queue(), block)
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
