package com.sarahang.playback.ui.audio

import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable

@Composable
fun Dismissable(onDismiss: () -> Unit, content: @Composable () -> Unit) {
    val dismissState = rememberSwipeToDismissBoxState(confirmValueChange = {
        if (it != SwipeToDismissBoxValue.Settled) {
            onDismiss.invoke()
        }
        true
    })

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {},
        content = { content() }
    )
}
