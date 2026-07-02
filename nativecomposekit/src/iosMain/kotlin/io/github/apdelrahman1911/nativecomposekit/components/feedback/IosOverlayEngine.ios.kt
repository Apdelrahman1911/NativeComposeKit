package io.github.apdelrahman1911.nativecomposekit.components.feedback

import io.github.apdelrahman1911.nativecomposekit.components.ButtonTapHandler
import io.github.apdelrahman1911.nativecomposekit.components.model.ResolvedFeedbackStyle
import io.github.apdelrahman1911.nativecomposekit.components.toUIColor
import io.github.apdelrahman1911.nativecomposekit.components.toUIFont
import io.github.apdelrahman1911.nativecomposekit.theme.NativeStrings
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGAffineTransformMakeTranslation
import platform.Foundation.NSTimer
import platform.UIKit.NSLayoutConstraint
import platform.UIKit.NSLayoutXAxisAnchor
import platform.UIKit.NSLayoutYAxisAnchor
import platform.UIKit.NSTextAlignmentCenter
import platform.UIKit.NSTextAlignmentLeft
import platform.UIKit.UIAccessibilityAnnouncementNotification
import platform.UIKit.UIAccessibilityPostNotification
import platform.UIKit.UIApplication
import platform.UIKit.UIButton
import platform.UIKit.UIColor
import platform.UIKit.UIControlContentHorizontalAlignmentLeading
import platform.UIKit.UIControlEventTouchUpInside
import platform.UIKit.UIControlStateNormal
import platform.UIKit.UIFont
import platform.UIKit.UIGestureRecognizerStateCancelled
import platform.UIKit.UIGestureRecognizerStateChanged
import platform.UIKit.UIGestureRecognizerStateEnded
import platform.UIKit.UIImage
import platform.UIKit.UIImageView
import platform.UIKit.UILabel
import platform.UIKit.UIPanGestureRecognizer
import platform.UIKit.UIView
import platform.UIKit.UIViewContentMode
import platform.UIKit.UIWindow
import platform.UIKit.accessibilityLabel
import platform.UIKit.keyboardLayoutGuide
import platform.darwin.NSObject
import platform.objc.sel_registerName

/** The key window we attach overlays to (same fallback as the original toaster). */
@OptIn(ExperimentalForeignApi::class)
internal fun feedbackKeyWindow(): UIWindow? {
    val app = UIApplication.sharedApplication
    return app.keyWindow ?: (app.windows.firstOrNull() as? UIWindow)
}

/** Default SF Symbol per status for the native overlays (banners). */
internal fun NativeFeedbackStatus.defaultSfSymbol(): String = when (this) {
    NativeFeedbackStatus.Info -> "info.circle.fill"
    NativeFeedbackStatus.Success -> "checkmark.circle.fill"
    NativeFeedbackStatus.Warning -> "exclamationmark.triangle.fill"
    NativeFeedbackStatus.Error -> "xmark.octagon.fill"
}

/**
 * A live key-window overlay (toast HUD / snackbar / banner). Holds the container view, an optional
 * auto-dismiss [NSTimer], and any target-action handlers that must stay retained for the overlay's
 * lifetime (UIControl targets are NOT retained by UIKit). [dismiss] is idempotent.
 */
@OptIn(ExperimentalForeignApi::class)
internal class IosOverlayHandle(
    private val container: UIView,
    private var timer: NSTimer?,
    @Suppress("unused") private val retained: List<Any>,
) {
    private var dismissed = false

    fun dismiss() {
        if (dismissed) return
        dismissed = true
        timer?.invalidate()
        timer = null
        UIView.animateWithDuration(
            duration = 0.22,
            animations = { container.alpha = 0.0 },
            completion = { _ -> container.removeFromSuperview() },
        )
    }
}

/** Swipe distance (pt) / velocity (pt/s) past which a drag dismisses the overlay instead of snapping back. */
private const val SWIPE_DISTANCE = 56.0
private const val SWIPE_VELOCITY = 800.0

