package io.github.apdelrahman1911.nativecomposekit.components.model

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Cross-platform icon reference so components aren't locked to one platform's icon system.
 * Provide an [androidImageVector] for Android (Material icons) and/or an [sfSymbolName] for iOS
 * (SF Symbols). Each platform renderer uses the field it understands and ignores the other.
 */
public class NativeIcon(
    public val androidImageVector: ImageVector? = null,
    public val sfSymbolName: String? = null,
    public val contentDescription: String? = null,
)
