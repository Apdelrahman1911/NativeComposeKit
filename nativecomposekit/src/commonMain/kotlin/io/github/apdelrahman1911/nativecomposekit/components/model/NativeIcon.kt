package io.github.apdelrahman1911.nativecomposekit.components.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Cross-platform icon reference so components aren't locked to one platform's icon system.
 * Provide an [androidImageVector] for Android (Material icons) and/or an [sfSymbolName] for iOS
 * (SF Symbols). Each platform renderer uses the field it understands and ignores the other.
 *
 * Compares equal by value so two references to the same icon compare equal — inline-constructed icons in
 * a composable call would otherwise defeat recomposition skipping (and iOS size-fingerprint caching). Not
 * a `data class` so fields (tint, rendering mode, …) can be added later without breaking binary
 * compatibility; the hand-written [equals]/[hashCode] keep the value-equality guarantee.
 */
@Immutable
public class NativeIcon(
    public val androidImageVector: ImageVector? = null,
    public val sfSymbolName: String? = null,
    public val contentDescription: String? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NativeIcon) return false
        return androidImageVector == other.androidImageVector &&
            sfSymbolName == other.sfSymbolName &&
            contentDescription == other.contentDescription
    }

    override fun hashCode(): Int {
        var result = androidImageVector?.hashCode() ?: 0
        result = 31 * result + (sfSymbolName?.hashCode() ?: 0)
        result = 31 * result + (contentDescription?.hashCode() ?: 0)
        return result
    }
}
