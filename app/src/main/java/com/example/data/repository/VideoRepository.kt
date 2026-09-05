package com.example.data.repository

import com.example.data.local.db.StreamUrlDao
import com.example.data.local.db.StreamUrlEntity
import com.example.data.local.db.VideoDao
import com.example.data.local.db.VideoEntity
import com.example.data.media.MediaScanner
import com.example.domain.model.FolderItem
import com.example.domain.model.VideoItem
import com.example.utils.StreamUrlHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class VideoRepository(
    private val videoDao: VideoDao,
    private val streamUrlDao: StreamUrlDao,
    private val mediaScanner: MediaScanner
) {
    // Real device storage videos
    val localVideos: Flow<List<VideoItem>> = videoDao.getAllVideos().map { entities ->
        entities.map { it.toDomain() }
    }

    // User-saved online stream URLs (YouTube, TikTok, direct video streams)
    val savedStreams: Flow<List<VideoItem>> = streamUrlDao.getAllStreams().map { entities ->
        entities.map { it.toVideoItem() }
    }

    // All videos combined (local storage + user-saved online streams)
    val allVideos: Flow<List<VideoItem>> = combine(localVideos, savedStreams) { local, streams ->
        streams + local
    }

    val recentlyPlayed: Flow<List<VideoItem>> = videoDao.getRecentlyPlayed().map { entities ->
        entities.map { it.toDomain() }
    }

    val continueWatching: Flow<List<VideoItem>> = videoDao.getContinueWatching().map { entities ->
        entities.map { it.toDomain() }
    }

    val favoriteVideos: Flow<List<VideoItem>> = videoDao.getFavoriteVideos().map { entities ->
        entities.map { it.toDomain() }
    }

    val folders: Flow<List<FolderItem>> = videoDao.getAllVideos().map { entities ->
        entities.groupBy { it.folderName }
            .map { (name, group) ->
                val first = group.first()
                FolderItem(
                    path = first.folderPath,
                    name = name,
                    videoCount = group.size,
                    totalSizeBytes = group.sumOf { it.sizeBytes },
                    latestVideoDate = group.maxOfOrNull { it.dateAdded } ?: 0L
                )
            }
            .sortedByDescending { it.videoCount }
    }

    suspend fun refreshVideos() = withContext(Dispatchers.IO) {
        // Clean out any legacy or sample placeholder videos completely
        videoDao.deleteSampleVideos()

        // Scan real local user device media storage
        val scanned = mediaScanner.scanDeviceVideos()
        if (scanned.isNotEmpty()) {
            val entities = scanned.map { VideoEntity.fromDomain(it) }
            videoDao.insertVideos(entities)
        }
    }

    suspend fun addStreamUrl(url: String, customTitle: String = ""): VideoItem = withContext(Dispatchers.IO) {
        val sanitized = StreamUrlHelper.sanitizeUrl(url)
        val entity = StreamUrlEntity.fromUrl(sanitized, customTitle)
        val insertedId = streamUrlDao.insertStream(entity)
        entity.copy(id = insertedId).toVideoItem()
    }

    suspend fun deleteStreamUrl(id: Long, url: String) = withContext(Dispatchers.IO) {
        streamUrlDao.deleteStream(id, url)
    }

    fun searchVideos(query: String): Flow<List<VideoItem>> {
        return combine(videoDao.searchVideos(query), savedStreams) { localEntities, streams ->
            val localItems = localEntities.map { it.toDomain() }
            val matchingStreams = streams.filter {
                it.title.contains(query, ignoreCase = true) || it.uri.contains(query, ignoreCase = true)
            }
            matchingStreams + localItems
        }
    }

    fun getVideosInFolder(folderName: String): Flow<List<VideoItem>> {
        return videoDao.getVideosInFolder(folderName).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun getVideoByUri(uri: String): VideoItem? = withContext(Dispatchers.IO) {
        videoDao.getVideoByUri(uri)?.toDomain() ?: streamUrlDao.getStreamByUrl(uri)?.toVideoItem()
    }

    suspend fun getVideoById(id: Long): VideoItem? = withContext(Dispatchers.IO) {
        videoDao.getVideoById(id)?.toDomain()
    }

    suspend fun toggleFavorite(uri: String, isFavorite: Boolean) = withContext(Dispatchers.IO) {
        videoDao.setFavorite(uri, isFavorite)
    }

    suspend fun updatePlaybackProgress(uri: String, positionMs: Long, timestamp: Long = System.currentTimeMillis()) = withContext(Dispatchers.IO) {
        videoDao.updatePlaybackProgress(uri, positionMs, timestamp)
        streamUrlDao.updatePlayback(uri, timestamp, positionMs)
    }

    suspend fun updateVideoSettings(
        uri: String,
        speed: Float,
        aspectRatio: Int,
        audioTrack: Int,
        subTrack: Int
    ) = withContext(Dispatchers.IO) {
        videoDao.updateVideoPlaybackSettings(uri, speed, aspectRatio, audioTrack, subTrack)
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        videoDao.clearHistory()
    }

    suspend fun deleteVideo(uri: String) = withContext(Dispatchers.IO) {
        videoDao.deleteVideo(uri)
        streamUrlDao.deleteStream(0L, uri)
    }
}
