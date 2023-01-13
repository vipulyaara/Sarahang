package com.sarahang.playback.ui.audio

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.compose.rememberLottieDynamicProperties
import com.airbnb.lottie.compose.rememberLottieDynamicProperty

@Composable
fun PlayBars(size: Dp, containerColor: Color, isPlaying: Boolean, modifier: Modifier = Modifier) {
    val dynamicProperties = rememberLottieDynamicProperties(
        rememberLottieDynamicProperty(
            property = LottieProperty.COLOR,
            value = containerColor.toArgb(),
            keyPath = arrayOf("**")
        ),
    )
    val composition by rememberLottieComposition(spec = LottieCompositionSpec.Url(musicBars))

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(4.dp))
    ) {
        LottieAnimation(
            composition = composition,
            dynamicProperties = dynamicProperties,
            iterations = LottieConstants.IterateForever,
            modifier = Modifier.align(Alignment.Center),
            isPlaying = isPlaying
        )
    }
}

private const val musicBars = "https://assets1.lottiefiles.com/packages/lf20_NsCkXA/music.json"
