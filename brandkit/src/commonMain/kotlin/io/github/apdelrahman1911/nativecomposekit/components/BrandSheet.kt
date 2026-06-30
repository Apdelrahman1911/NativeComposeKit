package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** Sheet sizes. [Medium] ≈ half screen, [Large] ≈ full. iOS maps these to `UISheetPresentationController`
 * detents; Android to a partially- vs fully-expanded `ModalBottomSheet`. */
public enum class BrandSheetDetent { Medium, Large }

/**
 * A bottom sheet for a scoped task. Renders the most native presentation per platform: on **iOS** a real
 * `UISheetPresentationController` (native detents, grabber, swipe-to-dismiss, and iOS 26 Liquid Glass) that
 * hosts the Compose [content]; on **Android** a Material `ModalBottomSheet`. Shown while [visible] is true;
 * [onDismissRequest] fires on swipe-down / scrim tap / back.
 *
 * **iOS context boundary:** the iOS sheet hosts [content] in a **separate Compose composition** (a presented
 * `ComposeUIViewController`), which inherits no CompositionLocals. To avoid a default "iOS" look, the
 * component captures the parent's theme (color scheme, typography, shapes), brand tokens/status colors, and
 * layout direction and **re-provides them inside the sheet** — so the content looks like your app (brand
 * colors, dark mode, RTL), not a system default. The hosting `ComposeUIViewController` is **transparent**
 * (`opaque = false`, clear background) so the sheet's **native** material (iOS 26 Liquid Glass) shows through
 * behind the content instead of being covered by an opaque backing; only light/dark is matched. Only
 * **non-theme** providers (e.g. `LocalBrandFeedbackController`) aren't resolvable inside — pass a captured
 * reference if the content needs one. ([modifier] applies to the Android sheet only.)
 */
@Composable
public fun BrandSheet(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    detents: List<BrandSheetDetent> = listOf(BrandSheetDetent.Large),
    showDragHandle: Boolean = true,
    testTag: String? = null,
    content: @Composable () -> Unit,
) {
    PlatformBrandSheet(visible, onDismissRequest, modifier, detents, showDragHandle, testTag, content)
}

@Composable
internal expect fun PlatformBrandSheet(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier,
    detents: List<BrandSheetDetent>,
    showDragHandle: Boolean,
    testTag: String?,
    content: @Composable () -> Unit,
)
