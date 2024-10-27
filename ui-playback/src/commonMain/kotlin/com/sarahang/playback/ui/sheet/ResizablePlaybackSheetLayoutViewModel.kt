package com.sarahang.playback.ui.sheet

import androidx.datastore.preferences.core.floatPreferencesKey
import com.sarahang.playback.core.PreferencesStore
import com.sarahang.playback.core.apis.PlayerEventLogger
import me.tatarka.inject.annotations.Inject

private val PlaybackSheetLayoutDragOffsetKey =
    floatPreferencesKey("PlaybackSheetLayoutDragOffsetKey")

@Inject
class ResizablePlaybackSheetLayoutViewModel(
    preferencesStore: PreferencesStore,
    logger: PlayerEventLogger,
) : ResizableLayoutViewModel(
    preferencesStore = preferencesStore,
    logger = logger,
    preferenceKey = PlaybackSheetLayoutDragOffsetKey,
    defaultDragOffset = 0f,
    analyticsPrefix = "playbackSheet.layout"
)
