package com.example.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddLink
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.domain.model.FolderItem
import com.example.domain.model.VideoItem
import com.example.ui.MainViewModel
import com.example.ui.components.AddStreamUrlDialog
import com.example.ui.components.EmptyStateView
import com.example.ui.components.VideoCard
import com.example.ui.components.VideoDetailsDialog
import com.example.ui.components.bouncingClickable
import com.example.ui.theme.AmoledBackground
import com.example.ui.theme.AmoledCard
import com.example.ui.theme.NxvYellow
import com.example.utils.FileUtils
import com.example.utils.StreamUrlHelper
import com.example.utils.TimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToPlayer: (VideoItem) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToFolder: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val allVideos by viewModel.allVideos.collectAsStateWithLifecycle()
    val localVideos by viewModel.localVideos.collectAsStateWithLifecycle()
    val savedStreams by viewModel.savedStreams.collectAsStateWithLifecycle()
    val continueWatching by viewModel.continueWatching.collectAsStateWithLifecycle()
    val recentlyPlayed by viewModel.recentlyPlayed.collectAsStateWithLifecycle()
    val favorites by viewModel.favoriteVideos.collectAsStateWithLifecycle()
    val folders by viewModel.folders.collectAsStateWithLifecycle()
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var selectedVideoForDetails by remember { mutableStateOf<VideoItem?>(null) }
    var showAddStreamDialog by remember { mutableStateOf(false) }

    if (showAddStreamDialog) {
        AddStreamUrlDialog(
            onDismiss = { showAddStreamDialog = false },
            onPlayUrl = { url, title, saveToLibrary ->
                if (saveToLibrary) {
                    viewModel.addStreamUrl(url, title) { videoItem ->
                        viewModel.playVideo(videoItem, allVideos + videoItem)
                        onNavigateToPlayer(videoItem)
                    }
                } else {
                    val streamItem = StreamUrlHelper.createStreamVideoItem(url, title)
                    viewModel.playVideo(streamItem, allVideos + streamItem)
                    onNavigateToPlayer(streamItem)
                }
            }
        )
    }

    selectedVideoForDetails?.let { video ->
        VideoDetailsDialog(video = video, onDismiss = { selectedVideoForDetails = null })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "NXV",
                            color = NxvYellow,
                            fontWeight = FontWeight.Black,
                            fontSize = 22.sp,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "PLAYER",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                },
                actions = {
                    // + Add URL Link Action Button
                    IconButton(
                        onClick = { showAddStreamDialog = true },
                        modifier = Modifier.testTag("home_add_stream_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddLink,
                            contentDescription = "Add Online Stream URL",
                            tint = NxvYellow
                        )
                    }

                    if (isScanning) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(24.dp)
                                .padding(end = 8.dp),
                            color = NxvYellow,
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        IconButton(
                            onClick = { viewModel.scanVideos() },
                            modifier = Modifier.testTag("home_refresh_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Scan Storage",
                                tint = Color.White
                            )
                        }
                    }

                    IconButton(
                        onClick = onNavigateToSearch,
                        modifier = Modifier.testTag("home_search_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AmoledBackground
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddStreamDialog = true },
                containerColor = NxvYellow,
                contentColor = Color.Black,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                modifier = Modifier
                    .padding(bottom = 70.dp)
                    .bouncingClickable(onClick = { showAddStreamDialog = true })
                    .testTag("home_fab_add_stream")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Online Video URL",
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        containerColor = AmoledBackground,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        if (allVideos.isEmpty() && !isScanning) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                EmptyStateView(
                    title = "No Media Found",
                    subtitle = "Scan your device storage or tap '+' to paste YouTube, TikTok, or video stream URLs.",
                    icon = Icons.Default.VideoLibrary,
                    actionButtonLabel = "Scan Device Storage",
                    onAction = { viewModel.scanVideos() }
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                // 1. ONLINE STREAMS & LINKS SECTION
                if (savedStreams.isNotEmpty()) {
                    item {
                        SectionHeader(title = "Online Streams & Links", icon = Icons.Default.Language)
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
                        ) {
                            items(savedStreams) { streamVideo ->
                                OnlineStreamCard(
                                    video = streamVideo,
                                    onClick = {
                                        viewModel.playVideo(streamVideo, allVideos)
                                        onNavigateToPlayer(streamVideo)
                                    },
                                    onDelete = {
                                        viewModel.deleteStream(streamVideo)
                                    }
                                )
                            }
                        }
                    }
                }

                // 2. CONTINUE WATCHING SECTION
                if (continueWatching.isNotEmpty()) {
                    item {
                        SectionHeader(title = "Continue Watching", icon = Icons.Default.History)
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
                        ) {
                            items(continueWatching) { video ->
                                ContinueWatchingCard(
                                    video = video,
                                    onClick = {
                                        viewModel.playVideo(video, allVideos)
                                        onNavigateToPlayer(video)
                                    }
                                )
                            }
                        }
                    }
                }

                // 3. FOLDERS QUICK ROW
                if (folders.isNotEmpty()) {
                    item {
                        SectionHeader(title = "Device Folders", icon = Icons.Default.Folder)
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
                        ) {
                            items(folders) { folder ->
                                FolderChip(
                                    folder = folder,
                                    onClick = { onNavigateToFolder(folder.name) }
                                )
                            }
                        }
                    }
                }

                // 4. RECENTLY PLAYED SECTION
                if (recentlyPlayed.isNotEmpty()) {
                    item {
                        SectionHeader(title = "Recently Played", icon = Icons.Default.History)
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
                        ) {
                            items(recentlyPlayed) { video ->
                                RecentVideoCard(
                                    video = video,
                                    onClick = {
                                        viewModel.playVideo(video, allVideos)
                                        onNavigateToPlayer(video)
                                    }
                                )
                            }
                        }
                    }
                }

                // 5. FAVORITES PREVIEW
                if (favorites.isNotEmpty()) {
                    item {
                        SectionHeader(title = "Favorites", icon = Icons.Default.Star)
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
                        ) {
                            items(favorites) { video ->
                                RecentVideoCard(
                                    video = video,
                                    onClick = {
                                        viewModel.playVideo(video, allVideos)
                                        onNavigateToPlayer(video)
                                    }
                                )
                            }
                        }
                    }
                }

                // 6. ALL VIDEOS SECTION
                item {
                    SectionHeader(
                        title = "Storage Media (${allVideos.size})",
                        icon = Icons.Default.VideoLibrary
                    )
                }

                items(allVideos.take(30)) { video ->
                    VideoCard(
                        video = video,
                        onClick = {
                            viewModel.playVideo(video, allVideos)
                            onNavigateToPlayer(video)
                        },
                        onToggleFavorite = { viewModel.toggleFavorite(video) },
                        onShowDetails = { selectedVideoForDetails = video },
                        onDelete = { viewModel.deleteVideo(video) },
                        onPlayAsAudio = {
                            viewModel.playVideoAsAudio(video, allVideos, context)
                            onNavigateToPlayer(video)
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = NxvYellow,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            color = Color.White,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun OnlineStreamCard(
    video: VideoItem,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val platform = video.resolution.ifBlank { StreamUrlHelper.detectPlatform(video.uri) }
    val ytId = StreamUrlHelper.extractYouTubeId(video.uri)
    val thumbUrl = if (ytId != null) StreamUrlHelper.getYouTubeThumbnail(ytId) else null

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = AmoledCard,
        modifier = Modifier
            .width(200.dp)
            .clip(RoundedCornerShape(14.dp))
            .bouncingClickable(onClick = onClick)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(Color(0xFF1B1B22)),
                contentAlignment = Alignment.Center
            ) {
                if (!thumbUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = thumbUrl,
                        contentDescription = video.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = when (platform) {
                            "YouTube" -> Icons.Default.SmartDisplay
                            "TikTok" -> Icons.Default.Videocam
                            "HLS Stream" -> Icons.Default.LiveTv
                            else -> Icons.Default.Language
                        },
                        contentDescription = null,
                        tint = NxvYellow,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Platform Badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xDD000000),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                ) {
                    Text(
                        text = platform,
                        color = NxvYellow,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                // Play Button Overlay
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xAA000000)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = video.title,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Tap to stream online",
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun ContinueWatchingCard(
    video: VideoItem,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = AmoledCard,
        modifier = Modifier
            .width(220.dp)
            .clip(RoundedCornerShape(14.dp))
            .bouncingClickable(onClick = onClick)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(Color(0xFF222222))
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(video.contentUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = video.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Play Icon Overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xAA000000)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = NxvYellow,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Time remaining badge
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xCC000000),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                ) {
                    val remainingMs = (video.durationMs - video.lastPlayedPositionMs).coerceAtLeast(0L)
                    Text(
                        text = "${TimeUtils.formatDuration(remainingMs)} left",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }

                // Progress line
                LinearProgressIndicator(
                    progress = { video.progressPercentage },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .align(Alignment.BottomCenter),
                    color = NxvYellow,
                    trackColor = Color(0x40FFFFFF)
                )
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = video.title,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Resume at ${TimeUtils.formatDuration(video.lastPlayedPositionMs)}",
                    color = NxvYellow,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun RecentVideoCard(
    video: VideoItem,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = AmoledCard,
        modifier = Modifier
            .width(160.dp)
            .clip(RoundedCornerShape(12.dp))
            .bouncingClickable(onClick = onClick)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(Color(0xFF222222))
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(video.contentUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = video.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xCC000000),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                ) {
                    Text(
                        text = TimeUtils.formatDuration(video.durationMs),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = video.title,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = FileUtils.formatFileSize(video.sizeBytes),
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun FolderChip(
    folder: FolderItem,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = AmoledCard,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .bouncingClickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = null,
                tint = NxvYellow,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = folder.name,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.width(6.dp))
            Surface(
                shape = CircleShape,
                color = Color(0x33FFFFFF)
            ) {
                Text(
                    text = "${folder.videoCount}",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}
