package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import io.github.apdelrahman1911.nativecomposekit.components.internal.resolveSurfaceFill
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeSearchBarColors
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeSearchBarIosOptions
import io.github.apdelrahman1911.nativecomposekit.components.model.ResolvedSearchStyle
import io.github.apdelrahman1911.nativecomposekit.theme.LocalNativeStrings
import io.github.apdelrahman1911.nativecomposekit.theme.LocalNativeSurface
import io.github.apdelrahman1911.nativecomposekit.theme.NativeTheme

/**
 * An inline search field — browse/search screens. Renders the most native control per platform: on **iOS**
 * a real `UISearchBar` (magnifier, rounded field, optional Cancel button, the system search keyboard); on
 * **Android** a Material search-styled, indicator-less rounded text field with a leading magnifier and a
 * trailing clear button. Distinct from a nav-bar `.searchable` (that stays native-shell chrome) — this is a
 * leaf control you place in the content.
 *
 * [onSearch] fires on the keyboard's Search/Return key; [onCancel] on the iOS Cancel button (and the
 * Android clear button, which also clears the text). iOS-only knobs (the Cancel button) live in
 * [ios] = [NativeSearchBarIosOptions]; they are documented no-ops on Android (which uses the trailing clear
 * affordance instead).
 *
 * [colorsOverride] restyles the field (text/placeholder/container/tint) without forking; unset fields
 * keep the theme defaults.
 *
 * `NativeSearchBar(query, onValueChange = { query = it }, onSearch = { run(query) })`
 */
@Composable
public fun NativeSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = LocalNativeStrings.current.searchPlaceholder,
    onSearch: (() -> Unit)? = null,
    onCancel: (() -> Unit)? = null,
    enabled: Boolean = true,
    colorsOverride: NativeSearchBarColors? = null,
    contentDescription: String? = null,
    testTag: String? = null,
    ios: NativeSearchBarIosOptions = NativeSearchBarIosOptions(),
) {
    val scheme = MaterialTheme.colorScheme
    val style = ResolvedSearchStyle(
        text = (colorsOverride?.text ?: Color.Unspecified).takeOrElse { scheme.onSurface },
        placeholder = (colorsOverride?.placeholder ?: Color.Unspecified).takeOrElse { scheme.onSurfaceVariant },
        container = (colorsOverride?.container ?: Color.Unspecified).takeOrElse { scheme.surfaceVariant },
        tint = (colorsOverride?.tint ?: Color.Unspecified).takeOrElse { scheme.onSurfaceVariant },
        cornerRadius = NativeTheme.tokens.cornerMedium,
        textStyle = MaterialTheme.typography.bodyLarge,
        // Drive the iOS control's light/dark from the surface it actually sits on (card/page/glass), not the page.
        surface = resolveSurfaceFill(LocalNativeSurface.current, scheme.background),
    )
    PlatformNativeSearchBar(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        placeholder = placeholder,
        onSearch = onSearch,
        onCancel = onCancel,
        enabled = enabled,
        showCancelButton = ios.showCancelButton,
        style = style,
        contentDescription = contentDescription,
        testTag = testTag,
    )
}

/** Native search renderer. Android → Material search-styled `TextField`; iOS → `UISearchBar`. */
@Composable
internal expect fun PlatformNativeSearchBar(
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
)
