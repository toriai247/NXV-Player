package com.example.domain.model

import android.net.Uri

data class VideoItem(
    val id: Long,
    val uri: String,
    val title: String,
    val displayName: String,
    val durationMs: Long,
    val sizeBytes: Long,
    val width: Int,
    val height: Int,
    val resolution: String,
    val mimeType: String,
    val folderName: String,
    val folderPath: String,
    val dateAdded: Long,
    val dateModified: Long,
    val isFavorite: Boolean = false,
    val lastPlayedPositionMs: Long = 0L,
    val lastPlayedTimestamp: Long = 0L,
    val playbackSpeed: Float = 1.0f,
    val aspectRatioMode: Int = 0, // 0: Fit, 1: Fill, 2: Zoom/Crop, 3: Original
    val selectedAudioTrackIndex: Int = -1,
    val selectedSubtitleTrackIndex: Int = -1
) {
    val contentUri: Uri get() = Uri.parse(uri)
    val progressPercentage: Float
        get() = if (durationMs > 0) (lastPlayedPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
    val isCompleted: Boolean
        get() = durationMs > 0 && lastPlayedPositionMs >= (durationMs * 0.95)
}
