package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics

/**
 * A small status/count badge (unread chapters, notification count, a "NEW" dot). **Compose-drawn on both
 * platforms** (a badge is a styled overlay, not a native leaf control). With [count] null it's a small
 * dot; with a positive count it's a numbered pill capped at [maxCount] (e.g. `100 → "99+"`); a
 * **non-positive count renders nothing** (the common "no badge when zero" UX). Defaults to the classic
 * unread red (`error`); pass [containerColor]/[contentColor] (e.g. `BrandTheme.statusColors.success`) for a
 * semantic badge. A numbered badge announces its count to screen readers by default — pass a contextual
 * [contentDescription] ("5 unread") when the bare number is ambiguous. Overlay it with [BrandBadgedBox].
 */
@Composable
public fun BrandBadge(
    modifier: Modifier = Modifier,
    count: Int? = null,
    maxCount: Int = 99,
    containerColor: Color? = null,
    contentColor: Color? = null,
    contentDescription: String? = null,
    testTag: String? = null,
) {
    if (!badgeIsVisible(count)) return // no badge when zero/negative

    val scheme = MaterialTheme.colorScheme
    val container = containerColor ?: scheme.error
    val onContainer = contentColor ?: scheme.onError

    if (count == null) {
        var m = modifier
        testTag?.let { m = m.testTag(it) }
        contentDescription?.let { cd -> m = m.semantics { this.contentDescription = cd } }
        Badge(modifier = m, containerColor = container, contentColor = onContainer)
    } else {
        val display = if (count > maxCount) "$maxCount+" else count.toString()
        val cd = contentDescription ?: display
        var m = modifier.semantics { this.contentDescription = cd }
        testTag?.let { m = m.testTag(it) }
        Badge(modifier = m, containerColor = container, contentColor = onContainer) { Text(display) }
    }
}

/**
 * Whether [BrandBadge] renders anything for [count]: a null count is a dot (visible), a positive count is a
 * numbered pill (visible), and a non-positive count renders nothing (the "no badge when zero" UX).
 */
internal fun badgeIsVisible(count: Int?): Boolean = count == null || count > 0

/**
 * Overlays [badge] on the top-end corner of [content] (e.g. a tab icon or manga cover). The badge anchors
 * correctly in RTL (top-start) because it uses Material's `BadgedBox`.
 *
 * `BrandBadgedBox(badge = { BrandBadge(count = 3) }) { Icon(...) }`
 */
@Composable
public fun BrandBadgedBox(
    badge: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    BadgedBox(badge = { badge() }, modifier = modifier, content = content)
}
