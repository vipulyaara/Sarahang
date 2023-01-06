package com.sarahang.playback.ui.components.icons

import androidx.compose.ui.graphics.vector.ImageVector
import compose.icons.TablerIcons
import compose.icons.tablericons.AlertCircle
import compose.icons.tablericons.Loader

object Icons {
    val Play: ImageVector
        get() = PlayCircle
    val Pause: ImageVector
        get() = PauseCircle
    val ErrorOutline: ImageVector
        get() = TablerIcons.AlertCircle
    val Hourglass: ImageVector
        get() = TablerIcons.Loader
}
