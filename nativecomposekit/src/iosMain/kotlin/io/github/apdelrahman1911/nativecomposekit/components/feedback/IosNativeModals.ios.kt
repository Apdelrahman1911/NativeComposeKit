package io.github.apdelrahman1911.nativecomposekit.components.feedback

import io.github.apdelrahman1911.nativecomposekit.components.ButtonTapHandler
import io.github.apdelrahman1911.nativecomposekit.components.model.ResolvedFeedbackStyle
import io.github.apdelrahman1911.nativecomposekit.components.pinFilling
import io.github.apdelrahman1911.nativecomposekit.components.toUIColor
import io.github.apdelrahman1911.nativecomposekit.components.toUIFont
import io.github.apdelrahman1911.nativecomposekit.theme.NativeStrings
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
import platform.UIKit.UIImage
import platform.UIKit.UIImageView
import platform.UIKit.UILabel
import platform.UIKit.UIPresentationController
import platform.UIKit.UITapGestureRecognizer
import platform.UIKit.UITouch
import platform.UIKit.UIView
import platform.UIKit.UIViewContentMode
import platform.UIKit.UIViewController
import platform.UIKit.accessibilityViewIsModal
import platform.UIKit.presentationController
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import platform.objc.sel_registerName

/**
 * True while a native modal's code-driven `dismissViewControllerAnimated` is in flight on this main-queue
 * tick. UIKit rejects a present issued while its presenter is still mid-dismissal, so the NEXT present
 * consumes this flag and defers itself by one tick (`dispatch_async`). Main-thread only, like everything
 * else in this file.
 */
private var nativeModalCodeDismissInFlight = false

/**
 * Presents the modal [record], returning a dismisser the host runs on dispose. Native by default (real
 * `UIAlertController`); the branded custom overlay when the record opted in. [primary]/[error]/
 * [cancelColor] are theme colors (resolved in composition) used by the branded button styling.
 *
 * If a previous native modal was just dismissed **by code** (not by tapping an action), the present is
 * deferred to the next main-queue tick so UIKit never sees a present-during-dismiss (which it drops,
 * leaving the lane's active record invisible). The returned dismisser stays valid either way: it cancels a
 * still-pending present, or tears down the presented modal.
 */
@OptIn(ExperimentalForeignApi::class)
internal fun presentModal(
    record: ModalRecord,
    cardStyle: ResolvedFeedbackStyle,
    primary: UIColor,
    error: UIColor,
    cancelColor: UIColor,
    strings: NativeStrings,
    controller: NativeFeedbackController,
): () -> Unit {
    val branded = when (record) {
        is AlertRecord -> record.iosPresentation == NativePresentation.Branded
        is SheetRecord -> record.iosPresentation == NativePresentation.Branded
    }
    val present: () -> (() -> Unit) = {
        if (branded) {
            presentBrandedModal(record, cardStyle, primary, error, cancelColor, strings, controller)
        } else {
            presentNativeModal(record, strings, controller)
        }
    }
    if (!nativeModalCodeDismissInFlight) return present()
    nativeModalCodeDismissInFlight = false
    var cancelled = false
    var dismisser: (() -> Unit)? = null
    dispatch_async(dispatch_get_main_queue()) {
        if (!cancelled) dismisser = present()
    }
    return {
        cancelled = true
        dismisser?.invoke()
    }
}

/**
 * The topmost presented view controller to present from (walks the presentation chain). The walk stops
 * below any controller that is animating out — presenting from (or on top of) a `beingDismissed` VC is the
 * present-during-dismiss UIKit rejects — so the presenter is always the stable controller underneath.
 */
@OptIn(ExperimentalForeignApi::class)
internal fun topmostViewController(): UIViewController? {
    val window = feedbackKeyWindow() ?: return null
    var vc: UIViewController? = window.rootViewController
    while (true) {
        val presented = vc?.presentedViewController ?: break
        if (presented.beingDismissed) break
        vc = presented
    }
    return vc
}

/** One resolved action row: label/role/handler, plus the Branded path's optional leading SF Symbol. */
private class ModalActionItem(
    val label: String,
    val role: NativeAlertActionRole,
    val sfSymbol: String?,
    val onClick: () -> Unit,
)

private fun ModalRecord.actionItems(): List<ModalActionItem> = when (this) {
    is AlertRecord -> actions.map { ModalActionItem(it.label, it.role, sfSymbol = null, it.onClick) }
    is SheetRecord -> actions.map { ModalActionItem(it.label, it.role, it.icon?.sfSymbolName, it.onClick) }
}

