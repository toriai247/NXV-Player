package com.example.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StreamUrlDao {

    @Query("SELECT * FROM saved_streams ORDER BY dateAdded DESC")
    fun getAllStreams(): Flow<List<StreamUrlEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStream(stream: StreamUrlEntity): Long

    @Query("DELETE FROM saved_streams WHERE id = :id OR url = :url")
    suspend fun deleteStream(id: Long, url: String)

    @Query("UPDATE saved_streams SET lastPlayedTimestamp = :timestamp, lastPlayedPositionMs = :pos WHERE url = :url")
    suspend fun updatePlayback(url: String, timestamp: Long, pos: Long)

    @Query("SELECT * FROM saved_streams WHERE url = :url LIMIT 1")
    suspend fun getStreamByUrl(url: String): StreamUrlEntity?

    @Query("SELECT COUNT(*) FROM saved_streams")
    suspend fun getStreamCount(): Int
}