/**
 * Drives swipe-to-dismiss for a key-window overlay [container]. Follows the finger toward the dismiss
 * edge ([dismissUp] = swipe up for a top banner; otherwise swipe down), rubber-banding the opposite way.
 * On release, a drag past [SWIPE_DISTANCE] or flung past [SWIPE_VELOCITY] animates the container off-screen
 * and calls [onDismiss] (which advances the queue → the host's dispose tears the view down); otherwise it
 * snaps back. Retained for the overlay's lifetime (UIKit does not retain gesture targets).
 */
@OptIn(BetaInteropApi::class, ExperimentalForeignApi::class)
private class PanDismissHandler(
    private val container: UIView,
    private val dismissUp: Boolean,
    private val onDismiss: () -> Unit,
) : NSObject() {
    @ObjCAction
    fun onPan(recognizer: UIPanGestureRecognizer) {
        val ty = recognizer.translationInView(container).useContents { y }
        when (recognizer.state) {
            UIGestureRecognizerStateChanged -> {
                val dy = if (dismissUp) minOf(ty, 8.0) else maxOf(ty, -8.0)
                container.transform = CGAffineTransformMakeTranslation(0.0, dy)
            }
            UIGestureRecognizerStateEnded, UIGestureRecognizerStateCancelled -> {
                val vy = recognizer.velocityInView(container).useContents { y }
                val passed = if (dismissUp) ty < -SWIPE_DISTANCE || vy < -SWIPE_VELOCITY
                else ty > SWIPE_DISTANCE || vy > SWIPE_VELOCITY
                if (passed) {
                    // Hand off to the controller immediately. The host's DisposableEffect then runs
                    // IosOverlayHandle.dismiss() as the SINGLE owner of the exit animation (fade + remove),
                    // so two animators never fight over the same view. The view fades from its dragged spot.
                    onDismiss()
                } else {
                    UIView.animateWithDuration(0.2, animations = {
                        container.transform = CGAffineTransformMakeTranslation(0.0, 0.0)
                    })
                }
            }
            else -> {}
        }
    }
}

/**
 * Builds and presents the key-window overlay for a transient [record], themed by [style], and wires its
 * timer/buttons back to [controller]. [strings] localizes the kit-rendered affordances (the close button's
 * accessibility label) — resolved by the composable host, since composition locals aren't readable here.
 * Returns a handle whose [IosOverlayHandle.dismiss] the host calls on dispose. Visual teardown always
 * happens via dispose → dismiss; the timer/buttons only notify the controller (which then clears state and
 * triggers the dispose).
 */
@OptIn(ExperimentalForeignApi::class)
internal fun presentTransient(
    record: TransientRecord,
    style: ResolvedFeedbackStyle,
    strings: NativeStrings,
    controller: NativeFeedbackController,
): IosOverlayHandle? {
    val window = feedbackKeyWindow()
    if (window == null) {
        // Nothing to attach to (posted before the key window exists / while backgrounded). Resolve the
        // record through the same completion path its timer would use — with no view and no timer it would
        // otherwise stay active forever, wedging every later transient behind it.
        controller.onTransientTimeout(record.id)
        return null
    }
    val retained = mutableListOf<Any>()

    val container: UIView
    val position: NativeFeedbackPosition
    val fullWidth: Boolean
    when (record) {
        is ToastRecord -> {
            container = buildPill(style, record.message)
            position = record.position
            fullWidth = false
        }
        is SnackbarRecord -> {
            container = buildSnackbar(style, record.message, record.actionLabel, retained) {
                controller.onTransientAction(record.id)
            }
            position = NativeFeedbackPosition.Bottom
            fullWidth = true
        }
        is BannerRecord -> {
            container = buildBanner(
                style = style,
                sfSymbol = record.status.defaultSfSymbol(),
                title = record.title,
                message = record.message,
                actionLabel = record.actionLabel,
                dismissible = record.dismissible,
                dismissLabel = strings.dismiss,
                retained = retained,
                onAction = { controller.onTransientAction(record.id) },
                onClose = { controller.dismiss(record.id) },
            )
            position = record.position
            fullWidth = true
        }
    }

    pinOverlay(window, container, position, fullWidth)
    UIView.animateWithDuration(0.22, animations = { container.alpha = 1.0 })

    // VoiceOver does not auto-announce an overlay we add imperatively to the key window, so speak it.
    UIAccessibilityPostNotification(UIAccessibilityAnnouncementNotification, transientAnnouncement(record))

    // Swipe-to-dismiss (banners + snackbars). A banner dismisses toward its pinned edge and routes to
    // dismiss(id) (same path as the close button → runs onDismiss); a snackbar swipes down and routes to
    // onTransientTimeout(id) (a plain dismiss — no action). The toast HUD is not swipeable (too small).
    var dismissUp = false
    var onSwipeDismiss: (() -> Unit)? = null
    when (record) {
        is BannerRecord -> if (record.swipeToDismiss) {
            dismissUp = record.position == NativeFeedbackPosition.Top
            onSwipeDismiss = { controller.dismiss(record.id) }
        }
        is SnackbarRecord -> if (record.swipeToDismiss) {
            dismissUp = false
            onSwipeDismiss = { controller.onTransientTimeout(record.id) }
        }
        is ToastRecord -> {}
    }
    onSwipeDismiss?.let { dismiss ->
        val panHandler = PanDismissHandler(container, dismissUp, dismiss)
        container.addGestureRecognizer(
            UIPanGestureRecognizer(target = panHandler, action = sel_registerName("onPan:")),
        )
        retained += panHandler
    }

    val ms = record.duration.toMillisOrNull()
    val timer = if (ms != null) {
        NSTimer.scheduledTimerWithTimeInterval(ms / 1000.0, repeats = false) { _ ->
            controller.onTransientTimeout(record.id)
        }
    } else {
        null
    }
    return IosOverlayHandle(container, timer, retained)
}

