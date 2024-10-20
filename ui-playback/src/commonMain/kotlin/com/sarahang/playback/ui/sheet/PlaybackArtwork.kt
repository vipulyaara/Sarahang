package com.sarahang.playback.ui.sheet

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sarahang.playback.core.PlaybackConnection
import com.sarahang.playback.core.models.LocalPlaybackConnection
import com.sarahang.playback.ui.components.CoverImage
import com.sarahang.playback.ui.theme.coloredRippleClickable
import kafka.ui_playback.generated.resources.Res
import kafka.ui_playback.generated.resources.cd_play_pause
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PlaybackArtwork(
    artwork: String?,
    modifier: Modifier = Modifier,
    contentColor: Color = LocalContentColor.current,
    onClick: (() -> Unit)? = null,
    playbackConnection: PlaybackConnection = LocalPlaybackConnection.current,
) {
    CoverImage(
        data = artwork,
        shape = RoundedCornerShape(8.dp),
        containerColor = Color.Transparent,
        contentColor = contentColor,
        modifier = Modifier
            .padding(horizontal = 32.dp)
            .then(modifier),
        imageModifier = Modifier
            .coloredRippleClickable(
                onClick = {
                    if (onClick != null) onClick.invoke()
                    else playbackConnection.playPause()
                },
                onClickLabel = stringResource(Res.string.cd_play_pause),
                color = contentColor,
                rippleRadius = Dp.Unspecified,
            ),
    )
}
