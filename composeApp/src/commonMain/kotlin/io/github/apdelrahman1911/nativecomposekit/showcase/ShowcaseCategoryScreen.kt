package io.github.apdelrahman1911.nativecomposekit.showcase

import androidx.compose.runtime.Composable
import io.github.apdelrahman1911.nativecomposekit.showcase.categories.AccessibilityShowcase
import io.github.apdelrahman1911.nativecomposekit.showcase.categories.ButtonsShowcase
import io.github.apdelrahman1911.nativecomposekit.showcase.categories.DisplayShowcase
import io.github.apdelrahman1911.nativecomposekit.showcase.categories.FeedbackShowcase
import io.github.apdelrahman1911.nativecomposekit.showcase.categories.InputsShowcase
import io.github.apdelrahman1911.nativecomposekit.showcase.categories.ListsShowcase
import io.github.apdelrahman1911.nativecomposekit.showcase.categories.OverlaysShowcase
import io.github.apdelrahman1911.nativecomposekit.showcase.categories.PickersShowcase
import io.github.apdelrahman1911.nativecomposekit.showcase.categories.SelectionShowcase
import io.github.apdelrahman1911.nativecomposekit.showcase.categories.SurfacesShowcase
import io.github.apdelrahman1911.nativecomposekit.showcase.categories.TypographyShowcase

/** Renders the showcase category screen for a route key (`showcase/<key>`). */
@Composable
fun ShowcaseCategoryScreen(key: String) {
    when (key) {
        "buttons" -> ButtonsShowcase()
        "inputs" -> InputsShowcase()
        "selection" -> SelectionShowcase()
        "pickers" -> PickersShowcase()
        "surfaces" -> SurfacesShowcase()
        "lists" -> ListsShowcase()
        "overlays" -> OverlaysShowcase()
        "feedback" -> FeedbackShowcase()
        "display" -> DisplayShowcase()
        "typography" -> TypographyShowcase()
        "accessibility" -> AccessibilityShowcase()
        else -> ShowcaseScreen(intro = "Unknown category: \"$key\".") {}
    }
}