/** The text VoiceOver should speak when [record] appears (banner reads title + message). */
private fun transientAnnouncement(record: TransientRecord): String = when (record) {
    is ToastRecord -> record.message
    is SnackbarRecord -> record.message
    is BannerRecord -> listOfNotNull(record.title, record.message).joinToString(". ")
}

// region builders

@OptIn(ExperimentalForeignApi::class)
private fun fbCard(style: ResolvedFeedbackStyle): UIView {
    val card = UIView()
    card.backgroundColor = style.background.toUIColor()
    card.layer.cornerRadius = style.cornerRadius.value.toDouble()
    card.clipsToBounds = true
    card.alpha = 0.0
    card.translatesAutoresizingMaskIntoConstraints = false
    return card
}

private fun fbLabel(text: String, font: UIFont, color: UIColor): UILabel {
    val l = UILabel()
    l.text = text
    l.font = font
    l.textColor = color
    l.numberOfLines = 0
    l.textAlignment = NSTextAlignmentLeft
    l.translatesAutoresizingMaskIntoConstraints = false
    return l
}

@OptIn(ExperimentalForeignApi::class)
private fun fbIcon(symbol: String, tint: UIColor): UIImageView {
    val iv = UIImageView()
    iv.image = UIImage.systemImageNamed(symbol)
    iv.tintColor = tint
    iv.contentMode = UIViewContentMode.UIViewContentModeScaleAspectFit
    iv.translatesAutoresizingMaskIntoConstraints = false
    return iv
}

private fun fbTextButton(title: String, color: UIColor): UIButton {
    val b = UIButton()
    b.setTitle(title, forState = UIControlStateNormal)
    b.setTitleColor(color, forState = UIControlStateNormal)
    b.titleLabel?.font = UIFont.boldSystemFontOfSize(15.0)
    b.translatesAutoresizingMaskIntoConstraints = false
    // A bare text button's intrinsic height (~20pt) is far below the 44pt HIG touch minimum; the title stays
    // centered, so the extra height is pure hit area.
    NSLayoutConstraint.activateConstraints(
        listOf(b.heightAnchor.constraintGreaterThanOrEqualToConstant(44.0)),
    )
    return b
}

/** Centered HUD pill (label only) — the toast look. */
@OptIn(ExperimentalForeignApi::class)
private fun buildPill(style: ResolvedFeedbackStyle, message: String): UIView {
    val card = fbCard(style)
    val label = fbLabel(message, style.textStyle.toUIFont(), style.content.toUIColor())
    label.textAlignment = NSTextAlignmentCenter
    card.addSubview(label)
    NSLayoutConstraint.activateConstraints(
        listOf(
            label.topAnchor.constraintEqualToAnchor(card.topAnchor, 11.0),
            label.bottomAnchor.constraintEqualToAnchor(card.bottomAnchor, -11.0),
            label.leadingAnchor.constraintEqualToAnchor(card.leadingAnchor, 18.0),
            label.trailingAnchor.constraintEqualToAnchor(card.trailingAnchor, -18.0),
        ),
    )
    return card
}

