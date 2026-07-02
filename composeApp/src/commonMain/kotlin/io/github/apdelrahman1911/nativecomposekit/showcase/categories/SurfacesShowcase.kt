package io.github.apdelrahman1911.nativecomposekit.showcase.categories

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.apdelrahman1911.nativecomposekit.components.NativeButton
import io.github.apdelrahman1911.nativecomposekit.components.NativeCard
import io.github.apdelrahman1911.nativecomposekit.components.NativeCardVariant
import io.github.apdelrahman1911.nativecomposekit.components.NativeDivider
import io.github.apdelrahman1911.nativecomposekit.components.NativeDividerOrientation
import io.github.apdelrahman1911.nativecomposekit.components.NativeText
import io.github.apdelrahman1911.nativecomposekit.components.feedback.LocalNativeFeedbackController
import io.github.apdelrahman1911.nativecomposekit.components.feedback.NativeFeedbackStatus
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeButtonSize
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeButtonVariant
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeTextStyle
import io.github.apdelrahman1911.nativecomposekit.showcase.ExampleLabel
import io.github.apdelrahman1911.nativecomposekit.showcase.Note
import io.github.apdelrahman1911.nativecomposekit.showcase.ShowcaseScreen
import io.github.apdelrahman1911.nativecomposekit.showcase.ShowcaseSection
import io.github.apdelrahman1911.nativecomposekit.showcase.WhenToUse

/**
 * "Cards & Surfaces" — the containers and separators that group content and build screen chrome.
 * NativeCard is the surface primitive (three variants, optionally tappable); NativeDivider is the
 * hairline between rows or columns; NativeScaffold + NativeTopBar frame whole screens.
 */
