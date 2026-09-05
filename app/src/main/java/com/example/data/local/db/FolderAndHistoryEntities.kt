package com.example.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.FolderItem

@Entity(tableName = "folders")
data class FolderEntity(
    @PrimaryKey val path: String,
    val name: String,
    val videoCount: Int,
    val totalSizeBytes: Long = 0L,
    val latestVideoDate: Long = 0L
) {
    fun toDomain(): FolderItem = FolderItem(
        path = path,
        name = name,
        videoCount = videoCount,
        totalSizeBytes = totalSizeBytes,
        latestVideoDate = latestVideoDate
    )
}

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val videoUri: String,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "playback_history")
data class PlaybackHistoryEntity(
    @PrimaryKey val videoUri: String,
    val title: String,
    val durationMs: Long,
    val positionMs: Long,
    val lastPlayedAt: Long = System.currentTimeMillis()
)
