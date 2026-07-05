package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.viewinterop.UIKitView
import androidx.compose.ui.viewinterop.remeasureRequester
import androidx.compose.ui.viewinterop.rememberUIKitInteropRemeasureRequester
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeInteropTouch
import io.github.apdelrahman1911.nativecomposekit.theme.LocalNativeSurface
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.NSLineBreakByTruncatingTail
import platform.UIKit.NSLineBreakByWordWrapping
import platform.UIKit.UILabel

/**
 * iOS NativeText. Styling comes entirely from the resolved [textStyle] (theme-derived). Two render paths,
 * chosen by [LocalNativeSurface] (the solid surface a parent published, or `Color.Unspecified`):
 *
 * 1. **Known solid surface** (page background, or a `NativeCard`/custom surface) → a real `UILabel` via
 *    `UIKitView`, background filled **opaquely** with that color. The opaque fill covers the `UIKitView`
 *    interop region, which otherwise exposes the `ComposeUIViewController`'s system backdrop. Native path.
 * 2. **No known solid surface** (`Color.Unspecified` — a Liquid Glass / native-material sheet, via
 *    `NativeAppearanceScope(drawBackground = false)`) → render with Compose `Text`, **not a `UIKitView`**. The
 *    glyphs composite straight over the material with no interop hole and no backing.
 *
 * Why path 2 isn't a native `UILabel`: the black/white rectangle on glass is the **`UIKitView` interop region**
 * exposing the host's system backdrop (`MetalView` clears its canvas to black/white via `canBeOpaque`), not the
 * label's background — so no label/wrapper transparency avoids it. The `placedAsOverlay = true` overlay lever
 * was wired as an opt-in experiment and **tested on-device: it still showed the rectangle** (2026-06-26), so it
 * was removed and Compose `Text` is the **permanent** material-surface path. The only native-text-on-glass
 * option is hosting the text from the SwiftUI/native shell (chrome: nav title/toolbar), not in Compose content.
 * On a solid surface NativeText must still NOT silently fall back to Compose `Text` — raise sizing issues there.
 */
@OptIn(ExperimentalForeignApi::class, ExperimentalComposeUiApi::class)
@Composable
internal actual fun PlatformNativeText(
    text: String,
    modifier: Modifier,
    textStyle: TextStyle,
    maxLines: Int,
    overflow: TextOverflow,
    testTag: String?,
) {
    val surface = LocalNativeSurface.current
    if (surface.isSpecified) {
        // Native path: a real UILabel, filled opaquely with the exact surface color so the UIKitView interop
        // region never reveals the system backdrop. Seeded with the first text/font so the very first Compose
        // measure (which precedes the first `update`) already sees real content instead of an empty label.
        val label = remember {
            UILabel().apply {
                this.text = text
                font = textStyle.toUIFont()
                numberOfLines = if (maxLines == Int.MAX_VALUE) 0L else maxLines.toLong()
            }
        }
        val remeasure = rememberUIKitInteropRemeasureRequester()
        val sizeFp = remember { InteropSizeFingerprint() }
        val layoutDirection = LocalLayoutDirection.current
        val mirroredText = AnnotatedString(text) // captured before the semantics scope shadows `text`
        DynamicTypeRemeasureEffect(remeasure) // live text-size changes rescale glyphs; re-measure the host
        InteropDisposeFailSafe(label) // synchronous ghost-kill; see UiKitInterop.ios.kt
        UIKitView(
            factory = { label },
            // The UILabel is display-only, so the interop node stays out of gesture arbitration
            // (NonInteractive) and mirrors its text into Compose semantics — without the mirror the label
            // would be INVISIBLE to VoiceOver (the a11y mediator ignores un-flagged native views).
            modifier = modifier
                .remeasureRequester(remeasure)
                .semantics { this.text = mirroredText },
            properties = NativeInteropTouch.NonInteractive.toInteropProperties(),
            update = { l ->
                l.text = text
                l.font = textStyle.toUIFont()
                l.adjustsFontForContentSizeCategory = true // honor live Dynamic Type changes
                l.textColor = textStyle.color.toUIColor()
                l.backgroundColor = surface.toUIColor()
                l.numberOfLines = if (maxLines == Int.MAX_VALUE) 0L else maxLines.toLong()
                l.semanticContentAttribute = layoutDirection.toUISemanticContentAttribute()
                l.textAlignment = textStyle.textAlign.toNSTextAlignment(layoutDirection)
                l.lineBreakMode =
                    if (overflow == TextOverflow.Ellipsis) NSLineBreakByTruncatingTail else NSLineBreakByWordWrapping
                testTag?.let { l.setAccessibilityId(it) }
                // Size-affecting inputs changed → re-measure, requested from update so it always lands AFTER
                // they were applied to the label (see InteropSizeFingerprint).
                sizeFp.requestIfChanged(listOf(text, textStyle, maxLines)) { remeasure.requestRemeasure() }
            },
        )
    } else {
        // Glass-safe path: no UIKitView (which would expose the system backdrop as a black box over the
        // material). Compose text composites directly over the native material with no backing. See KDoc.
        Text(
            text = text,
            modifier = if (testTag != null) modifier.testTag(testTag) else modifier,
            style = textStyle,
            maxLines = maxLines,
            overflow = overflow,
        )
    }
}
