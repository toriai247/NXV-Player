package com.example.ui.library

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.domain.model.VideoItem
import com.example.ui.MainViewModel

enum class VideoSort(val title: String) {
    DATE_DESC("Date Added (Newest)"),
    DATE_ASC("Date Added (Oldest)"),
    NAME_ASC("Name (A to Z)"),
    NAME_DESC("Name (Z to A)"),
    DURATION_DESC("Duration (Longest)"),
    DURATION_ASC("Duration (Shortest)"),
    SIZE_DESC("Size (Largest)")
}

@Composable
fun LibraryScreen(
    viewModel: MainViewModel,
    onNavigateToPlayer: (VideoItem) -> Unit,
    onNavigateToSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    LocalVideoGridScreen(
        viewModel = viewModel,
        onNavigateToPlayer = onNavigateToPlayer,
        onNavigateToSearch = onNavigateToSearch,
        modifier = modifier
    )
}
