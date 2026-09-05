package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.db.NxvDatabase
import com.example.data.local.preferences.PreferencesManager
import com.example.data.media.MediaScanner
import com.example.data.repository.VideoRepository
import com.example.domain.model.FolderItem
import com.example.domain.model.PlayerPreferences
import com.example.domain.model.VideoItem
import com.example.utils.StreamUrlHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = NxvDatabase.getDatabase(application)
    private val mediaScanner = MediaScanner(application)
    val repository = VideoRepository(database.videoDao(), database.streamUrlDao(), mediaScanner)
    val preferencesManager = PreferencesManager(application)

    val preferences: StateFlow<PlayerPreferences> = preferencesManager.playerPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PlayerPreferences()
        )

    val allVideos: StateFlow<List<VideoItem>> = repository.allVideos
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val localVideos: StateFlow<List<VideoItem>> = repository.localVideos
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val savedStreams: StateFlow<List<VideoItem>> = repository.savedStreams
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val recentlyPlayed: StateFlow<List<VideoItem>> = repository.recentlyPlayed
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val continueWatching: StateFlow<List<VideoItem>> = repository.continueWatching
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val favoriteVideos: StateFlow<List<VideoItem>> = repository.favoriteVideos
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val folders: StateFlow<List<FolderItem>> = repository.folders
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val searchResults: StateFlow<List<VideoItem>> = _searchQuery.flatMapLatest { query ->
        if (query.isBlank()) flowOf(emptyList()) else repository.searchVideos(query)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    // Current active playing video and playlist
    private val _activeVideo = MutableStateFlow<VideoItem?>(null)
    val activeVideo: StateFlow<VideoItem?> = _activeVideo.asStateFlow()

    private val _activePlaylist = MutableStateFlow<List<VideoItem>>(emptyList())
    val activePlaylist: StateFlow<List<VideoItem>> = _activePlaylist.asStateFlow()

    init {
        scanVideos()
    }

    fun scanVideos() {
        viewModelScope.launch {
            _isScanning.value = true
            repository.refreshVideos()
            _isScanning.value = false
        }
    }

    fun addStreamUrl(url: String, customTitle: String = "", onReady: (VideoItem) -> Unit = {}) {
        viewModelScope.launch {
            val videoItem = repository.addStreamUrl(url, customTitle)
            onReady(videoItem)
        }
    }

    fun deleteStream(video: VideoItem) {
        viewModelScope.launch {
            repository.deleteStreamUrl(video.id, video.uri)
        }
    }

    fun playVideo(video: VideoItem, playlist: List<VideoItem> = listOf(video)) {
        _activeVideo.value = video
        _activePlaylist.value = playlist
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleFavorite(video: VideoItem) {
        viewModelScope.launch {
            repository.toggleFavorite(video.uri, !video.isFavorite)
        }
    }

    fun deleteVideo(video: VideoItem) {
        viewModelScope.launch {
            repository.deleteVideo(video.uri)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            preferencesManager.setThemeMode(mode)
        }
    }

    fun setAutoNext(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setAutoNext(enabled)
        }
    }

    fun setResumePlayback(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setResumePlayback(enabled)
        }
    }

    fun setDefaultSpeed(speed: Float) {
        viewModelScope.launch {
            preferencesManager.setDefaultSpeed(speed)
        }
    }

    fun setBackgroundAudio(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setBackgroundAudio(enabled)
        }
    }

    fun playVideoAsAudio(video: VideoItem, playlist: List<VideoItem> = listOf(video), context: android.content.Context) {
        _activeVideo.value = video
        _activePlaylist.value = playlist
        val playerManager = com.example.player.PlayerManager.getInstance(context)
        playerManager.prepareAndPlay(
            video = video,
            playlist = playlist,
            resumePosition = if (preferences.value.resumePlayback) video.lastPlayedPositionMs else 0L,
            startInAudioMode = true
        )
    }
}
