package com.sarahang.playback.ui.sheet

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.pager.rememberPagerState
import com.sarahang.playback.core.NONE_PLAYBACK_STATE
import com.sarahang.playback.core.PlaybackConnection
import com.sarahang.playback.core.artwork
import com.sarahang.playback.core.isIdle
import com.sarahang.playback.core.models.LocalPlaybackConnection
import com.sarahang.playback.core.models.PlaybackQueue
import com.sarahang.playback.core.models.QueueTitle
import com.sarahang.playback.ui.R
import com.sarahang.playback.ui.audio.ADAPTIVE_COLOR_ANIMATION
import com.sarahang.playback.ui.audio.AudioRow
import com.sarahang.playback.ui.audio.LocalAudioActionHandler
import com.sarahang.playback.ui.audio.adaptiveColor
import com.sarahang.playback.ui.audio.audioActionHandler
import com.sarahang.playback.ui.components.ResizableLayout
import com.sarahang.playback.ui.components.copy
import com.sarahang.playback.ui.components.isWideLayout
import com.sarahang.playback.ui.theme.Specs
import com.sarahang.playback.ui.theme.simpleClickable
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

@Composable
fun PlaybackSheet(onClose: () -> Unit) {
    val listState = rememberLazyListState()

    val coroutine = rememberCoroutineScope()
    val scrollToTop = {
        coroutine.launch {
            listState.animateScrollToItem(0)
        }
        Unit
    }

    val audioActionHandler = audioActionHandler()
    CompositionLocalProvider(LocalAudioActionHandler provides audioActionHandler) {
        PlaybackSheet(
            onClose = onClose,
            scrollToTop = scrollToTop,
            listState = listState,
            queueListState = rememberLazyListState()
        )
    }
}


