package com.sarahang.playback.ui.components.icons

import androidx.compose.ui.graphics.vector.ImageVector
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Backward
import compose.icons.fontawesomeicons.solid.Forward
import compose.icons.fontawesomeicons.solid.Hourglass
import compose.icons.fontawesomeicons.solid.PauseCircle
import compose.icons.fontawesomeicons.solid.PlayCircle

object Icons {
    val Play: ImageVector
        get() = FontAwesomeIcons.Solid.PlayCircle
    val Pause: ImageVector
        get() = FontAwesomeIcons.Solid.PauseCircle
    val ErrorOutline: ImageVector
        get() = FontAwesomeIcons.Solid.PlayCircle
    val Hourglass: ImageVector
        get() = FontAwesomeIcons.Solid.Hourglass
    val Next: ImageVector
        get() = FontAwesomeIcons.Solid.Forward
    val Previous: ImageVector
        get() = FontAwesomeIcons.Solid.Backward
}
