package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitView
import androidx.compose.ui.platform.LocalDensity
import io.github.apdelrahman1911.nativecomposekit.theme.LocalNativeStrings
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeCapitalization
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeCharacterLimit
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeClearButtonMode
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeFieldColors
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeFieldFocus
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeFieldInput
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeIcon
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeImeAction
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeInteropTouch
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeKeyboardAccessory
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeKeyboardAccessoryStyle
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeKeyboardAppearance
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeTextContentType
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeTextFieldIosOptions
import io.github.apdelrahman1911.nativecomposekit.components.model.ResolvedFieldStyle
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import platform.CoreGraphics.CGRectMake
import platform.UIKit.UIBarButtonItem
import platform.UIKit.UIBarButtonItemStyle
import platform.UIKit.UIBarButtonSystemItem
import platform.UIKit.UIButton
import platform.UIKit.UIColor
import platform.UIKit.UIControlEventEditingChanged
import platform.UIKit.UIControlEventEditingDidBegin
import platform.UIKit.UIControlEventEditingDidEnd
import platform.UIKit.UIControlEventEditingDidEndOnExit
import platform.UIKit.UIControlEventTouchUpInside
import platform.UIKit.UIControlStateNormal
import platform.UIKit.UIFont
import platform.UIKit.UIImage
import platform.UIKit.UIImageView
import platform.UIKit.UIKeyboardAppearance
import platform.UIKit.UIKeyboardAppearanceDark
import platform.UIKit.UIKeyboardAppearanceDefault
import platform.UIKit.UIKeyboardAppearanceLight
import platform.UIKit.UILabel
import platform.UIKit.UIReturnKeyType
import platform.UIKit.UITextAutocapitalizationType
import platform.UIKit.UITextAutocorrectionType
import platform.UIKit.UITextBorderStyle
import platform.UIKit.UITextContentTypeCreditCardNumber
import platform.UIKit.UITextContentTypeEmailAddress
import platform.UIKit.UITextContentTypeFullStreetAddress
import platform.UIKit.UITextContentTypeName
import platform.UIKit.UITextContentTypeNewPassword
import platform.UIKit.UITextContentTypeOneTimeCode
import platform.UIKit.UITextContentTypePassword
import platform.UIKit.UITextContentTypePostalCode
import platform.UIKit.UITextContentTypeTelephoneNumber
import platform.UIKit.UITextContentTypeUsername
import platform.UIKit.UITextField
import platform.UIKit.UITextFieldViewMode
import platform.UIKit.UITextView
import platform.UIKit.UITextViewDelegateProtocol
import platform.UIKit.UIToolbar
import platform.UIKit.UIView
import platform.UIKit.UIViewAutoresizingFlexibleLeftMargin
import platform.UIKit.UIViewAutoresizingFlexibleWidth
import platform.UIKit.UIViewContentMode
import platform.UIKit.accessibilityLabel
import platform.darwin.NSObject
import platform.objc.sel_registerName

private const val H_PADDING = 14.0 // horizontal breathing room from the border
private const val V_PADDING = 10.0 // vertical inset for the multiline text view
private const val ICON_GAP = 8.0 // gap between a leading/trailing icon and the text
private const val ICON_SIZE = 22.0 // glyph box (UITextField centers it vertically)

// --- enum/value mappings (theme-resolved values in → native enums out) ---

private fun NativeImeAction.toReturnKeyType(): UIReturnKeyType = when (this) {
    NativeImeAction.Default -> UIReturnKeyType.UIReturnKeyDefault
    NativeImeAction.Done -> UIReturnKeyType.UIReturnKeyDone
    NativeImeAction.Go -> UIReturnKeyType.UIReturnKeyGo
    NativeImeAction.Next -> UIReturnKeyType.UIReturnKeyNext
    NativeImeAction.Search -> UIReturnKeyType.UIReturnKeySearch
    NativeImeAction.Send -> UIReturnKeyType.UIReturnKeySend
}

