package com.sarahang.playback.ui.audio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sarahang.playback.core.PlaybackConnection
import com.sarahang.playback.core.SET_MEDIA_STATE
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class PlaybackViewModel @Inject constructor(
    val playbackConnection: PlaybackConnection
) : ViewModel() {

    init {
        viewModelScope.launch {
            playbackConnection.isConnected.collect { connected ->
                if (connected) {
                    playbackConnection.transportControls?.sendCustomAction(SET_MEDIA_STATE, null)
                    Timber.d("PlaybackViewModel: connected")
                }
            }
        }
    }
}
