package com.ukkera.brandkit.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import com.ukkera.brandkit.components.BrandBadge
import com.ukkera.brandkit.components.BrandBadgedBox
import com.ukkera.brandkit.components.BrandButton
import com.ukkera.brandkit.components.BrandCard
import com.ukkera.brandkit.components.BrandCardVariant
import com.ukkera.brandkit.components.BrandChip
import com.ukkera.brandkit.components.BrandChipStyle
import com.ukkera.brandkit.components.BrandDivider
import com.ukkera.brandkit.components.BrandEmptyState
import com.ukkera.brandkit.components.BrandListItem
import com.ukkera.brandkit.components.BrandListSection
import com.ukkera.brandkit.components.BrandListSectionStyle
import com.ukkera.brandkit.components.BrandProgressIndicator
import com.ukkera.brandkit.components.BrandProgressKind
import com.ukkera.brandkit.components.BrandRating
import com.ukkera.brandkit.components.BrandSegmentedControl
import com.ukkera.brandkit.components.BrandSwipeAction
import com.ukkera.brandkit.components.BrandText
import com.ukkera.brandkit.components.feedback.BrandFeedbackStatus
import com.ukkera.brandkit.components.feedback.LocalBrandFeedbackController
import com.ukkera.brandkit.components.model.BrandButtonVariant
import com.ukkera.brandkit.components.model.BrandIcon
import com.ukkera.brandkit.components.model.BrandTextStyle

/**
 * The real manga flow (Tier 2 content): a cover **grid**, a **detail** screen, and a chapter **reader** — all
 * built only from `Brand*` components + Compose layout, navigator-agnostic (they take callbacks the graph
 * wires to `navigator.push(...)`). Data comes from [MangaLibrary]. Covers load over the network via **Coil**
 * (app-level loader, with memory caching) and a deterministic gradient fallback (loading / offline / error all
 * look intentional, and a fixed `aspectRatio` reserves the slot so nothing reflows when the image arrives).
 */

// ---- Cover (network image with an offline-safe gradient fallback) ----

/**
 * A manga cover. The caller sizes + shapes the footprint via [modifier] (use a fixed `aspectRatio` so the
 * layout never jumps when the image loads). Loaded with **Coil** (app-level — memory cache + request de-dup, so
 * covers don't re-fetch on scroll); falls back to a branded gradient on loading / offline / error — never a
 * broken-image glyph. No text is drawn over the gradient on purpose (a `UILabel`-backed [BrandText] can't render
 * transparently over a non-published surface on iOS, and the title already appears beside/below the cover).
 */
@Composable
fun MangaCover(manga: Manga, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(10.dp)
    SubcomposeAsyncImage(
        model = manga.coverUrl,
        contentDescription = manga.title,
        contentScale = ContentScale.Crop,
        modifier = modifier.clip(shape),
        loading = { GradientPlaceholder(manga.id, Modifier.fillMaxSize()) },
        error = { GradientPlaceholder(manga.id, Modifier.fillMaxSize()) },
    )
}

@Composable
private fun GradientPlaceholder(seed: String, modifier: Modifier) {
    val (top, bottom) = gradientColors(seed)
    Box(
        modifier.background(Brush.linearGradient(listOf(top, bottom))),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.Image,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.6f),
            modifier = Modifier.size(32.dp),
        )
    }
}

/** Deterministic two-stop gradient from a stable seed (same seed → same colors every run). */
internal fun gradientColors(seed: String): Pair<Color, Color> {
    val hue = (((seed.hashCode() % 360) + 360) % 360).toFloat()
    val top = Color.hsv(hue, 0.45f, 0.55f)
    val bottom = Color.hsv((hue + 35f) % 360f, 0.55f, 0.32f)
    return top to bottom
}

// ---- Library grid ----

@Composable
fun LibraryScreen(onOpenManga: (String) -> Unit) {
    val filters = listOf("All", "Reading", "Completed")
    var selected by remember { mutableIntStateOf(0) }
    val list = when (selected) {
        1 -> MangaLibrary.all.filter { it.unread > 0 }
        2 -> MangaLibrary.all.filter { it.unread == 0 }
        else -> MangaLibrary.all
    }

    Column(Modifier.fillMaxSize()) {
        BrandSegmentedControl(
            options = filters,
            selectedIndex = selected,
            onSelectedIndexChange = { selected = it },
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            contentDescription = "Library filter",
        )
        if (list.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                BrandEmptyState(
                    title = "Nothing here yet",
                    message = "No titles match this filter.",
                    icon = Icons.Outlined.Image,
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 120.dp),
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(list, key = { it.id }) { manga ->
                    MangaGridCell(manga, onClick = { onOpenManga(manga.id) })
                }
            }
        }
    }
}