private fun NativeCapitalization.toAutocapitalization(): UITextAutocapitalizationType = when (this) {
    NativeCapitalization.None -> UITextAutocapitalizationType.UITextAutocapitalizationTypeNone
    NativeCapitalization.Characters -> UITextAutocapitalizationType.UITextAutocapitalizationTypeAllCharacters
    NativeCapitalization.Words -> UITextAutocapitalizationType.UITextAutocapitalizationTypeWords
    NativeCapitalization.Sentences -> UITextAutocapitalizationType.UITextAutocapitalizationTypeSentences
}

private fun autocorrection(enabled: Boolean): UITextAutocorrectionType =
    if (enabled) UITextAutocorrectionType.UITextAutocorrectionTypeYes
    else UITextAutocorrectionType.UITextAutocorrectionTypeNo

private fun NativeKeyboardAppearance.toKeyboardAppearance(): UIKeyboardAppearance = when (this) {
    NativeKeyboardAppearance.Default -> UIKeyboardAppearanceDefault
    NativeKeyboardAppearance.Light -> UIKeyboardAppearanceLight
    NativeKeyboardAppearance.Dark -> UIKeyboardAppearanceDark
}

private fun NativeTextContentType.toUITextContentType(): String? = when (this) {
    NativeTextContentType.Name -> UITextContentTypeName
    NativeTextContentType.EmailAddress -> UITextContentTypeEmailAddress
    NativeTextContentType.Username -> UITextContentTypeUsername
    NativeTextContentType.Password -> UITextContentTypePassword
    NativeTextContentType.NewPassword -> UITextContentTypeNewPassword
    NativeTextContentType.OneTimeCode -> UITextContentTypeOneTimeCode
    NativeTextContentType.TelephoneNumber -> UITextContentTypeTelephoneNumber
    NativeTextContentType.PostalCode -> UITextContentTypePostalCode
    NativeTextContentType.FullStreetAddress -> UITextContentTypeFullStreetAddress
    NativeTextContentType.CreditCardNumber -> UITextContentTypeCreditCardNumber
}

private fun NativeClearButtonMode.toViewMode(): UITextFieldViewMode = when (this) {
    NativeClearButtonMode.Never -> UITextFieldViewMode.UITextFieldViewModeNever
    NativeClearButtonMode.WhileEditing -> UITextFieldViewMode.UITextFieldViewModeWhileEditing
    NativeClearButtonMode.UnlessEditing -> UITextFieldViewMode.UITextFieldViewModeUnlessEditing
    NativeClearButtonMode.Always -> UITextFieldViewMode.UITextFieldViewModeAlways
}

// --- single-line control events ---

@OptIn(BetaInteropApi::class)
private class FieldEvents : NSObject() {
    var onChange: (String) -> Unit = {}
    var onFocus: (Boolean) -> Unit = {}
    var onSubmit: () -> Unit = {}
    var onTrailingClick: (() -> Unit)? = null
    var field: UITextField? = null
    var characterLimit: NativeCharacterLimit? = null

    @ObjCAction fun editingChanged() {
        val f = field ?: return
        val raw = f.text ?: ""
        val limited = applyCharacterLimit(raw, characterLimit)
        // Trim the NATIVE text too, not just the value handed to state: when the caller echoes back the
        // same capped string, nothing recomposes, so `update` never re-runs to heal the display — the
        // field would keep showing the over-limit characters. Never touch in-flight IME composition
        // (markedTextRange), or multi-stage input (Japanese, pinyin) breaks mid-word.
        if (limited != raw && f.markedTextRange == null) f.text = limited
        onChange(if (f.markedTextRange == null) limited else raw)
    }
    @ObjCAction fun editingBegan() = onFocus(true)
    @ObjCAction fun editingEnded() = onFocus(false)
    @ObjCAction fun submitted() = onSubmit()
    @ObjCAction fun trailingTapped() { onTrailingClick?.invoke() }
    @ObjCAction fun doneTapped() {
        field?.resignFirstResponder()
        onSubmit()
    }
}

