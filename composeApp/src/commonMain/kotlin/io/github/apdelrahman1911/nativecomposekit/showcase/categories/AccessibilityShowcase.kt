package io.github.apdelrahman1911.nativecomposekit.showcase.categories

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.apdelrahman1911.nativecomposekit.components.NativeButton
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeButtonVariant
import io.github.apdelrahman1911.nativecomposekit.components.NativeText
import io.github.apdelrahman1911.nativecomposekit.components.NativeTextField
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeFieldInput
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeImeAction
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeKeyboardType
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeTextStyle
import io.github.apdelrahman1911.nativecomposekit.components.nativeAutoFocus
import io.github.apdelrahman1911.nativecomposekit.components.nativeDismissKeyboardOnTap
import io.github.apdelrahman1911.nativecomposekit.components.nativeFocusOrder
import io.github.apdelrahman1911.nativecomposekit.components.nativeFocusTarget
import io.github.apdelrahman1911.nativecomposekit.components.nativeHeading
import io.github.apdelrahman1911.nativecomposekit.components.rememberNativeFocusHandle
import io.github.apdelrahman1911.nativecomposekit.showcase.ExampleLabel
import io.github.apdelrahman1911.nativecomposekit.showcase.Note
import io.github.apdelrahman1911.nativecomposekit.showcase.ShowcaseScreen
import io.github.apdelrahman1911.nativecomposekit.showcase.ShowcaseSection
import io.github.apdelrahman1911.nativecomposekit.showcase.WhenToUse

/**
 * Accessibility & helpers — the focus/IME/semantics modifiers the kit uses internally, exposed for app
 * screens. Both are Compose-drawn and behave identically on Android and iOS.
 */
@Composable
fun AccessibilityShowcase() = ShowcaseScreen(
    intro = "Modifier helpers for keyboard dismissal and screen-reader navigation, plus the accessibility " +
        "defaults every Native* control ships with.",
) {
    ShowcaseSection(
        title = "Dismiss keyboard on tap",
        description = "Wrap a form's root/background with nativeDismissKeyboardOnTap() so a tap outside any " +
            "field clears focus and hides the soft keyboard — the common iOS/Android idiom.",
    ) {
        ExampleLabel("Tap the form background to dismiss the keyboard")
        ProfileForm()

        WhenToUse(
            "A form screen should hide the keyboard when the user taps outside a field.",
            "Applied to a scrollable root or background that wraps your text fields.",
        )

        Note(
            "Put the modifier on a container, not an interactive control: the tap detector consumes taps on " +
                "the element it's applied to. It's @Composable because it reads LocalFocusManager from " +
                "composition.",
        )
    }

    ShowcaseSection(
        title = "Headings for screen readers",
        description = "Mark a section title with nativeHeading() so VoiceOver/TalkBack can jump between " +
            "sections via heading/rotor navigation.",
    ) {
        ExampleLabel("Title node exposed as a heading")
        NativeText("Notifications", style = NativeTextStyle.Title, modifier = Modifier.nativeHeading())
        NativeText(
            "Choose which alerts reach this device. The title above is announced as a heading.",
            style = NativeTextStyle.Body,
        )

        WhenToUse(
            "Marking a section title so assistive tech can skip between sections.",
        )

        Note(
            "Don't over-mark headings — flagging body text makes rotor navigation noisier. nativeHeading() " +
                "takes no arguments and is not @Composable.",
        )
    }

    ShowcaseSection(
        title = "Focus management",
        description = "Auto-focus a field when a form opens, and move focus between fields from callbacks with " +
            "focus handles and an explicit next/previous order.",
    ) {
        var formOpen by remember { mutableStateOf(false) }
        ExampleLabel("Open the form — the first field auto-focuses; the keyboard's Next jumps to the second")
        NativeButton(
            text = if (formOpen) "Close form" else "Open form",
            onClick = { formOpen = !formOpen },
            variant = NativeButtonVariant.Secondary,
        )
        if (formOpen) FocusForm()

        WhenToUse(
            "Focus the first field when a form or dialog appears (nativeAutoFocus).",
            "Advance to the next field on the keyboard's Next action (a focus handle + nativeFocusOrder).",
        )
        Note(
            "nativeAutoFocus requests focus on first composition; rememberNativeFocusHandle + nativeFocusTarget " +
                "give a handle you can requestFocus() from a callback; nativeFocusOrder wires the next/previous " +
                "traversal chain; nativeFocusGroup groups controls into a single tab-stop. These drive Compose " +
                "focus: full on Android (auto-focus opens the keyboard, Next advances). On iOS the native " +
                "UITextField is tap-focused and outside Compose focus, so they don't move it there — tap a field.",
        )
    }

    Note(
        "Accessibility defaults the kit applies for you: every control carries a contentDescription (with a " +
            "contentDescription override where you need a custom label); toggles, sliders, and steppers announce " +
            "their state and value as they change; and feedback surfaces (toasts, banners, inline status) post " +
            "through live regions so screen readers read updates without stealing focus.",
    )
}

/** A small profile form whose root dismisses the keyboard on a background tap. */
@Composable
private fun ProfileForm() {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .nativeDismissKeyboardOnTap(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        NativeTextField(
            value = name,
            onValueChange = { name = it },
            label = "Name",
            placeholder = "Jane Doe",
            modifier = Modifier.fillMaxWidth(),
        )
        NativeTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email",
            placeholder = "you@example.com",
            input = NativeFieldInput(keyboardType = NativeKeyboardType.Email, imeAction = NativeImeAction.Done),
            modifier = Modifier.fillMaxWidth(),
        )
        NativeButton(
            text = "Save",
            onClick = { },
            variant = NativeButtonVariant.Primary,
            fullWidth = true,
        )
    }
}

/** Two fields: the first auto-focuses on appear; the keyboard's Next advances to the second via a focus handle. */
@Composable
private fun FocusForm() {
    var first by remember { mutableStateOf("") }
    var last by remember { mutableStateOf("") }
    val firstField = rememberNativeFocusHandle()
    val lastField = rememberNativeFocusHandle()
    Column(
        modifier = Modifier.fillMaxWidth().nativeDismissKeyboardOnTap(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        NativeTextField(
            value = first,
            onValueChange = { first = it },
            label = "First name",
            modifier = Modifier
                .fillMaxWidth()
                .nativeAutoFocus()
                .nativeFocusTarget(firstField)
                .nativeFocusOrder(next = lastField),
            // imeAction = Next + nativeFocusOrder IS the chain — no onSubmit needed (the kit runs a
            // submit handler AND the default action, so wiring requestFocus there would move twice).
            input = NativeFieldInput(imeAction = NativeImeAction.Next),
        )
        NativeTextField(
            value = last,
            onValueChange = { last = it },
            label = "Last name",
            modifier = Modifier
                .fillMaxWidth()
                .nativeFocusTarget(lastField)
                .nativeFocusOrder(previous = firstField),
            input = NativeFieldInput(imeAction = NativeImeAction.Done),
        )
    }
}
