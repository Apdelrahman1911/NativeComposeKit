package io.github.apdelrahman1911.nativecomposekit.showcase.categories

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.apdelrahman1911.nativecomposekit.components.NativeAvatar
import io.github.apdelrahman1911.nativecomposekit.components.NativeAvatarShape
import io.github.apdelrahman1911.nativecomposekit.components.NativeAvatarSize
import io.github.apdelrahman1911.nativecomposekit.components.NativeCard
import io.github.apdelrahman1911.nativecomposekit.components.NativeChip
import io.github.apdelrahman1911.nativecomposekit.components.NativeChipStyle
import io.github.apdelrahman1911.nativecomposekit.components.NativeContentState
import io.github.apdelrahman1911.nativecomposekit.components.NativeDivider
import io.github.apdelrahman1911.nativecomposekit.components.NativeEmptyState
import io.github.apdelrahman1911.nativecomposekit.components.NativeListItem
import io.github.apdelrahman1911.nativecomposekit.components.NativeLoadState
import io.github.apdelrahman1911.nativecomposekit.components.NativePullRefresh
import io.github.apdelrahman1911.nativecomposekit.components.NativeSegmentedControl
import io.github.apdelrahman1911.nativecomposekit.components.NativeSkeleton
import io.github.apdelrahman1911.nativecomposekit.components.NativeText
import io.github.apdelrahman1911.nativecomposekit.components.feedback.LocalNativeFeedbackController
import io.github.apdelrahman1911.nativecomposekit.components.feedback.NativeFeedbackStatus
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeTextStyle
import io.github.apdelrahman1911.nativecomposekit.showcase.ExampleLabel
import io.github.apdelrahman1911.nativecomposekit.showcase.Note
import io.github.apdelrahman1911.nativecomposekit.showcase.ShowcaseScreen
import io.github.apdelrahman1911.nativecomposekit.showcase.ShowcaseSection
import io.github.apdelrahman1911.nativecomposekit.showcase.WhenToUse

