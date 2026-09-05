package com.example.ui.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.VideoItem
import com.example.ui.MainViewModel
import com.example.ui.components.EmptyStateView
import com.example.ui.components.VideoCard
import com.example.ui.components.VideoDetailsDialog
import com.example.ui.theme.AmoledBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    viewModel: MainViewModel,
    onNavigateToPlayer: (VideoItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val favorites by viewModel.favoriteVideos.collectAsStateWithLifecycle()
    var selectedVideoForDetails by remember { mutableStateOf<VideoItem?>(null) }

    selectedVideoForDetails?.let { video ->
        VideoDetailsDialog(video = video, onDismiss = { selectedVideoForDetails = null })
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AmoledBackground)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Favorites (${favorites.size})",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = AmoledBackground)
        )

        if (favorites.isEmpty()) {
            EmptyStateView(
                title = "No Favorites Yet",
                subtitle = "Star your favorite videos in the library to easily access them here anytime.",
                icon = Icons.Default.Star
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(favorites, key = { it.id }) { video ->
                    VideoCard(
                        video = video,
                        onClick = {
                            viewModel.playVideo(video, favorites)
                            onNavigateToPlayer(video)
                        },
                        onToggleFavorite = { viewModel.toggleFavorite(video) },
                        onShowDetails = { selectedVideoForDetails = video },
                        onDelete = { viewModel.deleteVideo(video) }
                    )
                }
            }
        }
    }
}
