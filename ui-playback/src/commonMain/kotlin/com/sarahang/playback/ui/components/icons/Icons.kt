package com.sarahang.playback.ui.components.icons

import androidx.compose.ui.graphics.vector.ImageVector
import com.sarahang.playback.ui.icons.PlayerForward
import com.sarahang.playback.ui.icons.PlayerRewind
import compose.icons.FontAwesomeIcons
import compose.icons.TablerIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Backward
import compose.icons.fontawesomeicons.solid.Forward
import compose.icons.fontawesomeicons.solid.Hourglass
import compose.icons.fontawesomeicons.solid.Info
import compose.icons.fontawesomeicons.solid.PauseCircle
import compose.icons.fontawesomeicons.solid.Play
import compose.icons.fontawesomeicons.solid.PlayCircle
import compose.icons.tablericons.ChevronDown
import compose.icons.tablericons.Clock
import compose.icons.tablericons.Repeat
import compose.icons.tablericons.RepeatOnce
import compose.icons.tablericons.Shield

object Icons {
    val PlayCircle: ImageVector
        get() = FontAwesomeIcons.Solid.PlayCircle
    val Play: ImageVector
        get() = FontAwesomeIcons.Solid.Play
    val Pause: ImageVector
        get() = FontAwesomeIcons.Solid.PauseCircle
    val ErrorOutline: ImageVector
        get() = FontAwesomeIcons.Solid.Info
    val Hourglass: ImageVector
        get() = FontAwesomeIcons.Solid.Hourglass
    val Next: ImageVector
        get() = FontAwesomeIcons.Solid.Forward
    val Previous: ImageVector
        get() = FontAwesomeIcons.Solid.Backward
    val FastForward: ImageVector
        get() = PlayerForward
    val Rewind: ImageVector
        get() = PlayerRewind
    val TimerOff: ImageVector
        get() = TablerIcons.Clock
    val ChevronDown: ImageVector
        get() = TablerIcons.ChevronDown
    val Repeat: ImageVector
        get() = TablerIcons.Repeat
    val RepeatOnce: ImageVector
        get() = TablerIcons.RepeatOnce
    val Shuffle: ImageVector
        get() = TablerIcons.Shield
    val ShuffleOn: ImageVector
        get() = TablerIcons.Shield
}
