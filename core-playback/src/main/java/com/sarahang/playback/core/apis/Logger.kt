package com.sarahang.playback.core.apis

interface Logger {
    fun i(message: String)
    fun d(message: String)
    fun w(message: String)
    fun e(message: String)
    fun e(throwable: Throwable, message: String = "")
}