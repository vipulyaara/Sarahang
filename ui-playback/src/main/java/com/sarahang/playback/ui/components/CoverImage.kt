/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package com.sarahang.playback.ui.components

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.shapes.Shape
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.ContentAlpha
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.VectorPainter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import coil.compose.AsyncImagePainter.State
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest

@Composable
fun CoverImage(
    data: Any?,
    modifier: Modifier = Modifier,
    imageModifier: Modifier = Modifier,
    size: Dp = Dp.Unspecified,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.secondary,
    contentScale: ContentScale = ContentScale.Crop,
    shape: Shape = MaterialTheme.shapes.small,
    icon: VectorPainter = rememberVectorPainter(Icons.Default.PlayArrow),
    iconPadding: Dp = if (size != Dp.Unspecified) size * 0.25f else 24.dp,
    bitmapPlaceholder: Bitmap? = null,
    contentDescription: String? = null,
    elevation: Dp = 2.dp,
) {
    val sizeMod = if (size.isSpecified) Modifier.size(size) else Modifier
    Surface(
        tonalElevation = elevation,
        color = containerColor,
        shape = shape,
        modifier = modifier
            .then(sizeMod)
            .aspectRatio(1f)
    ) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(data)
                .build(),
            contentDescription = contentDescription,
            contentScale = contentScale,
        ) {
            val state = painter.state
            when (state) {
                is State.Error, State.Empty, is State.Loading -> {
                    Icon(
                        painter = icon,
                        tint = contentColor.copy(alpha = ContentAlpha.disabled),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(SnackbarDefaults.color)
                            .padding(iconPadding)
                    )
                }

                else -> SubcomposeAsyncImageContent(imageModifier.fillMaxSize())
            }

            if (bitmapPlaceholder != null && state is State.Loading) {
                Image(
                    painter = rememberAsyncImagePainter(bitmapPlaceholder),
                    contentDescription = null,
                    contentScale = contentScale,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(shape)
                )
            }
        }
    }
}
