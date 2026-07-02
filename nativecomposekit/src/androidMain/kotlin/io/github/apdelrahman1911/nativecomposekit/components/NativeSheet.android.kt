package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal actual fun PlatformNativeSheet(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier,
    detents: List<NativeSheetDetent>,
    showDragHandle: Boolean,
    testTag: String?,
    content: @Composable () -> Unit,
) {
    // A Medium detent ⇒ allow the partially-expanded state; otherwise the sheet only goes full.
    val skipPartial = !detents.contains(NativeSheetDetent.Medium)
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = skipPartial)
    // Keep the sheet composed through its exit animation: on a programmatic dismiss (visible → false) animate
    // it down via state.hide() BEFORE leaving composition, instead of yanking it (`if (!visible) return`).
    var rendered by remember { mutableStateOf(visible) }
    LaunchedEffect(visible) {
        if (visible) {
            if (rendered) {
                // visible flipped back to true while a hide() from the false branch was still animating:
                // that hide was cancelled by this effect restart, leaving the sheet composed but partially
                // swiped down — show() settles it back to expanded (a no-op if it never started hiding).
                state.show()
            } else {
                rendered = true
            }
        } else if (rendered) {
            state.hide()
            rendered = false
        }
    }
    if (!rendered) return
    val sheetModifier = if (testTag != null) modifier.testTag(testTag) else modifier
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = sheetModifier,
        sheetState = state,
        dragHandle = if (showDragHandle) ({ BottomSheetDefaults.DragHandle() }) else null,
    ) {
        content()
    }
}
