package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.github.apdelrahman1911.nativecomposekit.theme.NativeTheme
import io.github.apdelrahman1911.nativecomposekit.theme.LocalNativeSurface

/**
 * A general, **custom-content** modal dialog — a themed [NativeCard] in a Compose `Dialog`.
 *
 * **Kit thesis — intentionally Compose-on-both (documented exception).** A centered custom-content modal has no
 * single native control to delegate to, so this is the kept Compose primitive for *arbitrary* centered content
 * (a form, a list, an image). Use the native paths instead when they fit: a plain text + buttons system alert →
 * `NativeFeedbackController.alert` (a real `UIAlertController` on iOS); a bottom sheet / detented panel →
 * [NativeSheet] (a real `UISheetPresentationController` on iOS). Reach for `NativeDialog` only for custom centered
 * content that those two don't cover.
 *
 * Shown while it's in composition; [onDismissRequest] fires on scrim tap / back (gated by
 * [dismissOnClickOutside]/[dismissOnBackPress]). [title] is an optional heading; [actions] is a trailing
 * (end-aligned) button row; [content] is the dialog body.
 *
 * **iOS note:** a `Dialog` mounts a fresh native scene; on its first frame a cut-out `UIKitView` interop region
 * reveals the system backdrop (a black box in dark mode) until the native view is inserted. Two levers avoid the
 * flash here: the body is composed with `LocalNativeSurface = Unspecified` so `NativeText` takes its Compose-`Text`
 * path (no interop region at all — the same lever used on Liquid Glass surfaces), and the dialog provides
 * `LocalNativeInteropPlacement = Overlay` so the remaining native controls (`NativeButton` actions, and any
 * `UIKitView`-backed control in the body) composite ABOVE the opaque card with no cut-out hole. All component
 * types work normally inside a dialog.
 *
 * `NativeDialog(onDismissRequest = { open = false }, title = "Rename",`
 * `  actions = { NativeButton("Save", onClick = ::save) }) { NativeTextField(name, { name = it }) }`
 */
@Composable
public fun NativeDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    dismissOnBackPress: Boolean = true,
    dismissOnClickOutside: Boolean = true,
    testTag: String? = null,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = dismissOnBackPress,
            dismissOnClickOutside = dismissOnClickOutside,
        ),
    ) {
        // Overlay placement for the dialog's native interop (the action buttons): with overlay there is no
        // cut-out hole, so the freshly mounted dialog scene shows the opaque Compose-drawn card instead of
        // flashing its black host backdrop through a not-yet-filled hole. See [LocalNativeInteropPlacement].
        // (The body text separately avoids interop entirely via the Compose-`Text` path below.)
        CompositionLocalProvider(LocalNativeInteropPlacement provides NativeInteropPlacement.Overlay) {
            NativeCard(modifier = modifier.fillMaxWidth(), variant = NativeCardVariant.Elevated, testTag = testTag) {
                if (title != null) {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = NativeTheme.tokens.spacingSm),
                    )
                }
                // Render the body with LocalNativeSurface = Unspecified so NativeText takes its Compose-`Text`
                // path rather than a UILabel-in-UIKitView (no interop hole, no first-frame flash for text).
                val columnScope = this
                CompositionLocalProvider(LocalNativeSurface provides Color.Unspecified) {
                    with(columnScope) { content() }
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = NativeTheme.tokens.spacingMd),
                    horizontalArrangement = Arrangement.spacedBy(NativeTheme.tokens.spacingSm, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                    content = actions,
                )
            }
        }
    }
}
