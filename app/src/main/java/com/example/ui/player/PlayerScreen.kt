package com.example.ui.player

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Build
import android.util.Rational
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.domain.model.AspectRatioMode
import com.example.domain.model.VideoItem
import com.example.player.AudioTrackBottomSheet
import com.example.player.PlaybackSpeedBottomSheet
import com.example.player.PlayerControlsOverlay
import com.example.player.PlayerGestureDetector
import com.example.player.PlayerHudOverlay
import com.example.player.PlayerManager
import com.example.player.SubtitleBottomSheet
import com.example.ui.MainViewModel
import com.example.ui.theme.AmoledBackground
import com.example.ui.theme.NxvYellow
import com.example.utils.StreamUrlHelper
import kotlinx.coroutines.launch

fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
    video: VideoItem,
    playlist: List<VideoItem>,
    viewModel: MainViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val activity = remember(context) { context.findActivity() }
    val view = LocalView.current

    val playerManager = remember { PlayerManager.getInstance(context) }
    val uiState by playerManager.uiState.collectAsStateWithLifecycle()
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()

    val isYouTube = remember(video.uri) { StreamUrlHelper.isYouTubeUrl(video.uri) }
    val isTikTok = remember(video.uri) { StreamUrlHelper.isTikTokUrl(video.uri) }
    val isWebEmbed = isYouTube || isTikTok

    var showSubtitleSheet by remember { mutableStateOf(false) }
    var showAudioSheet by remember { mutableStateOf(false) }
    var showSpeedSheet by remember { mutableStateOf(false) }
    var isLandscape by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    // Prepare and play on entry if video changed (for non-web embed)
    LaunchedEffect(video.uri) {
        if (!isWebEmbed) {
            val currentUri = playerManager.uiState.value.currentVideo?.uri
            if (currentUri != video.uri) {
                val resumePos = if (preferences.resumePlayback) video.lastPlayedPositionMs else 0L
                playerManager.prepareAndPlay(video, playlist, resumePos)
            }
        }
    }

    // Keep screen on & manage immersive mode
    DisposableEffect(activity) {
        val window = activity?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val windowInsetsController = window?.let { WindowCompat.getInsetsController(it, view) }
        windowInsetsController?.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController?.hide(WindowInsetsCompat.Type.systemBars())

        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            windowInsetsController?.show(WindowInsetsCompat.Type.systemBars())
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

            if (!isWebEmbed) {
                // Save progress to database
                val currentPos = playerManager.uiState.value.currentPositionMs
                val currentVideo = playerManager.uiState.value.currentVideo ?: video
                coroutineScope.launch {
                    viewModel.repository.updatePlaybackProgress(
                        uri = currentVideo.uri,
                        positionMs = currentPos
                    )
                }

                val isBackgroundActive = preferences.backgroundAudio || playerManager.uiState.value.isAudioOnlyMode
                if (!isBackgroundActive) {
                    playerManager.pause()
                }
            }
        }
    }

    // Auto Next when completed
    LaunchedEffect(uiState.currentPositionMs, uiState.durationMs) {
        if (!isWebEmbed && preferences.autoNext && uiState.durationMs > 0 && uiState.currentPositionMs >= uiState.durationMs - 1000L) {
            if (uiState.hasNext) {
                playerManager.playNext()
            }
        }
    }

    // Subtitle Sheet
    if (showSubtitleSheet) {
        SubtitleBottomSheet(
            uiState = uiState,
            onDismiss = { showSubtitleSheet = false },
            onToggleSubtitle = { playerManager.setSubtitleEnabled(it) },
            onSelectTrack = {
                playerManager.selectSubtitleTrack(it)
                showSubtitleSheet = false
            },
            onLoadExternalSubtitle = { uri ->
                playerManager.loadExternalSubtitle(uri)
                showSubtitleSheet = false
            },
            onUpdateStyle = { size, opacity ->
                playerManager.setSubtitleStyle(size, opacity)
            }
        )
    }

    // Audio Sheet
    if (showAudioSheet) {
        AudioTrackBottomSheet(
            uiState = uiState,
            onDismiss = { showAudioSheet = false },
            onSelectTrack = {
                playerManager.selectAudioTrack(it)
                showAudioSheet = false
            }
        )
    }

    // Speed Sheet
    if (showSpeedSheet) {
        PlaybackSpeedBottomSheet(
            currentSpeed = uiState.playbackSpeed,
            onDismiss = { showSpeedSheet = false },
            onSelectSpeed = { playerManager.setPlaybackSpeed(it) }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AmoledBackground)
    ) {
        if (isWebEmbed) {
            // In-App Hardware-Accelerated Web Player for YouTube & TikTok URLs
            Box(modifier = Modifier.fillMaxSize()) {
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.mediaPlaybackRequiresUserGesture = false
                            settings.loadWithOverviewMode = true
                            settings.useWideViewPort = true
                            settings.allowContentAccess = true
                            settings.userAgentString = "Mozilla/5.0 (Linux; Android 13; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
                            webChromeClient = WebChromeClient()
                            webViewClient = WebViewClient()
                            setBackgroundColor(android.graphics.Color.BLACK)

                            val targetUrl = when {
                                isYouTube -> {
                                    val ytId = StreamUrlHelper.extractYouTubeId(video.uri)
                                    if (ytId != null) StreamUrlHelper.getYouTubeEmbedUrl(ytId) else video.uri
                                }
                                isTikTok -> StreamUrlHelper.getTikTokEmbedUrl(video.uri)
                                else -> video.uri
                            }
                            loadUrl(targetUrl)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Sleek Top Floating Overlay Bar for Web Embeds
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 24.dp)
                        .align(Alignment.TopCenter),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        onClick = onBack,
                        shape = CircleShape,
                        color = Color(0x99000000)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = video.title,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.widthIn(max = 200.dp)
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            onClick = {
                                isLandscape = !isLandscape
                                activity?.requestedOrientation = if (isLandscape) {
                                    ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                                } else {
                                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                                }
                            },
                            shape = CircleShape,
                            color = Color(0x99000000),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.ScreenRotation,
                                    contentDescription = "Rotate",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Native ExoPlayer Video / Audio Surface with Gestures and HUD
            PlayerGestureDetector(
                playerManager = playerManager,
                isLocked = uiState.isScreenLocked
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = uiState.zoomScale
                            scaleY = uiState.zoomScale
                        }
                ) {
                    if (uiState.isAudioOnlyMode) {
                        AudioModeView(
                            uiState = uiState,
                            onSwitchToVideo = { playerManager.setAudioOnlyMode(false) },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // ExoPlayer Surface View with TextureView for hardware transformation/scaling compatibility
                        AndroidView(
                            factory = { ctx ->
                                val playerView = LayoutInflater.from(ctx).inflate(
                                    com.example.R.layout.view_player,
                                    null,
                                    false
                                ) as PlayerView
                                playerView.player = playerManager.player
                                playerView.useController = false
                                playerView.layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                playerView
                            },
                            update = { playerView ->
                                playerView.player = playerManager.player
                                playerView.resizeMode = when (uiState.aspectRatioMode) {
                                    AspectRatioMode.FIT -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                                    AspectRatioMode.FILL -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                                    AspectRatioMode.ZOOM -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                                    AspectRatioMode.ORIGINAL -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // Buffering Spinner
                AnimatedVisibility(
                    visible = uiState.isBuffering,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    CircularProgressIndicator(
                        color = NxvYellow,
                        strokeWidth = 3.dp
                    )
                }

                // Gesture HUD Overlay (Brightness, Volume, Seek, 2x, Zoom)
                PlayerHudOverlay(
                    activeHud = uiState.activeHud,
                    modifier = Modifier.fillMaxSize()
                )

                // On-screen Controls Overlay
                PlayerControlsOverlay(
                    uiState = uiState,
                    onBack = {
                        // Update progress before back
                        val currentPos = playerManager.uiState.value.currentPositionMs
                        val currentVideo = playerManager.uiState.value.currentVideo ?: video
                        coroutineScope.launch {
                            viewModel.repository.updatePlaybackProgress(currentVideo.uri, currentPos)
                        }
                        onBack()
                    },
                    onPlayPause = { playerManager.playPauseToggle() },
                    onRewind10 = { playerManager.rewind10Seconds() },
                    onForward10 = { playerManager.forward10Seconds() },
                    onPrevious = { playerManager.playPrevious() },
                    onNext = { playerManager.playNext() },
                    onSeek = { playerManager.seekTo(it) },
                    onToggleLock = { playerManager.toggleScreenLock() },
                    onCycleAspectRatio = {
                        val nextMode = when (uiState.aspectRatioMode) {
                            AspectRatioMode.FIT -> AspectRatioMode.FILL
                            AspectRatioMode.FILL -> AspectRatioMode.ZOOM
                            AspectRatioMode.ZOOM -> AspectRatioMode.ORIGINAL
                            AspectRatioMode.ORIGINAL -> AspectRatioMode.FIT
                        }
                        playerManager.setAspectRatio(nextMode)
                    },
                    onRotateScreen = {
                        isLandscape = !isLandscape
                        activity?.requestedOrientation = if (isLandscape) {
                            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                        } else {
                            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                        }
                    },
                    onOpenPip = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            try {
                                val aspectRatio = if (video.width > 0 && video.height > 0) {
                                    Rational(video.width.coerceAtLeast(1), video.height.coerceAtLeast(1))
                                } else {
                                    Rational(16, 9)
                                }
                                val pipParams = PictureInPictureParams.Builder()
                                    .setAspectRatio(aspectRatio)
                                    .build()
                                activity?.enterPictureInPictureMode(pipParams)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    },
                    onOpenSpeedSheet = { showSpeedSheet = true },
                    onOpenSubtitleSheet = { showSubtitleSheet = true },
                    onOpenAudioSheet = { showAudioSheet = true },
                    onToggleAudioMode = { playerManager.toggleAudioOnlyMode() }
                )
            }

            // Error Dialog/Overlay
            AnimatedVisibility(
                visible = uiState.errorMessage != null,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = "Playback Error",
                            tint = Color(0xFFFF5252),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "Playback Failed",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = uiState.errorMessage ?: "An unexpected playback error occurred.",
                            color = Color.LightGray,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = onBack,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                            ) {
                                Text("Go Back")
                            }
                            Button(
                                onClick = { playerManager.retry() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NxvYellow,
                                    contentColor = Color.Black
                                )
                            ) {
                                Text("Retry", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}
