package io.github.apdelrahman1911.nativecomposekit.app

import androidx.compose.runtime.Composable

/**
 * Manual reproductions for the three confirmed iOS `UIKitView` interop limitations (see `docs/interop-notes.md`).
 * iOS renders the real raw-`UIKitView` repros; Android shows a short note (the behaviors are iOS-only).
 * Reachable via Settings → "iOS interop repro".
 */
@Composable
expect fun InteropReproScreen()
