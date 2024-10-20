package com.sarahang.playback.ui.theme

import androidx.compose.foundation.Indication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.coloredRippleClickable(
    onClick: () -> Unit,
    onClickLabel: String? = null,
    color: Color? = null,
    bounded: Boolean = false,
    interactionSource: MutableInteractionSource? = null,
    rippleRadius: Dp = 24.dp,
) = composed {
    clickable(
        onClick = onClick,
        onClickLabel = onClickLabel,
        role = Role.Button,
        indication = ripple(
            color = color ?: MaterialTheme.colorScheme.secondary,
            bounded = bounded,
            radius = rippleRadius
        ),
        interactionSource = interactionSource ?: remember { MutableInteractionSource() }
    )
}

fun Modifier.simpleClickable(
    interactionSource: MutableInteractionSource? = null,
    indication: Indication? = null,
    label: String? = null,
    onClick: () -> Unit,
) = composed {
    clickable(
        onClick = onClick,
        role = Role.Button,
        onClickLabel = label,
        indication = indication,
        interactionSource = interactionSource ?: remember { MutableInteractionSource() }
    )
}
