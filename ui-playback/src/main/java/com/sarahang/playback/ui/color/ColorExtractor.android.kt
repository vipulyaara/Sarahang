package com.sarahang.playback.ui.color

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import coil3.Image
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.request.allowRgb565
import coil3.toBitmap

internal actual fun ImageRequest.Builder.prepareForColorExtractor(): ImageRequest.Builder {
    return allowHardware(false)
        .allowRgb565(true)
}

internal actual fun Image.toComposeImageBitmap(): ImageBitmap = toBitmap().asImageBitmap()
