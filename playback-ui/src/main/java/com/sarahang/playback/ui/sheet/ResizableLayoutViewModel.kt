/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package com.sarahang.playback.ui.sheet

import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sarahang.playback.core.PreferencesStore
import com.sarahang.playback.core.apis.PlayerEventLogger
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import javax.inject.Inject

open class ResizableLayoutViewModel @Inject constructor(
    preferencesStore: PreferencesStore,
    preferenceKey: Preferences.Key<Float>,
    defaultDragOffset: Float = 0f,
    private val analyticsPrefix: String,
    private val logger: PlayerEventLogger
) : ViewModel() {

    private val dragOffsetState = preferencesStore.getStateFlow(
        preferenceKey, viewModelScope, defaultDragOffset,
        saveDebounce = 200
    )
    val dragOffset = dragOffsetState.asStateFlow()

    init {
        persistDragOffset()
    }

    fun setDragOffset(newOffset: Float) {
        viewModelScope.launch {
            dragOffsetState.value = newOffset
        }
    }

    private fun persistDragOffset() {
        viewModelScope.launch {
            dragOffsetState
                .debounce(2000)
                .collectLatest {
                    logger.logEvent("$analyticsPrefix.resize", mapOf("offset" to it.toString()))
                }
        }
    }
}
