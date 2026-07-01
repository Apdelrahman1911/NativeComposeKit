package io.github.apdelrahman1911.nativecomposekit.showcase

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import io.github.apdelrahman1911.nativecomposekit.app.LocalNativeContentTopInset
import io.github.apdelrahman1911.nativecomposekit.components.NativeListItem
import io.github.apdelrahman1911.nativecomposekit.components.NativeListSection
import io.github.apdelrahman1911.nativecomposekit.components.NativeText
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeTextStyle

/**
 * Showcase landing screen — the root of the Components tab. Explains what NativeComposeKit is, then lists the
 * component categories. Tapping a category pushes its screen via [onOpenCategory] (the route id key).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowcaseHomeScreen(onOpenCategory: (String) -> Unit) {
    // iOS's native nav bar overlays the content — begin below it but let the scroll fill behind it (0 on Android).
    val topInset = LocalNativeContentTopInset.current
    Scaffold(contentWindowInsets = WindowInsets(0, 0, 0, 0)) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, top = 16.dp + topInset, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                NativeText("NativeComposeKit", style = NativeTextStyle.Display)
                NativeText(
                    "One shared component API. Each platform renders the most native widget available — " +
                        "Jetpack Compose Material 3 on Android, real UIKit controls on iOS. Pick a category to see " +
                        "what each component does, when to use it, and how it's meant to be wired up.",
                    style = NativeTextStyle.Body,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            val rows: List<@Composable () -> Unit> = showcaseCategories.map { category ->
                {
                    NativeListItem(
                        headline = category.title,
                        supporting = category.subtitle,
                        onClick = { onOpenCategory(category.key) },
                    )
                }
            }
            NativeListSection(header = "COMPONENTS", rows = rows)

            Note(
                "The Library tab is a small reading app built entirely from NativeComposeKit — open it to see the " +
                    "components working together in a real screen flow.",
            )
        }
    }
}
