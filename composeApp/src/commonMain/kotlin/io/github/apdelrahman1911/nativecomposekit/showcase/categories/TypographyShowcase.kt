package io.github.apdelrahman1911.nativecomposekit.showcase.categories

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.apdelrahman1911.nativecomposekit.components.NativeText
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import io.github.apdelrahman1911.nativecomposekit.components.NativeToggle
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeTextStyle
import io.github.apdelrahman1911.nativecomposekit.showcase.ExampleLabel
import io.github.apdelrahman1911.nativecomposekit.showcase.Note
import io.github.apdelrahman1911.nativecomposekit.showcase.ShowcaseScreen
import io.github.apdelrahman1911.nativecomposekit.showcase.ShowcaseSection
import io.github.apdelrahman1911.nativecomposekit.showcase.WhenToUse
import io.github.apdelrahman1911.nativecomposekit.theme.NativeAppearance

/**
 * Typography and theme: one text primitive with a four-role type scale, plus the process-wide appearance
 * source that flips dark mode and RTL across every composition at once. NativeText renders the most native
 * label per platform (Compose Text on Android, a UILabel on iOS); AppTheme is where every default — colors,
 * type, shapes, tokens — is defined once.
 */
@Composable
fun TypographyShowcase() = ShowcaseScreen(
    intro = "One text primitive with a four-role type scale (Display / Title / Body / Label), and the " +
        "process-wide appearance switch for dark mode and RTL. Every default lives in AppTheme; components " +
        "read it rather than hardcoding colors or fonts.",
) {
    TypeScaleSection()
    WeightAlignTruncateSection()
    AppearanceSection()
}

// ---------------------------------------------------------------------------
// NativeText — the type scale. One role per line, named.
// ---------------------------------------------------------------------------

@Composable
private fun TypeScaleSection() {
    ShowcaseSection(
        title = "Type scale",
        description = "Pick a role by meaning, not size: Display for a screen's hero line, Title for a " +
            "section header, Body for prose, Label for captions and metadata.",
    ) {
        WhenToUse(
            "Any label, heading, or paragraph in content — reach for NativeText, not a raw platform label.",
            "You want the platform's native font and Dynamic Type behavior (iOS UILabel) for free.",
            "Override color / fontWeight / align only to adjust a role, not to rebuild one.",
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ExampleLabel("Display")
            NativeText("Good morning", style = NativeTextStyle.Display)

            ExampleLabel("Title")
            NativeText("Today's reading", style = NativeTextStyle.Title)

            ExampleLabel("Body")
            NativeText(
                "The quick brown fox jumps over the lazy dog. Body is the default role and carries most " +
                    "running text in a screen.",
                style = NativeTextStyle.Body,
            )

            ExampleLabel("Label")
            NativeText("UPDATED 2 MINUTES AGO", style = NativeTextStyle.Label)
        }

        ExampleLabel("Colored from the theme (primary)")
        NativeText(
            "Themed accent text",
            style = NativeTextStyle.Body,
            color = MaterialTheme.colorScheme.primary,
        )

        ExampleLabel("Muted metadata (onSurfaceVariant)")
        NativeText(
            "Synced across 3 devices",
            style = NativeTextStyle.Label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ---------------------------------------------------------------------------
// Weight, alignment, truncation — the per-call overrides.
// ---------------------------------------------------------------------------

@Composable
private fun WeightAlignTruncateSection() {
    ShowcaseSection(
        title = "Weight, alignment, truncation",
        description = "Each role ships a sensible weight and left alignment; override per call when a layout " +
            "needs it.",
    ) {
        ExampleLabel("fontWeight override")
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            NativeText("Regular total", style = NativeTextStyle.Body, fontWeight = FontWeight.Normal)
            NativeText("$148.00 due today", style = NativeTextStyle.Body, fontWeight = FontWeight.SemiBold)
        }

        ExampleLabel("align = Center")
        NativeText(
            "No items match this filter",
            style = NativeTextStyle.Body,
            align = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
        )

        ExampleLabel("maxLines = 1, overflow = Ellipsis")
        NativeText(
            "A long note that runs past the available width and is cut with a tail ellipsis instead of wrapping.",
            style = NativeTextStyle.Body,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        ExampleLabel("maxLines = 2, overflow = Ellipsis")
        NativeText(
            "A two-line summary for a list row: it wraps once, then truncates the rest so every row keeps the " +
                "same height no matter how much text the item carries.",
            style = NativeTextStyle.Body,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

// ---------------------------------------------------------------------------
// NativeAppearance — process-wide dark mode + RTL.
// ---------------------------------------------------------------------------

@Composable
private fun AppearanceSection() {
    // NativeAppearance is snapshot-backed, so reading it here subscribes this section to its changes —
    // no local remember needed. Both overrides are null while following the system; resolve the effective
    // values the same way NativeAppearanceScope does (the applied layout direction IS the effective RTL).
    val dark = NativeAppearance.darkOverride ?: isSystemInDarkTheme()
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    ShowcaseSection(
        title = "Appearance: dark mode & RTL",
        description = "NativeAppearance is the process-wide source of truth. Flipping it recomposes every " +
            "Compose composition at once, so these toggles re-skin the whole app, not just this screen.",
    ) {
        AppearanceRow(
            label = "Dark mode",
            checked = dark,
            onCheckedChange = { NativeAppearance.setDark(it) },
        )
        AppearanceRow(
            label = "Right-to-left layout",
            checked = rtl,
            onCheckedChange = { NativeAppearance.setRtl(it) },
        )

        Note(
            "AppTheme is the single styling source — colors, the type scale, shapes, and tokens are defined " +
                "there once and read through MaterialTheme + NativeTheme.tokens, so nothing hardcodes a hex or " +
                "a font. setDark also flips the native iOS chrome (window, nav and tab bars) so it matches the " +
                "Compose content.",
        )
    }
}

@Composable
private fun AppearanceRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NativeText(label, style = NativeTextStyle.Body)
        NativeToggle(checked = checked, onCheckedChange = onCheckedChange)
    }
}
