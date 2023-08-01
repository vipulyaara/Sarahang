package com.sarahang.playback.ui.sheet

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.isDebugInspectorInfoEnabled
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sarahang.playback.core.NONE_PLAYBACK_STATE
import com.sarahang.playback.core.PlaybackConnection
import com.sarahang.playback.core.artwork
import com.sarahang.playback.core.isIdle
import com.sarahang.playback.core.models.LocalPlaybackConnection
import com.sarahang.playback.core.models.PlaybackQueue
import com.sarahang.playback.core.models.QueueTitle
import com.sarahang.playback.ui.R
import com.sarahang.playback.ui.audio.AdaptiveColorResult
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

@Composable
fun PlaybackSheet(
    onClose: (() -> Unit)?,
    goToItem: () -> Unit = {},
    goToCreator: () -> Unit = {}
) {
    val listState = rememberLazyListState()
    val audioActionHandler = audioActionHandler()
    CompositionLocalProvider(LocalAudioActionHandler provides audioActionHandler) {
        PlaybackSheet(
            onClose = onClose,
            goToItem = goToItem,
            goToCreator = goToCreator,
            listState = listState,
            queueListState = rememberLazyListState()
        )
    }
}

@Composable
internal fun PlaybackSheet(
    onClose: (() -> Unit)?,
    goToItem: () -> Unit,
    goToCreator: () -> Unit,
    listState: LazyListState = rememberLazyListState(),
    queueListState: LazyListState = rememberLazyListState(),
    playbackConnection: PlaybackConnection = LocalPlaybackConnection.current,
) {
    val playbackState by playbackConnection.playbackState.collectAsStateWithLifecycle()
    val playbackQueue by rememberFlowWithLifecycle(playbackConnection.playbackQueue)
    val nowPlaying by playbackConnection.nowPlaying.collectAsStateWithLifecycle()

    val adaptiveColor by adaptiveColor(
        image = nowPlaying.artwork,
        initial = colorScheme.onBackground,
        gradientEndColor = colorScheme.background,
    )

    LaunchedEffect(playbackConnection) {
        playbackConnection.playbackState
            .filter { it != NONE_PLAYBACK_STATE }
            .collectLatest { if (it.isIdle) if (onClose != null) onClose() }
    }

    if (playbackState == NONE_PLAYBACK_STATE) {
        Row(Modifier.fillMaxSize()) { CircularProgressIndicator() }
        return
    }

    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .semantics { isDebugInspectorInfoEnabled = true }
            .testTag("playback_sheet")) {
        val isWideLayout = isWideLayout()
        val maxWidth = maxWidth

        Row(Modifier.fillMaxSize()) {
            if (isWideLayout) {
                ResizablePlaybackQueue(
                    maxWidth = maxWidth,
                    playbackQueue = playbackQueue,
                    queueListState = queueListState,
                    adaptiveColor = adaptiveColor
                )
            }

            Scaffold(
                containerColor = Color.Transparent,
                contentColor = colorScheme.onSurface,
                modifier = Modifier
                    .testTag("playback_sheet")
                    .background(adaptiveColor.gradient)
                    .weight(1f)
            ) { paddings ->
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentPadding = paddings.copy(top = 0.dp),
                ) {
                    if (onClose != null) {
                        item {
                            PlaybackSheetTopBar(
                                playbackQueue = playbackQueue,
                                onClose = onClose,
                                onTitleClick = goToItem,
                            )
                        }
                    }

                    item {
                        PlaybackArtworkPagerWithNowPlayingAndControls(
                            nowPlaying = nowPlaying,
                            playbackState = playbackState,
                            currentIndex = playbackQueue.currentIndex,
                            adaptiveColor = adaptiveColor,
                            onTitleClick = goToItem,
                            onArtistClick = goToCreator,
                            artworkVerticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillParentMaxHeight(fraction = 0.70f)
                                .fillParentMaxWidth()
                                .padding(vertical = 12.dp),
                        )
                    }

                    if (!isWideLayout && playbackQueue.isNotEmpty()) {
                        playbackQueue(
                            playbackQueue = playbackQueue,
                            playbackConnection = playbackConnection,
                            adaptiveColor = adaptiveColor
                        )
                    }

                    item { Spacer(Modifier.height(100.dp)) }
                }
            }
        }
    }
}

@Composable
private fun RowScope.ResizablePlaybackQueue(
    maxWidth: Dp,
    playbackQueue: PlaybackQueue,
    queueListState: LazyListState,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    resizableLayoutViewModel: ResizablePlaybackSheetLayoutViewModel = hiltViewModel(),
    dragOffset: State<Float> = resizableLayoutViewModel.dragOffset.collectAsStateWithLifecycle(),
    setDragOffset: (Float) -> Unit = resizableLayoutViewModel::setDragOffset,
    playbackConnection: PlaybackConnection = LocalPlaybackConnection.current,
    adaptiveColor: AdaptiveColorResult
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

                playbackQueue(
                    playbackQueue = playbackQueue,
                    playbackConnection = playbackConnection,
                    adaptiveColor = adaptiveColor
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

@Composable
private fun PlaybackSheetTopBar(
    playbackQueue: PlaybackQueue,
    onClose: () -> Unit,
    onTitleClick: () -> Unit = {},
) {
    MaterialTheme(colorScheme.copy(surface = Color.Transparent)) {
        CenterAlignedTopAppBar(
            title = { PlaybackSheetTopBarTitle(playbackQueue, onTitleClick = onTitleClick) },
            navigationIcon = {
                IconButton(onClick = onClose) {
                    Icon(
                        painter = rememberVectorPainter(Icons.Default.KeyboardArrowDown),
                        modifier = Modifier.size(Specs.iconSize),
                        contentDescription = stringResource(R.string.cd_minimize_player),
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
    modifier: Modifier = Modifier,
    onTitleClick: () -> Unit = {},
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .basicMarquee()
    ) {
        val queueTitle = QueueTitle.from(playbackQueue.title.orEmpty())
        Text(
            text = queueTitle.localizeValue().uppercase(),
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            modifier = Modifier.simpleClickable { onTitleClick() },
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

private fun LazyListScope.playbackQueue(
    playbackQueue: PlaybackQueue,
    playbackConnection: PlaybackConnection,
    adaptiveColor: AdaptiveColorResult
) {
    itemsIndexed(playbackQueue, key = { _, a -> a.id }) { index, audio ->
        AudioRow(
            audio = audio,
            audioIndex = index,
            adaptiveColor = adaptiveColor,
            onPlayAudio = {
                playbackConnection.transportControls?.skipToQueueItem(index.toLong())
            },
            modifier = Modifier.animateItemPlacement()
        )
    }
}