/**
 * State & Display: the components that stand in for content while it loads, when there is none, and when a
 * load fails — plus the small decorations (avatars, chips) that label content once it arrives.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DisplayShowcase() = ShowcaseScreen(
    intro = "What a screen shows before, instead of, and around its data: load-state switching, shimmer " +
        "placeholders, empty states, pull-to-refresh, avatars, and chips.",
) {
    ContentStateSection()
    SkeletonSection()
    EmptyStateSection()
    PullRefreshSection()
    AvatarSection()
    ChipSection()
}

// ---- NativeContentState ----------------------------------------------------------------------------------

@Composable
private fun ContentStateSection() {
    ShowcaseSection(
        title = "Content state",
        description = "One switcher over a NativeLoadState — Loading, Empty, Error, or Content — so a screen " +
            "handles its whole async lifecycle in one place.",
    ) {
        WhenToUse(
            "A screen loads data and you want the standard loading → empty → error → content flow.",
            "Your view-model already models state as a sealed type — NativeLoadState is Compose-free and lives there.",
        )

        // The four states map to one index so the segmented control drives them directly.
        val labels = listOf("Loading", "Empty", "Error", "Content")
        var phase by remember { mutableStateOf(3) }
        val state: NativeLoadState<List<String>> = when (phase) {
            0 -> NativeLoadState.Loading
            1 -> NativeLoadState.Empty
            2 -> NativeLoadState.Error("Couldn't reach the server.")
            else -> NativeLoadState.Content(listOf("Inbox", "Drafts", "Sent"))
        }

        ExampleLabel("Switch the state")
        NativeSegmentedControl(
            options = labels,
            selectedIndex = phase,
            onSelectedIndexChange = { phase = it },
            modifier = Modifier.fillMaxWidth(),
            contentDescription = "Content state",
        )

        NativeCard {
            NativeContentState(
                state = state,
                modifier = Modifier.fillMaxWidth().height(180.dp),
                onRetry = { phase = 0 },
                emptyTitle = "No mailboxes",
                emptyMessage = "Add an account to see your mail here.",
                emptyIcon = Icons.Default.Search,
            ) { folders ->
                Column {
                    folders.forEachIndexed { i, folder ->
                        NativeListItem(folder, trailingText = "${(folder.length * 7) % 24}")
                        if (i < folders.lastIndex) NativeDivider()
                    }
                }
            }
        }

        Note(
            "The Error variant only shows a Retry button when onRetry is supplied; here it resets to Loading. " +
                "Content is rendered directly and owns its layout — the placeholder states are centered in the box.",
        )
    }
}

// ---- NativeSkeleton --------------------------------------------------------------------------------------

@Composable
private fun SkeletonSection() {
    ShowcaseSection(
        title = "Skeleton",
        description = "Shimmer placeholders that suggest the final layout while data is in flight. The caller " +
            "sizes each block; compose several to mock a real card.",
    ) {
        ExampleLabel("Loading card — avatar, two text lines, thumbnail")
        NativeCard {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    NativeSkeleton(Modifier.size(40.dp), shape = CircleShape)
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        NativeSkeleton(Modifier.fillMaxWidth(0.6f).height(14.dp))
                        NativeSkeleton(Modifier.fillMaxWidth(0.4f).height(12.dp))
                    }
                }
                NativeSkeleton(Modifier.fillMaxWidth().height(120.dp))
                NativeSkeleton(Modifier.fillMaxWidth().height(12.dp))
                NativeSkeleton(Modifier.fillMaxWidth(0.8f).height(12.dp))
            }
        }
        Note(
            "Shimmer runs only while composed, so show skeletons only while actually loading and swap to real " +
                "content to stop them. It defaults to off when the OS reduce-motion setting is on.",
        )
    }
}

// ---- NativeEmptyState ------------------------------------------------------------------------------------

@Composable
private fun EmptyStateSection() {
    val feedback = LocalNativeFeedbackController.current
    ShowcaseSection(
        title = "Empty state",
        description = "A centered icon, title, and message with an optional call-to-action, for empty lists " +
            "and no-results screens.",
    ) {
        ExampleLabel("With an action button")
        NativeCard {
            NativeEmptyState(
                title = "No downloads yet",
                message = "Episodes you save for offline play will appear here.",
                icon = Icons.Default.Search,
                actionLabel = "Browse catalog",
                onAction = { feedback.toast("Browse catalog") },
                modifier = Modifier.height(220.dp),
            )
        }
        Note(
            "The button appears only when BOTH actionLabel and onAction are given. The icon is decorative; the " +
                "title carries the accessible meaning.",
        )
    }
}

// ---- NativePullRefresh -----------------------------------------------------------------------------------

@Composable
private fun PullRefreshSection() {
    val feedback = LocalNativeFeedbackController.current
    ShowcaseSection(
        title = "Pull to refresh",
        description = "Wrap scrollable content; a downward overscroll at the top fires onRefresh while you own " +
            "the isRefreshing flag.",
    ) {
        var refreshing by remember { mutableStateOf(false) }
        var rows by remember { mutableStateOf(listOf("Unread (3)", "Promotions", "Social", "Updates")) }

        // Simulate a load: hold the flag for ~1.2s of frames, prepend a row, then flip it back. Frame-clock
        // timing (withFrameMillis) keeps this on the Compose runtime — no extra coroutine dependency.
        LaunchedEffect(refreshing) {
            if (refreshing) {
                val start = withFrameMillis { it }
                while (withFrameMillis { it } - start < 1200L) { /* keep the spinner up for a beat */ }
                rows = listOf("Unread (${rows.size + 1})") + rows.drop(1)
                refreshing = false
                feedback.toast("Refreshed", status = NativeFeedbackStatus.Success)
            }
        }

        ExampleLabel("Pull down inside the box to refresh")
        NativeCard {
            NativePullRefresh(
                isRefreshing = refreshing,
                onRefresh = { refreshing = true },
                modifier = Modifier.fillMaxWidth().height(200.dp),
            ) {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(rows.size) { i ->
                        NativeListItem(rows[i], onClick = { feedback.toast("Open ${rows[i]}") })
                        if (i < rows.lastIndex) NativeDivider()
                    }
                }
            }
        }
        Note(
            "You own isRefreshing and flip it back when the load completes. On iOS this is the Compose " +
                "pull-refresh, not a native UIRefreshControl — a documented platform divergence.",
        )
    }
}

