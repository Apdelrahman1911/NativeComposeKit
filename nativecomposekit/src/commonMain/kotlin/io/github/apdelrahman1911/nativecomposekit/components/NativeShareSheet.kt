package io.github.apdelrahman1911.nativecomposekit.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember

/**
 * What to hand to the system share UI. Compares by value; not a `data class` so fields (images,
 * files, a subject line) can be added later without breaking binary compatibility.
 */
@Immutable
public class NativeShareContent(
    public val text: String? = null,
    public val url: String? = null,
) {
    override fun equals(other: Any?): Boolean =
        this === other || (other is NativeShareContent && text == other.text && url == other.url)

    override fun hashCode(): Int = (text?.hashCode() ?: 0) * 31 + (url?.hashCode() ?: 0)
}

/**
 * Imperative share entry point obtained via [rememberNativeShare]; call [share] from a click lambda.
 * Sharing is a one-shot action (HIG: a system activity/share sheet), so this is a handle you invoke, not a
 * placed composable — mirroring `NativeFeedbackController`.
 */
public class NativeShare internal constructor(private val present: (NativeShareContent) -> Unit) {
    public fun share(text: String? = null, url: String? = null): Unit = present(NativeShareContent(text, url))
    public fun share(content: NativeShareContent): Unit = present(content)
}

/**
 * Remembers a [NativeShare] backed by the **native** share UI: iOS `UIActivityViewController` (presented from
 * the top-most view controller; popover-anchored on iPad), Android the `ACTION_SEND` chooser.
 *
 * `val share = rememberNativeShare(); NativeButton("Share", { share.share(text = title, url = link) })`
 */
@Composable
public fun rememberNativeShare(): NativeShare {
    val present = rememberPlatformShare()
    return remember(present) { NativeShare(present) }
}

@Composable
internal expect fun rememberPlatformShare(): (NativeShareContent) -> Unit
