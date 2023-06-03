package com.sarahang.playback.ui.components.icons

import androidx.compose.ui.graphics.vector.ImageVector
import compose.icons.FeatherIcons
import compose.icons.FontAwesomeIcons
import compose.icons.TablerIcons
import compose.icons.feathericons.Moon
import compose.icons.feathericons.RotateCcw
import compose.icons.feathericons.RotateCw
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Backward
import compose.icons.fontawesomeicons.solid.Forward
import compose.icons.fontawesomeicons.solid.Hourglass
import compose.icons.fontawesomeicons.solid.PauseCircle
import compose.icons.fontawesomeicons.solid.PlayCircle
import compose.icons.fontawesomeicons.solid.StopCircle
import compose.icons.tablericons.Minus
import compose.icons.tablericons.Moon
import compose.icons.tablericons.MoonStars
import compose.icons.tablericons.PlayerPlay
import compose.icons.tablericons.Plus

object Icons {
    val Play: ImageVector
        get() = TablerIcons.PlayerPlay
    val PlayCircle: ImageVector
        get() = FontAwesomeIcons.Solid.PlayCircle
    val StopCircle: ImageVector
        get() = FontAwesomeIcons.Solid.StopCircle
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
    val FastForward: ImageVector
        get() = FeatherIcons.RotateCw
    val Rewind: ImageVector
        get() = FeatherIcons.RotateCcw
    val TimerOff: ImageVector
        get() = TablerIcons.Moon
    val TimerOn: ImageVector
        get() = TablerIcons.MoonStars
    val Plus: ImageVector
        get() = TablerIcons.Plus
    val Minus: ImageVector
        get() = TablerIcons.Minus
}
