package io.github.apdelrahman1911.nativecomposekit.components.model

import androidx.compose.runtime.Immutable

/**
 * iOS-only [io.github.apdelrahman1911.nativecomposekit.components.NativeSearchBar] knobs. Each is a
 * documented no-op on Android. Lives in `components.model` beside its sibling
 * [NativeTextFieldIosOptions] so the `*IosOptions` types share one import surface. Compares by value.
 */
@Immutable
public class NativeSearchBarIosOptions(
    /** Shows the native `UISearchBar` Cancel button. Android uses the trailing clear affordance instead. */
    public val showCancelButton: Boolean = false,
) {
    override fun equals(other: Any?): Boolean =
        this === other || (other is NativeSearchBarIosOptions && showCancelButton == other.showCancelButton)

    override fun hashCode(): Int = showCancelButton.hashCode()
}
