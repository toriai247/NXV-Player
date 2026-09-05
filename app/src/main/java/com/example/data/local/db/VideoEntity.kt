package com.example.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.VideoItem

@Entity(tableName = "videos")
data class VideoEntity(
    @PrimaryKey val id: Long,
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
    val aspectRatioMode: Int = 0,
    val selectedAudioTrackIndex: Int = -1,
    val selectedSubtitleTrackIndex: Int = -1
) {
    fun toDomain(): VideoItem = VideoItem(
        id = id,
        uri = uri,
        title = title,
        displayName = displayName,
        durationMs = durationMs,
        sizeBytes = sizeBytes,
        width = width,
        height = height,
        resolution = resolution,
        mimeType = mimeType,
        folderName = folderName,
        folderPath = folderPath,
        dateAdded = dateAdded,
        dateModified = dateModified,
        isFavorite = isFavorite,
        lastPlayedPositionMs = lastPlayedPositionMs,
        lastPlayedTimestamp = lastPlayedTimestamp,
        playbackSpeed = playbackSpeed,
        aspectRatioMode = aspectRatioMode,
        selectedAudioTrackIndex = selectedAudioTrackIndex,
        selectedSubtitleTrackIndex = selectedSubtitleTrackIndex
    )

    companion object {
        fun fromDomain(item: VideoItem): VideoEntity = VideoEntity(
            id = item.id,
            uri = item.uri,
            title = item.title,
            displayName = item.displayName,
            durationMs = item.durationMs,
            sizeBytes = item.sizeBytes,
            width = item.width,
            height = item.height,
            resolution = item.resolution,
            mimeType = item.mimeType,
            folderName = item.folderName,
            folderPath = item.folderPath,
            dateAdded = item.dateAdded,
            dateModified = item.dateModified,
            isFavorite = item.isFavorite,
            lastPlayedPositionMs = item.lastPlayedPositionMs,
            lastPlayedTimestamp = item.lastPlayedTimestamp,
            playbackSpeed = item.playbackSpeed,
            aspectRatioMode = item.aspectRatioMode,
            selectedAudioTrackIndex = item.selectedAudioTrackIndex,
            selectedSubtitleTrackIndex = item.selectedSubtitleTrackIndex
        )
    }
}
