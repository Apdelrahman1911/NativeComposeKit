package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.github.apdelrahman1911.nativecomposekit.theme.LocalNativeSurface

/**
 * A themed screen scaffold — top bar + bottom bar + FAB + content — **decoupled from navigation** (unlike the
 * chrome baked into a navigation host). Use it for any standalone screen: pair it
 * with [NativeTopBar] in [topBar]. **Compose-drawn on both platforms.**
 *
 * The [containerColor] (default `background`) is **published via `LocalNativeSurface`** for the content, so
 * surface-relative fills and the native-control light/dark probe adapt to the page the content sits on (the
 * kit's surface-adaptation rule) — exactly as [NativeCard] does for its children.
 *
 * `NativeScaffold(topBar = { NativeTopBar("Settings") }) { inner -> Column(Modifier.padding(inner)) { … } }`
 */
@Composable
public fun NativeScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    containerColor: Color? = null,
    contentColor: Color? = null,
    contentWindowInsets: WindowInsets = ScaffoldDefaults.contentWindowInsets,
    content: @Composable (PaddingValues) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val container = containerColor ?: scheme.background
    Scaffold(
        modifier = modifier,
        topBar = topBar,
        bottomBar = bottomBar,
        floatingActionButton = floatingActionButton,
        containerColor = container,
        contentColor = contentColor ?: scheme.onBackground,
        contentWindowInsets = contentWindowInsets,
    ) { inner ->
        // Publish the page surface so descendant native controls / surface-relative fills adapt to it.
        CompositionLocalProvider(LocalNativeSurface provides container) {
            content(inner)
        }
    }
}
