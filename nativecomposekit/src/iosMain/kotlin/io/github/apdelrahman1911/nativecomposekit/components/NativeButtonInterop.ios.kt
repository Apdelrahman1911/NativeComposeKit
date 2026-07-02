package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeMenu
import platform.UIKit.UIMenu
import io.github.apdelrahman1911.nativecomposekit.components.model.ResolvedButtonStyle
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGAffineTransformMakeScale
import platform.CoreGraphics.CGPoint
import platform.CoreGraphics.CGRectMake
import platform.UIKit.NSLayoutConstraint
import platform.UIKit.NSTextAlignmentCenter
import platform.UIKit.UIActivityIndicatorView
import platform.UIKit.UIActivityIndicatorViewStyleMedium
import platform.UIKit.UIButton
import platform.UIKit.UIColor
import platform.UIKit.UIControlEventTouchCancel
import platform.UIKit.UIControlEventTouchDown
import platform.UIKit.UIControlEventTouchUpInside
import platform.UIKit.UIControlEventTouchUpOutside
import platform.UIKit.UIEvent
import platform.UIKit.UIImage
import platform.UIKit.UIImageView
import platform.UIKit.UILabel
import platform.UIKit.UIStackView
import platform.UIKit.UIStackViewAlignmentCenter
import platform.UIKit.UIView
import platform.UIKit.UIViewContentMode
import platform.darwin.NSObject
import platform.objc.sel_registerName

/** Forwards a UIButton tap to a Kotlin lambda (target-action). */
@OptIn(BetaInteropApi::class)
internal class ButtonTapHandler : NSObject() {
    var onClick: () -> Unit = {}

    @ObjCAction
    fun handleTap() = onClick()
}

/**
 * Press feedback for a custom-content `UIButton` (which has no automatic highlight). Animates a brief
 * scale-down "pop" + dim. Crucially this is *animated on release*, not an instant alpha toggle: on a real
 * device the Compose interop runs touches in Cooperative mode and a quick tap collapses TouchDown and
 * TouchUpInside into one frame, so an instant set/reset would be invisible. Forcing the pressed state in
 * [pressUp] and animating back guarantees a visible pop for any tap speed (and a held press still dims).
 */
@OptIn(BetaInteropApi::class, ExperimentalForeignApi::class)
internal class ButtonPressDimmer : NSObject() {
    var target: UIView? = null

    @ObjCAction
    fun pressDown() {
        val v = target ?: return
        UIView.animateWithDuration(0.09, animations = {
            v.transform = CGAffineTransformMakeScale(0.96, 0.96)
            v.setAlpha(0.85)
        })
    }

    @ObjCAction
    fun pressUp() {
        val v = target ?: return
        // Ensure the pressed state is applied, then animate back — so an instant tap still shows the pop.
        v.transform = CGAffineTransformMakeScale(0.96, 0.96)
        v.setAlpha(0.85)
        UIView.animateWithDuration(0.28, animations = {
            v.transform = CGAffineTransformMakeScale(1.0, 1.0)
            v.setAlpha(1.0)
        })
    }
}

/**
 * A `UIButton` that reports a minimum hit area via [pointInside], so a visually compact button (e.g. a
 * 36pt-tall Small button centered in a ≥44pt host) still meets the HIG 44pt minimum touch target — taps in
 * the surrounding margin reach this view through its superview's hit-test and are accepted. [minHitWidth]/
 * [minHitHeight] default to 0 (no expansion = standard behavior); set them when the visual is below 44pt.
 */
@OptIn(ExperimentalForeignApi::class)
internal class MinHitButton : UIButton(frame = CGRectMake(0.0, 0.0, 0.0, 0.0)) {
    var minHitWidth: Double = 0.0
    var minHitHeight: Double = 0.0

