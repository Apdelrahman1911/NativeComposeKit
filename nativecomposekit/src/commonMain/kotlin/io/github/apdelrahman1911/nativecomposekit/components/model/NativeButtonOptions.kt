package io.github.apdelrahman1911.nativecomposekit.components.model

import androidx.compose.runtime.Immutable

/**
 * iOS background treatment for the button family (`NativeButton` / `NativeIconButton` /
 * `NativeSplitButton`).
 *
 * - [Automatic] (default): the variant's regular flat rendering — exactly the pre-options appearance.
 * - [Glass]: an iOS 26 **Liquid Glass** capsule — a real `UIGlassEffect` that refracts the content
 *   beneath the button and morphs on press. The button's content adopts the system label color (glass
 *   is content-adaptive by design; the variant's container/content colors don't apply). Per the HIG,
 *   glass belongs to elements *floating above content* (action buttons over a scroll, control strips),
 *   not to every inline form button.
 * - [ProminentGlass]: Liquid Glass **tinted with the variant's container color** (a `Primary` button
 *   becomes primary-tinted glass); content keeps the variant's content color.
 *
 * On systems without Liquid Glass (below iOS 26) both glass values silently fall back to [Automatic];
 * with Reduce Transparency enabled, the system renders its own accessible replacement. On Android
 * every value is a documented no-op — the variant renders through Material as always.
 */
public enum class NativeButtonIosBackground { Automatic, Glass, ProminentGlass }

/**
 * iOS-only knobs for the button family. Each is a documented no-op on Android. Lives in
 * `components.model` beside [NativeSearchBarIosOptions] so the `*IosOptions` types share one import
 * surface. Compares by value.
 */
@Immutable
public class NativeButtonIosOptions(
    /** See [NativeButtonIosBackground]. */
    public val background: NativeButtonIosBackground = NativeButtonIosBackground.Automatic,
) {
    override fun equals(other: Any?): Boolean =
        this === other || (other is NativeButtonIosOptions && background == other.background)

    override fun hashCode(): Int = background.hashCode()
}
