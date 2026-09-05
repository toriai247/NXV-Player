package com.example.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AspectRatioMode
import com.example.ui.theme.NxvYellow
import com.example.utils.TimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerControlsOverlay(
    uiState: PlayerUiState,
    onBack: () -> Unit,
    onPlayPause: () -> Unit,
    onRewind10: () -> Unit,
    onForward10: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSeek: (Long) -> Unit,
    onToggleLock: () -> Unit,
    onCycleAspectRatio: () -> Unit,
    onRotateScreen: () -> Unit,
    onOpenPip: () -> Unit,
    onOpenSpeedSheet: () -> Unit,
    onOpenSubtitleSheet: () -> Unit,
    onOpenAudioSheet: () -> Unit,
    onToggleAudioMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (uiState.isScreenLocked) {
        // Only show Unlock button when locked
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            Surface(
                onClick = onToggleLock,
                shape = CircleShape,
                color = Color(0xCC111111),
                tonalElevation = 6.dp,
                modifier = Modifier
                    .size(52.dp)
                    .testTag("unlock_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Unlock Screen",
                        tint = NxvYellow,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
        return
    }

    AnimatedVisibility(
        visible = uiState.areControlsVisible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xCC000000),
                            Color(0x40000000),
                            Color(0xCC000000)
                        )
                    )
                )
        ) {
            // TOP BAR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 12.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("player_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = uiState.currentVideo?.title ?: "Video Player",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${uiState.currentVideo?.resolution ?: "HD"} • ${uiState.aspectRatioMode.title}",
                        color = NxvYellow,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal
                    )
                }

                // Subtitles Button
                IconButton(
                    onClick = onOpenSubtitleSheet,
                    modifier = Modifier.testTag("player_subtitles_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ClosedCaption,
                        contentDescription = "Subtitles",
                        tint = if (uiState.isSubtitleEnabled) NxvYellow else Color.White.copy(alpha = 0.6f)
                    )
                }

                // Audio Track Button
                IconButton(
                    onClick = onOpenAudioSheet,
                    modifier = Modifier.testTag("player_audio_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Audiotrack,
                        contentDescription = "Audio Tracks",
                        tint = if (uiState.audioTracks.size > 1) NxvYellow else Color.White.copy(alpha = 0.8f)
                    )
                }

                // Aspect Ratio Button
                IconButton(
                    onClick = onCycleAspectRatio,
                    modifier = Modifier.testTag("player_aspect_ratio_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AspectRatio,
                        contentDescription = "Aspect Ratio",
                        tint = Color.White
                    )
                }

                // Screen Rotation Button
                IconButton(
                    onClick = onRotateScreen,
                    modifier = Modifier.testTag("player_rotate_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ScreenRotation,
                        contentDescription = "Orientation",
                        tint = Color.White
                    )
                }

                // Picture-in-Picture Button
                IconButton(
                    onClick = onOpenPip,
                    modifier = Modifier.testTag("player_pip_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PictureInPictureAlt,
                        contentDescription = "Picture in Picture",
                        tint = Color.White
                    )
                }

                // Audio / Music Mode Button
                IconButton(
                    onClick = onToggleAudioMode,
                    modifier = Modifier.testTag("player_audio_mode_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Headphones,
                        contentDescription = "Background Audio Mode",
                        tint = if (uiState.isAudioOnlyMode) NxvYellow else Color.White
                    )
                }
            }

            // CENTER CONTROLS (Rewind, Previous, Play/Pause, Next, Forward)
            Row(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(0.85f),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Previous
                IconButton(
                    onClick = onPrevious,
                    enabled = uiState.hasPrevious,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("player_prev_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous Video",
                        tint = if (uiState.hasPrevious) Color.White else Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.size(32.dp)
                    )
                }

                // 10s Rewind
                IconButton(
                    onClick = onRewind10,
                    modifier = Modifier
                        .size(52.dp)
                        .testTag("player_rewind_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.FastRewind,
                        contentDescription = "Rewind 10 Seconds",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Play / Pause FAB
                FloatingActionButton(
                    onClick = onPlayPause,
                    shape = CircleShape,
                    containerColor = NxvYellow,
                    contentColor = Color.Black,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp),
                    modifier = Modifier
                        .size(68.dp)
                        .testTag("player_play_pause_button")
                ) {
                    Icon(
                        imageVector = if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (uiState.isPlaying) "Pause" else "Play",
                        modifier = Modifier.size(38.dp)
                    )
                }

                // 10s Forward
                IconButton(
                    onClick = onForward10,
                    modifier = Modifier
                        .size(52.dp)
                        .testTag("player_forward_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.FastForward,
                        contentDescription = "Forward 10 Seconds",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Next
                IconButton(
                    onClick = onNext,
                    enabled = uiState.hasNext,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("player_next_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next Video",
                        tint = if (uiState.hasNext) Color.White else Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            // BOTTOM CONTROLS (Seek Bar, Timers, Speed, Lock)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 20.dp)
            ) {
                var isSeeking by remember { mutableStateOf(false) }
                var seekSliderValue by remember { mutableFloatStateOf(0f) }

                val currentPos = if (isSeeking) seekSliderValue.toLong() else uiState.currentPositionMs
                val maxDur = uiState.durationMs.coerceAtLeast(1L)

                // Seek slider
                Slider(
                    value = currentPos.toFloat().coerceIn(0f, maxDur.toFloat()),
                    onValueChange = {
                        isSeeking = true
                        seekSliderValue = it
                    },
                    onValueChangeFinished = {
                        isSeeking = false
                        onSeek(seekSliderValue.toLong())
                    },
                    valueRange = 0f..maxDur.toFloat(),
                    colors = SliderDefaults.colors(
                        thumbColor = NxvYellow,
                        activeTrackColor = NxvYellow,
                        inactiveTrackColor = Color(0x55FFFFFF)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("player_seek_slider")
                )

                // Time Row and Quick Action Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Time indicators
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = TimeUtils.formatDuration(currentPos),
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = " / ",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 13.sp
                        )
                        Text(
                            text = TimeUtils.formatDuration(uiState.durationMs),
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 13.sp
                        )
                    }

                    // Action buttons (Speed, Lock)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Playback Speed badge button
                        Surface(
                            onClick = onOpenSpeedSheet,
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0x40FFFFFF),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .testTag("player_speed_chip")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = null,
                                    tint = NxvYellow,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${uiState.playbackSpeed}x",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Screen Lock button
                        Surface(
                            onClick = onToggleLock,
                            shape = CircleShape,
                            color = Color(0x40FFFFFF),
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("player_lock_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.LockOpen,
                                    contentDescription = "Lock Screen",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
