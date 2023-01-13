package com.sarahang.playback.ui.audio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sarahang.playback.core.PlaybackConnection
import com.sarahang.playback.core.SET_MEDIA_STATE
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaybackConnectionViewModel @Inject constructor(
    val playbackConnection: PlaybackConnection
) : ViewModel() {

    init {
        viewModelScope.launch {
            playbackConnection.isConnected.collectLatest { connected ->
                if (connected) {
                    playbackConnection.transportControls?.sendCustomAction(SET_MEDIA_STATE, null)
                }
            }
        }
    }
}
