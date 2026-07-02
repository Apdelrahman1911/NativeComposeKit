package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.foundation.focusGroup
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics

/**
 * Small focus / IME / accessibility helpers — the building blocks the kit's own components use, exposed for
 * app screens. All Compose-drawn, platform-neutral.
 */

/**
 * Tapping anywhere on this element clears focus, **dismissing the soft keyboard**. Apply to a form screen's
 * scrollable root / background so a tap outside any field hides the keyboard (the common iOS/Android idiom).
 * `@Composable` because it reads the focus manager from composition.
 *
 * Works for the kit's **native iOS fields** too: clearing Compose focus alone cannot resign a
 * `UITextField`/`UITextView` first responder (their focus lives in UIKit, outside Compose's focus system),
 * so the tap additionally ends editing on the key window there.
 *
 * `Column(Modifier.fillMaxSize().nativeDismissKeyboardOnTap()) { NativeTextField(...) }`
 */
@Composable
public fun Modifier.nativeDismissKeyboardOnTap(): Modifier {
    val focusManager = LocalFocusManager.current
    return this.pointerInput(Unit) {
        detectTapGestures(onTap = {
            focusManager.clearFocus()
            platformEndEditing()
        })
    }
}

/** iOS: resign the native first responder (kit fields are UIKit-focused). Android: no-op (Compose owns focus). */
internal expect fun platformEndEditing()

/**
 * Marks this node as a **heading** for screen-reader heading/rotor navigation (VoiceOver, TalkBack). Apply to
 * a section title so assistive tech can jump between sections.
 *
 * `NativeText("Settings", modifier = Modifier.nativeHeading())`
 */
public fun Modifier.nativeHeading(): Modifier = this.semantics { heading() }

/**
 * Request focus for this element when it first enters composition (while [enabled]) — e.g. focus the first field
 * when a dialog or screen opens. Apply to a focusable element (a `NativeTextField`, or any element made focusable).
 * `@Composable` because it remembers a `FocusRequester` and runs a one-shot effect.
 *
 * `NativeTextField(value, onValueChange, modifier = Modifier.nativeAutoFocus())`
 *
 * **Platform note — this drives Compose focus.** On Android a `NativeTextField` is a Compose text field, so it
 * takes focus and the soft keyboard opens. On iOS the field hosts a native `UITextField`/`UITextView` whose focus
 * is system-driven (a tap → `becomeFirstResponder`) and lives outside Compose's focus system, so this does not
 * raise the iOS keyboard for a native field — iOS users focus native fields by tapping. It still moves focus
 * among Compose-drawn focusables on both platforms. The same applies to [rememberNativeFocusHandle],
 * [Modifier.nativeFocusOrder], and [Modifier.nativeFocusGroup].
 */
@Composable
public fun Modifier.nativeAutoFocus(enabled: Boolean = true): Modifier {
    val requester = remember { FocusRequester() }
    LaunchedEffect(enabled) {
        if (enabled) requester.requestFocus()
    }
    return this.focusRequester(requester)
}

/**
 * A handle to imperatively move focus to (or release it from) a target element from callbacks — e.g. advance to
 * the next field on submit. Obtain with [rememberNativeFocusHandle], attach with [Modifier.nativeFocusTarget],
 * then call [requestFocus]. Chain handles with [Modifier.nativeFocusOrder] to define traversal order.
 */
@Stable
public class NativeFocusHandle internal constructor() {
    internal val requester: FocusRequester = FocusRequester()

    /** Move focus to the attached element (on Android, opens the keyboard for a `NativeTextField`; on iOS the
     * native field is tap-focused — see [Modifier.nativeAutoFocus]). */
    public fun requestFocus() {
        requester.requestFocus()
    }

    /** Release focus from the attached element without moving focus elsewhere. */
    public fun freeFocus() {
        requester.freeFocus()
    }
}

/** Remember a [NativeFocusHandle] for imperative focus control. Attach it with [Modifier.nativeFocusTarget]. */
@Composable
public fun rememberNativeFocusHandle(): NativeFocusHandle = remember { NativeFocusHandle() }

/** Attach [handle] to this element so its [NativeFocusHandle.requestFocus]/[NativeFocusHandle.freeFocus] control it. */
public fun Modifier.nativeFocusTarget(handle: NativeFocusHandle): Modifier =
    this.focusRequester(handle.requester)

/**
 * Set explicit focus traversal order for this element — where focus goes on **next** / **previous** (Tab, a
 * hardware keyboard, or the IME "next" action). Point each field at the [next] (and optionally [previous]) handle
 * to build a form's tab chain.
 *
 * `Modifier.nativeFocusTarget(emailHandle).nativeFocusOrder(next = passwordHandle)`
 */
public fun Modifier.nativeFocusOrder(
    next: NativeFocusHandle? = null,
    previous: NativeFocusHandle? = null,
): Modifier = this.focusProperties {
    next?.let { this.next = it.requester }
    previous?.let { this.previous = it.requester }
}

/**
 * Group descendant focusables so they behave as a single tab-stop / 2D-navigable cluster (Compose `focusGroup`).
 * Apply to a row/section of controls so keyboard/hardware-key navigation treats them as one unit.
 */
public fun Modifier.nativeFocusGroup(): Modifier = this.focusGroup()
