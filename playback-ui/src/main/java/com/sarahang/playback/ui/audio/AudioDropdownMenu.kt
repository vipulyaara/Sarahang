package com.sarahang.playback.ui.audio

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import com.sarahang.playback.ui.R

private val defaultMenuActionLabels = listOf(
    R.string.audio_menu_play,
    R.string.audio_menu_playNext
)

@Composable
fun AudioDropdownMenu(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    actionLabels: List<Int> = defaultMenuActionLabels,
    tint: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    onDropdownSelect: (Int) -> Unit = {}
) {
    IconButton(onClick = { onExpandedChange(true) }) {
        Icon(
            modifier = modifier,
            painter = rememberVectorPainter(Icons.Default.MoreVert),
            contentDescription = stringResource(R.string.audio_menu_cd),
            tint = tint
        )
    }

    Box {
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            modifier = Modifier
                .width(IntrinsicSize.Min)
                .align(Alignment.Center)
        ) {
            actionLabels.forEach { item ->
                val label = stringResource(item)
                DropdownMenuItem(
                    text = { Text(text = label) },
                    onClick = {
                        onExpandedChange(false)
                        onDropdownSelect(item)
                    }
                )
            }
        }
    }
}
