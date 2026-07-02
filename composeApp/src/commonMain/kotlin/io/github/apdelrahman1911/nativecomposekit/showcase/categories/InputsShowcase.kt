package io.github.apdelrahman1911.nativecomposekit.showcase.categories

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.apdelrahman1911.nativecomposekit.components.NativeButton
import io.github.apdelrahman1911.nativecomposekit.components.NativeOtpField
import io.github.apdelrahman1911.nativecomposekit.components.NativeSearchBar
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeSearchBarIosOptions
import io.github.apdelrahman1911.nativecomposekit.components.NativeText
import io.github.apdelrahman1911.nativecomposekit.components.NativeTextField
import io.github.apdelrahman1911.nativecomposekit.components.feedback.LocalNativeFeedbackController
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeButtonVariant
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeCharacterLimit
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeCharacterLimitBehavior
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeFieldFocus
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeFieldInput
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeIcon
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeImeAction
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeKeyboardType
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeTextContentType
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeTextStyle
import io.github.apdelrahman1911.nativecomposekit.showcase.ExampleLabel
import io.github.apdelrahman1911.nativecomposekit.showcase.Note
import io.github.apdelrahman1911.nativecomposekit.showcase.ShowcaseScreen
import io.github.apdelrahman1911.nativecomposekit.showcase.ShowcaseSection
import io.github.apdelrahman1911.nativecomposekit.showcase.WhenToUse

/**
 * "Text & Inputs" showcase. Covers NativeTextField (label/placeholder/helper/error, secured, disabled,
 * read-only, multiline, character limit, autofill content type), NativeSearchBar filtering a list live,
 * and NativeOtpField. Each field renders the most native input per platform; the label/helper/error are
 * drawn in Compose around it.
 */
@Composable
fun InputsShowcase() = ShowcaseScreen(
    intro = "Text input renders the most native primitive per platform — UITextField/UITextView on iOS, " +
        "OutlinedTextField on Android — with the label, helper, and error drawn in Compose around it. " +
        "Keyboard type, IME action, secure entry, line count, and character limits live in NativeFieldInput.",
) {
    SignInForm()
    FieldStates()
    LongFormFields()
    SearchSection()
    OtpSection()
}

/** A realistic sign-in form: email with the email keyboard + helper, password secured. */
@Composable
private fun SignInForm() {
    val feedback = LocalNativeFeedbackController.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    ShowcaseSection(
        title = "NativeTextField",
        description = "A single- or multiline input with label, placeholder, helper, and error decoration. " +
            "High-frequency props are top-level; keyboard/secure/limit knobs group into NativeFieldInput.",
    ) {
        WhenToUse(
            "Freeform typed text: names, emails, passwords, multiline notes.",
            "You want a native keyboard, autofill, and secure entry per platform.",
            "Use NativeSearchBar for an inline search field and NativeOtpField for a fixed-length code.",
        )

        ExampleLabel("Sign in")
        NativeTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email",
            placeholder = "you@example.com",
            helperText = "We'll never share it.",
            leadingIcon = NativeIcon(Icons.Default.Email, sfSymbolName = "envelope"),
            input = NativeFieldInput(
                keyboardType = NativeKeyboardType.Email,
                imeAction = NativeImeAction.Next,
                autoCorrect = false,
            ),
            contentType = NativeTextContentType.EmailAddress,
            modifier = Modifier.fillMaxWidth(),
        )
        NativeTextField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            placeholder = "••••••••",
            leadingIcon = NativeIcon(Icons.Default.Lock, sfSymbolName = "lock"),
            input = NativeFieldInput(secure = true, imeAction = NativeImeAction.Done),
            contentType = NativeTextContentType.Password,
            focus = NativeFieldFocus(onSubmit = { feedback.toast("Signing in…") }),
            modifier = Modifier.fillMaxWidth(),
        )
        NativeButton(
            text = "Sign in",
            onClick = { feedback.toast("Signing in…") },
            enabled = email.isNotBlank() && password.isNotBlank(),
            fullWidth = true,
        )

        Note(
            "secure = true maps to UITextField.isSecureTextEntry on iOS and a password visual transformation " +
                "on Android. contentType drives autofill on both platforms (iOS UITextContentType, Android " +
                "Compose autofill), so the OS can offer saved logins.",
        )
    }
}