// ---- NativeAvatar ----------------------------------------------------------------------------------------

@Composable
private fun AvatarSection() {
    ShowcaseSection(
        title = "Avatar",
        description = "Renders the first available of an image, initials, or an icon — circle for people, " +
            "rounded for content thumbnails.",
    ) {
        ExampleLabel("Sizes — Small, Medium, Large")
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NativeAvatar(initials = "JD", size = NativeAvatarSize.Small)
            NativeAvatar(initials = "AK", size = NativeAvatarSize.Medium)
            NativeAvatar(initials = "MX", size = NativeAvatarSize.Large)
        }

        ExampleLabel("Shapes & icon fallback")
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // The kit ships no image loader; with no painter the avatar falls back to initials, then icon.
            NativeAvatar(initials = "SR", shape = NativeAvatarShape.Circle)
            NativeAvatar(initials = "TV", shape = NativeAvatarShape.Rounded, size = NativeAvatarSize.Large)
            NativeAvatar(icon = Icons.Default.Person, contentDescription = "Guest")
        }

        ExampleLabel("In a list row")
        NativeCard {
            NativeListItem(
                "Eiichiro Oda",
                supporting = "12 series",
                leading = { NativeAvatar(initials = "EO") },
            )
        }
        Note(
            "The kit ships no network image loader: pass your own loaded Painter to image. initials uses the " +
                "first two code points, uppercased (surrogate-pair safe).",
        )
    }
}

// ---- NativeChip ------------------------------------------------------------------------------------------

@Composable
private fun ChipSection() {
    val feedback = LocalNativeFeedbackController.current
    ShowcaseSection(
        title = "Chip",
        description = "Compact tags, selectable filters, and removable entries. Each style maps to the matching " +
            "Material chip.",
    ) {
        ExampleLabel("Filter — multi-select, tinted when on")
        val genres = listOf("Action", "Romance", "Comedy", "Horror")
        var selected by remember { mutableStateOf(setOf("Action")) }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            genres.forEach { g ->
                NativeChip(
                    label = g,
                    style = NativeChipStyle.Filter,
                    selected = g in selected,
                    onClick = { selected = if (g in selected) selected - g else selected + g },
                )
            }
        }

        ExampleLabel("Input — removable active filters")
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (selected.isEmpty()) {
                NativeText("No filters", style = NativeTextStyle.Label)
            } else {
                selected.forEach { g ->
                    NativeChip(
                        label = g,
                        style = NativeChipStyle.Input,
                        selected = true,
                        trailingIcon = Icons.Default.Close,
                        onTrailingClick = { selected = selected - g },
                    )
                }
            }
        }

        ExampleLabel("Assist & Suggestion — one-shot actions")
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            NativeChip(
                label = "Add to library",
                style = NativeChipStyle.Assist,
                leadingIcon = Icons.Default.Person,
                onClick = { feedback.toast("Added to library") },
            )
            NativeChip(
                label = "Trending now",
                style = NativeChipStyle.Suggestion,
                onClick = { feedback.toast("Trending now") },
            )
            NativeChip(label = "Disabled", style = NativeChipStyle.Assist, enabled = false)
        }

        Note(
            "Chips are interactive by Material convention (ripple, announced as a button). For a purely static " +
                "label, render styled text instead. onTrailingClick adds a >=48dp remove target announced as \"Remove\".",
        )
    }
}
