package io.github.apdelrahman1911.nativecomposekit.showcase.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.apdelrahman1911.nativecomposekit.components.NativeButton
import io.github.apdelrahman1911.nativecomposekit.components.NativeCard
import io.github.apdelrahman1911.nativecomposekit.components.NativeCardVariant
import io.github.apdelrahman1911.nativecomposekit.components.NativeColorWell
import io.github.apdelrahman1911.nativecomposekit.components.NativeDatePicker
import io.github.apdelrahman1911.nativecomposekit.components.NativeLoadMoreEffect
import io.github.apdelrahman1911.nativecomposekit.components.NativePageControl
import io.github.apdelrahman1911.nativecomposekit.components.NativePageLoadState
import io.github.apdelrahman1911.nativecomposekit.components.NativePager
import io.github.apdelrahman1911.nativecomposekit.components.NativeText
import io.github.apdelrahman1911.nativecomposekit.components.nativePaginationFooter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeButtonColors
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeButtonSize
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeButtonVariant
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeTextStyle
import io.github.apdelrahman1911.nativecomposekit.showcase.ExampleLabel
import io.github.apdelrahman1911.nativecomposekit.showcase.Note
import io.github.apdelrahman1911.nativecomposekit.showcase.ShowcaseScreen
import io.github.apdelrahman1911.nativecomposekit.showcase.ShowcaseSection
import io.github.apdelrahman1911.nativecomposekit.showcase.WhenToUse

private val onboardingPages = listOf("Track what you read", "Sync across devices", "Get release alerts")

@Composable
fun PickersShowcase() = ShowcaseScreen(
    intro = "Controls for picking a value — a date, a color, the active page in a pager — plus pagination " +
        "helpers. Each renders the most native control per platform where one exists (UIDatePicker / " +
        "UIColorWell / UIPageControl on iOS); the pager and load-more helpers are Compose-drawn on both.",
) {
    DatePickerSection()
    ColorWellSection()
    PageControlSection()
    LoadMoreSection()
}

// ---------- NativeDatePicker ----------

@Composable
private fun DatePickerSection() {
    ShowcaseSection(
        title = "Date picker",
        description = "A single calendar date. The native iOS compact field expands into the system " +
            "calendar; Android shows the Material inline calendar.",
    ) {
        WhenToUse(
            "You need one calendar date and want the native picker on each platform.",
            "A booking, reminder, or filter form that captures a day.",
        )

        // A settings/form row: a labelled field whose trailing control is the picker, with the
        // chosen date echoed back below so the selection is always visible.
        var dueMillis by remember { mutableStateOf<Long?>(null) }
        ExampleLabel("In a form row")
        NativeCard(variant = NativeCardVariant.Outlined) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                NativeText("Due date", style = NativeTextStyle.Body)
                NativeDatePicker(
                    selectedMillis = dueMillis,
                    onSelectedMillisChange = { dueMillis = it },
                    contentDescription = "Due date",
                )
            }
            Spacer(Modifier.size(8.dp))
            NativeText(
                text = dueMillis?.let { "Selected: $it (UTC epoch ms)" } ?: "No date selected",
                style = NativeTextStyle.Label,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        ExampleLabel("Disabled")
        NativeDatePicker(
            selectedMillis = dueMillis,
            onSelectedMillisChange = { dueMillis = it },
            enabled = false,
        )

        Note(
            "selectedMillis is UTC epoch milliseconds at the start of the day — what Material's " +
                "DatePickerState emits and what the iOS renderer mirrors (its UIDatePicker is pinned to " +
                "UTC). Convert to the device zone at the display layer; don't assume the value is local " +
                "midnight. Programmatic selectedMillis changes reach the UI on both platforms.",
        )
    }
}

// ---------- NativeColorWell ----------