/** Error, disabled, and read-only states. */
@Composable
private fun FieldStates() {
    var badEmail by remember { mutableStateOf("not-an-email") }

    ShowcaseSection(
        title = "States",
        description = "Validation error, disabled, and read-only. isError defaults to errorText != null, so " +
            "setting errorText is enough to flip the field into the error state.",
    ) {
        ExampleLabel("Error")
        NativeTextField(
            value = badEmail,
            onValueChange = { badEmail = it },
            label = "Email",
            errorText = "Enter a valid email address.",
            input = NativeFieldInput(keyboardType = NativeKeyboardType.Email, autoCorrect = false),
            modifier = Modifier.fillMaxWidth(),
        )

        ExampleLabel("Disabled")
        NativeTextField(
            value = "jane@example.com",
            onValueChange = {},
            label = "Email",
            helperText = "Sign out to change the account email.",
            enabled = false,
            modifier = Modifier.fillMaxWidth(),
        )

        ExampleLabel("Read-only")
        NativeTextField(
            value = "ACCT-4471-9920",
            onValueChange = {},
            label = "Account number",
            helperText = "Selectable, but not editable.",
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Note(
            "enabled = false greys the field and blocks focus. readOnly = true keeps it focusable and " +
                "selectable (copyable) but rejects edits — use it for generated identifiers and locked values.",
        )
    }
}

/** Multiline notes and a character-limited field. */
@Composable
private fun LongFormFields() {
    var notes by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    val bioMax = 80

    ShowcaseSection(
        title = "Multiline & limits",
        description = "singleLine = false grows the field within minLines/maxLines. A characterLimit caps or " +
            "warns; the counter and any over-limit error are the caller's to render.",
    ) {
        ExampleLabel("Multiline notes")
        NativeTextField(
            value = notes,
            onValueChange = { notes = it },
            label = "Notes",
            placeholder = "Add a few lines…",
            input = NativeFieldInput(singleLine = false, minLines = 3, maxLines = 6),
            modifier = Modifier.fillMaxWidth(),
        )

        ExampleLabel("Character limit (warn at $bioMax)")
        NativeTextField(
            value = bio,
            onValueChange = { bio = it },
            label = "Bio",
            placeholder = "Short bio for your profile",
            helperText = "${bio.length}/$bioMax",
            errorText = if (bio.length > bioMax) "Over by ${bio.length - bioMax}" else null,
            input = NativeFieldInput(
                singleLine = false,
                minLines = 2,
                maxLines = 4,
                characterLimit = NativeCharacterLimit(max = bioMax, behavior = NativeCharacterLimitBehavior.WarnOnly),
            ),
            modifier = Modifier.fillMaxWidth(),
        )

        Note(
            "WarnOnly lets the value pass the limit so you can show a counter/error (above). Enforce instead " +
                "hard-caps input — typing past the max is rejected and pasted text is trimmed. Limits run in " +
                "shared code, so both platforms behave identically. For SMS code autofill, set " +
                "contentType = NativeTextContentType.OneTimeCode on a NativeTextField (a native UITextField on iOS).",
        )
    }
}

private val FRUITS = listOf(
    "Apple", "Apricot", "Banana", "Blackberry", "Blueberry", "Cherry",
    "Grape", "Grapefruit", "Lemon", "Lime", "Mango", "Orange", "Peach", "Pear",
)

/** NativeSearchBar filtering a small in-memory list live. */
@Composable
private fun SearchSection() {
    var query by remember { mutableStateOf("") }
    val matches = remember(query) {
        if (query.isBlank()) FRUITS else FRUITS.filter { it.contains(query.trim(), ignoreCase = true) }
    }

    ShowcaseSection(
        title = "NativeSearchBar",
        description = "An inline search field for browse/search content. iOS draws a real UISearchBar; Android " +
            "a rounded search-styled field with a leading magnifier and trailing clear.",
    ) {
        WhenToUse(
            "Filtering or searching content placed inside a screen.",
            "You want the system search keyboard and a native clear/Cancel affordance.",
            "Filter live on value changes; onSearch fires on the keyboard's Search/Return key.",
        )

        NativeSearchBar(
            value = query,
            onValueChange = { query = it },
            placeholder = "Search fruit…",
            onCancel = { query = "" },
            ios = NativeSearchBarIosOptions(showCancelButton = true),
            modifier = Modifier.fillMaxWidth(),
        )

        if (matches.isEmpty()) {
            NativeText("No matches for \"$query\".", style = NativeTextStyle.Body)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                matches.forEach { NativeText(it, style = NativeTextStyle.Body) }
            }
        }

        Note(
            "ios.showCancelButton shows the native UISearchBar Cancel button; Android uses the trailing clear " +
                "affordance instead, which also fires onCancel. Distinct from a nav-bar .searchable — this is a " +
                "leaf control you place in content.",
        )
    }
}

/** NativeOtpField for a verification code. */
@Composable
private fun OtpSection() {
    val feedback = LocalNativeFeedbackController.current

    var code by remember { mutableStateOf("") }
    var wrong by remember { mutableStateOf(false) }

    ShowcaseSection(
        title = "NativeOtpField",
        description = "Segmented digit cells for a fixed-length code. Input is filtered to digits and capped at " +
            "length; onFilled fires once the code is complete.",
    ) {
        WhenToUse(
            "Collecting a numeric code or PIN with the segmented cell look.",
            "You want manual entry and paste into one logical field.",
            "For native SMS one-time-code autofill, use NativeTextField(contentType = OneTimeCode) instead.",
        )

        ExampleLabel("Verification code")
        NativeOtpField(
            value = code,
            onValueChange = {
                code = it
                wrong = false
            },
            length = 6,
            isError = wrong,
            onFilled = { entered ->
                if (entered == "123456") {
                    feedback.toast("Verified")
                } else {
                    wrong = true
                    feedback.toast("Incorrect code")
                }
            },
        )
        if (wrong) {
            NativeText("That code didn't match. Try 123456.", style = NativeTextStyle.Label)
        }
        NativeButton(
            text = "Clear",
            onClick = {
                code = ""
                wrong = false
            },
            variant = NativeButtonVariant.Tertiary,
            enabled = code.isNotEmpty(),
        )

        Note(
            "The cells are Compose-drawn on both platforms, so iOS SMS autofill is not available here by design. " +
                "isError renders all cells in the error state; the active cell is highlighted while typing.",
        )
    }
}
