package io.github.apdelrahman1911.nativecomposekit.theme

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * The solid surface color currently painted **behind** content — published by `NativeAppearanceScope`
 * (the page background, for a normal screen) and overridden by surface-bearing containers (e.g. `NativeCard`
 * with its own container color). Default [Color.Unspecified] means "no known solid surface" — the host is
 * transparent, e.g. a Liquid Glass sheet (`NativeAppearanceScope(drawBackground = false)`), or nothing wrapped
 * the content.
 *
 * Why this exists (iOS): a `NativeText` is a real `UILabel` inside a `UIKitView`, and a `UIKitView`'s
 * transparent pixels reveal the **`ComposeUIViewController`'s system backdrop (white/black)**, NOT the Compose
 * surface drawn behind it (the same reason rounded controls use `UiKitInterop.pinFilling`). So a transparent
 * label on any non-system-colored surface shows a box. `NativeText` reads this local and fills the label with
 * the real surface color **when one is known** (so it matches the painted background/card), and stays
 * transparent when it is [Color.Unspecified] — on Liquid Glass, where a fill would cover the material. Android
 * ignores it (Compose `Text` composites normally).
 *
 * It is `compositionLocalOf` (not static) so nested containers can override it and readers recompose.
 */
public val LocalNativeSurface: ProvidableCompositionLocal<Color> = compositionLocalOf { Color.Unspecified }
