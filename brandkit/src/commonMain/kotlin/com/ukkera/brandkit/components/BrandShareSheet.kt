package com.ukkera.brandkit.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/** What to hand to the system share UI. Extend with images/files as the app needs them. */
public data class BrandShareContent(val text: String? = null, val url: String? = null)

/**
 * Imperative share entry point obtained via [rememberBrandShare]; call [share] from a click lambda.
 * Sharing is a one-shot action (HIG: a system activity/share sheet), so this is a handle you invoke, not a
 * placed composable — mirroring `BrandFeedbackController`.
 */
public class BrandShare internal constructor(private val present: (BrandShareContent) -> Unit) {
    public fun share(text: String? = null, url: String? = null): Unit = present(BrandShareContent(text, url))
    public fun share(content: BrandShareContent): Unit = present(content)
}

/**
 * Remembers a [BrandShare] backed by the **native** share UI: iOS `UIActivityViewController` (presented from
 * the top-most view controller; popover-anchored on iPad), Android the `ACTION_SEND` chooser.
 *
 * `val share = rememberBrandShare(); BrandButton("Share", { share.share(text = title, url = link) })`
 */
@Composable
public fun rememberBrandShare(): BrandShare {
    val present = rememberPlatformShare()
    return remember(present) { BrandShare(present) }
}

@Composable
internal expect fun rememberPlatformShare(): (BrandShareContent) -> Unit
