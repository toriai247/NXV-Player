package com.example.utils

import android.net.Uri
import com.example.domain.model.VideoItem
import java.util.regex.Pattern

object StreamUrlHelper {

    private val YOUTUBE_PATTERN = Pattern.compile(
        "^.*(youtu.be\\/|v\\/|u\\/\\w\\/|embed\\/|watch\\?v=|\\&v=|shorts\\/)([^#\\&\\?]*).*"
    )

    fun sanitizeUrl(raw: String): String {
        var trimmed = raw.trim()
        if (trimmed.isEmpty()) return ""
        if (!trimmed.startsWith("http://", ignoreCase = true) && !trimmed.startsWith("https://", ignoreCase = true)) {
            trimmed = "https://$trimmed"
        }
        return trimmed
    }

    fun isYouTubeUrl(url: String): Boolean {
        val lower = url.lowercase()
        return lower.contains("youtube.com") || lower.contains("youtu.be")
    }

    fun extractYouTubeId(url: String): String? {
        if (!isYouTubeUrl(url)) return null
        val matcher = YOUTUBE_PATTERN.matcher(url)
        return if (matcher.matches() && matcher.group(2) != null && matcher.group(2)!!.length == 11) {
            matcher.group(2)
        } else {
            // Fallback parse query parameter
            try {
                val uri = Uri.parse(url)
                val v = uri.getQueryParameter("v")
                if (!v.isNullOrBlank() && v.length == 11) v else null
            } catch (e: Exception) {
                null
            }
        }
    }

    fun getYouTubeThumbnail(videoId: String): String {
        return "https://img.youtube.com/vi/$videoId/hqdefault.jpg"
    }

    fun getYouTubeEmbedUrl(videoId: String): String {
        return "https://www.youtube-nocookie.com/embed/$videoId?autoplay=1&playsinline=1&rel=0&modestbranding=1"
    }

    fun isTikTokUrl(url: String): Boolean {
        val lower = url.lowercase()
        return lower.contains("tiktok.com") || lower.contains("douyin.com")
    }

    fun getTikTokEmbedUrl(url: String): String {
        return if (url.contains("/video/")) {
            val videoId = url.substringAfter("/video/").substringBefore("?").substringBefore("/")
            "https://www.tiktok.com/embed/v2/$videoId"
        } else {
            url
        }
    }

    fun isDirectMediaUrl(url: String): Boolean {
        val lower = url.lowercase().substringBefore("?")
        return lower.endsWith(".mp4") || lower.endsWith(".mkv") || lower.endsWith(".m3u8") ||
                lower.endsWith(".mpd") || lower.endsWith(".webm") || lower.endsWith(".mov") ||
                lower.endsWith(".3gp") || lower.endsWith(".ts") || lower.endsWith(".flv") ||
                lower.endsWith(".avi") || lower.endsWith(".mp3") || lower.endsWith(".m4a") ||
                lower.endsWith(".aac") || lower.endsWith(".ogg") || lower.endsWith(".flac")
    }

    fun detectPlatform(url: String): String {
        return when {
            isYouTubeUrl(url) -> "YouTube"
            isTikTokUrl(url) -> "TikTok"
            url.lowercase().contains(".m3u8") -> "HLS Stream"
            url.lowercase().contains(".mpd") -> "DASH Stream"
            isDirectMediaUrl(url) -> "Direct Video"
            else -> "Web Stream"
        }
    }

    fun getMimeType(url: String): String {
        val lower = url.lowercase().substringBefore("?")
        return when {
            isYouTubeUrl(url) -> "video/youtube"
            isTikTokUrl(url) -> "video/tiktok"
            lower.endsWith(".m3u8") -> "application/x-mpegURL"
            lower.endsWith(".mpd") -> "application/dash+xml"
            lower.endsWith(".mkv") -> "video/x-matroska"
            lower.endsWith(".webm") -> "video/webm"
            lower.endsWith(".mp3") -> "audio/mpeg"
            lower.endsWith(".m4a") -> "audio/mp4"
            lower.endsWith(".aac") -> "audio/aac"
            lower.endsWith(".ogg") -> "audio/ogg"
            lower.endsWith(".flac") -> "audio/flac"
            else -> "video/mp4"
        }
    }

    fun suggestTitle(url: String): String {
        val ytId = extractYouTubeId(url)
        if (ytId != null) {
            return "YouTube ($ytId)"
        }
        if (isTikTokUrl(url)) {
            return "TikTok Video"
        }
        val cleanPath = url.substringBefore("?").substringAfterLast("/")
        if (cleanPath.isNotBlank() && cleanPath.length > 3) {
            return cleanPath.replace("%20", " ").replace("+", " ")
        }
        return "Online Stream (${detectPlatform(url)})"
    }

    fun createStreamVideoItem(
        url: String,
        customTitle: String = "",
        customId: Long = 0L
    ): VideoItem {
        val sanitized = sanitizeUrl(url)
        val platform = detectPlatform(sanitized)
        val title = if (customTitle.isNotBlank()) customTitle else suggestTitle(sanitized)
        val ytId = extractYouTubeId(sanitized)

        val id = if (customId != 0L) customId else -(100000L + Math.abs(sanitized.hashCode().toLong()))

        return VideoItem(
            id = id,
            uri = sanitized,
            title = title,
            displayName = title,
            durationMs = 0L,
            sizeBytes = 0L,
            width = 1920,
            height = 1080,
            resolution = platform,
            mimeType = getMimeType(sanitized),
            folderName = "Online Streams",
            folderPath = "Online Streams",
            dateAdded = System.currentTimeMillis(),
            dateModified = System.currentTimeMillis(),
            isFavorite = false,
            lastPlayedPositionMs = 0L,
            lastPlayedTimestamp = System.currentTimeMillis()
        )
    }
}