@Composable
private fun MangaGridCell(manga: Manga, onClick: () -> Unit) {
    BrandCard(
        variant = BrandCardVariant.Elevated,
        onClick = onClick,
        contentPadding = PaddingValues(8.dp),
    ) {
        BrandBadgedBox(
            badge = { if (manga.unread > 0) BrandBadge(count = manga.unread, contentDescription = "${manga.unread} unread") },
            modifier = Modifier.fillMaxWidth(),
        ) {
            MangaCover(manga, Modifier.fillMaxWidth().aspectRatio(2f / 3f))
        }
        Spacer(Modifier.height(8.dp))
        BrandText(
            manga.title,
            style = BrandTextStyle.Label,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        BrandText(
            manga.author,
            style = BrandTextStyle.Label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

// ---- Manga detail ----

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MangaDetailScreen(mangaId: String, onOpenChapter: (String) -> Unit) {
    val manga = MangaLibrary.byId(mangaId)
    if (manga == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            BrandEmptyState(
                title = "Title unavailable",
                message = "This manga could not be found.",
                icon = Icons.Outlined.Image,
            )
        }
        return
    }

    val feedback = LocalBrandFeedbackController.current
    // Live read-state on top of the sample data, so "Mark read" actually toggles within this screen session.
    val readState = remember(manga.id) {
        mutableStateMapOf<String, Boolean>().also { m -> manga.chapters.forEach { m[it.id] = it.read } }
    }
    val firstUnread = manga.chapters.firstOrNull { readState[it.id] != true } ?: manga.chapters.first()
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            MangaCover(manga, Modifier.width(120.dp).aspectRatio(2f / 3f))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                BrandText(manga.title, style = BrandTextStyle.Title)
                BrandText("by ${manga.author}", style = BrandTextStyle.Label, color = onSurfaceVariant)
                BrandRating(manga.rating)
                BrandText(
                    "${manga.status.label} · ${manga.chapters.size} chapters",
                    style = BrandTextStyle.Label,
                    color = onSurfaceVariant,
                )
            }
        }

        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            manga.genres.forEach { genre -> BrandChip(genre, style = BrandChipStyle.Suggestion) }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BrandButton(
                text = "Read",
                onClick = { onOpenChapter(firstUnread.id) },
                leadingIcon = BrandIcon(Icons.Filled.PlayArrow, sfSymbolName = "play.fill"),
                modifier = Modifier.weight(1f),
            )
            BrandButton(
                text = "Add to Library",
                onClick = { feedback.snackbar("Added to library", BrandFeedbackStatus.Success) },
                variant = BrandButtonVariant.Outline,
                leadingIcon = BrandIcon(Icons.Filled.Add, sfSymbolName = "plus"),
                modifier = Modifier.weight(1f),
            )
        }

        BrandText(manga.synopsis, style = BrandTextStyle.Body)

        BrandListSection(
            header = "CHAPTERS",
            style = BrandListSectionStyle.Plain,
            rows = manga.chapters.map { ch ->
                {
                    val read = readState[ch.id] == true
                    BrandListItem(
                        headline = ch.title,
                        supporting = ch.date,
                        onClick = { onOpenChapter(ch.id) },
                        trailing = if (!read) ({ BrandBadge(contentDescription = "Unread") }) else null,
                        swipeAction = if (!read) {
                            BrandSwipeAction(
                                label = "Mark read",
                                onAction = {
                                    readState[ch.id] = true
                                    feedback.toast("Marked ${ch.title} as read", BrandFeedbackStatus.Success)
                                },
                            )
                        } else {
                            null
                        },
                    )
                }
            },
        )
    }
}

// ---- Chapter reader ----

@Composable
fun ReaderScreen(mangaId: String, chapterId: String) {
    val pair = MangaLibrary.chapter(mangaId, chapterId)
    if (pair == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            BrandEmptyState(
                title = "Chapter unavailable",
                message = "This chapter could not be loaded.",
                icon = Icons.Outlined.Image,
            )
        }
        return
    }
    val (manga, chapter) = pair
    val pageCount = MangaLibrary.pageCount(chapter)
    val pages = remember(chapter.id) { (1..pageCount).toList() }
    val listState = rememberLazyListState()
    val current by remember { derivedStateOf { (listState.firstVisibleItemIndex + 1).coerceIn(1, pageCount) } }

    Column(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            BrandText(
                "Page $current of $pageCount",
                style = BrandTextStyle.Label,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            BrandProgressIndicator(
                kind = BrandProgressKind.Linear,
                progress = current.toFloat() / pageCount,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        BrandDivider()
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            itemsIndexed(pages) { _, page ->
                ReaderPage(manga, chapter, page)
            }
        }
    }
}

@Composable
private fun ReaderPage(manga: Manga, chapter: Chapter, page: Int) {
    SubcomposeAsyncImage(
        model = manga.pageUrl(chapter.id, page),
        contentDescription = "Page $page",
        contentScale = ContentScale.Fit,
        modifier = Modifier.fillMaxWidth().aspectRatio(0.7f),
        loading = { GradientPlaceholder("${manga.id}-${chapter.id}-$page", Modifier.fillMaxSize()) },
        error = { GradientPlaceholder("${manga.id}-${chapter.id}-$page", Modifier.fillMaxSize()) },
    )
}