@Composable
internal fun PlaybackSheet(
    onClose: () -> Unit,
    scrollToTop: () -> Unit,
    listState: LazyListState = rememberLazyListState(),
    queueListState: LazyListState = rememberLazyListState(),
    playbackConnection: PlaybackConnection = LocalPlaybackConnection.current,
) {
    val playbackState by playbackConnection.playbackState.collectAsStateWithLifecycle()
    val playbackQueue by rememberFlowWithLifecycle(stateFlow = playbackConnection.playbackQueue)
    val nowPlaying by playbackConnection.nowPlaying.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(playbackQueue.currentIndex)

    val adaptiveColor by adaptiveColor(
        nowPlaying.artwork,
        initial = MaterialTheme.colorScheme.onBackground,
        gradientEndColor = MaterialTheme.colorScheme.background,
    )
    val contentColor by animateColorAsState(adaptiveColor.color, ADAPTIVE_COLOR_ANIMATION)

    LaunchedEffect(playbackConnection) {
        playbackConnection.playbackState
            .filter { it != NONE_PLAYBACK_STATE }
            .collectLatest { if (it.isIdle) onClose() }
    }

    if (playbackState == NONE_PLAYBACK_STATE) {
        Row(Modifier.fillMaxSize()) { CircularProgressIndicator() }
        return
    }

    MaterialTheme(MaterialTheme.colorScheme) {
        BoxWithConstraints {
            val isWideLayout = isWideLayout()
            val maxWidth = maxWidth

            Row(Modifier.fillMaxSize()) {
                if (isWideLayout) {
                    ResizablePlaybackQueue(
                        maxWidth = maxWidth,
                        playbackQueue = playbackQueue,
                        queueListState = queueListState,
                        scrollToTop = scrollToTop
                    )
                }

                Scaffold(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .background(adaptiveColor.gradient)
                        .weight(1f)
                ) { paddings ->
                    LazyColumn(
                        state = listState,
                        contentPadding = paddings.copy(top = 0.dp),
                    ) {
                        item {
                            PlaybackSheetTopBar(
                                playbackQueue = playbackQueue,
                                onClose = onClose,
                                onTitleClick = { }
                            )
                        }
                        item {
                            PlaybackArtworkPagerWithNowPlayingAndControls(
                                nowPlaying = nowPlaying,
                                playbackState = playbackState,
                                pagerState = pagerState,
                                contentColor = contentColor,
                                onTitleClick = { },
                                onArtistClick = { },
                                artworkVerticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillParentMaxHeight(fraction = 0.75f),
                            )
                        }

                        if (playbackQueue.isValid)
                            item {
//                                PlaybackAudioInfo(playbackQueue.currentAudio)
                            }

                        if (!isWideLayout && !playbackQueue.isLastAudio && playbackQueue.isNotEmpty()) {
                            playbackQueue(
                                playbackQueue = playbackQueue,
                                scrollToTop = scrollToTop,
                                playbackConnection = playbackConnection,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.ResizablePlaybackQueue(
    maxWidth: Dp,
    playbackQueue: PlaybackQueue,
    scrollToTop: () -> Unit,
    queueListState: LazyListState,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    resizableLayoutViewModel: ResizablePlaybackSheetLayoutViewModel = hiltViewModel(),
    dragOffset: State<Float> = resizableLayoutViewModel.dragOffset.collectAsStateWithLifecycle(),
    setDragOffset: (Float) -> Unit = resizableLayoutViewModel::setDragOffset,
    playbackConnection: PlaybackConnection = LocalPlaybackConnection.current,
) {
    ResizableLayout(
        availableWidth = maxWidth,
        initialWeight = 0.6f,
        minWeight = 0.4f,
        maxWeight = 1.25f,
        dragOffset = dragOffset,
        setDragOffset = setDragOffset,
        analyticsPrefix = "playbackSheet.layout",
        modifier = modifier,
    ) { resizableModifier ->
        val labelMod = Modifier.padding(top = Specs.padding)
        Surface {
            LazyColumn(
                state = queueListState,
                contentPadding = contentPadding,
                modifier = Modifier.fillMaxHeight()
            ) {
                playbackQueueLabel(resizableModifier.then(labelMod))

                if (playbackQueue.isLastAudio) {
                    item {
                        Text(
                            text = stringResource(R.string.playback_queue_empty),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = Specs.padding)
                        )
                    }
                }

                playbackQueue(
                    playbackQueue = playbackQueue,
                    scrollToTop = scrollToTop,
                    playbackConnection = playbackConnection,
                )
            }
            Divider(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .align(Alignment.CenterEnd)
                    .then(resizableModifier)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaybackSheetTopBar(
    playbackQueue: PlaybackQueue,
    onClose: () -> Unit,
    onTitleClick: () -> Unit,
) {
    // TODO: Remove after https://android-review.googlesource.com/c/platform/frameworks/support/+/2209896/ is available
    // override colorScheme for TopAppBar so we can make the background transparent
    // but revert it back for actions since PlaybackSheetTopBarActions uses a Surface
    val colorScheme = MaterialTheme.colorScheme
    MaterialTheme(colorScheme.copy(surface = Color.Transparent)) {
        TopAppBar(
            title = { PlaybackSheetTopBarTitle(playbackQueue, onTitleClick) },
            actions = {
                MaterialTheme(colorScheme) {
//                    PlaybackSheetTopBarActions(playbackQueue)
                }
            },
            navigationIcon = {
                IconButton(onClick = onClose) {
                    Icon(
                        rememberVectorPainter(Icons.Default.KeyboardArrowDown),
                        modifier = Modifier.size(Specs.iconSize),
                        contentDescription = null,
                    )
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color.Transparent,
                scrolledContainerColor = Color.Transparent,
            ),
        )
    }
}

@Composable
private fun PlaybackSheetTopBarTitle(
    playbackQueue: PlaybackQueue,
    onTitleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .offset(x = (-8).dp)
            .simpleClickable(onClick = onTitleClick)
    ) {
        val context = LocalContext.current
        val queueTitle = QueueTitle.from(playbackQueue.title.orEmpty())
        Text(
            text = queueTitle.localizeValue(context).uppercase(),
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
        )
    }
}

private fun LazyListScope.playbackQueueLabel(modifier: Modifier = Modifier) {
    item {
        Row(modifier = modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.playback_queue_title),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(Specs.padding)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.playbackQueue(
    playbackQueue: PlaybackQueue,
    scrollToTop: () -> Unit,
    playbackConnection: PlaybackConnection,
) {
    val lastIndex = playbackQueue.lastIndex
    val firstIndex = (playbackQueue.currentIndex + 1).coerceAtMost(lastIndex)
    val queue = playbackQueue.subList(firstIndex, lastIndex)
    itemsIndexed(queue, key = { _, a -> a.id }) { index, audio ->
        val realPosition = firstIndex + index
        AudioRow(
            audio = audio,
            imageSize = 40.dp,
            onPlayAudio = {
                playbackConnection.transportControls?.skipToQueueItem(realPosition.toLong())
                scrollToTop()
            },
            modifier = Modifier.animateItemPlacement()
        )
    }
}
