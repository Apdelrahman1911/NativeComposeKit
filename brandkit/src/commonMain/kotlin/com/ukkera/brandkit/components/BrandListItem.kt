package com.ukkera.brandkit.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ukkera.brandkit.components.internal.resolveSurfaceFill
import com.ukkera.brandkit.theme.BrandTheme
import com.ukkera.brandkit.theme.LocalBrandSurface

/**
 * A single list row — the backbone of settings screens, chapter lists, and navigation menus.
 * **Compose-drawn on both platforms** (a row is layout, not a native leaf control). Rich but optional:
 * an [overline]/[headline]/[supporting] text column, a [leading] slot (icon / [BrandAvatar] / cover), and
 * a trailing affordance ([trailingText] for a value, [trailing] for a custom control such as a
 * [BrandToggle]). Pass [onClick] to make the row tappable; [showChevron] then adds a **layout-direction
 * aware** disclosure indicator (defaults on for tappable rows — an iOS idiom that also reads well on
 * Android for navigation).
 *
 * Accessibility: a tappable row with no interactive [trailing] is merged into a single focus target; when
 * [trailing] holds its own control (e.g. a toggle) the row is left unmerged so that control stays
 * focusable. A row with an interactive [trailing] is therefore usually left non-clickable (tap the control).
 *
 * [showDivider] draws a bottom hairline (for standalone rows); inside a [BrandListSection] leave it off —
 * the section draws the separators between rows for you.
 */
/**
 * A trailing swipe action for a [BrandListItem] (archive, delete, …). [onAction] runs when the row is
 * swiped past the threshold (right-to-left); the row then snaps back — if the action deletes the item,
 * remove it from your list in [onAction] and the row disappears. [destructive] tints the reveal red.
 */
