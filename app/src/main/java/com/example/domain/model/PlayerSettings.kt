package com.example.domain.model

enum class AspectRatioMode(val title: String) {
    FIT("Fit to Screen"),
    FILL("Stretch / Fill"),
    ZOOM("Zoom / Crop"),
    ORIGINAL("100% Original")
}

data class SubtitleConfig(
    val isEnabled: Boolean = true,
    val fontSizeSp: Int = 18,
    val opacity: Float = 1.0f,
    val textColor: Long = 0xFFFFFFFF,
    val backgroundColor: Long = 0x80000000,
    val delayMs: Long = 0L,
    val verticalOffsetPercent: Float = 0.08f
)

data class AudioConfig(
    val selectedTrackIndex: Int = -1,
    val delayMs: Long = 0L,
    val volumeBoostPercent: Int = 100 // 100% is normal, up to 150%
)

data class PlayerPreferences(
    val themeMode: String = "AMOLED", // "AMOLED", "LIGHT", "SYSTEM"
    val autoNext: Boolean = true,
    val resumePlayback: Boolean = true,
    val defaultSpeed: Float = 1.0f,
    val defaultAspectRatio: Int = 0,
    val backgroundAudio: Boolean = false,
    val screenOrientation: Int = 0 // 0: Auto, 1: Landscape, 2: Portrait
)