    override fun pointInside(point: CValue<CGPoint>, withEvent: UIEvent?): Boolean {
        var inside = false
        bounds.useContents {
            val bw = size.width
            val bh = size.height
            val dx = maxOf(0.0, (minHitWidth - bw) / 2.0)
            val dy = maxOf(0.0, (minHitHeight - bh) / 2.0)
            point.useContents {
                inside = x >= -dx && x <= bw + dx && y >= -dy && y <= bh + dy
            }
        }
        return inside
    }
}

private const val ICON_PT = 20.0 // glyph box for leading/trailing SF Symbols

/**
 * A real `UIButton` whose content is a horizontal stack `[leading icon | label | trailing icon]`, with a
 * centered loading spinner overlay. [build] wires it once; [apply] re-themes it from a theme-resolved
 * [ResolvedButtonStyle]. Because the icons are independent stack items, a leading AND trailing icon can
 * show together in their true positions — no forced-RTL hack. Reused by NativeButton, NativeIconButton,
 * and NativeSplitButton (so they stay visually consistent).
 */
@OptIn(ExperimentalForeignApi::class)
internal class NativeButtonViews {
    val button = MinHitButton()
    val label = UILabel()
    val leadingImage = UIImageView()
    val trailingImage = UIImageView()
    val spinner = UIActivityIndicatorView(activityIndicatorStyle = UIActivityIndicatorViewStyleMedium)
    val tap = ButtonTapHandler()

    private val stack = UIStackView()
    private val dimmer = ButtonPressDimmer()
    private var leadingConstraint: NSLayoutConstraint? = null
    private var trailingConstraint: NSLayoutConstraint? = null
    private var built = false

    /**
     * Assemble subviews, constraints, and press/tap targets. Idempotent (safe to call from a factory).
     * When [centered] is true the content is centered and the button is constrained square (for
     * icon-only buttons); otherwise the content is pinned with the style's horizontal insets so the
     * button hugs its content and a label can size naturally.
     */
    fun build(spacing: Double, centered: Boolean = false) {
        if (built) return
        built = true

        stack.alignment = UIStackViewAlignmentCenter
        stack.spacing = spacing
        stack.userInteractionEnabled = false
        label.textAlignment = NSTextAlignmentCenter
        label.adjustsFontForContentSizeCategory = true // re-scale live when the user changes Dynamic Type
        leadingImage.contentMode = UIViewContentMode.UIViewContentModeScaleAspectFit
        trailingImage.contentMode = UIViewContentMode.UIViewContentModeScaleAspectFit
        spinner.hidesWhenStopped = true

        stack.addArrangedSubview(leadingImage)
        stack.addArrangedSubview(label)
        stack.addArrangedSubview(trailingImage)

        stack.translatesAutoresizingMaskIntoConstraints = false
        spinner.translatesAutoresizingMaskIntoConstraints = false
        button.addSubview(stack)
        button.addSubview(spinner)
        dimmer.target = button // scale/dim the whole button on press (visible at any tap speed)

        val constraints = mutableListOf(
            stack.centerYAnchor.constraintEqualToAnchor(button.centerYAnchor),
            spinner.centerXAnchor.constraintEqualToAnchor(button.centerXAnchor),
            spinner.centerYAnchor.constraintEqualToAnchor(button.centerYAnchor),
            leadingImage.widthAnchor.constraintEqualToConstant(ICON_PT),
            leadingImage.heightAnchor.constraintEqualToConstant(ICON_PT),
            trailingImage.widthAnchor.constraintEqualToConstant(ICON_PT),
            trailingImage.heightAnchor.constraintEqualToConstant(ICON_PT),
        )
        if (centered) {
            // Icon-only: center the glyph and force a square button (width == height).
            constraints += stack.centerXAnchor.constraintEqualToAnchor(button.centerXAnchor)
            constraints += button.widthAnchor.constraintEqualToAnchor(button.heightAnchor)
        } else {
            // Pin to the insets so the button hugs its content (and the label can size naturally).
            val lead = stack.leadingAnchor.constraintEqualToAnchor(button.leadingAnchor, constant = 0.0)
            val trail = button.trailingAnchor.constraintEqualToAnchor(stack.trailingAnchor, constant = 0.0)
            leadingConstraint = lead
            trailingConstraint = trail
            constraints += lead
            constraints += trail
        }
        NSLayoutConstraint.activateConstraints(constraints)

        button.addTarget(dimmer, sel_registerName("pressDown"), UIControlEventTouchDown)
        button.addTarget(dimmer, sel_registerName("pressUp"), UIControlEventTouchUpInside)
        button.addTarget(dimmer, sel_registerName("pressUp"), UIControlEventTouchUpOutside)
        button.addTarget(dimmer, sel_registerName("pressUp"), UIControlEventTouchCancel)
        button.addTarget(tap, sel_registerName("handleTap"), UIControlEventTouchUpInside)
    }

