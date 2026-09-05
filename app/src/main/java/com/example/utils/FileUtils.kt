package com.example.utils

import java.text.DecimalFormat
import kotlin.math.log10
import kotlin.math.pow

object FileUtils {
    fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (log10(bytes.toDouble()) / log10(1024.0)).toInt().coerceIn(0, units.size - 1)
        val formatted = DecimalFormat("#,##0.#").format(bytes / 1024.0.pow(digitGroups.toDouble()))
        return "$formatted ${units[digitGroups]}"
    }

    fun formatResolution(width: Int, height: Int): String {
        val maxDim = maxOf(width, height)
        val minDim = minOf(width, height)
        return when {
            minDim >= 2160 || maxDim >= 3840 -> "4K"
            minDim >= 1440 || maxDim >= 2560 -> "2K"
            minDim >= 1080 || maxDim >= 1920 -> "1080p"
            minDim >= 720 || maxDim >= 1280 -> "720p"
            minDim >= 480 || maxDim >= 854 -> "480p"
            minDim > 0 -> "${width}x${height}"
            else -> "HD"
        }
    }

    fun getFileNameWithoutExtension(fileName: String): String {
        val lastDotIndex = fileName.lastIndexOf('.')
        return if (lastDotIndex > 0) fileName.substring(0, lastDotIndex) else fileName
    }
}
