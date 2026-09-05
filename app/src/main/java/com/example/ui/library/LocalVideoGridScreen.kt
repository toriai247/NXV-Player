package com.example.ui.library

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLink
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.TableRows
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import coil.decode.VideoFrameDecoder
import com.example.domain.model.VideoItem
import com.example.ui.MainViewModel
import com.example.ui.components.AddStreamUrlDialog
import com.example.ui.components.EmptyStateView
import com.example.ui.components.VideoDetailsDialog
import com.example.ui.components.bouncingClickable
import com.example.ui.theme.AmoledBackground
import com.example.ui.theme.AmoledCard
import com.example.ui.theme.AmoledSurface
import com.example.ui.theme.NxvYellow
import com.example.utils.FileUtils
import com.example.utils.StreamUrlHelper
import com.example.utils.TimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalVideoGridScreen(
    viewModel: MainViewModel,
    onNavigateToPlayer: (VideoItem) -> Unit,
    onNavigateToSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Determine target permission based on Android version
    // Android 13+ (API 33+) requires READ_MEDIA_VIDEO for secure granular media access
    val targetPermission = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_VIDEO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
    }

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, targetPermission) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Runtime Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        if (isGranted) {
            viewModel.scanVideos()
        }
    }

    // Re-check permission on lifecycle ON_RESUME (e.g., returning from App Settings)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val currentGranted = ContextCompat.checkSelfPermission(
                    context,
                    targetPermission
                ) == PackageManager.PERMISSION_GRANTED
                if (currentGranted != hasPermission) {
                    hasPermission = currentGranted
                    if (currentGranted) {
                        viewModel.scanVideos()
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Auto-prompt permission on first launch if not yet granted
    LaunchedEffect(targetPermission) {
        if (!hasPermission) {
            permissionLauncher.launch(targetPermission)
        }
    }

    val videos by viewModel.allVideos.collectAsStateWithLifecycle()
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()

    var columnCount by remember { mutableIntStateOf(2) } // 2 or 3 columns in grid
    var currentSort by remember { mutableStateOf(VideoSort.DATE_DESC) }
    var sortMenuExpanded by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchExpanded by remember { mutableStateOf(false) }
    var selectedVideoForDetails by remember { mutableStateOf<VideoItem?>(null) }
    var showAddStreamDialog by remember { mutableStateOf(false) }

    if (showAddStreamDialog) {
        AddStreamUrlDialog(
            onDismiss = { showAddStreamDialog = false },
            onPlayUrl = { url, title, saveToLibrary ->
                if (saveToLibrary) {
                    viewModel.addStreamUrl(url, title) { videoItem ->
                        viewModel.playVideo(videoItem, videos + videoItem)
                        onNavigateToPlayer(videoItem)
                    }
                } else {
                    val streamItem = StreamUrlHelper.createStreamVideoItem(url, title)
                    viewModel.playVideo(streamItem, videos + streamItem)
                    onNavigateToPlayer(streamItem)
                }
            }
        )
    }

    selectedVideoForDetails?.let { video ->
        VideoDetailsDialog(video = video, onDismiss = { selectedVideoForDetails = null })
    }

    // Filter, search and sort
    val filteredVideos = remember(videos, selectedFilter, currentSort, searchQuery) {
        var result = videos

        if (searchQuery.isNotBlank()) {
            val query = searchQuery.trim().lowercase()
            result = result.filter { it.title.lowercase().contains(query) || it.folderName.lowercase().contains(query) }
        }

        result = when (selectedFilter) {
            "Online Streams" -> result.filter { it.folderName == "Online Streams" || it.mimeType.startsWith("video/youtube") || it.mimeType.startsWith("video/tiktok") }
            "4K / HD" -> result.filter { it.resolution in listOf("4K", "2K", "1080p") }
            "Short (<5m)" -> result.filter { it.durationMs in 1..299_999L }
            "Long (>30m)" -> result.filter { it.durationMs >= 1_800_000L }
            else -> result
        }

        when (currentSort) {
            VideoSort.DATE_DESC -> result.sortedByDescending { it.dateAdded }
            VideoSort.DATE_ASC -> result.sortedBy { it.dateAdded }
            VideoSort.NAME_ASC -> result.sortedBy { it.title.lowercase() }
            VideoSort.NAME_DESC -> result.sortedByDescending { it.title.lowercase() }
            VideoSort.DURATION_DESC -> result.sortedByDescending { it.durationMs }
            VideoSort.DURATION_ASC -> result.sortedBy { it.durationMs }
            VideoSort.SIZE_DESC -> result.sortedByDescending { it.sizeBytes }
        }
    }

    val totalSizeBytes = remember(videos) { videos.sumOf { it.sizeBytes } }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AmoledBackground)
    ) {
        // App Bar
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = "Local Videos",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (hasPermission) {
                            "${videos.size} videos • ${FileUtils.formatFileSize(totalSizeBytes)}"
                        } else {
                            "Permission required"
                        },
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            },
            actions = {
                // + Add Online Stream URL
                IconButton(
                    onClick = { showAddStreamDialog = true },
                    modifier = Modifier.testTag("grid_add_stream_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AddLink,
                        contentDescription = "Add Online Stream URL",
                        tint = NxvYellow
                    )
                }

                if (hasPermission) {
                    // Search Button
                    IconButton(
                        onClick = { isSearchExpanded = !isSearchExpanded },
                        modifier = Modifier.testTag("grid_search_toggle")
                    ) {
                        Icon(
                            imageVector = if (isSearchExpanded) Icons.Default.Clear else Icons.Default.Search,
                            contentDescription = "Search Local Videos",
                            tint = if (isSearchExpanded) NxvYellow else Color.White
                        )
                    }

                    // Column Switcher (2 columns <-> 3 columns)
                    IconButton(
                        onClick = { columnCount = if (columnCount == 2) 3 else 2 },
                        modifier = Modifier.testTag("grid_columns_toggle")
                    ) {
                        Icon(
                            imageVector = if (columnCount == 2) Icons.Default.GridView else Icons.Default.TableRows,
                            contentDescription = "Toggle Grid Density",
                            tint = Color.White
                        )
                    }

                    // Refresh / Rescan
                    IconButton(
                        onClick = { viewModel.scanVideos() },
                        modifier = Modifier.testTag("rescan_videos_button")
                    ) {
                        if (isScanning) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = NxvYellow,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Rescan Storage",
                                tint = Color.White
                            )
                        }
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = AmoledBackground)
        )

        // If permission is NOT granted, show clear, privacy-focused rationale UI
        if (!hasPermission) {
            StoragePermissionCard(
                targetPermission = targetPermission,
                onRequestPermission = { permissionLauncher.launch(targetPermission) },
                onOpenSettings = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                }
            )
        } else {
            // Search Input Row (Collapsible)
            AnimatedVisibility(
                visible = isSearchExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search local video titles or folders...", color = Color.Gray, fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = NxvYellow, modifier = Modifier.size(20.dp))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.Gray, modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = NxvYellow,
                        unfocusedBorderColor = Color(0x33FFFFFF),
                        focusedContainerColor = AmoledCard,
                        unfocusedContainerColor = AmoledCard
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .testTag("grid_search_input")
                )
            }

            // Scanning banner if active
            if (isScanning) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().height(3.dp),
                    color = NxvYellow,
                    trackColor = Color(0x22FFFFFF)
                )
            }

            // Filter Chips and Sort Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Filter chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    val filters = listOf("All", "Online Streams", "4K / HD", "Short (<5m)", "Long (>30m)")
                    items(filters) { filter ->
                        val isSelected = selectedFilter == filter
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFilter = filter },
                            label = { Text(filter, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NxvYellow,
                                selectedLabelColor = Color.Black,
                                containerColor = AmoledCard,
                                labelColor = Color.White
                            ),
                            border = null
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Sort Dropdown Button
                Box {
                    Surface(
                        onClick = { sortMenuExpanded = true },
                        shape = RoundedCornerShape(8.dp),
                        color = AmoledCard,
                        modifier = Modifier.testTag("grid_sort_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sort,
                                contentDescription = null,
                                tint = NxvYellow,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Sort",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = sortMenuExpanded,
                        onDismissRequest = { sortMenuExpanded = false },
                        modifier = Modifier.background(AmoledCard)
                    ) {
                        VideoSort.entries.forEach { sort ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = sort.title,
                                        color = if (currentSort == sort) NxvYellow else Color.White,
                                        fontWeight = if (currentSort == sort) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    currentSort = sort
                                    sortMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Video Content Grid or Empty State
            if (filteredVideos.isEmpty()) {
                if (videos.isEmpty() && !isScanning) {
                    EmptyStateView(
                        title = "No Local Videos Found",
                        subtitle = "No video files were detected in device storage. Transfer or record videos and rescan.",
                        actionButtonLabel = "Rescan Storage",
                        onAction = { viewModel.scanVideos() },
                        modifier = Modifier.testTag("empty_videos_view")
                    )
                } else if (searchQuery.isNotBlank() || selectedFilter != "All") {
                    EmptyStateView(
                        title = "No Matching Videos",
                        subtitle = "No videos match the current search query or filter.",
                        actionButtonLabel = "Reset Filters",
                        onAction = {
                            searchQuery = ""
                            selectedFilter = "All"
                        }
                    )
                }
            } else {
                // LAZY VERTICAL GRID for Local Video Files
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columnCount),
                    contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 6.dp, bottom = 100.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("local_videos_vertical_grid")
                ) {
                    items(filteredVideos, key = { it.id }) { video ->
                        LocalVideoGridCard(
                            video = video,
                            onClick = {
                                viewModel.playVideo(video, filteredVideos)
                                onNavigateToPlayer(video)
                            },
                            onPlayAsAudio = {
                                viewModel.playVideoAsAudio(video, filteredVideos, context)
                                onNavigateToPlayer(video)
                            },
                            onToggleFavorite = { viewModel.toggleFavorite(video) },
                            onShowDetails = { selectedVideoForDetails = video },
                            onDelete = { viewModel.deleteVideo(video) },
                            isCompact = columnCount >= 3
                        )
                    }
                }
            }
        }
    }
}

