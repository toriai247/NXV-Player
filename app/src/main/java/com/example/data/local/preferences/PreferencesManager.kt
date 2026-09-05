package com.example.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.domain.model.PlayerPreferences
import com.example.domain.model.SubtitleConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "nxv_preferences")

class PreferencesManager(private val context: Context) {

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val AUTO_NEXT = booleanPreferencesKey("auto_next")
        val RESUME_PLAYBACK = booleanPreferencesKey("resume_playback")
        val DEFAULT_SPEED = floatPreferencesKey("default_speed")
        val DEFAULT_ASPECT_RATIO = intPreferencesKey("default_aspect_ratio")
        val BACKGROUND_AUDIO = booleanPreferencesKey("background_audio")
        val SUBTITLE_ENABLED = booleanPreferencesKey("subtitle_enabled")
        val SUBTITLE_FONT_SIZE = intPreferencesKey("subtitle_font_size")
        val SUBTITLE_OPACITY = floatPreferencesKey("subtitle_opacity")
        val SUBTITLE_DELAY = longPreferencesKey("subtitle_delay")
        val AUDIO_DELAY = longPreferencesKey("audio_delay")
    }

    val playerPreferencesFlow: Flow<PlayerPreferences> = context.dataStore.data.map { preferences ->
        PlayerPreferences(
            themeMode = preferences[PreferencesKeys.THEME_MODE] ?: "AMOLED",
            autoNext = preferences[PreferencesKeys.AUTO_NEXT] ?: true,
            resumePlayback = preferences[PreferencesKeys.RESUME_PLAYBACK] ?: true,
            defaultSpeed = preferences[PreferencesKeys.DEFAULT_SPEED] ?: 1.0f,
            defaultAspectRatio = preferences[PreferencesKeys.DEFAULT_ASPECT_RATIO] ?: 0,
            backgroundAudio = preferences[PreferencesKeys.BACKGROUND_AUDIO] ?: false
        )
    }

    val subtitleConfigFlow: Flow<SubtitleConfig> = context.dataStore.data.map { preferences ->
        SubtitleConfig(
            isEnabled = preferences[PreferencesKeys.SUBTITLE_ENABLED] ?: true,
            fontSizeSp = preferences[PreferencesKeys.SUBTITLE_FONT_SIZE] ?: 18,
            opacity = preferences[PreferencesKeys.SUBTITLE_OPACITY] ?: 1.0f,
            delayMs = preferences[PreferencesKeys.SUBTITLE_DELAY] ?: 0L
        )
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode
        }
    }

    suspend fun setAutoNext(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_NEXT] = enabled
        }
    }

    suspend fun setResumePlayback(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.RESUME_PLAYBACK] = enabled
        }
    }

    suspend fun setDefaultSpeed(speed: Float) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_SPEED] = speed
        }
    }

    suspend fun setDefaultAspectRatio(mode: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_ASPECT_RATIO] = mode
        }
    }

    suspend fun setBackgroundAudio(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.BACKGROUND_AUDIO] = enabled
        }
    }

    suspend fun setSubtitleEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SUBTITLE_ENABLED] = enabled
        }
    }

    suspend fun setSubtitleFontSize(sizeSp: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SUBTITLE_FONT_SIZE] = sizeSp
        }
    }

    suspend fun setSubtitleOpacity(opacity: Float) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SUBTITLE_OPACITY] = opacity
        }
    }

    suspend fun setSubtitleDelay(delayMs: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SUBTITLE_DELAY] = delayMs
        }
    }

    suspend fun setAudioDelay(delayMs: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUDIO_DELAY] = delayMs
        }
    }
}