// --- multiline (UITextView) delegate ---

@OptIn(BetaInteropApi::class)
private class TextViewEvents : NSObject(), UITextViewDelegateProtocol {
    var onChange: (String) -> Unit = {}
    var onFocus: (Boolean) -> Unit = {}
    var placeholderLabel: UILabel? = null
    var editor: UITextView? = null
    var characterLimit: NativeCharacterLimit? = null

    override fun textViewDidChange(textView: UITextView) {
        val raw = textView.text
        val limited = applyCharacterLimit(raw, characterLimit)
        // Same native-trim rule as the single-line field (see FieldEvents.editingChanged).
        if (limited != raw && textView.markedTextRange == null) textView.text = limited
        val text = if (textView.markedTextRange == null) limited else raw
        onChange(text)
        placeholderLabel?.setHidden(text.isNotEmpty())
    }
    override fun textViewDidBeginEditing(textView: UITextView) = onFocus(true)
    override fun textViewDidEndEditing(textView: UITextView) = onFocus(false)
    @ObjCAction fun doneTapped() { editor?.resignFirstResponder() }
}

/**
 * iOS NativeTextField. Single-line → `UITextField`; multiline → `UITextView` (with a placeholder
 * overlay). The rounded field is pinned inside a theme-colored backing so its transparent corners
 * blend (see architecture.md §1). Label/helper/error are drawn by Compose around the native input.
 */