@Immutable
public data class BrandSwipeAction(
    val label: String,
    val onAction: () -> Unit,
    /** Decorative reveal glyph — a plain Compose [ImageVector] (the reveal is Compose-drawn on both platforms). */
    val icon: ImageVector? = null,
    val destructive: Boolean = false,
)

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
public fun BrandListItem(
    headline: String,
    modifier: Modifier = Modifier,
    overline: String? = null,
    supporting: String? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    trailingText: String? = null,
    onClick: (() -> Unit)? = null,
    onClickLabel: String? = null,
    onLongClick: (() -> Unit)? = null,
    onLongClickLabel: String? = null,
    enabled: Boolean = true,
    showChevron: Boolean = onClick != null,
    showDivider: Boolean = false,
    swipeAction: BrandSwipeAction? = null,
    minHeight: Dp = 56.dp,
    contentPadding: PaddingValues? = null,
    contentDescription: String? = null,
    testTag: String? = null,
) {
    val scheme = MaterialTheme.colorScheme
    val type = MaterialTheme.typography
    val headlineColor = if (enabled) scheme.onSurface else scheme.onSurface.copy(alpha = 0.38f)
    val secondaryColor = if (enabled) scheme.onSurfaceVariant else scheme.onSurfaceVariant.copy(alpha = 0.38f)
    val pad = contentPadding ?: PaddingValues(
        horizontal = BrandTheme.tokens.spacingMd,
        vertical = BrandTheme.tokens.spacingSm + BrandTheme.tokens.spacingXs,
    )

    Column(modifier.fillMaxWidth()) {
        // Gesture-only affordances (swipe, long-press) are invisible to screen readers, so expose them as
        // explicit custom accessibility actions. Long-press only when the caller named it via onLongClickLabel.
        val sa = swipeAction
        val olc = onLongClick
        val a11yActions = buildList {
            if (sa != null) add(CustomAccessibilityAction(sa.label) { sa.onAction(); true })
            if (olc != null && onLongClickLabel != null) add(CustomAccessibilityAction(onLongClickLabel) { olc(); true })
        }
        // Click + accessibility live on ONE node (the row). Merge children into a single focus target for a
        // plain navigational/long-press/swipe row, but NOT when `trailing` holds its own interactive control.
        val merge = (onClick != null || onLongClick != null || a11yActions.isNotEmpty()) && trailing == null
        var row = Modifier.fillMaxWidth()
        if (onClick != null || onLongClick != null) {
            row = row.combinedClickable(
                enabled = enabled,
                onClickLabel = onClickLabel,
                role = Role.Button,
                onLongClickLabel = onLongClickLabel,
                onClick = onClick ?: {},
                onLongClick = onLongClick,
            )
        }
        if (merge || contentDescription != null || a11yActions.isNotEmpty()) {
            row = row.semantics(mergeDescendants = merge) {
                if (contentDescription != null) this.contentDescription = contentDescription
                if (a11yActions.isNotEmpty()) this.customActions = a11yActions
            }
        }
        testTag?.let { row = row.testTag(it) }

        val rowUi: @Composable () -> Unit = {
            Row(
                modifier = row.heightIn(min = minHeight).padding(pad),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(BrandTheme.tokens.spacingMd),
            ) {
                if (leading != null) {
                    Box(contentAlignment = Alignment.Center) { leading() }
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(1.dp),
                ) {
                    if (overline != null) {
                        Text(overline, style = type.labelSmall, color = secondaryColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Text(headline, style = type.bodyLarge, color = headlineColor, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    if (supporting != null) {
                        Text(supporting, style = type.bodyMedium, color = secondaryColor, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                }
                if (trailingText != null) {
                    Text(
                        trailingText,
                        style = type.bodyMedium,
                        color = secondaryColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = 140.dp),
                    )
                }
                if (trailing != null) {
                    Box(contentAlignment = Alignment.Center) { trailing() }
                }
                if (showChevron) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = secondaryColor,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }

        if (swipeAction != null) {
            // Swipe right-to-left to reveal + fire the action, then snap back (the caller removes the item in
            // onAction if it deletes). Driven by currentValue + reset() — the non-deprecated equivalent of a
            // veto callback.
            val dismissState = rememberSwipeToDismissBoxState()
            LaunchedEffect(dismissState.currentValue) {
                if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
                    swipeAction.onAction()
                    dismissState.reset()
                }
            }
            // The foreground must be OPAQUE or the reveal shows through at rest. Fill with the surface the row
            // sits on (the published LocalBrandSurface — page background or card container), falling back to
            // `surface` when unknown (e.g. a Liquid Glass host).
            val rowSurface = resolveSurfaceFill(LocalBrandSurface.current, scheme.surface)
            SwipeToDismissBox(
                state = dismissState,
                enableDismissFromStartToEnd = false,
                backgroundContent = { SwipeActionBackground(swipeAction, scheme) },
            ) {
                Box(Modifier.fillMaxWidth().background(rowSurface)) { rowUi() }
            }
        } else {
            rowUi()
        }

        if (showDivider) {
            BrandDivider(startIndent = BrandTheme.tokens.spacingMd, endIndent = BrandTheme.tokens.spacingMd)
        }
    }
}

/** The colored reveal behind a [BrandListItem] swipe action (trailing-aligned icon + label). */
@Composable
private fun SwipeActionBackground(action: BrandSwipeAction, scheme: ColorScheme) {
    val bg = if (action.destructive) scheme.error else scheme.primary
    val fg = if (action.destructive) scheme.onError else scheme.onPrimary
    Box(
        modifier = Modifier.fillMaxSize().background(bg).padding(horizontal = BrandTheme.tokens.spacingLg),
        contentAlignment = Alignment.CenterEnd,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(BrandTheme.tokens.spacingSm),
        ) {
            action.icon?.let {
                Icon(it, contentDescription = null, tint = fg, modifier = Modifier.size(20.dp))
            }
            Text(action.label, color = fg, style = MaterialTheme.typography.labelLarge)
        }
    }
}
