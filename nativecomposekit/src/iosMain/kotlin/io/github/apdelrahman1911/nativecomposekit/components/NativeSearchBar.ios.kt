package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.viewinterop.UIKitView
import androidx.compose.ui.viewinterop.remeasureRequester
import androidx.compose.ui.viewinterop.rememberUIKitInteropRemeasureRequester
import io.github.apdelrahman1911.nativecomposekit.components.model.ResolvedSearchStyle
import kotlinx.cinterop.BetaInteropApi
import platform.UIKit.UISearchBar
import platform.UIKit.UISearchBarDelegateProtocol
import platform.UIKit.UISearchBarStyle
import platform.UIKit.UIUserInterfaceStyle
import platform.UIKit.UIView
import platform.UIKit.accessibilityLabel
import platform.darwin.NSObject

/** Bridges the native `UISearchBar` delegate callbacks to Kotlin lambdas (the delegate is held weakly by
 * the search bar, so the composition retains it via `remember`). */
@OptIn(BetaInteropApi::class)
private class SearchBarDelegate : NSObject(), UISearchBarDelegateProtocol {
    var onText: (String) -> Unit = {}
    var onSearch: () -> Unit = {}
    var onCancel: () -> Unit = {}

    override fun searchBar(searchBar: UISearchBar, textDidChange: String) = onText(textDidChange)

    override fun searchBarSearchButtonClicked(searchBar: UISearchBar) {
        searchBar.resignFirstResponder()
        onSearch()
    }

    override fun searchBarCancelButtonClicked(searchBar: UISearchBar) {
        searchBar.resignFirstResponder()
        onCancel()
    }
}

/**
 * iOS search → a real `UISearchBar` (Minimal style). The native field handles its own rounded background,
 * magnifier, clear button, Cancel button, and the search keyboard; we sync [value], theme it, and route the
 * delegate callbacks. The container color from the style is Android-only — iOS uses the native search-field
 * appearance (adapted to light/dark via `overrideUserInterfaceStyle`).
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal actual fun PlatformNativeSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier,
    placeholder: String,
    onSearch: (() -> Unit)?,
    onCancel: (() -> Unit)?,
    enabled: Boolean,
    showCancelButton: Boolean,
    style: ResolvedSearchStyle,
    contentDescription: String?,
    testTag: String?,
) {
    val delegate = remember { SearchBarDelegate() }
    delegate.onText = onValueChange
    delegate.onSearch = { onSearch?.invoke() }
    delegate.onCancel = {
        onValueChange("")
        onCancel?.invoke()
    }

    val control = remember {
        UISearchBar().apply {
            searchBarStyle = UISearchBarStyle.UISearchBarStyleMinimal
        }
    }
    control.delegate = delegate
    // Pin the search bar inside a backing that matches the published surface so its rounded field doesn't
    // reveal the interop host backdrop on a solid surface — and stays clear on Liquid Glass (same technique
    // as Toggle/Slider/SegmentedControl).
    val backing = remember { UIView() }
    val backingColor = interopBackingColor() // published surface on solid; clear on Liquid Glass
    val remeasure = rememberUIKitInteropRemeasureRequester()
    val sizeFp = remember { InteropSizeFingerprint() }

    UIKitView(
        factory = {
            backing.pinFilling(control)
            backing
        },
        modifier = modifier.remeasureRequester(remeasure),
        properties = scrollSafeInteropProperties(), // overlay placement so the backing isn't clipped on scroll
        update = {
            backing.backgroundColor = backingColor
            if (control.text != value) control.text = value
            control.placeholder = placeholder
            control.tintColor = style.tint.toUIColor()
            // Dynamic Type: the search field honors the user's text size (scaled brand font + live re-scale).
            control.searchTextField.font = style.textStyle.toUIFont()
            control.searchTextField.adjustsFontForContentSizeCategory = true
            control.searchTextField.textColor = style.text.toUIColor() // themed text, matching Android
            control.setShowsCancelButton(showCancelButton, animated = false)
            control.userInteractionEnabled = enabled
            control.overrideUserInterfaceStyle =
                if (style.surface.luminance() < 0.5f) UIUserInterfaceStyle.UIUserInterfaceStyleDark
                else UIUserInterfaceStyle.UIUserInterfaceStyleLight
            contentDescription?.let { control.accessibilityLabel = it }
            testTag?.let { control.setAccessibilityId(it) }
            // Fixed intrinsic size: measure once, on the first update (see InteropSizeFingerprint).
            sizeFp.requestIfChanged(Unit) { remeasure.requestRemeasure() }
        },
        // Detach the delegate when the node leaves for good — the released bar must not message a
        // collected Kotlin object if UIKit keeps it alive for a removal frame.
        onRelease = { control.delegate = null },
    )
}
