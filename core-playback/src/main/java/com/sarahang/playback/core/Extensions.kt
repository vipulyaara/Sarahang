package com.sarahang.playback.core

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.size.Precision
import coil3.toBitmap

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.O)
fun isOreo() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

fun <T> List<T>.swap(from: Int, to: Int): List<T> {
    val new = toMutableList()
    val element = new.removeAt(from)
    new.add(to, element)
    return new
}

suspend fun Context.getBitmap(uri: Uri, size: Int): Bitmap? {
    val request = ImageRequest.Builder(this)
        .data(uri)
        .size(size)
        .precision(Precision.INEXACT)
        .allowHardware(true)
        .build()

    return when (val result = imageLoader.execute(request)) {
        is SuccessResult -> result.image.toBitmap()
        else -> null
    }
}
