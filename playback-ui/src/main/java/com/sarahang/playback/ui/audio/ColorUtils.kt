/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package com.sarahang.playback.ui.audio

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.ColorUtils
import androidx.core.math.MathUtils
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.palette.graphics.Palette
import coil.imageLoader
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult
import coil.size.Precision
import com.sarahang.playback.core.PlaybackConnection
import com.sarahang.playback.core.artwork
import com.sarahang.playback.core.models.LocalPlaybackConnection
import com.sarahang.playback.ui.theme.PlayerTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import android.graphics.Color as AColor

val ADAPTIVE_COLOR_ANIMATION: AnimationSpec<Color> = tween(easing = FastOutSlowInEasing)

@Immutable
data class AdaptiveColorResult(val color: Color, val contentColor: Color, val gradient: Brush) {
    val backgroundColor = color.copy(alpha = 0.10f)
    val borderColor = color.copy(alpha = 0.4f)
}

fun Color.toAdaptiveColor(
    isDarkColors: Boolean,
    gradientEndColor: Color = if (isDarkColors) Color.White else Color.Black,
) = AdaptiveColorResult(
    color = this,
    contentColor = this.contentColor(),
    gradient = backgroundGradient(this, gradientEndColor, isDarkColors)
)

private val adaptiveColorCache = mutableMapOf<String, Color>()

@Composable
fun adaptiveColor(
    imageData: Any?,
    fallback: Color = MaterialTheme.colorScheme.secondary.contrastComposite(),
    initial: Color = fallback,
    animationSpec: AnimationSpec<Color> = ADAPTIVE_COLOR_ANIMATION,
    gradientEndColor: Color = if (PlayerTheme.isLightTheme) Color.White else Color.Black,
): State<AdaptiveColorResult> {
    val context = LocalContext.current

    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    if (imageData != null)
        LaunchedEffect(imageData) {
            launch(Dispatchers.Unconfined) {
                val result = context.getBitmap(imageData, size = 300, allowHardware = false)
                if (result is Bitmap) {
                    bitmap = result
                }
            }
        }

    return adaptiveColor(
        image = bitmap,
        imageSource = imageData,
        fallback = fallback,
        initial = initial,
        animationSpec = animationSpec,
        gradientEndColor = gradientEndColor
    )
}

@Composable
fun adaptiveColor(
    image: Bitmap? = null,
    imageSource: Any? = image,
    fallback: Color = MaterialTheme.colorScheme.secondary.contrastComposite(),
    initial: Color = fallback,
    animationSpec: AnimationSpec<Color> = ADAPTIVE_COLOR_ANIMATION,
    gradientEndColor: Color = if (PlayerTheme.isLightTheme) Color.White else Color.Black,
    isDarkColors: Boolean = !PlayerTheme.isLightTheme
): State<AdaptiveColorResult> {
    val imageHash = imageSource.hashCode().toString()
    val initialAccent = adaptiveColorCache.getOrElse(imageHash) { initial }

    var accent by remember { mutableStateOf(initialAccent) }
    val accentAnimated by animateColorAsState(accent, animationSpec)

    var paletteGenerated by remember { mutableStateOf(false) }
    var delayInitialFallback by remember { mutableStateOf(imageSource != null) }

    LaunchedEffect(image, fallback, isDarkColors) {
        if (image != null && imageSource != null) {
            accent = adaptiveColorCache.getOrPut(imageHash) {
                val palette = Palette.from(image).generate()
                getAccentColor(isDarkColors, fallback.toArgb(), palette).toColors()
            }
            paletteGenerated = true
        }
    }

    // when fallback color changes
    // reset initial accent color if palette hasn't been generated yet
    LaunchedEffect(fallback) {
        if (delayInitialFallback)
            delay(1000)
        if (!paletteGenerated) {
            accent = fallback
            delayInitialFallback = false
        }
    }

    return derivedStateOf {
        accentAnimated.toAdaptiveColor(isDarkColors, gradientEndColor)
    }
}

fun backgroundGradient(
    accent: Color,
    endColor: Color,
    isDark: Boolean,
): Brush {
    val first = gradientShift(isDark, accent.toArgb(), 0.4f, 100)
    val second = gradientShift(isDark, accent.toArgb(), 0.26f, 66)
    val third = gradientShift(isDark, accent.toArgb(), 0.13f, 33)

    return Brush.verticalGradient(listOf(first, second, third, endColor))
}

/**
 * Applies linear gradient background with given [colorStops] and [angle].
 */
fun Modifier.gradientBackground(vararg colorStops: Pair<Float, Color>, angle: Float) = this.then(
    Modifier.drawBehind {
        val angleRad = angle / 180f * PI
        val x = cos(angleRad).toFloat() // Fractional x
        val y = sin(angleRad).toFloat() // Fractional y

        val radius = sqrt(size.width.pow(2) + size.height.pow(2)) / 2f
        val offset = center + Offset(x * radius, y * radius)

        val exactOffset = Offset(
            x = min(offset.x.coerceAtLeast(0f), size.width),
            y = size.height - min(offset.y.coerceAtLeast(0f), size.height)
        )

        drawRect(
            brush = Brush.linearGradient(
                colorStops = colorStops,
                start = Offset(size.width, size.height) - exactOffset,
                end = exactOffset
            ),
            size = size
        )
    }
)

