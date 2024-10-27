package com.sarahang.playback.ui.color

import androidx.collection.lruCache
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import coil3.Image
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.request.ImageRequest
import coil3.size.SizeResolver
import com.kafka.base.ApplicationScope
import com.materialkolor.DynamicMaterialTheme
import com.materialkolor.PaletteStyle
import com.materialkolor.ktx.themeColors
import kotlinx.coroutines.suspendCancellableCoroutine
import me.tatarka.inject.annotations.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@ApplicationScope
@Inject
class ColorExtractor(private val platformContext: PlatformContext) {
    private val cache = lruCache<Any, Color>(100)

    suspend fun calculatePrimaryColor(
        model: Any,
        sizeResolver: SizeResolver = DEFAULT_REQUEST_SIZE,
    ): Color {
        val cached = cache.get(model)
        if (cached != null) {
            return cached
        }

        val bitmap = suspendCancellableCoroutine { cont ->
            val request = ImageRequest.Builder(platformContext)
                .data(model)
                .size(sizeResolver)
                .prepareForColorExtractor()
                .target(
                    onSuccess = { result ->
                        cont.resume(result.toComposeImageBitmap())
                    },
                    onError = {
                        cont.resumeWithException(IllegalArgumentException())
                    },
                )
                .build()

            ImageLoader.Builder(platformContext).build().enqueue(request)
        }

        val suitableColors = bitmap.themeColors()
        return suitableColors.first()
            .also { cache.put(model, it) }
    }

    private companion object {
        val DEFAULT_REQUEST_SIZE = SizeResolver(coil3.size.Size(96, 96))
    }
}

@Composable
fun DynamicTheme(
    model: Any?,
    useDarkTheme: Boolean,
    fallback: Color = MaterialTheme.colorScheme.primary,
    style: PaletteStyle = PaletteStyle.Content,
    content: @Composable () -> Unit,
) {
    val colorExtractor = LocalColorExtractor.current

    val color by produceState<Color?>(initialValue = null, model, colorExtractor) {
        val result = cancellableRunCatching {
            colorExtractor.calculatePrimaryColor(model!!)
        }.onFailure {
            it.printStackTrace()
        }
        value = result.getOrNull()
    }

    DynamicMaterialTheme(
        seedColor = color ?: fallback,
        useDarkTheme = useDarkTheme,
        animate = true,
        style = style,
        content = content,
    )
}

inline fun <T, R> T.cancellableRunCatching(block: T.() -> R): Result<R> {
    return try {
        Result.success(block())
    } catch (ce: CancellationException) {
        throw ce
    } catch (e: Throwable) {
        Result.failure(e)
    }
}

val LocalColorExtractor = staticCompositionLocalOf<ColorExtractor> {
    error("LocalColorExtractor not provided")
}

internal expect fun ImageRequest.Builder.prepareForColorExtractor(): ImageRequest.Builder

internal expect fun Image.toComposeImageBitmap(): ImageBitmap
