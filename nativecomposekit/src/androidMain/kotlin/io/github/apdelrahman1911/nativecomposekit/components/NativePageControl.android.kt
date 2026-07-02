package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.apdelrahman1911.nativecomposekit.components.model.ResolvedPageControlStyle
import io.github.apdelrahman1911.nativecomposekit.theme.LocalNativeStrings

/** Android page control: a branded row of dots (the current one tinted [ResolvedPageControlStyle.current]). */
@Composable
internal actual fun PlatformNativePageControl(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier,
    onCurrentPageChange: ((Int) -> Unit)?,
    style: ResolvedPageControlStyle,
    contentDescription: String?,
    testTag: String?,
) {
    // Match iOS `hidesForSinglePage`: one page (or none) has nothing to indicate, so a lone always-active
    // dot would only add noise — render nothing.
    if (pageCount <= 1) return
    val strings = LocalNativeStrings.current
    var m = modifier
    testTag?.let { m = m.testTag(it) }
    val desc = contentDescription ?: if (pageCount > 0) strings.pageDescription(currentPage + 1, pageCount) else null
    desc?.let { d -> m = m.semantics { this.contentDescription = d } }

    // Interactive dots get a ≥48dp touch target (with the 8dp visual centered); the larger targets supply
    // their own spacing, so drop the inter-dot gap when interactive. Display-only dots stay compact.
    val interactive = onCurrentPageChange != null
    Row(
        modifier = m,
        horizontalArrangement = Arrangement.spacedBy(if (interactive) 0.dp else 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        for (i in 0 until pageCount) {
            val dotColor = if (i == currentPage) style.current else style.inactive
            val cb = onCurrentPageChange
            if (cb != null) {
                Box(
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .clickable(onClickLabel = strings.goToPage(i + 1), role = Role.Button) { cb(i) },
                    contentAlignment = Alignment.Center,
                ) {
                    Box(Modifier.size(8.dp).clip(CircleShape).background(dotColor))
                }
            } else {
                Box(Modifier.size(8.dp).clip(CircleShape).background(dotColor))
            }
        }
    }
}
