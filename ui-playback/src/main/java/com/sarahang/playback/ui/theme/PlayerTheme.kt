package com.sarahang.playback.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object Specs {
    val padding = 12.dp
    val paddingSmall = 8.dp
    val paddingLarge = 24.dp
    val iconSize = 32.dp
}

object PlayerTheme {
    val isLightTheme
        @Composable get() = !isSystemInDarkTheme()
}

fun String?.orNa() = this ?: "N/A"

@Composable
fun Color.disabledAlpha(condition: Boolean): Color =
    copy(alpha = if (condition) alpha else 0.38f)

@Composable
fun plainSurfaceColor() = if (isSystemInDarkTheme()) Color.Black else Color.White