@Composable
fun SurfacesShowcase() = ShowcaseScreen(
    intro = "Cards group related content onto a themed surface; dividers separate items inside or between " +
        "those groups. Cards and the page scaffold both publish their surface color so nested controls " +
        "theme against the right background.",
) {
    val feedback = LocalNativeFeedbackController.current

    ShowcaseSection(
        title = "Card variants",
        description = "Three surface treatments. Pick by how much the card should stand off its background, " +
            "not by importance — they all read as one container.",
    ) {
        WhenToUse(
            "Filled — the default content container; a tonal surface that reads as grouped on the page.",
            "Elevated — lift a card off a busy or scrolling background with a soft shadow.",
            "Outlined — the quietest separation: a hairline, no shadow, no tonal shift.",
        )

        ExampleLabel("Filled")
        NativeCard(variant = NativeCardVariant.Filled, modifier = Modifier.fillMaxWidth()) {
            NativeText("Filled", style = NativeTextStyle.Title)
            NativeText("Tonal surface (surfaceVariant). The default.", style = NativeTextStyle.Body)
        }

        ExampleLabel("Elevated")
        NativeCard(variant = NativeCardVariant.Elevated, modifier = Modifier.fillMaxWidth()) {
            NativeText("Elevated", style = NativeTextStyle.Title)
            NativeText("Raised container tone with a soft shadow.", style = NativeTextStyle.Body)
        }

        ExampleLabel("Outlined")
        NativeCard(variant = NativeCardVariant.Outlined, modifier = Modifier.fillMaxWidth()) {
            NativeText("Outlined", style = NativeTextStyle.Title)
            NativeText("Page surface with a hairline outline.", style = NativeTextStyle.Body)
        }
    }

    ShowcaseSection(
        title = "Tappable card",
        description = "Pass onClick to make the whole card a button — Material ripple, clipped to the corner " +
            "radius. Used here as a navigation tile.",
    ) {
        ExampleLabel("Navigation tile")
        NavigationTile(
            title = "Downloads",
            detail = "3 series · 412 MB",
            onClick = { feedback.toast("Open Downloads", status = NativeFeedbackStatus.Info) },
        )

        ExampleLabel("Disabled tile")
        NavigationTile(
            title = "Sync",
            detail = "Sign in to enable",
            enabled = false,
            onClick = { feedback.toast("Sync", status = NativeFeedbackStatus.Info) },
        )
    }

    ShowcaseSection(
        title = "Content card",
        description = "A card carrying its own heading, body, and a single inline action — the common " +
            "promo / detail block.",
    ) {
        var dismissed by remember { mutableStateOf(false) }
        if (dismissed) {
            ExampleLabel("Dismissed")
            NativeCard(variant = NativeCardVariant.Outlined, modifier = Modifier.fillMaxWidth()) {
                NativeText("Tip dismissed.", style = NativeTextStyle.Body)
                NativeButton(
                    "Show again",
                    { dismissed = false },
                    variant = NativeButtonVariant.Tertiary,
                    size = NativeButtonSize.Small,
                )
            }
        } else {
            NativeCard(variant = NativeCardVariant.Elevated, modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    NativeText("Offline reading", style = NativeTextStyle.Title)
                    NativeText(
                        "Download a series to read without a connection. Downloads stay until you remove them.",
                        style = NativeTextStyle.Body,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        NativeButton(
                            "Manage",
                            { feedback.toast("Manage downloads", status = NativeFeedbackStatus.Info) },
                            variant = NativeButtonVariant.Secondary,
                            size = NativeButtonSize.Small,
                        )
                        NativeButton(
                            "Dismiss",
                            { dismissed = true },
                            variant = NativeButtonVariant.Tertiary,
                            size = NativeButtonSize.Small,
                        )
                    }
                }
            }
        }
    }

    ShowcaseSection(
        title = "Dividers",
        description = "A hairline separator. Horizontal between stacked rows; vertical between side-by-side " +
            "items. Horizontal indents are layout-direction aware.",
    ) {
        WhenToUse(
            "Separate stacked rows that share a card without wrapping each in its own surface.",
            "Split two inline items in a Row — give the vertical divider a bounded height.",
            "For a list of rows, prefer NativeListSection, which draws the separators for you.",
        )

        ExampleLabel("Horizontal — between rows")
        NativeCard(variant = NativeCardVariant.Outlined, modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AccountRow("Plan", "Pro")
                NativeDivider()
                AccountRow("Renews", "Jul 30, 2026")
                NativeDivider()
                AccountRow("Seats", "4 of 5")
            }
        }

        ExampleLabel("Vertical — between two items in a Row")
        NativeCard(variant = NativeCardVariant.Filled, modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().height(44.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StatCell("128", "Read")
                NativeDivider(
                    orientation = NativeDividerOrientation.Vertical,
                    modifier = Modifier.height(32.dp),
                )
                StatCell("12", "Following")
            }
        }
    }

    ShowcaseSection(
        title = "Screen chrome",
        description = "NativeScaffold and NativeTopBar frame an entire screen — top bar, content, optional " +
            "bottom bar and FAB. This showcase already sits inside that chrome.",
    ) {
        Note(
            "NativeScaffold lays out a screen (top bar + content + bottom bar + FAB) decoupled from navigation; " +
                "pair its topBar slot with NativeTopBar, and apply the PaddingValues it hands content so the " +
                "content clears the bars. NativeTopBar is the in-content/Android bar (the iOS nav bar belongs to " +
                "the SwiftUI shell). Both the scaffold and every NativeCard publish their surface color via " +
                "LocalNativeSurface, so nested native controls probe the right background and theme correctly.",
        )
    }
}

/** A tappable card acting as a navigation row: title, detail, trailing chevron-style cue. */
@Composable
private fun NavigationTile(
    title: String,
    detail: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    NativeCard(
        variant = NativeCardVariant.Filled,
        onClick = onClick,
        onClickLabel = title,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                NativeText(title, style = NativeTextStyle.Body, fontWeight = FontWeight.Medium)
                NativeText(detail, style = NativeTextStyle.Label)
            }
            NativeText("›", style = NativeTextStyle.Title)
        }
    }
}

/** A label/value row used inside a divider-separated card. */
@Composable
private fun AccountRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NativeText(label, style = NativeTextStyle.Body)
        NativeText(value, style = NativeTextStyle.Body, fontWeight = FontWeight.Medium)
    }
}

/** A small stacked metric used on either side of a vertical divider. */
@Composable
private fun StatCell(value: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 4.dp),
    ) {
        NativeText(value, style = NativeTextStyle.Title)
        NativeText(label, style = NativeTextStyle.Label)
    }
}