    /**
     * Re-theme and set state. [showLabel] is false for icon-only buttons. [leadName]/[trailName] are SF
     * Symbol names (null = hidden). When [menu] is non-null the button presents it as its primary action.
     */
    fun apply(
        style: ResolvedButtonStyle,
        text: String,
        showLabel: Boolean,
        enabled: Boolean,
        loading: Boolean,
        leadName: String?,
        trailName: String?,
        menu: NativeMenu?,
    ) {
        val content = style.colors.content.toUIColor()
        val transparent = style.colors.container == Color.Transparent

        button.backgroundColor = if (transparent) UIColor.clearColor() else style.colors.container.toUIColor()
        button.layer.cornerRadius = style.cornerRadius.value.toDouble()
        button.clipsToBounds = true
        if (style.colors.border.isSpecified) {
            button.layer.borderWidth = 1.0
            button.layer.borderColor = style.colors.border.toUIColor().CGColor
        } else {
            button.layer.borderWidth = 0.0
        }

        leadingConstraint?.setConstant(style.insets.start.value.toDouble())
        trailingConstraint?.setConstant(style.insets.end.value.toDouble())

        label.text = text
        label.font = style.textStyle.toUIFont()
        label.textColor = content
        label.setHidden(!showLabel || loading || text.isEmpty())

        leadingImage.image = if (loading) null else leadName?.let { UIImage.systemImageNamed(it) }
        leadingImage.tintColor = content
        leadingImage.setHidden(loading || leadName == null)

        trailingImage.image = if (loading) null else trailName?.let { UIImage.systemImageNamed(it) }
        trailingImage.tintColor = content
        trailingImage.setHidden(loading || trailName == null)

        spinner.color = content
        if (loading) spinner.startAnimating() else spinner.stopAnimating()

        button.setEnabled(enabled && !loading)

        if (menu != null) {
            // Rebuild the UIMenu only when its STRUCTURE changes (titles/icons/roles/enabled/selected) —
            // rebuilding per update re-fires on every scroll frame and dismisses an open menu mid-gesture.
            // The cached actions dispatch by index into `currentMenu`, so fresh onSelect lambdas from each
            // recomposition are always the ones invoked (no stale captures).
            currentMenu = menu
            val fingerprint = menu.structuralFingerprint()
            if (builtMenuFingerprint != fingerprint) {
                builtMenuFingerprint = fingerprint
                builtMenu = buildUIMenu(menu) { index -> currentMenu?.items?.getOrNull(index)?.onSelect?.invoke() }
            }
            if (button.menu !== builtMenu) button.menu = builtMenu
            button.showsMenuAsPrimaryAction = true
        } else {
            currentMenu = null
            builtMenu = null
            builtMenuFingerprint = null
            button.menu = null
            button.showsMenuAsPrimaryAction = false
        }
    }

    private var currentMenu: NativeMenu? = null
    private var builtMenu: UIMenu? = null
    private var builtMenuFingerprint: List<Any?>? = null
}
