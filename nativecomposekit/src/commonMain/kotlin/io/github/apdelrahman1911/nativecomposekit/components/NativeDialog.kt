package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeDialogColors
import io.github.apdelrahman1911.nativecomposekit.theme.LocalNativeSurface
import io.github.apdelrahman1911.nativecomposekit.theme.NativeTheme

/**
 * A general, **custom-content** modal dialog — a themed surface in a Compose `Dialog`, built to fit *your* design
 * system rather than one fixed layout. Every visual is overridable: [icon]/[title]/[actions]/[content] are slots,
 * colors come from [colorsOverride], and shape/elevation/border/padding/alignment are all parameters. Defaults
 * resolve from `NativeKitTheme` and look good out of the box.
 *
 * **Kit thesis — intentionally Compose-on-both (documented exception).** A centered custom-content modal has no
 * single native control to delegate to, so this is the kept Compose primitive for *arbitrary* centered content
 * (a form, a list, an image). Prefer the native paths when they fit: a plain text + buttons system alert →
 * `NativeFeedbackController.alert` (a real `UIAlertController` on iOS); a bottom sheet / detented panel →
 * [NativeSheet] (a real `UISheetPresentationController` on iOS).
 *
 * Shown while it is in composition; [onDismissRequest] fires on scrim tap / back (gated by
 * [dismissOnClickOutside]/[dismissOnBackPress], or supply your own [properties]). [actions] is a trailing,
 * end-aligned button row (omit for an informational dialog).
 *
 * **iOS note:** a `Dialog` mounts a fresh native scene; on its first frame a cut-out `UIKitView` interop region
 * would reveal the system backdrop (a black box in dark mode) until the native view is inserted. Two levers avoid
 * the flash: the whole body composes with `LocalNativeSurface = Unspecified` so `NativeText` takes its Compose-`Text`
 * path (no interop region), and `LocalNativeInteropPlacement = Overlay` so `UIKitView`-backed controls (e.g.
 * `NativeButton` actions) composite ABOVE the opaque surface with no cut-out hole. All component types work inside.
 *
 * `NativeDialog(title = "Rename", onDismissRequest = { open = false },`
 * `  actions = { NativeButton("Save", onClick = ::save) }) { NativeTextField(name, { name = it }) }`
 */
@Composable
public fun NativeDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    title: (@Composable () -> Unit)? = null,
    dismissOnBackPress: Boolean = true,
    dismissOnClickOutside: Boolean = true,
    colorsOverride: NativeDialogColors? = null,
    cornerRadius: Dp? = null,
    elevation: Dp? = null,
    contentPadding: PaddingValues? = null,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    properties: DialogProperties? = null,
    testTag: String? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val colors = colorsOverride ?: NativeDialogColors(
        container = scheme.surface,
        content = scheme.onSurface,
        title = scheme.onSurface,
    )
    val shape = RoundedCornerShape(cornerRadius ?: NativeTheme.tokens.cornerLarge)
    val resolvedElevation = elevation ?: NativeTheme.tokens.elevationOverlay
    val pad = contentPadding ?: PaddingValues(NativeTheme.tokens.spacingLg)
    val tokens = NativeTheme.tokens

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = properties ?: DialogProperties(
            dismissOnBackPress = dismissOnBackPress,
            dismissOnClickOutside = dismissOnClickOutside,
        ),
    ) {
        // Overlay placement so the dialog's native interop (action buttons, any UIKitView in the body) composites
        // above the opaque surface with no cut-out hole — avoids the freshly-mounted scene flashing its host
        // backdrop. See [LocalNativeInteropPlacement].
        CompositionLocalProvider(LocalNativeInteropPlacement provides NativeInteropPlacement.Overlay) {
            var surface: Modifier = modifier.fillMaxWidth()
            if (resolvedElevation > 0.dp) surface = surface.shadow(resolvedElevation, shape)
            surface = surface.clip(shape).background(colors.container)
            if (colors.border.isSpecified) surface = surface.border(1.dp, colors.border, shape)
            testTag?.let { surface = surface.testTag(it) }

            Column(modifier = surface.padding(pad), horizontalAlignment = horizontalAlignment) {
                val columnScope = this
                // Whole card: content color from the theme/override; surface Unspecified so any NativeText takes
                // the Compose-`Text` path (no interop hole / first-frame flash).
                CompositionLocalProvider(
                    LocalContentColor provides colors.content,
                    LocalNativeSurface provides Color.Unspecified,
                ) {
                    if (icon != null) {
                        icon()
                        Spacer(Modifier.height(tokens.spacingSm))
                    }
                    if (title != null) {
                        CompositionLocalProvider(LocalContentColor provides colors.title) { title() }
                        Spacer(Modifier.height(tokens.spacingSm))
                    }
                    with(columnScope) { content() }
                    if (actions != null) {
                        Spacer(Modifier.height(tokens.spacingMd))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(tokens.spacingSm, Alignment.End),
                            verticalAlignment = Alignment.CenterVertically,
                            content = actions,
                        )
                    }
                }
            }
        }
    }
}

/**
 * Convenience overload with a plain-text [title] (rendered in the title slot with the dialog's title color). Use
 * the slot-based [NativeDialog] above when you need a custom title (icon+text, styled, etc.).
 *
 * `NativeDialog(title = "Delete file?", onDismissRequest = { open = false }, actions = { … }) { NativeText("This can't be undone.") }`
 */
@Composable
public fun NativeDialog(
    title: String?,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    dismissOnBackPress: Boolean = true,
    dismissOnClickOutside: Boolean = true,
    colorsOverride: NativeDialogColors? = null,
    cornerRadius: Dp? = null,
    elevation: Dp? = null,
    contentPadding: PaddingValues? = null,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    properties: DialogProperties? = null,
    testTag: String? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val titleSlot: (@Composable () -> Unit)? =
        title?.let { text -> @Composable { Text(text, style = MaterialTheme.typography.titleLarge) } }
    NativeDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        title = titleSlot,
        dismissOnBackPress = dismissOnBackPress,
        dismissOnClickOutside = dismissOnClickOutside,
        colorsOverride = colorsOverride,
        cornerRadius = cornerRadius,
        elevation = elevation,
        contentPadding = contentPadding,
        horizontalAlignment = horizontalAlignment,
        properties = properties,
        testTag = testTag,
        actions = actions,
        content = content,
    )
}
