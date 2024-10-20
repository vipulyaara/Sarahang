@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package com.sarahang.playback.ui.playback.timer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kafka.ui_playback.generated.resources.Res
import kafka.ui_playback.generated.resources.cd_set_timer
import kafka.ui_playback.generated.resources.cd_stop_timer
import kafka.ui_playback.generated.resources.sleep_timer
import kafka.ui_playback.generated.resources.stop_timer
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun SleepTimer(
    viewModelFactory: () -> SleepTimerViewModel,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val viewModel = viewModel { viewModelFactory() }
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()

    val dismissSheet: () -> Unit = {
        coroutineScope
            .launch { sheetState.hide() }
            .invokeOnCompletion { onDismiss() }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = MaterialTheme.shapes.large,
        containerColor = MaterialTheme.colorScheme.background,
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 24.dp)
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.sleep_timer),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    HorizontalDivider()
                }
            }

            items(viewState.timerIntervals) { interval ->
                Text(
                    text = interval.text,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClickLabel = stringResource(Res.string.cd_set_timer)) {
                            viewModel.startTimer(timerInterval = interval)
                            dismissSheet()
                        }
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                )
            }

            item {
                if (viewState.isTimerRunning) {
                    StopTimerItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClickLabel = stringResource(Res.string.cd_stop_timer)) {
                                viewModel.stopTimer()
                                dismissSheet()
                            }
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StopTimerItem(modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(Res.string.stop_timer),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}
