package com.sarahang.playback.core.audio

import android.support.v4.media.session.MediaSessionCompat
import com.sarahang.playback.core.apis.AudioDataSource
import com.sarahang.playback.core.apis.Logger
import com.sarahang.playback.core.models.Audio
import com.sarahang.playback.core.models.toQueueItems
import com.sarahang.playback.core.position
import com.sarahang.playback.core.swap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

interface AudioQueueManager {
    var currentAudioIndex: Int
    val currentAudioId: String
    var currentAudio: Audio?

    var queue: List<String>
    var queueTitle: String

    val previousAudioIndex: Int?
    val nextAudioIndex: Int?

    suspend fun refreshCurrentAudio(): Audio?

    fun setMediaSession(session: MediaSessionCompat)
    fun playNext(id: String)
    fun skipTo(position: Int)
    fun remove(position: Int)
    fun remove(id: String)
    fun swap(from: Int, to: Int)
    fun queue(): String
    fun clear()
    fun clearPlayedAudios()
    suspend fun shuffleQueue(isShuffle: Boolean = false)
}

@Inject
class AudioQueueManagerImpl(
    private val audioDataSource: AudioDataSource,
    private val logger: Logger,
) : AudioQueueManager, CoroutineScope by MainScope() {

    private lateinit var mediaSession: MediaSessionCompat
    private val playedAudios = mutableListOf<String>()
    private var originalQueue = listOf<String>()

    override var currentAudio: Audio? = null

    override var currentAudioIndex = 0
    override val currentAudioId get() = queue.getOrNull(currentAudioIndex) ?: ""

    override suspend fun refreshCurrentAudio(): Audio? {
        currentAudio = audioDataSource.findAudio(queue[currentAudioIndex])
        return currentAudio
    }

    override var queue: List<String> = listOf()
        set(value) {
            field = value
            setQueueItems(value)
        }

    override var queueTitle: String = ""
        set(value) {
            field = value
            mediaSession.setQueueTitle(value)
        }

    override val previousAudioIndex: Int?
        get() {
            if (mediaSession.position() >= 5000) return currentAudioIndex
            val previousIndex = currentAudioIndex - 1

            return when {
                previousIndex >= 0 -> previousIndex
                else -> null
            }
        }

    override val nextAudioIndex: Int?
        get() {
            val nextIndex = currentAudioIndex + 1
            return when {
                nextIndex < queue.size -> nextIndex
                else -> null
            }
        }

    private fun setQueueItems(ids: List<String>) {
        if (ids.isNotEmpty()) {
            launch {
                withContext(Dispatchers.Default) {
                    val audios = audioDataSource.getByIds(ids).associateBy { it.id }
                    val audiosOrdered = buildList {
                        ids.forEach { id ->
                            // map not found audios to empty ones to keep index integrity
                            add(audios[id] ?: Audio.unknown)
                        }
                    }
                    mediaSession.setQueue(audiosOrdered.toQueueItems())
                }
            }
        }
    }

    override fun setMediaSession(session: MediaSessionCompat) {
        mediaSession = session
    }

    override fun playNext(id: String) {
        val nextIndex = currentAudioIndex + 1
        queue = queue.toMutableList().apply {
            add(nextIndex, id)
        }
    }

    override fun skipTo(position: Int) {
        currentAudioIndex = position
    }

    override fun remove(position: Int) {
        queue = queue.toMutableList().apply { removeAt(position) }
    }

    override fun remove(id: String) {
        queue = queue.toMutableList().apply { remove(id) }
    }

    override fun swap(from: Int, to: Int) {
        queue = queue.swap(from, to)
    }

    override fun queue(): String {
        return "${currentAudioIndex + 1}/${queue.size}"
    }

    override fun clear() {
        queue = emptyList()
        queueTitle = ""
        currentAudioIndex = 0
    }

    override fun clearPlayedAudios() {
        playedAudios.clear()
    }

    override suspend fun shuffleQueue(isShuffle: Boolean) {
        withContext(Dispatchers.Default) {
            if (isShuffle) shuffleQueue()
            else restoreQueueOrder()
        }
    }

    private fun shuffleQueue() {
        var shuffled = queue.shuffled()

        val currentIdIndex = shuffled.indexOfFirst { id -> id == queue[currentAudioIndex] }
        if (currentIdIndex >= 0) {
            currentAudioIndex = 0
            shuffled = shuffled.swap(currentIdIndex, 0)
        } else {
            logger.e("CurrentIdIndex is not found")
            return
        }

        logger.d("Saving shuffled queue: ${shuffled.size}")

        // save non-shuffled original queue
        originalQueue = queue
        // set and return shuffled queue
        queue = shuffled
    }

    private fun restoreQueueOrder() {
        val currentIndexId = currentAudioId
        queue = originalQueue
        currentAudioIndex = queue.indexOf(currentIndexId)
    }
}
