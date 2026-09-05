package com.example.player

import android.content.Context
import android.media.AudioManager
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.TrackGroup
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.MediaSession
import com.example.domain.model.AspectRatioMode
import com.example.domain.model.VideoItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
class PlayerManager(
    private val context: Context,
    private val coroutineScope: CoroutineScope
) {
    private val httpDataSourceFactory = DefaultHttpDataSource.Factory()
        .setUserAgent("Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36 NXVPlayer/1.0")
        .setConnectTimeoutMs(15_000)
        .setReadTimeoutMs(15_000)
        .setAllowCrossProtocolRedirects(true)

    private val dataSourceFactory = DefaultDataSource.Factory(context, httpDataSourceFactory)
    private val mediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory)
    private val renderersFactory = DefaultRenderersFactory(context)
        .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)
        .setEnableDecoderFallback(true)

    private val audioAttributes = AudioAttributes.Builder()
        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
        .setUsage(C.USAGE_MEDIA)
        .build()

    val player: ExoPlayer = ExoPlayer.Builder(context, renderersFactory)
        .setMediaSourceFactory(mediaSourceFactory)
        .setSeekBackIncrementMs(10_000)
        .setSeekForwardIncrementMs(10_000)
        .setAudioAttributes(audioAttributes, true)
        .setWakeMode(C.WAKE_MODE_NETWORK)
        .setHandleAudioBecomingNoisy(true)
        .build()

    private var mediaSession: MediaSession? = null
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private var progressJob: Job? = null
    private var controlsTimeoutJob: Job? = null
    private var hudDismissJob: Job? = null
    private var savedSpeedBeforeLongPress: Float = 1.0f

    init {
        try {
            mediaSession = MediaSession.Builder(context, player).build()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                _uiState.update {
                    it.copy(
                        isBuffering = playbackState == Player.STATE_BUFFERING,
                        durationMs = player.duration.coerceAtLeast(0L)
                    )
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _uiState.update { it.copy(isPlaying = isPlaying) }
                if (isPlaying) {
                    startProgressTracker()
                    resetControlsTimeout()
                } else {
                    stopProgressTracker()
                }
            }

            override fun onTracksChanged(tracks: Tracks) {
                updateTracks(tracks)
            }

            override fun onPlayerError(error: PlaybackException) {
                val errorMsg = when (error.errorCode) {
                    PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS -> "HTTP error (${error.message ?: "403 Forbidden"}). Server denied access."
                    PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED -> "Network connection failed. Check your internet connection."
                    PlaybackException.ERROR_CODE_DECODER_INIT_FAILED -> "Video codec not supported by device."
                    else -> error.localizedMessage ?: "Playback error encountered"
                }
                _uiState.update {
                    it.copy(
                        errorMessage = errorMsg,
                        isBuffering = false
                    )
                }
            }
        })

        // Initial volume level
        val currentVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
        _uiState.update { it.copy(volumePercent = (currentVol * 100) / maxVol) }
    }

    fun retry() {
        val currentVideo = _uiState.value.currentVideo ?: return
        val currentPos = _uiState.value.currentPositionMs
        prepareAndPlay(currentVideo, _uiState.value.playlist, currentPos)
    }

    fun getOrCreateMediaSession(serviceContext: Context? = null): MediaSession? {
        if (mediaSession == null) {
            try {
                mediaSession = MediaSession.Builder(serviceContext ?: context, player).build()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return mediaSession
    }

    fun prepareAndPlay(
        video: VideoItem,
        playlist: List<VideoItem> = listOf(video),
        resumePosition: Long = 0L,
        startInAudioMode: Boolean = false
    ) {
        val index = playlist.indexOfFirst { it.uri == video.uri }.coerceAtLeast(0)
        val shouldBeAudioMode = startInAudioMode || _uiState.value.isAudioOnlyMode

        _uiState.update {
            it.copy(
                currentVideo = video,
                playlist = playlist,
                currentVideoIndex = index,
                aspectRatioMode = AspectRatioMode.entries.getOrElse(video.aspectRatioMode) { AspectRatioMode.FIT },
                playbackSpeed = video.playbackSpeed.coerceIn(0.25f, 3.0f),
                isAudioOnlyMode = shouldBeAudioMode,
                errorMessage = null
            )
        }

        val mediaMetadata = MediaMetadata.Builder()
            .setTitle(video.title)
            .setDisplayTitle(video.title)
            .setArtist(if (shouldBeAudioMode) "NXV Player • Audio Mode" else "NXV Player")
            .setAlbumTitle(if (video.folderName.isNotBlank()) video.folderName else "Local Video")
            .setArtworkUri(if (video.uri.isNotBlank()) Uri.parse(video.uri) else null)
            .build()

        val mediaItem = MediaItem.Builder()
            .setUri(Uri.parse(video.uri))
            .setMediaMetadata(mediaMetadata)
            .build()

        setVideoTrackDisabled(shouldBeAudioMode)

        player.setMediaItem(mediaItem)
        player.setPlaybackSpeed(video.playbackSpeed.coerceIn(0.25f, 3.0f))
        player.prepare()

        if (resumePosition > 1000L) {
            player.seekTo(resumePosition)
        }
        player.playWhenReady = true
        startProgressTracker()
        resetControlsTimeout()

        // Start background media session service for lock screen and notification controls
        NxvPlaybackService.start(context)
    }

    fun toggleAudioOnlyMode() {
        setAudioOnlyMode(!_uiState.value.isAudioOnlyMode)
    }

    fun setAudioOnlyMode(enabled: Boolean) {
        _uiState.update { it.copy(isAudioOnlyMode = enabled) }
        setVideoTrackDisabled(enabled)

        val currentMediaItem = player.currentMediaItem ?: return
        val updatedMetadata = currentMediaItem.mediaMetadata.buildUpon()
            .setArtist(if (enabled) "NXV Player • Audio Mode" else "NXV Player")
            .build()
        val updatedItem = currentMediaItem.buildUpon()
            .setMediaMetadata(updatedMetadata)
            .build()
        player.setMediaItem(updatedItem, player.currentPosition)
    }

    private fun setVideoTrackDisabled(disabled: Boolean) {
        try {
            val params = player.trackSelectionParameters.buildUpon()
                .setTrackTypeDisabled(C.TRACK_TYPE_VIDEO, disabled)
                .build()
            player.trackSelectionParameters = params
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun play() {
        player.play()
        NxvPlaybackService.start(context)
        resetControlsTimeout()
    }

    fun pause() {
        player.pause()
        showControls(persistent = true)
    }

    fun stopAndClear() {
        stopProgressTracker()
        player.stop()
        player.clearMediaItems()
        NxvPlaybackService.stop(context)
        _uiState.update {
            it.copy(
                currentVideo = null,
                isPlaying = false,
                currentPositionMs = 0L,
                durationMs = 0L,
                bufferedPositionMs = 0L
            )
        }
    }

    fun playPauseToggle() {
        if (player.isPlaying) {
            player.pause()
            showControls(persistent = true)
        } else {
            player.play()
            resetControlsTimeout()
        }
    }

    fun rewind10Seconds() {
        val target = (player.currentPosition - 10_000).coerceAtLeast(0L)
        player.seekTo(target)
        showHud(ActiveGestureHud.DoubleTapSeek(isForward = false, diffSeconds = 10))
        resetControlsTimeout()
    }

    fun forward10Seconds() {
        val maxDuration = player.duration.coerceAtLeast(0L)
        val target = if (maxDuration > 0) (player.currentPosition + 10_000).coerceAtMost(maxDuration) else player.currentPosition + 10_000
        player.seekTo(target)
        showHud(ActiveGestureHud.DoubleTapSeek(isForward = true, diffSeconds = 10))
        resetControlsTimeout()
    }

    fun seekTo(positionMs: Long) {
        val duration = player.duration.coerceAtLeast(0L)
        val target = if (duration > 0) positionMs.coerceIn(0L, duration) else positionMs.coerceAtLeast(0L)
        player.seekTo(target)
        _uiState.update { it.copy(currentPositionMs = target) }
        resetControlsTimeout()
    }

    fun playNext() {
        val state = _uiState.value
        if (state.hasNext) {
            val nextIndex = state.currentVideoIndex + 1
            val nextVideo = state.playlist[nextIndex]
            prepareAndPlay(nextVideo, state.playlist, 0L)
        }
    }

    fun playPrevious() {
        val state = _uiState.value
        if (state.hasPrevious) {
            val prevIndex = state.currentVideoIndex - 1
            val prevVideo = state.playlist[prevIndex]
            prepareAndPlay(prevVideo, state.playlist, 0L)
        } else {
            player.seekTo(0L)
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        val clamped = speed.coerceIn(0.25f, 3.0f)
        player.setPlaybackSpeed(clamped)
        _uiState.update { it.copy(playbackSpeed = clamped) }
        resetControlsTimeout()
    }

    fun startTemporary2xSpeed() {
        if (!_uiState.value.isTemporary2xSpeed) {
            savedSpeedBeforeLongPress = _uiState.value.playbackSpeed
            player.setPlaybackSpeed(2.0f)
            _uiState.update {
                it.copy(
                    isTemporary2xSpeed = true,
                    activeHud = ActiveGestureHud.SpeedBoost
                )
            }
        }
    }

    fun stopTemporary2xSpeed() {
        if (_uiState.value.isTemporary2xSpeed) {
            player.setPlaybackSpeed(savedSpeedBeforeLongPress)
            _uiState.update {
                it.copy(
                    isTemporary2xSpeed = false,
                    activeHud = null
                )
            }
        }
    }

    fun setAspectRatio(mode: AspectRatioMode) {
        _uiState.update { it.copy(aspectRatioMode = mode, zoomScale = 1.0f) }
        resetControlsTimeout()
    }

    fun setZoomScale(scale: Float) {
        val clamped = scale.coerceIn(1.0f, 4.0f)
        _uiState.update { it.copy(zoomScale = clamped) }
        showHud(ActiveGestureHud.Zoom(clamped))
    }

    fun toggleScreenLock() {
        _uiState.update {
            val nextLocked = !it.isScreenLocked
            it.copy(isScreenLocked = nextLocked, areControlsVisible = !nextLocked)
        }
    }

    fun toggleControlsVisibility() {
        if (_uiState.value.isScreenLocked) return
        val current = _uiState.value.areControlsVisible
        if (current) {
            _uiState.update { it.copy(areControlsVisible = false) }
        } else {
            showControls()
        }
    }

    fun showControls(persistent: Boolean = false) {
        _uiState.update { it.copy(areControlsVisible = true) }
        if (!persistent && player.isPlaying) {
            resetControlsTimeout()
        } else {
            controlsTimeoutJob?.cancel()
        }
    }

    fun resetControlsTimeout() {
        controlsTimeoutJob?.cancel()
        controlsTimeoutJob = coroutineScope.launch {
            delay(4000)
            if (player.isPlaying && !_uiState.value.isScreenLocked) {
                _uiState.update { it.copy(areControlsVisible = false) }
            }
        }
    }

    // Gesture adjustments:
    fun adjustBrightness(deltaPercent: Int) {
        val current = _uiState.value.brightnessPercent
        val updated = (current + deltaPercent).coerceIn(0, 100)
        _uiState.update { it.copy(brightnessPercent = updated) }
        showHud(ActiveGestureHud.Brightness(updated))
    }

    fun adjustVolume(deltaPercent: Int) {
        val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
        val currentPercent = _uiState.value.volumePercent
        val updatedPercent = (currentPercent + deltaPercent).coerceIn(0, 100)
        val targetIndex = (updatedPercent * maxVol) / 100
        try {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetIndex, 0)
        } catch (_: Exception) {}
        _uiState.update { it.copy(volumePercent = updatedPercent) }
        showHud(ActiveGestureHud.Volume(updatedPercent))
    }

    fun adjustHorizontalSeek(deltaMs: Long) {
        val current = player.currentPosition
        val duration = player.duration.coerceAtLeast(0L)
        val target = if (duration > 0) (current + deltaMs).coerceIn(0L, duration) else (current + deltaMs).coerceAtLeast(0L)
        val diffSec = ((target - current) / 1000).toInt()
        showHud(ActiveGestureHud.Seek(targetPositionMs = target, diffSeconds = diffSec))
    }

    fun commitHorizontalSeek(targetPositionMs: Long) {
        seekTo(targetPositionMs)
        hideHud()
    }

    fun showHud(hud: ActiveGestureHud) {
        _uiState.update { it.copy(activeHud = hud) }
        hudDismissJob?.cancel()
        hudDismissJob = coroutineScope.launch {
            delay(1500)
            _uiState.update { if (it.activeHud !is ActiveGestureHud.SpeedBoost) it.copy(activeHud = null) else it }
        }
    }

    fun hideHud() {
        hudDismissJob?.cancel()
        _uiState.update { it.copy(activeHud = null) }
    }

    // Audio & Subtitle Tracks:
    private fun updateTracks(tracks: Tracks) {
        val audioList = mutableListOf<AudioTrackInfo>()
        val subList = mutableListOf<SubtitleTrackInfo>()

        var aIdx = 0
        var sIdx = 0

        for (group in tracks.groups) {
            val trackType = group.type
            val trackGroup = group.mediaTrackGroup

            if (trackType == C.TRACK_TYPE_AUDIO) {
                for (i in 0 until trackGroup.length) {
                    val format = trackGroup.getFormat(i)
                    val isSelected = group.isTrackSelected(i)
                    val label = format.label ?: format.language ?: "Track ${aIdx + 1}"
                    audioList.add(
                        AudioTrackInfo(
                            id = format.id ?: "$aIdx-$i",
                            index = aIdx,
                            name = "$label ${format.sampleMimeType?.substringAfterLast('/') ?: ""}".trim(),
                            language = format.language,
                            isSelected = isSelected
                        )
                    )
                    aIdx++
                }
            } else if (trackType == C.TRACK_TYPE_TEXT) {
                for (i in 0 until trackGroup.length) {
                    val format = trackGroup.getFormat(i)
                    val isSelected = group.isTrackSelected(i)
                    val label = format.label ?: format.language ?: "Subtitle ${sIdx + 1}"
                    subList.add(
                        SubtitleTrackInfo(
                            id = format.id ?: "$sIdx-$i",
                            index = sIdx,
                            name = label,
                            language = format.language,
                            isSelected = isSelected
                        )
                    )
                    sIdx++
                }
            }
        }

        _uiState.update {
            it.copy(audioTracks = audioList, subtitleTracks = subList)
        }
    }

    fun selectAudioTrack(trackIndex: Int) {
        val tracks = player.currentTracks
        var currentAudioIdx = 0
        for (group in tracks.groups) {
            if (group.type == C.TRACK_TYPE_AUDIO) {
                val mediaTrackGroup = group.mediaTrackGroup
                for (i in 0 until mediaTrackGroup.length) {
                    if (currentAudioIdx == trackIndex) {
                        player.trackSelectionParameters = player.trackSelectionParameters
                            .buildUpon()
                            .setOverrideForType(TrackSelectionOverride(mediaTrackGroup, i))
                            .build()
                        return
                    }
                    currentAudioIdx++
                }
            }
        }
    }

    fun selectSubtitleTrack(trackIndex: Int) {
        if (trackIndex < 0) {
            // Disable subtitles
            player.trackSelectionParameters = player.trackSelectionParameters
                .buildUpon()
                .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
                .build()
            _uiState.update { it.copy(isSubtitleEnabled = false) }
            return
        }

        val tracks = player.currentTracks
        var currentSubIdx = 0
        for (group in tracks.groups) {
            if (group.type == C.TRACK_TYPE_TEXT) {
                val mediaTrackGroup = group.mediaTrackGroup
                for (i in 0 until mediaTrackGroup.length) {
                    if (currentSubIdx == trackIndex) {
                        player.trackSelectionParameters = player.trackSelectionParameters
                            .buildUpon()
                            .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
                            .setOverrideForType(TrackSelectionOverride(mediaTrackGroup, i))
                            .build()
                        _uiState.update { it.copy(isSubtitleEnabled = true) }
                        return
                    }
                    currentSubIdx++
                }
            }
        }
    }

    fun loadExternalSubtitle(subtitleUri: Uri, mimeType: String = MimeTypes.APPLICATION_SUBRIP) {
        val currentVideo = _uiState.value.currentVideo ?: return
        val pos = player.currentPosition
        val subConfig = MediaItem.SubtitleConfiguration.Builder(subtitleUri)
            .setMimeType(mimeType)
            .setLanguage("External")
            .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
            .build()

        val mediaItem = MediaItem.Builder()
            .setUri(Uri.parse(currentVideo.uri))
            .setSubtitleConfigurations(listOf(subConfig))
            .build()

        player.setMediaItem(mediaItem)
        player.prepare()
        player.seekTo(pos)
        player.play()
    }

    fun setSubtitleEnabled(enabled: Boolean) {
        player.trackSelectionParameters = player.trackSelectionParameters
            .buildUpon()
            .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, !enabled)
            .build()
        _uiState.update { it.copy(isSubtitleEnabled = enabled) }
    }

    fun setSubtitleStyle(fontSizeSp: Int, opacity: Float) {
        _uiState.update { it.copy(subtitleFontSizeSp = fontSizeSp, subtitleOpacity = opacity) }
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = coroutineScope.launch(Dispatchers.Main) {
            while (isActive) {
                if (player.isPlaying) {
                    val pos = player.currentPosition.coerceAtLeast(0L)
                    val dur = player.duration.coerceAtLeast(0L)
                    val buf = player.bufferedPosition.coerceAtLeast(0L)
                    _uiState.update {
                        it.copy(
                            currentPositionMs = pos,
                            durationMs = dur,
                            bufferedPositionMs = buf
                        )
                    }
                }
                delay(500)
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
    }

    fun release() {
        progressJob?.cancel()
        controlsTimeoutJob?.cancel()
        hudDismissJob?.cancel()
        NxvPlaybackService.stop(context)
        try {
            mediaSession?.release()
            mediaSession = null
        } catch (_: Exception) {}
        player.release()
        if (instance === this) {
            instance = null
        }
    }

    companion object {
        @Volatile
        private var instance: PlayerManager? = null

        fun getInstance(context: Context): PlayerManager {
            return instance ?: synchronized(this) {
                instance ?: PlayerManager(
                    context.applicationContext,
                    CoroutineScope(SupervisorJob() + Dispatchers.Main)
                ).also { instance = it }
            }
        }
    }
}

/**
 * Backward compatibility alias
 */
typealias NxvPlayerManager = PlayerManager
