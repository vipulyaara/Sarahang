package com.sarahang.playback.ui.sheet

import androidx.datastore.preferences.core.floatPreferencesKey
import com.sarahang.playback.core.PreferencesStore
import com.sarahang.playback.core.apis.PlayerEventLogger
import javax.inject.Inject

private val PlaybackSheetLayoutDragOffsetKey = floatPreferencesKey("PlaybackSheetLayoutDragOffsetKey")

class ResizablePlaybackSheetLayoutViewModel @Inject constructor(
    preferencesStore: PreferencesStore,
    logger: PlayerEventLogger,
) : ResizableLayoutViewModel(
    preferencesStore = preferencesStore,
    logger = logger,
    preferenceKey = PlaybackSheetLayoutDragOffsetKey,
    defaultDragOffset = 0f,
    analyticsPrefix = "playbackSheet.layout"
)
