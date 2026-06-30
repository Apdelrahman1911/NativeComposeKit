package io.github.apdelrahman1911.nativecomposekit.components.feedback

import io.github.apdelrahman1911.nativecomposekit.components.ButtonTapHandler
import io.github.apdelrahman1911.nativecomposekit.components.model.ResolvedFeedbackStyle
import io.github.apdelrahman1911.nativecomposekit.components.pinFilling
import io.github.apdelrahman1911.nativecomposekit.components.toUIColor
import io.github.apdelrahman1911.nativecomposekit.components.toUIFont
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSValue
import platform.Foundation.setValue
import platform.Foundation.valueForKey
import platform.UIKit.NSLayoutConstraint
import platform.UIKit.NSTextAlignmentCenter
import platform.UIKit.UIAccessibilityPostNotification
import platform.UIKit.UIAccessibilityScreenChangedNotification
import platform.UIKit.UIAdaptivePresentationControllerDelegateProtocol
import platform.UIKit.valueWithCGRect
import platform.UIKit.UIAlertAction
import platform.UIKit.UIAlertActionStyle
import platform.UIKit.UIAlertActionStyleCancel
import platform.UIKit.UIAlertActionStyleDefault
import platform.UIKit.UIAlertActionStyleDestructive
import platform.UIKit.UIAlertController
import platform.UIKit.UIAlertControllerStyleActionSheet
import platform.UIKit.UIAlertControllerStyleAlert
import platform.UIKit.UIButton
import platform.UIKit.UIColor
import platform.UIKit.UIControlEventTouchUpInside
import platform.UIKit.UIControlStateNormal
import platform.UIKit.UIFont
import platform.UIKit.UIGestureRecognizer
import platform.UIKit.UIGestureRecognizerDelegateProtocol
import platform.UIKit.UILabel
import platform.UIKit.UIPresentationController
import platform.UIKit.UITapGestureRecognizer
import platform.UIKit.UITouch
import platform.UIKit.UIView
import platform.UIKit.UIViewController
import platform.UIKit.accessibilityViewIsModal
import platform.UIKit.presentationController
import platform.darwin.NSObject
import platform.objc.sel_registerName

/**
 * Presents the modal [record], returning a dismisser the host runs on dispose. Native by default (real
 * `UIAlertController`); the branded custom overlay when the record opted in. [primary]/[error]/
 * [cancelColor] are theme colors (resolved in composition) used by the branded button styling.
 */
@OptIn(ExperimentalForeignApi::class)
internal fun presentModal(
    record: ModalRecord,
    cardStyle: ResolvedFeedbackStyle,
    primary: UIColor,
    error: UIColor,
    cancelColor: UIColor,
    controller: BrandFeedbackController,
): () -> Unit {
    val branded = when (record) {
        is AlertRecord -> record.iosPresentation == BrandPresentation.Branded
        is SheetRecord -> record.iosPresentation == BrandPresentation.Branded
    }
    return if (branded) {
        presentBrandedModal(record, cardStyle, primary, error, cancelColor, controller)
    } else {
        presentNativeModal(record, controller)
    }
}

/** The topmost presented view controller to present from (walks the presentation chain). */
@OptIn(ExperimentalForeignApi::class)
internal fun topmostViewController(): UIViewController? {
    val window = feedbackKeyWindow() ?: return null
    var vc: UIViewController? = window.rootViewController
    while (true) {
        val presented = vc?.presentedViewController ?: break
        vc = presented
    }
    return vc
}

private fun ModalRecord.actionItems(): List<Triple<String, BrandAlertActionRole, () -> Unit>> = when (this) {
    is AlertRecord -> actions.map { Triple(it.label, it.role, it.onClick) }
    is SheetRecord -> actions.map { Triple(it.label, it.role, it.onClick) }
}

private fun BrandAlertActionRole.toUIAlertActionStyle(): UIAlertActionStyle = when (this) {
    BrandAlertActionRole.Default -> UIAlertActionStyleDefault
    BrandAlertActionRole.Cancel -> UIAlertActionStyleCancel
    BrandAlertActionRole.Destructive -> UIAlertActionStyleDestructive
}

// region native

/**
 * iPad-only safety net. An action sheet shown as a **popover** can be dismissed by tapping OUTSIDE it,
 * which fires NO `UIAlertAction` handler — so without this the modal lane would never advance (the bug
 * the audit flagged). Routes that interactive dismissal to [onSystemDismiss] (id-guarded inside). On
 * iPhone the alert/sheet is never a popover, so this callback simply never fires.
 */
