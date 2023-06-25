package com.sarahang.playback.core.apis

interface PlayerEventLogger {
    fun logEvent(event: String, data: Map<String, String> = mapOf()) {}
}
