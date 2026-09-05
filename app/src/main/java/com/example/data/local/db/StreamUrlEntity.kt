package com.example.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.VideoItem
import com.example.utils.StreamUrlHelper

@Entity(tableName = "saved_streams")
data class StreamUrlEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val url: String,
    val title: String,
    val platform: String, // "YouTube", "TikTok", "HLS Stream", "Direct Video", "Web Stream"
    val thumbnailUrl: String = "",
    val durationMs: Long = 0L,
    val dateAdded: Long = System.currentTimeMillis(),
    val lastPlayedTimestamp: Long = 0L,
    val lastPlayedPositionMs: Long = 0L
) {
    fun toVideoItem(): VideoItem {
        return VideoItem(
            id = if (id > 0) -(100000L + id) else -(100000L + Math.abs(url.hashCode().toLong())),
            uri = url,
            title = title.ifBlank { StreamUrlHelper.suggestTitle(url) },
            displayName = title.ifBlank { StreamUrlHelper.suggestTitle(url) },
            durationMs = durationMs,
            sizeBytes = 0L,
            width = 1920,
            height = 1080,
            resolution = platform,
            mimeType = StreamUrlHelper.getMimeType(url),
            folderName = "Online Streams",
            folderPath = "Online Streams",
            dateAdded = dateAdded,
            dateModified = dateAdded,
            isFavorite = false,
            lastPlayedPositionMs = lastPlayedPositionMs,
            lastPlayedTimestamp = lastPlayedTimestamp
        )
    }

    companion object {
        fun fromUrl(url: String, title: String = ""): StreamUrlEntity {
            val sanitized = StreamUrlHelper.sanitizeUrl(url)
            val platform = StreamUrlHelper.detectPlatform(sanitized)
            val ytId = StreamUrlHelper.extractYouTubeId(sanitized)
            val thumbnail = if (ytId != null) StreamUrlHelper.getYouTubeThumbnail(ytId) else ""
            val finalTitle = if (title.isNotBlank()) title else StreamUrlHelper.suggestTitle(sanitized)

            return StreamUrlEntity(
                url = sanitized,
                title = finalTitle,
                platform = platform,
                thumbnailUrl = thumbnail,
                dateAdded = System.currentTimeMillis()
            )
        }
    }
}
