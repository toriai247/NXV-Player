package com.example.ui.player

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.player.PlayerUiState
import com.example.ui.components.AnimatedEqualizerBars
import com.example.ui.components.SpinningVinylRecord
import com.example.ui.components.bouncingClickable
import com.example.ui.theme.AmoledCard
import com.example.ui.theme.NxvYellow

@Composable
fun AudioModeView(
    uiState: PlayerUiState,
    onSwitchToVideo: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentVideo = uiState.currentVideo

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0C0C10),
                        Color(0xFF000000),
                        Color(0xFF13131A)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            // "Audio Mode" Pill Badge
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = NxvYellow.copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.dp, NxvYellow.copy(alpha = 0.4f)),
                modifier = Modifier.padding(bottom = 18.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Headphones,
                        contentDescription = null,
                        tint = NxvYellow,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "AUDIO ONLY MODE • BACKGROUND PLAY",
                        color = NxvYellow,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                }
            }

            // Spinning Vinyl Record Animation
            SpinningVinylRecord(
                isPlaying = uiState.isPlaying,
                artworkUri = currentVideo?.contentUri,
                size = 210.dp,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Animated Equalizer Frequency Bars
            AnimatedEqualizerBars(
                isPlaying = uiState.isPlaying,
                barCount = 12,
                barWidth = 4.dp,
                spacing = 4.dp,
                maxHeight = 32.dp,
                modifier = Modifier.height(36.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Video / Audio Title
            Text(
                text = currentVideo?.title ?: "Playing Audio",
                color = Color.White,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(0.9f)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Subtitle info
            Text(
                text = "${currentVideo?.folderName ?: "Media"} • Video file played as lossless audio",
                color = Color.Gray,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Background & Lock Screen Notice Card
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0x22FFFFFF),
                modifier = Modifier.fillMaxWidth(0.92f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = NxvYellow,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Phone screen can be locked or turned off. Controls are active on notification bar & lock screen.",
                        color = Color(0xFFD0D0D0),
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Switch to Video Button with Tap Bounce Animation
            ElevatedButton(
                onClick = onSwitchToVideo,
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = NxvYellow,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .bouncingClickable(onClick = onSwitchToVideo)
                    .testTag("switch_to_video_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Videocam,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Switch to Video Screen",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}
