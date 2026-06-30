package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
 * `Column(Modifier.fillMaxSize().brandDismissKeyboardOnTap()) { BrandTextField(...) }`
 */
@Composable
public fun Modifier.brandDismissKeyboardOnTap(): Modifier {
    val focusManager = LocalFocusManager.current
    return this.pointerInput(Unit) {
        detectTapGestures(onTap = { focusManager.clearFocus() })
    }
}

/**
 * Marks this node as a **heading** for screen-reader heading/rotor navigation (VoiceOver, TalkBack). Apply to
 * a section title so assistive tech can jump between sections.
 *
 * `BrandText("Settings", modifier = Modifier.brandHeading())`
 */
public fun Modifier.brandHeading(): Modifier = this.semantics { heading() }