@Composable
private fun ColorWellSection() {
    ShowcaseSection(
        title = "Color well",
        description = "Pick a color. iOS opens the system UIColorWell (full spectrum, eyedropper, opacity); " +
            "Android opens a preset palette dialog.",
    ) {
        WhenToUse(
            "Theming or personalization — an accent, tag, or highlight color.",
            "You want the real iOS color picker and accept a preset palette on Android.",
        )

        var accent by remember { mutableStateOf(Color(0xFF1E88E5)) }
        ExampleLabel("Accent color")
        NativeCard(variant = NativeCardVariant.Outlined) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    NativeText("Highlight", style = NativeTextStyle.Body)
                    NativeText(
                        "Tap the well to change",
                        style = NativeTextStyle.Label,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // A swatch of the current selection, so the chosen color reads at a glance next to the well.
                    Swatch(accent)
                    NativeColorWell(
                        color = accent,
                        onColorChange = { accent = it },
                        contentDescription = "Accent color",
                    )
                }
            }
        }

        // A live preview that the picked color actually drives.
        ExampleLabel("Applied")
        NativeButton(
            text = "Save changes",
            onClick = {},
            colorsOverride = NativeButtonColors(container = accent, content = Color.White),
            fullWidth = true,
        )

        Note(
            "iOS has a real system color picker; Android does not, so the Android side opens a fixed " +
                "preset palette (opaque swatches). ios.supportsAlpha only applies on iOS.",
        )
    }
}

@Composable
private fun Swatch(color: Color) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(color),
    )
}

// ---------- NativePageControl ----------

@Composable
private fun PageControlSection() {
    ShowcaseSection(
        title = "Pager & page control",
        description = "A swipeable NativePager for the pages, with NativePageControl showing — and driving — " +
            "the dots. The pager owns the current page; the control indicates it (and pages on tap).",
    ) {
        WhenToUse(
            "A carousel, featured banner, or onboarding flow.",
            "NativePager for swipeable content; NativePageControl for the dots indicator.",
        )

        val pageCount = onboardingPages.size
        val pagerState = rememberPagerState { pageCount }
        val scope = rememberCoroutineScope()

        ExampleLabel("Swipe the pager or tap the dots")
        NativeCard(variant = NativeCardVariant.Outlined) {
            // The state overload: the state's pageCount lambda is the single source of the count.
            NativePager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth(),
            ) { page ->
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    NativeText(
                        "Step ${page + 1} of $pageCount",
                        style = NativeTextStyle.Label,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    NativeText(onboardingPages[page], style = NativeTextStyle.Title)
                }
            }
            Spacer(Modifier.size(12.dp))
            NativePageControl(
                pageCount = pageCount,
                currentPage = pagerState.currentPage,
                onCurrentPageChange = { scope.launch { pagerState.animateScrollToPage(it) } },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Note(
            "NativePager wraps Compose Foundation's HorizontalPager — Compose-drawn on both platforms (no UIKit " +
                "control hosts arbitrary Compose pages). Drive it with a PagerState and bind NativePageControl to " +
                "state.currentPage + animateScrollToPage. Give the page control a width or it collapses on iOS.",
        )
    }
}

// ---------- Load more (infinite list) ----------

@Composable
private fun LoadMoreSection() {
    ShowcaseSection(
        title = "Load more (infinite list)",
        description = "Infinite scroll for a paginated list: fire loadNext near the end, show a footer while " +
            "loading, and stop at the end.",
    ) {
        WhenToUse(
            "A long, server-paged list that loads the next page as you approach the end.",
            "NativeLoadMoreEffect detects the scroll; nativePaginationFooter shows the loading/error/end row.",
        )

        var rows by remember { mutableStateOf((1..20).toList()) }
        var loadState by remember { mutableStateOf(NativePageLoadState.Idle) }
        val listState = rememberLazyListState()
        val scope = rememberCoroutineScope()

        ExampleLabel("Scroll to the end to load more (stops at 60)")
        NativeCard(variant = NativeCardVariant.Outlined) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().height(220.dp),
                state = listState,
            ) {
                items(rows, key = { it }) { n ->
                    NativeText(
                        "Item $n",
                        style = NativeTextStyle.Body,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    )
                }
                nativePaginationFooter(loadState)
            }
        }

        NativeLoadMoreEffect(
            listState = listState,
            enabled = loadState == NativePageLoadState.Idle && rows.size < 60,
        ) {
            loadState = NativePageLoadState.Loading
            scope.launch {
                delay(700)
                rows = rows + ((rows.size + 1)..(rows.size + 20))
                loadState = if (rows.size >= 60) NativePageLoadState.EndReached else NativePageLoadState.Idle
            }
        }

        Note(
            "NativeLoadMoreEffect fires onLoadMore when the list scrolls within `buffer` items of the end — " +
                "once per item count, re-arming when data grows so short pages keep chaining; guard it against " +
                "concurrent loads (here enabled = state == Idle). nativePaginationFooter renders a spinner " +
                "while loading, a retry on error (hidden without onRetry), and nothing at the end — both " +
                "slots overridable.",
        )
    }
}