/** Bottom snackbar: message + optional trailing action button. */
@OptIn(ExperimentalForeignApi::class)
private fun buildSnackbar(
    style: ResolvedFeedbackStyle,
    message: String,
    actionLabel: String?,
    retained: MutableList<Any>,
    onAction: () -> Unit,
): UIView {
    val card = fbCard(style)
    val label = fbLabel(message, style.textStyle.toUIFont(), style.content.toUIColor())
    card.addSubview(label)

    val cs = mutableListOf<NSLayoutConstraint>(
        label.topAnchor.constraintEqualToAnchor(card.topAnchor, 12.0),
        label.bottomAnchor.constraintEqualToAnchor(card.bottomAnchor, -12.0),
        label.leadingAnchor.constraintEqualToAnchor(card.leadingAnchor, 16.0),
    )
    if (actionLabel != null) {
        val button = fbTextButton(actionLabel, style.iconTint.toUIColor())
        val handler = ButtonTapHandler().apply { onClick = onAction }
        button.addTarget(handler, sel_registerName("handleTap"), UIControlEventTouchUpInside)
        retained += handler
        card.addSubview(button)
        cs += button.centerYAnchor.constraintEqualToAnchor(card.centerYAnchor)
        cs += button.trailingAnchor.constraintEqualToAnchor(card.trailingAnchor, -16.0)
        cs += label.trailingAnchor.constraintLessThanOrEqualToAnchor(button.leadingAnchor, -12.0)
    } else {
        cs += label.trailingAnchor.constraintEqualToAnchor(card.trailingAnchor, -16.0)
    }
    NSLayoutConstraint.activateConstraints(cs)
    return card
}

