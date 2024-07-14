package com.sarahang.playback.ui.color

import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import coil.request.ImageRequest

internal fun Drawable.toComposeImageBitmap(): ImageBitmap = toBitmap().asImageBitmap()

internal fun ImageRequest.Builder.prepareForColorExtractor(): ImageRequest.Builder {
    return allowHardware(false)
        .allowRgb565(true)
}
