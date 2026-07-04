package io.github.apdelrahman1911.nativecomposekit.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/** Background treatment for an embedded iOS bar preview (mirrors `NativeShellBarBackground`). */
enum class IosPreviewBackground { Themed, Material, Custom }

/**
 * A REAL, non-interactive `UINavigationBar` rendered inline (iOS) so the toolbar-styles catalog can show
 * genuine native bar variants side by side — the same `UINavigationBarAppearance` knobs the shell style
 * registry maps to. On Android this renders a labeled placeholder (the variants are iOS-only by design).
 * Preview-only: the app's actual chrome is styled once via `applyNativeShellStyle`.
 */
@Composable
expect fun IosNavBarPreview(
    title: String,
    modifier: Modifier = Modifier,
    background: IosPreviewBackground = IosPreviewBackground.Themed,
    customBackground: Color? = null,
    tint: Color? = null,
    actionSymbols: List<String> = emptyList(),
    showsBack: Boolean = false,
    hairline: Boolean = false,
)

/** A REAL, non-interactive `UITabBar` preview (iOS); a labeled placeholder on Android. */
@Composable
expect fun IosTabBarPreview(
    modifier: Modifier = Modifier,
    selectedColor: Color? = null,
    unselectedColor: Color? = null,
    tint: Color? = null,
)
