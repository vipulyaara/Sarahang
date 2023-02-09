package com.sarahang.playback.ui.components

import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import app.rive.runtime.kotlin.core.SMIBoolean
import com.sarahang.playback.ui.R

@Composable
fun PlayPause(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    val playing by rememberSaveable(isPlaying) { mutableStateOf(isPlaying) }
    FloatingActionButton(
        modifier = modifier,
        shape = CircleShape,
        containerColor = containerColor,
        elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp),
        onClick = { onClick() }
    ) {
        RiveAnimation(resource = R.raw.play_pause) { view ->
            val playingInput = view.stateMachines
                .firstOrNull { it.name == "State Machine 1" }?.inputs
                ?.firstOrNull { it.name == "Play" } as? SMIBoolean

            if (playingInput?.value != playing) {
                view.setBooleanState("State Machine 1", "Play", playing)
            }
            enableDisableView(view, false)
        }
    }
}

private fun enableDisableView(view: View, enabled: Boolean) {
    view.isEnabled = enabled
    view.isClickable = enabled
    view.isFocusable = enabled
    if (view is ViewGroup) {
        val viewGroup = view
        for (i in 0 until viewGroup.childCount) {
            val child = viewGroup.getChildAt(i)
            enableDisableView(child, enabled)
        }
    }
}