/**
 * Android 13+ Compliant Storage Permission Card with Granular Explanation
 */
@Composable
fun StoragePermissionCard(
    targetPermission: String,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isAndroid13Plus = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = AmoledCard,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFFFFF)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("storage_permission_card")
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Security Icon
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(NxvYellow.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = NxvYellow,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Storage Permission Required",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Privacy Disclosure Tag for Android 13+
                if (isAndroid13Plus) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0x224CAF50),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x444CAF50)),
                        modifier = Modifier.padding(vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF81C784),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Android 13+ READ_MEDIA_VIDEO Compliant",
                                color = Color(0xFF81C784),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (isAndroid13Plus) {
                        "To discover and play your local videos, NXV Player requires video media access (READ_MEDIA_VIDEO). In compliance with modern Android security standards, only video files are queried. Your photos, documents, and personal files are never accessed."
                    } else {
                        "To discover and play your local videos, NXV Player requires storage read permission to scan your video library."
                    },
                    color = Color.LightGray,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Grant Permission Primary Button
                Button(
                    onClick = onRequestPermission,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NxvYellow,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("grant_permission_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Allow Video Access",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Open App Settings Button
                OutlinedButton(
                    onClick = onOpenSettings,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x44FFFFFF)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("open_settings_button")
                ) {
                    Text(
                        text = "Open App Settings",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * Modern Grid Card with Coil Video Frame Thumbnail Preview
 */
@Composable
fun LocalVideoGridCard(
    video: VideoItem,
    onClick: () -> Unit,
    onPlayAsAudio: () -> Unit,
    onToggleFavorite: () -> Unit,
    onShowDetails: () -> Unit,
    onDelete: () -> Unit,
    isCompact: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var menuExpanded by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = AmoledCard,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .bouncingClickable(onClick = onClick)
            .testTag("video_grid_card_${video.id}")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Thumbnail container with Coil image loader
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(Color(0xFF1C1C22))
            ) {
                // Coil SubcomposeAsyncImage with frame decoding
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(video.contentUri)
                        .decoderFactory { result, options, _ -> VideoFrameDecoder(result.source, options) }
                        .crossfade(true)
                        .build(),
                    contentDescription = video.title,
                    contentScale = ContentScale.Crop,
                    loading = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF22222A)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = NxvYellow.copy(alpha = 0.5f),
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    error = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF22222A)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = null,
                                tint = Color(0x66FFFFFF),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Gradient scrim on bottom for readability
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0xAA000000))
                            )
                        )
                )

                // Resolution Badge (Top Left)
                if (video.resolution.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xD9000000),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(5.dp)
                    ) {
                        Text(
                            text = video.resolution,
                            color = NxvYellow,
                            fontSize = if (isCompact) 8.sp else 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                // Duration Badge (Bottom Right)
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xD9000000),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(5.dp)
                ) {
                    Text(
                        text = TimeUtils.formatDuration(video.durationMs),
                        color = Color.White,
                        fontSize = if (isCompact) 9.sp else 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }

                // Play icon pill overlay (Bottom Left)
                Surface(
                    shape = CircleShape,
                    color = Color(0xCC000000),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(5.dp)
                        .size(if (isCompact) 18.dp else 22.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.White,
                        modifier = Modifier.padding(if (isCompact) 2.dp else 3.dp)
                    )
                }

                // Watched Progress Bar
                if (video.progressPercentage > 0.05f) {
                    LinearProgressIndicator(
                        progress = { video.progressPercentage },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .align(Alignment.BottomCenter),
                        color = NxvYellow,
                        trackColor = Color(0x44FFFFFF)
                    )
                }
            }

            // Info & Options below thumbnail
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = video.title,
                        color = Color.White,
                        fontSize = if (isCompact) 12.sp else 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    // 3-dot Options Menu
                    Box {
                        IconButton(
                            onClick = { menuExpanded = true },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Video Options",
                                tint = Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            modifier = Modifier.background(AmoledSurface)
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = NxvYellow, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Play Video", color = Color.White)
                                    }
                                },
                                onClick = {
                                    menuExpanded = false
                                    onClick()
                                }
                            )

                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Headphones, contentDescription = null, tint = NxvYellow, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Play as Audio", color = Color.White)
                                    }
                                },
                                onClick = {
                                    menuExpanded = false
                                    onPlayAsAudio()
                                }
                            )

                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (video.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                            contentDescription = null,
                                            tint = if (video.isFavorite) Color.Red else Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(if (video.isFavorite) "Remove Favorite" else "Add to Favorites", color = Color.White)
                                    }
                                },
                                onClick = {
                                    menuExpanded = false
                                    onToggleFavorite()
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Video Details", color = Color.White) },
                                onClick = {
                                    menuExpanded = false
                                    onShowDetails()
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Delete Video", color = Color(0xFFFF5252)) },
                                onClick = {
                                    menuExpanded = false
                                    onDelete()
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${FileUtils.formatFileSize(video.sizeBytes)} • ${video.folderName}",
                        color = Color.Gray,
                        fontSize = if (isCompact) 10.sp else 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    if (video.isFavorite) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Favorite",
                            tint = Color.Red,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }
}