@OptIn(ExperimentalForeignApi::class, ExperimentalComposeUiApi::class)
@Composable
internal actual fun PlatformNativeTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    readOnly: Boolean,
    label: String?,
    placeholder: String?,
    helperText: String?,
    errorText: String?,
    isError: Boolean,
    leadingIcon: NativeIcon?,
    trailingIcon: NativeIcon?,
    onTrailingIconClick: (() -> Unit)?,
    input: NativeFieldInput,
    focus: NativeFieldFocus,
    contentType: NativeTextContentType?,
    style: ResolvedFieldStyle,
    touch: NativeInteropTouch,
    contentDescription: String?,
    testTag: String?,
    ios: NativeTextFieldIosOptions,
) {
    Column(modifier = modifier) {
        if (label != null) {
            Text(text = label, color = style.colors.label, style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(4.dp))
        }

        // The visible `label` above is separate Compose text, not the native field's a11y name — so
        // wire it (or an explicit contentDescription) onto the native control for VoiceOver.
        val a11yLabel = contentDescription ?: label
        if (input.singleLine) {
            SingleLineField(
                value, onValueChange, enabled, readOnly, placeholder, isError,
                leadingIcon, trailingIcon, onTrailingIconClick, input, focus, contentType, style, touch, testTag, ios,
                accessibilityLabel = a11yLabel,
            )
        } else {
            MultilineField(
                value, onValueChange, enabled, readOnly, placeholder, isError,
                input, focus, contentType, style, touch, testTag, ios,
                accessibilityLabel = a11yLabel,
            )
        }

        val sub = errorText ?: helperText
        if (sub != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = sub,
                color = if (errorText != null || isError) style.colors.error else style.colors.helper,
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

@OptIn(ExperimentalForeignApi::class, ExperimentalComposeUiApi::class)
@Composable
private fun SingleLineField(
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    readOnly: Boolean,
    placeholder: String?,
    isError: Boolean,
    leadingIcon: NativeIcon?,
    trailingIcon: NativeIcon?,
    onTrailingIconClick: (() -> Unit)?,
    input: NativeFieldInput,
    focus: NativeFieldFocus,
    contentType: NativeTextContentType?,
    style: ResolvedFieldStyle,
    touch: NativeInteropTouch,
    testTag: String?,
    ios: NativeTextFieldIosOptions,
    accessibilityLabel: String?,
) {
    var focused by remember { mutableStateOf(false) }
    val events = remember { FieldEvents() }
    events.onChange = onValueChange
    events.onFocus = { f -> focused = f; focus.onFocusChanged?.invoke(f) }
    events.onSubmit = { focus.onSubmit?.invoke() }
    events.onTrailingClick = onTrailingIconClick
    events.characterLimit = input.characterLimit
    // Resolve the accessory button title in composition (localized default) for the update closure below.
    val doneText = ios.keyboardAccessory.doneText ?: LocalNativeStrings.current.done
    // Built per (accessory, title, colors) so a theme flip or strings change replaces the bar — a
    // build-once accessory kept the first theme's opaque colors and Done title for the field's lifetime.
    val accessoryView = remember(ios.keyboardAccessory, doneText, style.colors) {
        if (ios.keyboardAccessory.doneButton) {
            makeAccessory(ios.keyboardAccessory, doneText, style.colors, events, sel_registerName("doneTapped"))
        } else {
            null
        }
    }

    val field = remember {
        UITextField().apply {
            borderStyle = UITextBorderStyle.UITextBorderStyleNone
            addTarget(events, sel_registerName("editingChanged"), UIControlEventEditingChanged)
            addTarget(events, sel_registerName("editingBegan"), UIControlEventEditingDidBegin)
            addTarget(events, sel_registerName("editingEnded"), UIControlEventEditingDidEnd)
            addTarget(events, sel_registerName("submitted"), UIControlEventEditingDidEndOnExit)
        }
    }
    events.field = field
    val backing = remember { InteropBackingView() }

    // The left/right accessory views are built ONCE per icon/style config (remember), not per update — the
    // update block re-fires on every keystroke, and re-allocating a UIImageView/UIButton there churned layout
    // and could swallow a trailing-button tap mid-gesture. The button targets `events`, which always forwards
    // to the CURRENT onTrailingIconClick, so the cached view never goes stale.
    val leadName = leadingIcon?.sfSymbolName
    val leftAccessory = remember(leadName, style) {
        if (leadName != null) paddedIcon(leadName, H_PADDING, ICON_GAP, style) else spacer(H_PADDING)
    }
    val trailName = trailingIcon?.sfSymbolName
    val hasTrailingClick = onTrailingIconClick != null
    val rightAccessory = remember(trailName, hasTrailingClick, style) {
        when {
            trailName == null -> spacer(H_PADDING)
            hasTrailingClick -> UIButton().apply {
                setImage(UIImage.systemImageNamed(trailName), forState = UIControlStateNormal)
                tintColor = style.colors.placeholder.toUIColor()
                setFrame(CGRectMake(0.0, 0.0, ICON_GAP + ICON_SIZE + H_PADDING, ICON_SIZE))
                addTarget(events, sel_registerName("trailingTapped"), UIControlEventTouchUpInside)
            }
            else -> paddedIcon(trailName, ICON_GAP, H_PADDING, style)
        }
    }

    // Dynamic Type: the glyphs scale live (UIFontMetrics), so the fixed host must scale with them or large
    // accessibility sizes clip inside it. Compose surfaces the preferred content size as Density.fontScale.
    val hostHeight = style.minHeight * maxOf(1f, LocalDensity.current.fontScale)

    UIKitView(
        factory = {
            backing.pinFilling(field)
            backing
        },
        modifier = Modifier.fillMaxWidth().height(hostHeight),
        properties = touch.toInteropProperties(nativeAccessibility = true),
        update = { _ ->
            backing.backgroundColor = style.colors.container.toUIColor()
            if (field.text != value) field.text = value
            field.font = style.textStyle.toUIFont()
            field.adjustsFontForContentSizeCategory = true // Dynamic Type
            field.textColor = style.colors.text.toUIColor()
            field.tintColor = style.colors.cursor.toUIColor()
            field.backgroundColor = style.colors.container.toUIColor()
            field.clipsToBounds = true
            field.layer.cornerRadius = style.cornerRadius.value.toDouble()
            field.layer.borderWidth = 1.0
            field.layer.borderColor = when {
                isError -> style.colors.errorBorder
                focused -> style.colors.focusedBorder
                else -> style.colors.border
            }.toUIColor().CGColor

            field.placeholder = placeholder
            field.secureTextEntry = input.secure
            field.enabled = enabled
            field.userInteractionEnabled = enabled && !readOnly
            // Disabled fields must LOOK disabled: `enabled = false` alone doesn't dim custom-colored text.
            field.alpha = if (enabled) 1.0 else 0.38
            field.keyboardType = input.keyboardType.toUIKeyboardType()
            field.returnKeyType = input.imeAction.toReturnKeyType()
            field.autocapitalizationType = input.capitalization.toAutocapitalization()
            field.autocorrectionType = autocorrection(input.autoCorrect)
            field.keyboardAppearance = ios.keyboardAppearance.toKeyboardAppearance()
            field.textContentType = contentType?.toUITextContentType()

            // Leading icon / left padding — assign the cached view only when it actually changed.
            if (field.leftView !== leftAccessory) {
                field.leftView = leftAccessory
                field.leftViewMode = UITextFieldViewMode.UITextFieldViewModeAlways
            }

            // Trailing icon takes priority over the system clear button (they share the right slot). With a
            // click handler the cached trailing view is a real tappable UIButton (reveal-password, clear, …)
            // wired to `trailingTapped`; without one it's a display-only glyph.
            if (field.rightView !== rightAccessory) {
                field.rightView = rightAccessory
                field.rightViewMode = UITextFieldViewMode.UITextFieldViewModeAlways
            }
            field.clearButtonMode =
                if (trailName != null) UITextFieldViewMode.UITextFieldViewModeNever else ios.clearButton.toViewMode()

            if (field.inputAccessoryView !== accessoryView) field.inputAccessoryView = accessoryView

            field.accessibilityLabel = accessibilityLabel
            testTag?.let { field.setAccessibilityId(it) }
        },
    )
}

@OptIn(ExperimentalForeignApi::class, ExperimentalComposeUiApi::class)
@Composable
private fun MultilineField(
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    readOnly: Boolean,
    placeholder: String?,
    isError: Boolean,
    input: NativeFieldInput,
    focus: NativeFieldFocus,
    contentType: NativeTextContentType?,
    style: ResolvedFieldStyle,
    touch: NativeInteropTouch,
    testTag: String?,
    ios: NativeTextFieldIosOptions,
    accessibilityLabel: String?,
) {
    var focused by remember { mutableStateOf(false) }
    val events = remember { TextViewEvents() }
    events.onChange = onValueChange
    events.onFocus = { f -> focused = f; focus.onFocusChanged?.invoke(f) }
    events.characterLimit = input.characterLimit

    val placeholderLabel = remember { UILabel() }
    val textView = remember {
        UITextView().apply {
            delegate = events
            setTextContainerInset(platform.UIKit.UIEdgeInsetsMake(V_PADDING, H_PADDING, V_PADDING, H_PADDING))
        }
    }
    events.placeholderLabel = placeholderLabel
    events.editor = textView
    val backing = remember { InteropBackingView() }
    // Resolve the accessory button title in composition (localized default) for the update closure below.
    val doneText = ios.keyboardAccessory.doneText ?: LocalNativeStrings.current.done
    // A multiline UITextView's Return key inserts a newline (no built-in dismiss), so the Done accessory is
    // ON by default — but an explicit `doneButton = false` is caller intent and is honored (no accessory).
    // Rebuilt per (accessory, title, colors) so theme flips don't leave a stale-colored bar.
    val accessoryView = remember(ios.keyboardAccessory, doneText, style.colors) {
        if (ios.keyboardAccessory.doneButton) {
            makeAccessory(ios.keyboardAccessory, doneText, style.colors, events, sel_registerName("doneTapped"))
        } else {
            null
        }
    }

    // Height honors the caller's minLines (the visible-lines contract Android already keeps): the native
    // font's line height is Dynamic-Type-scaled already, so no extra fontScale factor. Content beyond
    // minLines scrolls inside the editor (the iOS box does not auto-grow; documented divergence).
    val lineHeightPoints = style.textStyle.toUIFont().lineHeight
    val computedHeight = (lineHeightPoints * input.minLines.coerceAtLeast(1) + 2 * V_PADDING).toFloat().dp
    val minHeight = maxOf(style.minHeight, computedHeight)

    UIKitView(
        factory = {
            textView.addSubview(placeholderLabel)
            // Return the BACKING as the interop root (the editor pinned inside it) — matching SingleLineField.
            // Returning the editor itself detached it from the backing, so the theme-colored backing never
            // rendered and the editor's rounded corners exposed the system backdrop.
            backing.pinFilling(textView)
            backing
        },
        modifier = Modifier.fillMaxWidth().height(minHeight),
        properties = touch.toInteropProperties(nativeAccessibility = true),
        update = { _ ->
            backing.backgroundColor = style.colors.container.toUIColor()
            textView.backgroundColor = style.colors.container.toUIColor()
            if (textView.text != value) textView.text = value
            textView.font = style.textStyle.toUIFont()
            textView.adjustsFontForContentSizeCategory = true // Dynamic Type
            textView.textColor = style.colors.text.toUIColor()
            textView.tintColor = style.colors.cursor.toUIColor()
            textView.setEditable(enabled && !readOnly)
            // Disabled dims (readOnly stays full-strength: it is still readable/selectable content).
            textView.alpha = if (enabled) 1.0 else 0.38
            textView.secureTextEntry = input.secure // parity: Android masks multiline secure input too
            textView.keyboardType = input.keyboardType.toUIKeyboardType()
            textView.keyboardAppearance = ios.keyboardAppearance.toKeyboardAppearance()
            textView.autocapitalizationType = input.capitalization.toAutocapitalization()
            textView.autocorrectionType = autocorrection(input.autoCorrect)
            textView.textContentType = contentType?.toUITextContentType()
            textView.clipsToBounds = true
            textView.layer.cornerRadius = style.cornerRadius.value.toDouble()
            textView.layer.borderWidth = 1.0
            textView.layer.borderColor = when {
                isError -> style.colors.errorBorder
                focused -> style.colors.focusedBorder
                else -> style.colors.border
            }.toUIColor().CGColor

            placeholderLabel.text = placeholder
            placeholderLabel.font = style.textStyle.toUIFont()
            placeholderLabel.adjustsFontForContentSizeCategory = true // Dynamic Type
            placeholderLabel.textColor = style.colors.placeholder.toUIColor()
            placeholderLabel.setHidden(textView.text.isNotEmpty())
            placeholderLabel.setFrame(CGRectMake(H_PADDING + 5.0, V_PADDING, 220.0, 22.0))

            if (textView.inputAccessoryView !== accessoryView) textView.inputAccessoryView = accessoryView

            textView.accessibilityLabel = accessibilityLabel
            testTag?.let { textView.setAccessibilityId(it) }
        },
    )
}

// Wrap a leading/trailing icon (or an empty spacer) in a sized box so the native left/right view is
// inset from the border instead of touching it.
@OptIn(ExperimentalForeignApi::class)
private fun paddedIcon(name: String, leftInset: Double, rightInset: Double, style: ResolvedFieldStyle): UIView {
    val iv = UIImageView(image = UIImage.systemImageNamed(name))
    iv.tintColor = style.colors.placeholder.toUIColor()
    iv.contentMode = UIViewContentMode.UIViewContentModeScaleAspectFit
    iv.setFrame(CGRectMake(leftInset, 0.0, ICON_SIZE, ICON_SIZE))
    val box = UIView()
    box.setFrame(CGRectMake(0.0, 0.0, leftInset + ICON_SIZE + rightInset, ICON_SIZE))
    box.addSubview(iv)
    return box
}

@OptIn(ExperimentalForeignApi::class)
private fun spacer(width: Double): UIView =
    UIView().apply { setFrame(CGRectMake(0.0, 0.0, width, ICON_SIZE)) }

// Native input-accessory toolbar: [Done] right-aligned above the keyboard. Tapping fires [action] on
// [target] (which resigns the responder). A reliable native way to dismiss the keyboard in a scroll.
@OptIn(ExperimentalForeignApi::class)
private fun makeDoneToolbar(doneText: String, tint: UIColor, target: NSObject, action: COpaquePointer?): UIToolbar {
    val toolbar = UIToolbar()
    toolbar.sizeToFit()
    toolbar.tintColor = tint
    val flexible = UIBarButtonItem(
        barButtonSystemItem = UIBarButtonSystemItem.UIBarButtonSystemItemFlexibleSpace,
        target = null,
        action = null,
    )
    val done = UIBarButtonItem(
        title = doneText,
        style = UIBarButtonItemStyle.UIBarButtonItemStyleDone,
        target = target,
        action = action,
    )
    toolbar.setItems(listOf(flexible, done), animated = false)
    return toolbar
}

// Full-width rectangular accessory bar above the keyboard: a clean rectangle (spanning the keyboard
// width via flexible autoresizing) with a hairline top separator and a right-aligned, easy-to-tap Done
// button. Tapping fires [action] on [target] (which resigns the responder).
@OptIn(ExperimentalForeignApi::class)
private fun makeDoneBar(
    doneText: String,
    barColor: UIColor,
    separatorColor: UIColor,
    doneColor: UIColor,
    target: NSObject,
    action: COpaquePointer?,
): UIView {
    val barHeight = 48.0
    val initialWidth = 393.0 // overridden by the system to the keyboard width; autoresizing fills it
    val bar = UIView()
    bar.setFrame(CGRectMake(0.0, 0.0, initialWidth, barHeight))
    bar.backgroundColor = barColor
    bar.autoresizingMask = UIViewAutoresizingFlexibleWidth

    val separator = UIView()
    separator.setFrame(CGRectMake(0.0, 0.0, initialWidth, 0.5))
    separator.backgroundColor = separatorColor
    separator.autoresizingMask = UIViewAutoresizingFlexibleWidth
    bar.addSubview(separator)

    val done = UIButton()
    done.setTitle(doneText, UIControlStateNormal)
    done.setTitleColor(doneColor, UIControlStateNormal)
    done.titleLabel?.font = UIFont.boldSystemFontOfSize(17.0)
    val buttonWidth = 96.0
    done.setFrame(CGRectMake(initialWidth - buttonWidth, 0.0, buttonWidth, barHeight))
    done.autoresizingMask = UIViewAutoresizingFlexibleLeftMargin // stays right-aligned as the bar widens
    done.addTarget(target, action, UIControlEventTouchUpInside)
    bar.addSubview(done)
    return bar
}

@OptIn(ExperimentalForeignApi::class)
private fun makeAccessory(
    accessory: NativeKeyboardAccessory,
    doneText: String,
    colors: NativeFieldColors,
    target: NSObject,
    action: COpaquePointer?,
): UIView = when (accessory.style) {
    NativeKeyboardAccessoryStyle.FullWidthBar -> makeDoneBar(
        doneText,
        colors.container.toUIColor(),
        colors.border.toUIColor(),
        colors.focusedBorder.toUIColor(),
        target,
        action,
    )
    NativeKeyboardAccessoryStyle.Toolbar -> makeDoneToolbar(
        doneText,
        colors.focusedBorder.toUIColor(),
        target,
        action,
    )
}
