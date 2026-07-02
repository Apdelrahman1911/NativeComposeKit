package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.apdelrahman1911.nativecomposekit.theme.NativeTheme

/**
 * Section presentation.
 * - [Grouped] (default): rows sit inside a rounded, clipped inset card — the iOS "inset grouped" settings
 *   look. Assumes the section sits on the page `surface`/`background` (the tonal card reads as raised
 *   against those).
 * - [Plain]: rows run edge-to-edge with no surrounding card (a flat Android-style list).
 */
public enum class NativeListSectionStyle { Grouped, Plain }

/**
 * Groups rows under an optional [header] and [footer] — the standard settings / details section.
 * **Compose-drawn on both platforms.** Pass the rows as a list; the section draws the hairline separators
 * **between** them (never after the last), so there's no per-row book-keeping. [Grouped] wraps the rows in
 * a rounded inset card; [Plain] leaves them flat.
 *
 * The header/footer use muted secondary text; pass an already-uppercased [header] for the classic iOS
 * grouped-header casing (we don't force it, for i18n).
 *
 * ```
 * NativeListSection(header = "Reader", rows = listOf(
 *     { NativeListItem("Direction", trailingText = "L → R", onClick = …) },
 *     { NativeListItem("Theme", trailingText = "Dark", onClick = …) },
 * ))
 * ```
 */
@Composable
public fun NativeListSection(
    rows: List<@Composable () -> Unit>,
    modifier: Modifier = Modifier,
    header: String? = null,
    footer: String? = null,
    style: NativeListSectionStyle = NativeListSectionStyle.Grouped,
    showDividers: Boolean = true,
    dividerInset: Dp = NativeTheme.tokens.spacingMd,
    contentDescription: String? = null,
    testTag: String? = null,
) {
    // NOTE: rows are matched POSITIONALLY across recompositions — right for a static settings section.
    // For a section over a changing collection (delete/insert/reorder), use the keyed items overload so
    // per-row remembered state (a swipe reveal mid-animation, focus) moves WITH its item instead of
    // bleeding into the neighbor that slides into the same position.
    val scheme = MaterialTheme.colorScheme
    val type = MaterialTheme.typography

    var outer = modifier.fillMaxWidth()
    testTag?.let { outer = outer.testTag(it) }
    contentDescription?.let { cd -> outer = outer.semantics { this.contentDescription = cd } }

    // Rows + separators between (never after the last). Reused by both Grouped and Plain.
    val body: @Composable ColumnScope.() -> Unit = {
        rows.forEachIndexed { index, row ->
            row()
            if (showDividers && index < rows.lastIndex) {
                NativeDivider(startIndent = dividerInset, endIndent = dividerInset)
            }
        }
    }

    Column(outer) {
        if (header != null) {
            Text(
                text = header,
                style = type.labelMedium,
                color = scheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(
                        start = NativeTheme.tokens.spacingMd,
                        end = NativeTheme.tokens.spacingMd,
                        bottom = NativeTheme.tokens.spacingXs,
                    )
                    .nativeHeading(), // screen readers can jump between sections
            )
        }
        when (style) {
            NativeListSectionStyle.Grouped ->
                NativeCard(
                    modifier = Modifier.fillMaxWidth(),
                    variant = NativeCardVariant.Filled,
                    contentPadding = PaddingValues(0.dp),
                    content = body,
                )
            NativeListSectionStyle.Plain ->
                Column(Modifier.fillMaxWidth(), content = body)
        }
        if (footer != null) {
            Text(
                text = footer,
                style = type.bodySmall,
                color = scheme.onSurfaceVariant,
                modifier = Modifier.padding(
                    start = NativeTheme.tokens.spacingMd,
                    end = NativeTheme.tokens.spacingMd,
                    top = NativeTheme.tokens.spacingXs,
                ),
            )
        }
    }
}

/**
 * Keyed overload for sections over a **changing collection** (delete/insert/reorder). Each row composes
 * inside `key(key(item))`, so per-row remembered state — a swipe reveal mid-animation, focus, text
 * selection — moves with its item instead of being inherited positionally by whichever item slides into
 * the removed row's slot. Prefer this whenever [items] can change; the positional overload is for static
 * sections.
 *
 * ```
 * NativeListSection(items = people, key = { it.id }) { person ->
 *     NativeListItem(person.name, swipeAction = NativeSwipeAction("Delete", { remove(person) }))
 * }
 * ```
 */
@Composable
public fun <T> NativeListSection(
    items: List<T>,
    key: (T) -> Any,
    modifier: Modifier = Modifier,
    header: String? = null,
    footer: String? = null,
    style: NativeListSectionStyle = NativeListSectionStyle.Grouped,
    showDividers: Boolean = true,
    dividerInset: Dp = NativeTheme.tokens.spacingMd,
    contentDescription: String? = null,
    testTag: String? = null,
    row: @Composable (T) -> Unit,
) {
    NativeListSection(
        rows = items.map { item -> { key(key(item)) { row(item) } } },
        modifier = modifier,
        header = header,
        footer = footer,
        style = style,
        showDividers = showDividers,
        dividerInset = dividerInset,
        contentDescription = contentDescription,
        testTag = testTag,
    )
}