fun getAccentColor(isDark: Boolean, default: Int, palette: Palette): Int {
    when (isDark) {
        true -> {
            val darkMutedColor = palette.getDarkMutedColor(default)
            val lightMutedColor = palette.getLightMutedColor(darkMutedColor)
            val darkVibrant = palette.getDarkVibrantColor(lightMutedColor)
            val lightVibrant = palette.getLightVibrantColor(darkVibrant)
            val mutedColor = palette.getMutedColor(lightVibrant)
            return palette.getVibrantColor(mutedColor)
        }

        false -> {
            val lightMutedColor = palette.getLightMutedColor(default)
            val lightVibrant = palette.getLightVibrantColor(lightMutedColor)
            val mutedColor = palette.getMutedColor(lightVibrant)
            val darkMutedColor = palette.getDarkMutedColor(mutedColor)
            val vibrant = palette.getVibrantColor(darkMutedColor)
            return palette.getDarkVibrantColor(vibrant)
        }
    }
}

private fun gradientShift(isDarkMode: Boolean, color: Int, shift: Float, alpha: Int): Color {
    return Color(
        if (isDarkMode) shiftColor(color, shift) else ColorUtils.setAlphaComponent(
            shiftColor(color, 2f),
            alpha
        )
    )
}

fun Color.contentColor() = getContrastColor(toArgb()).toColors()

fun getContrastColor(@ColorInt color: Int): Int {
    // Counting the perceptive luminance - human eye favors green color...
    val a: Double =
        1 - (0.299 * AColor.red(color) + 0.587 * AColor.green(color) + 0.114 * AColor.blue(color)) / 255
    return if (a < 0.5) AColor.BLACK else AColor.WHITE
}

private fun desaturate(isDarkMode: Boolean, color: Int): Int {
    if (!isDarkMode) {
        return color
    }

    if (color == AColor.TRANSPARENT) {
        // can't desaturate transparent color
        return color
    }
    val amount = .25f
    val minDesaturation = .75f

    val hsl = floatArrayOf(0f, 0f, 0f)
    ColorUtils.colorToHSL(color, hsl)
    if (hsl[1] > minDesaturation) {
        hsl[1] = MathUtils.clamp(
            hsl[1] - amount,
            minDesaturation - 0.1f,
            1f
        )
    }
    return ColorUtils.HSLToColor(hsl)
}

fun shiftColor(@ColorInt color: Int, @FloatRange(from = 0.0, to = 2.0) by: Float): Int {
    return if (by == 1.0f) {
        color
    } else {
        val alpha = AColor.alpha(color)
        val hsv = FloatArray(3)
        AColor.colorToHSV(color, hsv)
        hsv[2] *= by
        (alpha shl 24) + (16777215 and AColor.HSVToColor(hsv))
    }
}

private fun Pair<Color, Color>.mergeColors(): Color {
    val a = first
    val b = second
    var r = Color.Black

    r = r.copy(alpha = 1 - (1 - b.alpha) * (1 - a.alpha))
    r = r.copy(red = b.red * b.alpha / r.alpha + a.red * a.alpha * (1 - b.alpha) / r.alpha)
    r = r.copy(green = b.green * b.alpha / r.alpha + a.green * a.alpha * (1 - b.alpha) / r.alpha)
    r = r.copy(blue = b.blue * b.alpha / r.alpha + a.blue * a.alpha * (1 - b.alpha) / r.alpha)
    return r
}

fun blendColors(
    @ColorInt color: Int,
    @ColorInt otherColor: Int,
    @FloatRange(from = 0.0, to = 1.0) percentage: Float
): Int {
    return ColorUtils.blendARGB(color, otherColor, percentage)
}

fun Color.blendWith(
    otherColor: Color,
    @FloatRange(from = 0.0, to = 1.0) percentage: Float
): Color {
    return blendColors(toArgb(), otherColor.toArgb(), percentage).toColors()
}

fun Int.toColors() = Color(this)


@Composable
fun Color.contrastComposite(alpha: Float = 0.1f) = contentColorFor(this).copy(alpha = alpha).compositeOver(this)

suspend fun Context.getBitmap(data: Any?, size: Int = Int.MAX_VALUE, allowHardware: Boolean = true): Bitmap? {
    val request = ImageRequest.Builder(this)
        .data(data)
        .size(size)
        .precision(Precision.INEXACT)
        .allowHardware(allowHardware)
        .build()

    return when (val result = imageLoader.execute(request)) {
        is SuccessResult -> (result.drawable as BitmapDrawable).bitmap
        is ErrorResult -> {
            Timber.e(result.throwable)
            null
        }
    }
}


@Composable
fun nowPlayingArtworkAdaptiveColor(
    playbackConnection: PlaybackConnection = LocalPlaybackConnection.current
): State<AdaptiveColorResult> {
    val nowPlaying by playbackConnection.nowPlaying.collectAsStateWithLifecycle()
    return adaptiveColor(nowPlaying.artwork, initial = MaterialTheme.colorScheme.background)
}