@OptIn(BetaInteropApi::class)
private class ModalDismissDelegate(
    private val onSystemDismiss: () -> Unit,
) : NSObject(), UIAdaptivePresentationControllerDelegateProtocol {
    override fun presentationControllerDidDismiss(presentationController: UIPresentationController) {
        onSystemDismiss()
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun presentNativeModal(record: ModalRecord, controller: BrandFeedbackController): () -> Unit {
    val retained = mutableListOf<Any>()
    val isSheet = record is SheetRecord
    val controllerStyle = if (isSheet) UIAlertControllerStyleActionSheet else UIAlertControllerStyleAlert
    val alert = UIAlertController.alertControllerWithTitle(record.title, record.message, controllerStyle)
    var handled = false

    val items = record.actionItems()
    items.forEach { (label, role, action) ->
        alert.addAction(
            UIAlertAction.actionWithTitle(label, role.toUIAlertActionStyle(), handler = { _ ->
                handled = true
                controller.onModalResult(record.id, action)
            }),
        )
    }
    // Guarantee the modal is always resolvable.
    val hasCancel = items.any { it.second == BrandAlertActionRole.Cancel }
    if (isSheet && !hasCancel) {
        alert.addAction(
            UIAlertAction.actionWithTitle("Cancel", UIAlertActionStyleCancel, handler = { _ ->
                handled = true
                controller.onModalResult(record.id, record.onCancel)
            }),
        )
    }
    if (!isSheet && items.isEmpty()) {
        alert.addAction(
            UIAlertAction.actionWithTitle("OK", UIAlertActionStyleDefault, handler = { _ ->
                handled = true
                controller.onModalResult(record.id, null)
            }),
        )
    }

    val dismissDelegate = ModalDismissDelegate {
        handled = true
        controller.onModalResult(record.id, record.onCancel)
    }
    retained += dismissDelegate

    val vc = topmostViewController()
    if (vc != null) {
        // iPad presents an action sheet as a popover, which UIKit asserts on unless its
        // popoverPresentationController has a source anchor. Anchor it (KVC; see helper) before presenting.
        if (isSheet) applyIPadPopoverAnchor(alert, vc)
        vc.presentViewController(alert, animated = true, completion = null)
        // Catch the interactive (iPad popover outside-tap) dismissal of an ACTION SHEET — set after presenting
        // so the presentation controller exists. ONLY for action sheets: UIKit asserts/crashes if you modify
        // the delegate of an alert (`.alert` style) presentation controller, and a `.alert` can't be
        // interactively dismissed anyway (it requires a button), so it never needs this.
        if (isSheet) alert.presentationController?.delegate = dismissDelegate
    }
    return {
        if (!handled) alert.dismissViewControllerAnimated(true, null)
        retained.clear() // release the (weakly-held) presentation delegate at end of life
    }
}

/**
 * Anchors an action sheet's `popoverPresentationController` for iPad. That property is **not bound** as a
 * static member in this Kotlin/Native UIKit version, so we reach it via KVC ([valueForKey]). UIKit only
 * creates a popover controller when it presents as a popover — i.e. **iPad action sheets**; on iPhone the
 * value is `nil` and this is a clean no-op, so **iPhone behavior is unchanged**. We anchor to the center
 * of the presenter's view and suppress the arrow (`permittedArrowDirections = 0`): there is no triggering
 * control to point at (the sheet is posted imperatively), so a centered, arrow-less popover reads better
 * than an arrow into empty space — and it satisfies UIKit's requirement so it doesn't raise. `sourceRect`
 * is boxed as an `NSValue` (KVC can't take a raw struct); the arrow mask is a plain boxed number.
 */
@OptIn(ExperimentalForeignApi::class)
private fun applyIPadPopoverAnchor(alert: UIAlertController, presenter: UIViewController) {
    val popover = alert.valueForKey("popoverPresentationController") as? NSObject ?: return
    val hostView: UIView = presenter.view
    popover.setValue(hostView, forKey = "sourceView")
    hostView.bounds.useContents {
        val rect = CGRectMake(x = size.width / 2.0, y = size.height / 2.0, width = 1.0, height = 1.0)
        popover.setValue(NSValue.valueWithCGRect(rect), forKey = "sourceRect")
    }
    popover.setValue(0, forKey = "permittedArrowDirections")
}

// endregion

// region branded custom overlay

/** Restricts the scrim's tap-to-cancel to taps OUTSIDE the card, so taps on the card/buttons pass through. */
@OptIn(BetaInteropApi::class)
private class ScrimTapDelegate(private val card: UIView) : NSObject(), UIGestureRecognizerDelegateProtocol {
    override fun gestureRecognizer(
        gestureRecognizer: UIGestureRecognizer,
        shouldReceiveTouch: UITouch,
    ): Boolean {
        val touched = shouldReceiveTouch.view
        return touched == null || !touched.isDescendantOfView(card)
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun presentBrandedModal(
    record: ModalRecord,
    style: ResolvedFeedbackStyle,
    primary: UIColor,
    error: UIColor,
    cancelColor: UIColor,
    controller: BrandFeedbackController,
): () -> Unit {
    val window = feedbackKeyWindow() ?: return {}
    val retained = mutableListOf<Any>()
    val isSheet = record is SheetRecord

    val dim = UIView()
    dim.backgroundColor = UIColor(white = 0.0, alpha = 0.4)
    dim.alpha = 0.0
    window.pinFilling(dim)

    val card = UIView()
    card.backgroundColor = style.background.toUIColor()
    card.layer.cornerRadius = 16.0
    card.clipsToBounds = true
    card.translatesAutoresizingMaskIntoConstraints = false
    card.accessibilityViewIsModal = true // trap VoiceOver inside the overlay while it's up
    dim.addSubview(card)

    // Tap the dimmed area to cancel — only for sheets (matching native action-sheet behavior; alerts
    // require an explicit choice). Routes to onCancel + advance via the id-guarded onModalResult.
    if (isSheet) {
        val scrimDelegate = ScrimTapDelegate(card)
        val scrimTap = ButtonTapHandler().apply {
            onClick = { controller.onModalResult(record.id, record.onCancel) }
        }
        val tapGesture = UITapGestureRecognizer(target = scrimTap, action = sel_registerName("handleTap"))
        tapGesture.delegate = scrimDelegate
        dim.addGestureRecognizer(tapGesture)
        retained += scrimTap
        retained += scrimDelegate
    }

    val guide = window.safeAreaLayoutGuide
    val cardCs = mutableListOf<NSLayoutConstraint>()
    if (isSheet) {
        cardCs += card.leadingAnchor.constraintEqualToAnchor(guide.leadingAnchor, 12.0)
        cardCs += card.trailingAnchor.constraintEqualToAnchor(guide.trailingAnchor, -12.0)
        cardCs += card.bottomAnchor.constraintEqualToAnchor(guide.bottomAnchor, -12.0)
    } else {
        cardCs += card.centerXAnchor.constraintEqualToAnchor(dim.centerXAnchor)
        cardCs += card.centerYAnchor.constraintEqualToAnchor(dim.centerYAnchor)
        cardCs += card.leadingAnchor.constraintGreaterThanOrEqualToAnchor(guide.leadingAnchor, 24.0)
        cardCs += card.trailingAnchor.constraintLessThanOrEqualToAnchor(guide.trailingAnchor, -24.0)
        cardCs += card.widthAnchor.constraintLessThanOrEqualToConstant(420.0)
    }
    NSLayoutConstraint.activateConstraints(cardCs)

    val views = mutableListOf<UIView>()
    record.title?.let { views += brandedLabel(it, style.titleTextStyle.toUIFont(), style.content.toUIColor()) }
    record.message?.let { views += brandedLabel(it, style.textStyle.toUIFont(), style.content.toUIColor()) }

    val items = record.actionItems().toMutableList()
    if (isSheet && items.none { it.second == BrandAlertActionRole.Cancel }) {
        items += Triple("Cancel", BrandAlertActionRole.Cancel, record.onCancel ?: {})
    }
    if (!isSheet && items.isEmpty()) {
        items += Triple<String, BrandAlertActionRole, () -> Unit>("OK", BrandAlertActionRole.Default, {})
    }
    items.forEach { (label, role, action) ->
        val color = when (role) {
            BrandAlertActionRole.Destructive -> error
            BrandAlertActionRole.Cancel -> cancelColor
            BrandAlertActionRole.Default -> primary
        }
        val button = UIButton()
        button.setTitle(label, forState = UIControlStateNormal)
        button.setTitleColor(color, forState = UIControlStateNormal)
        button.titleLabel?.font = UIFont.boldSystemFontOfSize(16.0)
        button.translatesAutoresizingMaskIntoConstraints = false
        val handler = ButtonTapHandler().apply { onClick = { controller.onModalResult(record.id, action) } }
        button.addTarget(handler, sel_registerName("handleTap"), UIControlEventTouchUpInside)
        retained += handler
        views += button
    }

    views.forEach { card.addSubview(it) }
    val cs = mutableListOf<NSLayoutConstraint>()
    var top = card.topAnchor
    var topConst = 20.0
    views.forEachIndexed { index, view ->
        cs += view.leadingAnchor.constraintEqualToAnchor(card.leadingAnchor, 20.0)
        cs += view.trailingAnchor.constraintEqualToAnchor(card.trailingAnchor, -20.0)
        cs += view.topAnchor.constraintEqualToAnchor(top, topConst)
        if (view is UIButton) cs += view.heightAnchor.constraintEqualToConstant(44.0)
        if (index == views.lastIndex) cs += view.bottomAnchor.constraintEqualToAnchor(card.bottomAnchor, -16.0)
        top = view.bottomAnchor
        topConst = 8.0
    }
    NSLayoutConstraint.activateConstraints(cs)

    UIView.animateWithDuration(0.2, animations = { dim.alpha = 1.0 })
    // Move VoiceOver focus into the overlay (it does not auto-focus a view we add imperatively).
    UIAccessibilityPostNotification(UIAccessibilityScreenChangedNotification, card)
    return {
        UIView.animateWithDuration(0.2, animations = { dim.alpha = 0.0 }, completion = { _ -> dim.removeFromSuperview() })
        retained.clear()
    }
}

private fun brandedLabel(text: String, font: UIFont, color: UIColor): UILabel {
    val l = UILabel()
    l.text = text
    l.font = font
    l.textColor = color
    l.numberOfLines = 0
    l.textAlignment = NSTextAlignmentCenter
    l.translatesAutoresizingMaskIntoConstraints = false
    return l
}

// endregion
