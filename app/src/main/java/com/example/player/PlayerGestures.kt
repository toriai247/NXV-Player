package com.example.player

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.abs

@Composable
fun PlayerGestureDetector(
    playerManager: PlayerManager,
    isLocked: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var totalDragY by remember { mutableFloatStateOf(0f) }
    var totalDragX by remember { mutableFloatStateOf(0f) }
    var startDragOffset by remember { mutableStateOf(Offset.Zero) }
    var isDraggingSeek by remember { mutableStateOf(false) }
    var initialSeekPosition by remember { mutableLongStateOf(0L) }

    Box(
        modifier = modifier
            .fillMaxSize()
            // Pinch-to-zoom gesture
            .pointerInput(isLocked) {
                if (isLocked) return@pointerInput
                detectTransformGestures { _, _, zoom, _ ->
                    if (abs(zoom - 1.0f) > 0.01f) {
                        val currentScale = playerManager.uiState.value.zoomScale
                        playerManager.setZoomScale(currentScale * zoom)
                    }
                }
            }
            // Taps and long press
            .pointerInput(isLocked) {
                if (isLocked) {
                    detectTapGestures {
                        // When locked, single tap could show unlock hint or do nothing
                    }
                    return@pointerInput
                }

                detectTapGestures(
                    onTap = {
                        playerManager.toggleControlsVisibility()
                    },
                    onDoubleTap = { offset ->
                        val screenWidth = size.width
                        when {
                            offset.x < screenWidth * 0.35f -> {
                                playerManager.rewind10Seconds()
                            }
                            offset.x > screenWidth * 0.65f -> {
                                playerManager.forward10Seconds()
                            }
                            else -> {
                                playerManager.playPauseToggle()
                            }
                        }
                    },
                    onLongPress = {
                        playerManager.startTemporary2xSpeed()
                    },
                    onPress = {
                        val released = tryAwaitRelease()
                        if (released) {
                            playerManager.stopTemporary2xSpeed()
                        }
                    }
                )
            }
            // Swipes for Brightness, Volume, and Seek
            .pointerInput(isLocked) {
                if (isLocked) return@pointerInput
                awaitPointerEventScope {
                    while (true) {
                        val down = awaitPointerEvent().changes.firstOrNull() ?: continue
                        startDragOffset = down.position
                        totalDragX = 0f
                        totalDragY = 0f
                        isDraggingSeek = false
                        initialSeekPosition = playerManager.uiState.value.currentPositionMs

                        var isPointerDown = true
                        while (isPointerDown) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull()
                            if (change == null || !change.pressed) {
                                isPointerDown = false
                                if (isDraggingSeek) {
                                    val finalPos = playerManager.uiState.value.currentPositionMs
                                    playerManager.commitHorizontalSeek(finalPos)
                                }
                                playerManager.hideHud()
                                break
                            }

                            val delta = change.position - change.previousPosition
                            totalDragX += delta.x
                            totalDragY += delta.y

                            val screenWidth = size.width
                            val absX = abs(totalDragX)
                            val absY = abs(totalDragY)

                            // Threshold of 16px to distinguish swipe from simple touch
                            if (absX > 20f || absY > 20f) {
                                change.consume()

                                if (absX > absY) {
                                    // Horizontal Seek Gesture
                                    isDraggingSeek = true
                                    // 1000px drag = 60s seek
                                    val seekDeltaMs = (totalDragX * 60).toLong()
                                    playerManager.adjustHorizontalSeek(seekDeltaMs)
                                } else {
                                    // Vertical Gesture
                                    isDraggingSeek = false
                                    val deltaPercent = (-delta.y / 8f).toInt()
                                    if (startDragOffset.x < screenWidth * 0.5f) {
                                        // Left side: Brightness
                                        playerManager.adjustBrightness(deltaPercent)
                                    } else {
                                        // Right side: Volume
                                        playerManager.adjustVolume(deltaPercent)
                                    }
                                }
                            }
                        }
                    }
                }
            }
    ) {
        content()
    }
}
