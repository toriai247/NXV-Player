package com.example.player

import com.example.domain.model.AspectRatioMode
import com.example.domain.model.VideoItem

data class AudioTrackInfo(
    val id: String,
    val index: Int,
    val name: String,
    val language: String?,
    val isSelected: Boolean
)

data class SubtitleTrackInfo(
    val id: String,
    val index: Int,
    val name: String,
    val language: String?,
    val isSelected: Boolean
)

sealed class ActiveGestureHud {
    data class Brightness(val levelPercent: Int) : ActiveGestureHud()
    data class Volume(val levelPercent: Int) : ActiveGestureHud()
    data class Seek(val targetPositionMs: Long, val diffSeconds: Int) : ActiveGestureHud()
    data class DoubleTapSeek(val isForward: Boolean, val diffSeconds: Int) : ActiveGestureHud()
    object SpeedBoost : ActiveGestureHud()
    data class Zoom(val scale: Float) : ActiveGestureHud()
}

data class PlayerUiState(
    val currentVideo: VideoItem? = null,
    val playlist: List<VideoItem> = emptyList(),
    val currentVideoIndex: Int = 0,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val bufferedPositionMs: Long = 0L,
    val playbackSpeed: Float = 1.0f,
    val isTemporary2xSpeed: Boolean = false,
    val aspectRatioMode: AspectRatioMode = AspectRatioMode.FIT,
    val zoomScale: Float = 1.0f,
    val isScreenLocked: Boolean = false,
    val areControlsVisible: Boolean = true,
    val brightnessPercent: Int = 50,
    val volumePercent: Int = 50,
    val activeHud: ActiveGestureHud? = null,
    val audioTracks: List<AudioTrackInfo> = emptyList(),
    val subtitleTracks: List<SubtitleTrackInfo> = emptyList(),
    val isSubtitleEnabled: Boolean = true,
    val subtitleFontSizeSp: Int = 18,
    val subtitleOpacity: Float = 1.0f,
    val currentSubtitleText: String = "",
    val errorMessage: String? = null,
    val isAudioOnlyMode: Boolean = false,
    val isBackgroundPlayEnabled: Boolean = true
) {
    val hasPrevious: Boolean get() = currentVideoIndex > 0
    val hasNext: Boolean get() = currentVideoIndex < playlist.size - 1
}
