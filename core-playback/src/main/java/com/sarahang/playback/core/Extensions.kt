package com.sarahang.playback.core

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.ChecksSdkIntAtLeast
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.size.Precision
import coil3.toBitmap
import java.io.Serializable

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

fun Bundle.toMap(): Map<String, Any?> {
    val map = mutableMapOf<String, Any?>()
    for (key in keySet()) {
        map[key] = get(key)
    }
    return map
}

fun Map<String, Any?>.toBundle(): Bundle {
    val bundle = Bundle()

    for ((key, value) in this) {
        when (value) {
            is Int -> bundle.putInt(key, value)
            is String -> bundle.putString(key, value)
            is Boolean -> bundle.putBoolean(key, value)
            is Float -> bundle.putFloat(key, value)
            is Double -> bundle.putDouble(key, value)
            is Long -> bundle.putLong(key, value)
            is Bundle -> bundle.putBundle(key, value)
            is Parcelable -> bundle.putParcelable(key, value)
            is Serializable -> bundle.putSerializable(key, value)
            // Add more cases here as needed for other data types
            else -> throw IllegalArgumentException("Unsupported type ${value?.javaClass?.canonicalName} for key $key")
        }
    }

    return bundle
}