private fun NativeAlertActionRole.toUIAlertActionStyle(): UIAlertActionStyle = when (this) {
    NativeAlertActionRole.Default -> UIAlertActionStyleDefault
    NativeAlertActionRole.Cancel -> UIAlertActionStyleCancel
    NativeAlertActionRole.Destructive -> UIAlertActionStyleDestructive
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
private fun presentNativeModal(
    record: ModalRecord,
    strings: NativeStrings,
    controller: NativeFeedbackController,
): () -> Unit {
    val retained = mutableListOf<Any>()
    val isSheet = record is SheetRecord
    val controllerStyle = if (isSheet) UIAlertControllerStyleActionSheet else UIAlertControllerStyleAlert
    val alert = UIAlertController.alertControllerWithTitle(record.title, record.message, controllerStyle)
    var handled = false

    val items = record.actionItems()
    items.forEach { item ->
        alert.addAction(
            UIAlertAction.actionWithTitle(item.label, item.role.toUIAlertActionStyle(), handler = { _ ->
                handled = true
                controller.onModalResult(record.id, item.onClick)
            }),
        )
    }
    // Guarantee the modal is always resolvable.
    val hasCancel = items.any { it.role == NativeAlertActionRole.Cancel }
    if (isSheet && !hasCancel) {
        alert.addAction(
            UIAlertAction.actionWithTitle(strings.alertCancel, UIAlertActionStyleCancel, handler = { _ ->
                handled = true
                controller.onModalResult(record.id, record.onCancel)
            }),
        )
    }
    if (!isSheet && items.isEmpty()) {
        alert.addAction(
            UIAlertAction.actionWithTitle(strings.alertOk, UIAlertActionStyleDefault, handler = { _ ->
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
    } else {
        // No presenter resolves (no window/root yet): the modal can never be shown, so deliver its cancel
        // right away and let the lane advance — otherwise the record stays active forever with nothing on
        // screen to resolve it. `handled` makes this exactly-once: the dispose-time dismisser below must
        // not touch the never-presented alert.
        handled = true
        controller.onModalResult(record.id, record.onCancel)
    }
    return {
        if (!handled) {
            // Dismissed by code (dismiss()/clearAll()), not by a chosen action: flag the in-flight teardown
            // so the next queued present defers past it (see presentModal).
            nativeModalCodeDismissInFlight = true
            alert.dismissViewControllerAnimated(true, null)
        }
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
    strings: NativeStrings,
    controller: NativeFeedbackController,
): () -> Unit {
    val window = feedbackKeyWindow()
    if (window == null) {
        // No window to attach to: deliver the cancel immediately so the modal lane advances — a silently
        // skipped overlay would otherwise hold the lane forever. (The controller's id guard keeps this
        // exactly-once against any later stale dismiss.)
        controller.onModalResult(record.id, record.onCancel)
        return {}
    }
    val retained = mutableListOf<Any>()
    val isSheet = record is SheetRecord

    val dim = UIView()
    dim.backgroundColor = UIColor(white = 0.0, alpha = 0.4)
    dim.alpha = 0.0
    // VoiceOver honors accessibilityViewIsModal only by excluding the flagged view's SIBLINGS. The dim
    // scrim is the window subview whose siblings are the app content, so the trap goes here — on the card
    // (an only child) the flag excludes nothing and focus escapes into the background UI.
    dim.accessibilityViewIsModal = true
    window.pinFilling(dim)

    val card = UIView()
    card.backgroundColor = style.background.toUIColor()
    card.layer.cornerRadius = 16.0
    card.clipsToBounds = true
    card.translatesAutoresizingMaskIntoConstraints = false
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
    if (isSheet && items.none { it.role == NativeAlertActionRole.Cancel }) {
        items += ModalActionItem(strings.alertCancel, NativeAlertActionRole.Cancel, sfSymbol = null, record.onCancel ?: {})
    }
    if (!isSheet && items.isEmpty()) {
        items += ModalActionItem(strings.alertOk, NativeAlertActionRole.Default, sfSymbol = null, {})
    }
    items.forEach { item ->
        val color = when (item.role) {
            NativeAlertActionRole.Destructive -> error
            NativeAlertActionRole.Cancel -> cancelColor
            NativeAlertActionRole.Default -> primary
        }
        val button = UIButton()
        button.setTitle(item.label, forState = UIControlStateNormal)
        button.setTitleColor(color, forState = UIControlStateNormal)
        button.titleLabel?.font = UIFont.boldSystemFontOfSize(16.0)
        button.translatesAutoresizingMaskIntoConstraints = false
        // Leading SF Symbol glyph (the Branded half of NativeConfirmationAction.icon's contract; the Native
        // system alert has no public action-image API). Hung off the title label so the centered title
        // keeps its position; tinted with the row's text color, so destructive rows stay red. Decorative
        // for VoiceOver — the button title already carries the label.
        item.sfSymbol?.let { symbol ->
            val glyphImage = UIImage.systemImageNamed(symbol)
            val titleLabel = button.titleLabel
            if (glyphImage != null && titleLabel != null) {
                val glyph = UIImageView()
                glyph.image = glyphImage
                glyph.tintColor = color
                glyph.contentMode = UIViewContentMode.UIViewContentModeScaleAspectFit
                glyph.translatesAutoresizingMaskIntoConstraints = false
                button.addSubview(glyph)
                NSLayoutConstraint.activateConstraints(
                    listOf(
                        glyph.centerYAnchor.constraintEqualToAnchor(button.centerYAnchor),
                        glyph.trailingAnchor.constraintEqualToAnchor(titleLabel.leadingAnchor, -8.0),
                        glyph.widthAnchor.constraintEqualToConstant(20.0),
                        glyph.heightAnchor.constraintEqualToConstant(20.0),
                    ),
                )
            }
        }
        val handler = ButtonTapHandler().apply { onClick = { controller.onModalResult(record.id, item.onClick) } }
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
