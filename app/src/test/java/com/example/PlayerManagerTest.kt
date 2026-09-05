package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.player.PlayerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PlayerManagerTest {

    @Test
    fun `playerManager initializes with stable default state and exoPlayer instance`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val testScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        val playerManager = PlayerManager(context, testScope)

        assertNotNull(playerManager.player)
        val state = playerManager.uiState.value

        assertNull(state.currentVideo)
        assertEquals(1.0f, state.playbackSpeed, 0.001f)
        assertEquals(1.0f, state.zoomScale, 0.001f)
        assertEquals(false, state.isScreenLocked)
        assertEquals(true, state.areControlsVisible)

        playerManager.release()
    }

    @Test
    fun `playerManager brightness and speed manipulation updates uiState within bounds`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val testScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        val playerManager = PlayerManager(context, testScope)

        playerManager.adjustBrightness(20)
        assertEquals(70, playerManager.uiState.value.brightnessPercent)

        playerManager.setPlaybackSpeed(1.5f)
        assertEquals(1.5f, playerManager.uiState.value.playbackSpeed, 0.001f)

        playerManager.setZoomScale(2.5f)
        assertEquals(2.5f, playerManager.uiState.value.zoomScale, 0.001f)

        playerManager.toggleScreenLock()
        assertEquals(true, playerManager.uiState.value.isScreenLocked)
        assertEquals(false, playerManager.uiState.value.areControlsVisible)

        playerManager.release()
    }

    @Test
    fun `playerManager audio-only mode toggles cleanly and maintains player state`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val playerManager = PlayerManager.getInstance(context)

        assertEquals(false, playerManager.uiState.value.isAudioOnlyMode)

        playerManager.toggleAudioOnlyMode()
        assertEquals(true, playerManager.uiState.value.isAudioOnlyMode)

        playerManager.setAudioOnlyMode(false)
        assertEquals(false, playerManager.uiState.value.isAudioOnlyMode)

        playerManager.stopAndClear()
        assertNull(playerManager.uiState.value.currentVideo)
        assertEquals(false, playerManager.uiState.value.isPlaying)
    }
}
