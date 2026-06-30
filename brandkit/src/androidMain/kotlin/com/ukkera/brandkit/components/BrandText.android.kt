package com.ukkera.brandkit.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow

@Composable
internal actual fun PlatformBrandText(
    text: String,
    modifier: Modifier,
    textStyle: TextStyle,
    maxLines: Int,
    overflow: TextOverflow,
    testTag: String?,
) {
    Text(
        text = text,
        modifier = if (testTag != null) modifier.testTag(testTag) else modifier,
        style = textStyle,
        maxLines = maxLines,
        overflow = overflow,
    )
}
