package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * A transient label that floats above an anchor on long-press (touch) — manga/feature hints, icon-button
 * affordance names. **Compose-drawn on both platforms** (Material `TooltipBox` + `PlainTooltip`, themed by
 * AppTheme). Wrap the anchor as [content]; [text] is the tip. Distinct from [NativePopover] (a richer,
 * caller-controlled surface): a tooltip is a short, self-dismissing system-style hint.
 *
 * `NativeTooltip("Add to library") { NativeIconButton(addIcon, ::add, contentDescription = "Add") }`
 *
 * **Deprecated (kit thesis — native-per-platform).** A Material-only, Compose-on-both wrapper with no native
 * iOS renderer. iOS has no hover-tooltip idiom (a long-press maps to a context menu), so this adds no native
 * value. Prefer inline helper text, an accessible `contentDescription`, or a [NativePopover] for caller-controlled
 * contextual content. Scheduled for removal in a later release.
 */
@Deprecated(
    message = "Material-only wrapper with no native iOS idiom (long-press = context menu on iOS). Prefer inline " +
        "helper text / contentDescription, or NativePopover for caller-controlled contextual content.",
    level = DeprecationLevel.WARNING,
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun NativeTooltip(
    text: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = { PlainTooltip { Text(text) } },
        state = rememberTooltipState(),
        modifier = modifier,
        content = content,
    )
}
