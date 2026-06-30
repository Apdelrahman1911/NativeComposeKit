package com.ukkera.brandkit.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.ukkera.brandkit.components.internal.resolveSurfaceFill
import com.ukkera.brandkit.components.model.ResolvedSearchStyle
import com.ukkera.brandkit.theme.BrandTheme
import com.ukkera.brandkit.theme.LocalBrandSurface

/** iOS-only [BrandSearchBar] knobs. Each is a documented no-op on Android. */
@Immutable
public data class BrandSearchBarIosOptions(
    /** Shows the native `UISearchBar` Cancel button. Android uses the trailing clear affordance instead. */
    val showCancelButton: Boolean = false,
)

/**
 * An inline search field — browse/search screens. Renders the most native control per platform: on **iOS**
 * a real `UISearchBar` (magnifier, rounded field, optional Cancel button, the system search keyboard); on
 * **Android** a Material search-styled, indicator-less rounded text field with a leading magnifier and a
 * trailing clear button. Distinct from a nav-bar `.searchable` (that stays native-shell chrome) — this is a
 * leaf control you place in the content.
 *
 * [onSearch] fires on the keyboard's Search/Return key; [onCancel] on the iOS Cancel button (and the
 * Android clear button, which also clears the text). iOS-only knobs (the Cancel button) live in
 * [ios] = [BrandSearchBarIosOptions]; they are documented no-ops on Android (which uses the trailing clear
 * affordance instead).
 *
 * `BrandSearchBar(query, onValueChange = { query = it }, onSearch = { run(query) })`
 */
@Composable
public fun BrandSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search",
    onSearch: (() -> Unit)? = null,
    onCancel: (() -> Unit)? = null,
    enabled: Boolean = true,
    contentDescription: String? = null,
    testTag: String? = null,
    ios: BrandSearchBarIosOptions = BrandSearchBarIosOptions(),
) {
    val scheme = MaterialTheme.colorScheme
    val style = ResolvedSearchStyle(
        text = scheme.onSurface,
        placeholder = scheme.onSurfaceVariant,
        container = scheme.surfaceVariant,
        tint = scheme.onSurfaceVariant,
        cornerRadius = BrandTheme.tokens.cornerMedium,
        textStyle = MaterialTheme.typography.bodyLarge,
        // Drive the iOS control's light/dark from the surface it actually sits on (card/page/glass), not the page.
        surface = resolveSurfaceFill(LocalBrandSurface.current, scheme.background),
    )
    PlatformBrandSearchBar(
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
internal expect fun PlatformBrandSearchBar(
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
