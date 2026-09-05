package com.example.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoDao {
    @Query("SELECT * FROM videos ORDER BY dateAdded DESC")
    fun getAllVideos(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE lastPlayedTimestamp > 0 ORDER BY lastPlayedTimestamp DESC LIMIT 20")
    fun getRecentlyPlayed(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE lastPlayedPositionMs > 3000 AND (durationMs == 0 OR lastPlayedPositionMs < durationMs * 0.95) ORDER BY lastPlayedTimestamp DESC LIMIT 15")
    fun getContinueWatching(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE isFavorite = 1 ORDER BY title ASC")
    fun getFavoriteVideos(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE folderName = :folderName ORDER BY dateAdded DESC")
    fun getVideosInFolder(folderName: String): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE title LIKE '%' || :query || '%' OR displayName LIKE '%' || :query || '%' ORDER BY title ASC")
    fun searchVideos(query: String): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE uri = :uri LIMIT 1")
    suspend fun getVideoByUri(uri: String): VideoEntity?

    @Query("SELECT * FROM videos WHERE id = :id LIMIT 1")
    suspend fun getVideoById(id: Long): VideoEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertVideos(videos: List<VideoEntity>)

    @Update
    suspend fun updateVideo(video: VideoEntity)

    @Query("UPDATE videos SET lastPlayedPositionMs = :positionMs, lastPlayedTimestamp = :timestamp WHERE uri = :uri")
    suspend fun updatePlaybackProgress(uri: String, positionMs: Long, timestamp: Long)

    @Query("UPDATE videos SET isFavorite = :isFavorite WHERE uri = :uri")
    suspend fun setFavorite(uri: String, isFavorite: Boolean)

    @Query("UPDATE videos SET playbackSpeed = :speed, aspectRatioMode = :aspectRatio, selectedAudioTrackIndex = :audioTrack, selectedSubtitleTrackIndex = :subTrack WHERE uri = :uri")
    suspend fun updateVideoPlaybackSettings(uri: String, speed: Float, aspectRatio: Int, audioTrack: Int, subTrack: Int)

    @Query("UPDATE videos SET lastPlayedPositionMs = 0, lastPlayedTimestamp = 0")
    suspend fun clearHistory()

    @Query("DELETE FROM videos WHERE uri = :uri")
    suspend fun deleteVideo(uri: String)

    @Query("DELETE FROM videos WHERE uri LIKE '%commondatastorage.googleapis.com%' OR uri LIKE '%media.w3.org%' OR uri LIKE '%storage.googleapis.com/exoplayer-test-media%'")
    suspend fun deleteSampleVideos(): Int

    @Query("SELECT COUNT(*) FROM videos")
    suspend fun getVideoCount(): Int
}
