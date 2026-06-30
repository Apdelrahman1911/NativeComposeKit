package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.apdelrahman1911.nativecomposekit.theme.BrandTheme

/**
 * Section presentation.
 * - [Grouped] (default): rows sit inside a rounded, clipped inset card — the iOS "inset grouped" settings
 *   look. Assumes the section sits on the page `surface`/`background` (the tonal card reads as raised
 *   against those).
 * - [Plain]: rows run edge-to-edge with no surrounding card (a flat Android-style list).
 */
public enum class BrandListSectionStyle { Grouped, Plain }

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
 * BrandListSection(header = "Reader", rows = listOf(
 *     { BrandListItem("Direction", trailingText = "L → R", onClick = …) },
 *     { BrandListItem("Theme", trailingText = "Dark", onClick = …) },
 * ))
 * ```
 */
@Composable
public fun BrandListSection(
    rows: List<@Composable () -> Unit>,
    modifier: Modifier = Modifier,
    header: String? = null,
    footer: String? = null,
    style: BrandListSectionStyle = BrandListSectionStyle.Grouped,
    showDividers: Boolean = true,
    dividerInset: Dp = BrandTheme.tokens.spacingMd,
    contentDescription: String? = null,
    testTag: String? = null,
) {
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
                BrandDivider(startIndent = dividerInset, endIndent = dividerInset)
            }
        }
    }

    Column(outer) {
        if (header != null) {
            Text(
                text = header,
                style = type.labelMedium,
                color = scheme.onSurfaceVariant,
                modifier = Modifier.padding(
                    start = BrandTheme.tokens.spacingMd,
                    end = BrandTheme.tokens.spacingMd,
                    bottom = BrandTheme.tokens.spacingXs,
                ),
            )
        }
        when (style) {
            BrandListSectionStyle.Grouped ->
                BrandCard(
                    modifier = Modifier.fillMaxWidth(),
                    variant = BrandCardVariant.Filled,
                    contentPadding = PaddingValues(0.dp),
                    content = body,
                )
            BrandListSectionStyle.Plain ->
                Column(Modifier.fillMaxWidth(), content = body)
        }
        if (footer != null) {
            Text(
                text = footer,
                style = type.bodySmall,
                color = scheme.onSurfaceVariant,
                modifier = Modifier.padding(
                    start = BrandTheme.tokens.spacingMd,
                    end = BrandTheme.tokens.spacingMd,
                    top = BrandTheme.tokens.spacingXs,
                ),
            )
        }
    }
}
