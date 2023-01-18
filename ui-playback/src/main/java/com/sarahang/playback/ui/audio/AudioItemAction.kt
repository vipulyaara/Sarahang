package com.sarahang.playback.ui.audio

import com.sarahang.playback.core.models.Audio
import com.sarahang.playback.ui.R

sealed class AudioItemAction(open val audio: Audio) {
    data class Play(override val audio: Audio) : AudioItemAction(audio)
    data class PlayNext(override val audio: Audio) : AudioItemAction(audio)
    data class Download(override val audio: Audio) : AudioItemAction(audio)
    data class DownloadById(override val audio: Audio) : AudioItemAction(audio)
    data class CopyLink(override val audio: Audio) : AudioItemAction(audio)

    companion object {
        fun from(actionLabelRes: Int, audio: Audio) = when (actionLabelRes) {
            R.string.audio_menu_play -> Play(audio)
            R.string.audio_menu_playNext -> PlayNext(audio)
            R.string.audio_menu_download -> Download(audio)
            R.string.audio_menu_downloadById -> DownloadById(audio)
            R.string.audio_menu_copyLink -> CopyLink(audio)
            else -> error("Unknown action: $actionLabelRes")
        }
    }
}
