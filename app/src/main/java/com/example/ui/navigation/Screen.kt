package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Library : Screen("library", "Library", Icons.Default.VideoLibrary)
    object Folders : Screen("folders", "Folders", Icons.Default.Folder)
    object Favorites : Screen("favorites", "Favorites", Icons.Default.Favorite)
    object Search : Screen("search", "Search", Icons.Default.Search)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    object FolderDetail : Screen("folder_detail/{folderName}", "Folder") {
        fun createRoute(folderName: String) = "folder_detail/${android.net.Uri.encode(folderName)}"
    }
    object Player : Screen("player", "Player")
}

val bottomNavScreens = listOf(
    Screen.Home,
    Screen.Library,
    Screen.Folders,
    Screen.Favorites,
    Screen.Settings
)
