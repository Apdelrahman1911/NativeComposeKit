package io.github.apdelrahman1911.nativecomposekit.showcase

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.apdelrahman1911.nativecomposekit.app.LocalNativeContentBottomInset
import io.github.apdelrahman1911.nativecomposekit.components.NativeCard
import io.github.apdelrahman1911.nativecomposekit.components.NativeCardVariant
import io.github.apdelrahman1911.nativecomposekit.components.NativeText
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeTextStyle
import io.github.apdelrahman1911.nativecomposekit.components.nativeImePadding
import io.github.apdelrahman1911.nativecomposekit.theme.NativeTheme

/**
 * Shared layout pieces for the component showcase. They keep every category screen consistent: the same
 * scroll container, section rhythm, "when to use" callout, and notes. Screens compose these around real
 * `Native*` components rather than re-styling things by hand.
 */

/**
 * The scroll container every showcase category screen sits in. [intro] is a one- or two-sentence summary shown
 * under the (native) nav-bar title. The screen is hosted inside the nav stack, which already draws the title bar,
 * so there is no in-content top bar here.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowcaseScreen(intro: String, content: @Composable ColumnScope.() -> Unit) {
    // The native nav bar overlays the content on iOS; begin below it (0 on Android — its Material bar reserves
    // space) while the scroll viewport still fills behind the bar so content scrolls under the Liquid Glass.
    val bottomInset = LocalNativeContentBottomInset.current
    Scaffold(contentWindowInsets = WindowInsets(0, 0, 0, 0)) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .verticalScroll(rememberScrollState())
                // Inset INSIDE the scroll: it extends the scrollable content's bottom (so a focused field can
                // scroll clear of the keyboard, and the last content clears the overlaying tab bar) instead of
                // shrinking the viewport and clipping the rows.
                .nativeImePadding()
                .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 16.dp + bottomInset),
            verticalArrangement = Arrangement.spacedBy(28.dp),
        ) {
            if (intro.isNotBlank()) {
                NativeText(intro, style = NativeTextStyle.Body, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            content()
        }
    }
}

/** A titled section with an optional one-line description, then its examples spaced evenly. */
@Composable
fun ShowcaseSection(
    title: String,
    description: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        NativeText(title, style = NativeTextStyle.Title)
        if (!description.isNullOrBlank()) {
            NativeText(description, style = NativeTextStyle.Body, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        content()
    }
}

/** A small caption above an individual example, e.g. "Disabled" or "With leading icon". */
@Composable
fun ExampleLabel(text: String) {
    NativeText(text, style = NativeTextStyle.Label, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

/** A filled callout listing when to reach for the component. */
@Composable
fun WhenToUse(vararg points: String) {
    NativeCard(
        variant = NativeCardVariant.Filled,
        contentPadding = PaddingValues(NativeTheme.tokens.spacingMd),
    ) {
        NativeText("WHEN TO USE", style = NativeTextStyle.Label, color = MaterialTheme.colorScheme.primary)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            points.forEach { NativeText("•  $it", style = NativeTextStyle.Body) }
        }
    }
}

/** An outlined note for platform-specific or native-behavior details worth calling out. */
@Composable
fun Note(text: String) {
    NativeCard(
        variant = NativeCardVariant.Outlined,
        contentPadding = PaddingValues(NativeTheme.tokens.spacingMd),
    ) {
        NativeText("NOTE", style = NativeTextStyle.Label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        NativeText(text, style = NativeTextStyle.Body)
    }
}

/** A single component/category in the showcase. [key] becomes the route id `showcase/<key>`. */
data class ShowcaseCategory(val key: String, val title: String, val subtitle: String)

/** The showcase's table of contents — drives the home list, the route titles, and the screen dispatcher. */
val showcaseCategories: List<ShowcaseCategory> = listOf(
    ShowcaseCategory("buttons", "Buttons & Actions", "Buttons, icon buttons, split buttons, and menus"),
    ShowcaseCategory("inputs", "Text & Inputs", "Text fields, search, and one-time-code entry"),
    ShowcaseCategory("selection", "Selection Controls", "Toggles, checkboxes, radios, segmented, sliders, steppers"),
    ShowcaseCategory("pickers", "Pickers", "Date, color, and page controls"),
    ShowcaseCategory("surfaces", "Cards & Surfaces", "Cards, dividers, and surface theming"),
    ShowcaseCategory("lists", "Lists & Rows", "Sections, rows, settings lists, and swipe actions"),
    ShowcaseCategory("overlays", "Dialogs, Sheets & Popovers", "Modals, bottom sheets, popovers, and sharing"),
    ShowcaseCategory("feedback", "Feedback & Status", "Progress, alerts, toasts, banners, and inline status"),
    ShowcaseCategory("display", "State & Display", "Loading, empty states, avatars, chips, and badges"),
    ShowcaseCategory("typography", "Typography & Theme", "Text styles, theming, dark mode, and RTL"),
    ShowcaseCategory("accessibility", "Accessibility & Helpers", "Focus, keyboard dismissal, and headings"),
)

/** Title for the nav-bar chrome of a showcase category route. */
fun showcaseTitle(key: String): String = showcaseCategories.firstOrNull { it.key == key }?.title ?: "Components"
