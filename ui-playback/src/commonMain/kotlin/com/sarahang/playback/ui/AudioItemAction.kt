package com.sarahang.playback.ui

import com.sarahang.playback.core.models.Audio
import kafka.ui_playback.generated.resources.Res
import kafka.ui_playback.generated.resources.audio_menu_copyLink
import kafka.ui_playback.generated.resources.audio_menu_download
import kafka.ui_playback.generated.resources.audio_menu_downloadById
import kafka.ui_playback.generated.resources.audio_menu_play
import kafka.ui_playback.generated.resources.audio_menu_playNext
import org.jetbrains.compose.resources.StringResource

sealed class AudioItemAction(open val audio: Audio) {
    data class Play(override val audio: Audio) : AudioItemAction(audio)
    data class PlayNext(override val audio: Audio) : AudioItemAction(audio)
    data class Download(override val audio: Audio) : AudioItemAction(audio)
    data class DownloadById(override val audio: Audio) : AudioItemAction(audio)
    data class CopyLink(override val audio: Audio) : AudioItemAction(audio)

    companion object {
        fun from(actionLabelRes: StringResource, audio: Audio) = when (actionLabelRes) {
            Res.string.audio_menu_play -> Play(audio)
            Res.string.audio_menu_playNext -> PlayNext(audio)
            Res.string.audio_menu_download -> Download(audio)
            Res.string.audio_menu_downloadById -> DownloadById(audio)
            Res.string.audio_menu_copyLink -> CopyLink(audio)
            else -> error("Unknown action: $actionLabelRes")
        }
    }
}