/** Top/bottom banner: leading status icon, title + message column, optional action, optional close. */
@OptIn(ExperimentalForeignApi::class)
private fun buildBanner(
    style: ResolvedFeedbackStyle,
    sfSymbol: String,
    title: String?,
    message: String,
    actionLabel: String?,
    dismissible: Boolean,
    dismissLabel: String,
    retained: MutableList<Any>,
    onAction: () -> Unit,
    onClose: () -> Unit,
): UIView {
    val card = fbCard(style)
    val content = style.content.toUIColor()

    val icon = fbIcon(sfSymbol, style.iconTint.toUIColor())
    card.addSubview(icon)

    val close: UIButton? = if (dismissible) {
        val b = UIButton()
        b.setImage(UIImage.systemImageNamed("xmark"), forState = UIControlStateNormal)
        b.tintColor = content
        b.translatesAutoresizingMaskIntoConstraints = false
        // The image-only button has no title for VoiceOver to speak — label it with the localized dismiss.
        b.accessibilityLabel = dismissLabel
        val handler = ButtonTapHandler().apply { onClick = onClose }
        b.addTarget(handler, sel_registerName("handleTap"), UIControlEventTouchUpInside)
        retained += handler
        card.addSubview(b)
        b
    } else {
        null
    }

    // Vertical text column (title? / message / action?) laid out manually between icon and close.
    val column = mutableListOf<UIView>()
    if (title != null) column += fbLabel(title, style.titleTextStyle.toUIFont(), content)
    column += fbLabel(message, style.textStyle.toUIFont(), content)
    if (actionLabel != null) {
        val button = fbTextButton(actionLabel, style.iconTint.toUIColor())
        button.contentHorizontalAlignment = UIControlContentHorizontalAlignmentLeading
        val handler = ButtonTapHandler().apply { onClick = onAction }
        button.addTarget(handler, sel_registerName("handleTap"), UIControlEventTouchUpInside)
        retained += handler
        column += button
    }
    column.forEach { card.addSubview(it) }

    val cs = mutableListOf<NSLayoutConstraint>(
        icon.leadingAnchor.constraintEqualToAnchor(card.leadingAnchor, 16.0),
        icon.topAnchor.constraintEqualToAnchor(card.topAnchor, 14.0),
        icon.widthAnchor.constraintEqualToConstant(22.0),
        icon.heightAnchor.constraintEqualToConstant(22.0),
    )
    val textLeading: NSLayoutXAxisAnchor = icon.trailingAnchor
    val textTrailing: NSLayoutXAxisAnchor = close?.leadingAnchor ?: card.trailingAnchor
    // The 44pt close button carries ~10pt of empty hit area inside each edge, so the text can run right up
    // to its leading edge and still keep the same visual gap to the glyph.
    val textTrailingConst = if (close != null) 0.0 else -16.0
    close?.let {
        // ≥44pt touch target (HIG minimum) around the unchanged ~24pt glyph box: the button grows by 10pt
        // per side and the edge constants shrink by the same 10pt, keeping the glyph's visual spot.
        cs += it.trailingAnchor.constraintEqualToAnchor(card.trailingAnchor, -2.0)
        cs += it.topAnchor.constraintEqualToAnchor(card.topAnchor, 2.0)
        cs += it.widthAnchor.constraintEqualToConstant(44.0)
        cs += it.heightAnchor.constraintEqualToConstant(44.0)
        // The card clips to bounds, so a one-line banner must grow to keep the whole target tappable
        // (labels stretch a few points — soft hugging — rather than the hit area being clipped away).
        cs += it.bottomAnchor.constraintLessThanOrEqualToAnchor(card.bottomAnchor, -2.0)
    }

    var top: NSLayoutYAxisAnchor = card.topAnchor
    var topConst = 12.0
    column.forEachIndexed { index, view ->
        cs += view.leadingAnchor.constraintEqualToAnchor(textLeading, 12.0)
        cs += view.trailingAnchor.constraintEqualToAnchor(textTrailing, textTrailingConst)
        cs += view.topAnchor.constraintEqualToAnchor(top, topConst)
        if (index == column.lastIndex) {
            cs += view.bottomAnchor.constraintEqualToAnchor(card.bottomAnchor, -12.0)
        }
        top = view.bottomAnchor
        topConst = 3.0
    }
    NSLayoutConstraint.activateConstraints(cs)
    return card
}

/** Pins an overlay [container] to the [window] safe area at [position]; [fullWidth] pins both edges. */
@OptIn(ExperimentalForeignApi::class)
private fun pinOverlay(
    window: UIWindow,
    container: UIView,
    position: NativeFeedbackPosition,
    fullWidth: Boolean,
) {
    window.addSubview(container)
    val guide = window.safeAreaLayoutGuide
    val cs = mutableListOf<NSLayoutConstraint>(
        container.centerXAnchor.constraintEqualToAnchor(window.centerXAnchor),
    )
    if (fullWidth) {
        // Safe-area sides, not window edges: on notched devices in landscape the window edges sit under
        // the sensor housing / home-indicator corners and would clip the banner's leading icon or close.
        cs += container.leadingAnchor.constraintEqualToAnchor(guide.leadingAnchor, 12.0)
        cs += container.trailingAnchor.constraintEqualToAnchor(guide.trailingAnchor, -12.0)
    } else {
        cs += container.leadingAnchor.constraintGreaterThanOrEqualToAnchor(window.leadingAnchor, 16.0)
        cs += container.trailingAnchor.constraintLessThanOrEqualToAnchor(window.trailingAnchor, -16.0)
        cs += container.widthAnchor.constraintLessThanOrEqualToConstant(480.0)
    }
    when (position) {
        NativeFeedbackPosition.Top -> cs += container.topAnchor.constraintEqualToAnchor(guide.topAnchor, 12.0)
        NativeFeedbackPosition.Bottom ->
            // The keyboard layout guide (iOS 15+ deployment target) tracks the keyboard's top and collapses
            // to the safe-area bottom when it's offscreen — so a bottom transient rides above an open
            // keyboard instead of being covered by it, and sits exactly where it used to otherwise.
            cs += container.bottomAnchor.constraintEqualToAnchor(window.keyboardLayoutGuide.topAnchor, -24.0)
    }
    NSLayoutConstraint.activateConstraints(cs)
}

// endregion
