package com.example.ui.components

import android.net.Uri
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.ui.theme.NxvYellow

/**
 * Spring physics tap animation modifier that scales down on press and bounces back up on release
 */
fun Modifier.bouncingClickable(
    scaleDown: Float = 0.94f,
    onClick: () -> Unit
): Modifier = composed {
    var isPressed by remember { mutableStateOf(false) }
    val scale = remember { Animatable(1f) }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            scale.animateTo(
                targetValue = scaleDown,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        } else {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
    }

    this
        .graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
        }
        .pointerInput(Unit) {
            while (true) {
                awaitPointerEventScope {
                    awaitFirstDown(requireUnconsumed = false)
                    isPressed = true
                    val upOrCancel = waitForUpOrCancellation()
                    isPressed = false
                    if (upOrCancel != null) {
                        onClick()
                    }
                }
            }
        }
}

/**
 * Animated real-time frequency equalizer bars
 */
@Composable
fun AnimatedEqualizerBars(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 8,
    barWidth: Dp = 3.5.dp,
    spacing: Dp = 3.dp,
    maxHeight: Dp = 24.dp,
    activeColor: Color = NxvYellow,
    inactiveColor: Color = Color(0x55FFFFFF)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "equalizer_bars")

    val heights = (0 until barCount).map { index ->
        if (isPlaying) {
            val duration = 280 + ((index * 79) % 250)
            val anim by infiniteTransition.animateFloat(
                initialValue = 0.15f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = duration, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bar_height_$index"
            )
            anim
        } else {
            0.2f
        }
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        heights.forEach { fraction ->
            val height = (fraction * maxHeight.value).coerceAtLeast(4f).dp
            Box(
                modifier = Modifier
                    .width(barWidth)
                    .height(height)
                    .clip(CircleShape)
                    .background(
                        if (isPlaying) {
                            Brush.verticalGradient(
                                colors = listOf(activeColor, Color(0xFFFF9100))
                            )
                        } else {
                            Brush.verticalGradient(
                                colors = listOf(inactiveColor, inactiveColor)
                            )
                        }
                    )
            )
        }
    }
}

/**
 * Realistic Spinning Vinyl Record with Grooves and Center Label
 */
@Composable
fun SpinningVinylRecord(
    isPlaying: Boolean,
    artworkUri: Uri?,
    modifier: Modifier = Modifier,
    size: Dp = 220.dp
) {
    var currentRotation by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            var lastTime = System.currentTimeMillis()
            while (true) {
                withFrameNanos {
                    val now = System.currentTimeMillis()
                    val delta = (now - lastTime).coerceIn(0, 100)
                    lastTime = now
                    currentRotation = (currentRotation + (delta * 0.09f)) % 360f
                }
            }
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .shadow(
                elevation = 28.dp,
                shape = CircleShape,
                ambientColor = NxvYellow.copy(alpha = 0.35f),
                spotColor = NxvYellow.copy(alpha = 0.45f)
            )
            .clip(CircleShape)
            .background(Color(0xFF0F0F12))
            .border(3.dp, Color(0xFF222228), CircleShape)
    ) {
        // Vinyl Grooves Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = this.size.minDimension / 2f
            val center = Offset(this.size.width / 2f, this.size.height / 2f)

            // Groove concentric rings
            val grooveRadii = listOf(0.92f, 0.86f, 0.80f, 0.74f, 0.68f, 0.62f, 0.56f)
            grooveRadii.forEach { factor ->
                drawCircle(
                    color = Color(0x22FFFFFF),
                    radius = radius * factor,
                    center = center,
                    style = Stroke(width = 1f)
                )
            }

            // Shiny sheen sweep overlay
            drawCircle(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0x18FFFFFF),
                        Color.Transparent,
                        Color(0x18FFFFFF),
                        Color.Transparent
                    )
                ),
                radius = radius * 0.95f,
                center = center
            )
        }

        // Rotating Center Label / Album Cover
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(size * 0.46f)
                .rotate(currentRotation)
                .clip(CircleShape)
                .border(2.dp, NxvYellow, CircleShape)
                .background(Color(0xFF1E1E24))
        ) {
            if (artworkUri != null && artworkUri.toString().isNotBlank()) {
                AsyncImage(
                    model = artworkUri,
                    contentDescription = "Album Cover",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color(0xFF2A2A35), Color(0xFF14141A))
                            )
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = NxvYellow,
                        modifier = Modifier.size(size * 0.22f)
                    )
                }
            }

            // Center Spindle Hole
            Box(
                modifier = Modifier
                    .size(size * 0.08f)
                    .clip(CircleShape)
                    .background(Color(0xFF0A0A0C))
                    .border(1.5.dp, Color(0xFF444444), CircleShape)
            )
        }
    }
}
