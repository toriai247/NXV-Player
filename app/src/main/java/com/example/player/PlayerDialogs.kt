package com.example.player

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmoledCard
import com.example.ui.theme.AmoledSurface
import com.example.ui.theme.NxvYellow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubtitleBottomSheet(
    uiState: PlayerUiState,
    onDismiss: () -> Unit,
    onToggleSubtitle: (Boolean) -> Unit,
    onSelectTrack: (Int) -> Unit,
    onLoadExternalSubtitle: (Uri) -> Unit,
    onUpdateStyle: (fontSize: Int, opacity: Float) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var currentSize by remember { mutableIntStateOf(uiState.subtitleFontSizeSp) }
    var currentOpacity by remember { mutableFloatStateOf(uiState.subtitleOpacity) }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            onLoadExternalSubtitle(uri)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AmoledSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .testTag("subtitle_sheet")
        ) {
            Text(
                text = "Subtitle Settings",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Enable / Disable Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Enable Subtitles",
                    color = Color.White,
                    fontSize = 16.sp
                )
                Switch(
                    checked = uiState.isSubtitleEnabled,
                    onCheckedChange = onToggleSubtitle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = NxvYellow
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Load External Subtitle File Button
            OutlinedButton(
                onClick = {
                    filePicker.launch(arrayOf("*/*", "text/*", "application/x-subrip"))
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = NxvYellow
                )
            ) {
                Icon(
                    imageVector = Icons.Default.FileOpen,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Load External Subtitle (.srt, .vtt, .ass)")
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = AmoledCard)
            Spacer(modifier = Modifier.height(12.dp))

            // Available Subtitle Tracks
            Text(
                text = "Tracks",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.subtitleTracks.isEmpty()) {
                Text(
                    text = "No internal subtitle tracks found. You can load external .srt files above.",
                    color = Color.Gray,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                uiState.subtitleTracks.forEach { track ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectTrack(track.index) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = track.isSelected,
                            onClick = { onSelectTrack(track.index) },
                            colors = RadioButtonDefaults.colors(selectedColor = NxvYellow)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = track.name,
                            color = if (track.isSelected) NxvYellow else Color.White,
                            fontSize = 15.sp,
                            fontWeight = if (track.isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = AmoledCard)
            Spacer(modifier = Modifier.height(12.dp))

            // Font Size Slider
            Text(
                text = "Font Size: ${currentSize}sp",
                color = Color.White,
                fontSize = 14.sp
            )
            Slider(
                value = currentSize.toFloat(),
                onValueChange = {
                    currentSize = it.toInt()
                    onUpdateStyle(currentSize, currentOpacity)
                },
                valueRange = 12f..36f,
                colors = SliderDefaults.colors(
                    thumbColor = NxvYellow,
                    activeTrackColor = NxvYellow
                )
            )

            // Opacity Slider
            Text(
                text = "Opacity: ${(currentOpacity * 100).toInt()}%",
                color = Color.White,
                fontSize = 14.sp
            )
            Slider(
                value = currentOpacity,
                onValueChange = {
                    currentOpacity = it
                    onUpdateStyle(currentSize, currentOpacity)
                },
                valueRange = 0.3f..1.0f,
                colors = SliderDefaults.colors(
                    thumbColor = NxvYellow,
                    activeTrackColor = NxvYellow
                )
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioTrackBottomSheet(
    uiState: PlayerUiState,
    onDismiss: () -> Unit,
    onSelectTrack: (Int) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AmoledSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .testTag("audio_sheet")
        ) {
            Text(
                text = "Audio Tracks",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.audioTracks.isEmpty()) {
                Text(
                    text = "Default stereo audio track",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(uiState.audioTracks) { track ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onSelectTrack(track.index) }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = track.isSelected,
                                onClick = { onSelectTrack(track.index) },
                                colors = RadioButtonDefaults.colors(selectedColor = NxvYellow)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = track.name,
                                    color = if (track.isSelected) NxvYellow else Color.White,
                                    fontWeight = if (track.isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 15.sp
                                )
                                if (!track.language.isNullOrBlank()) {
                                    Text(
                                        text = "Language: ${track.language}",
                                        color = Color.Gray,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            if (track.isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = NxvYellow,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaybackSpeedBottomSheet(
    currentSpeed: Float,
    onDismiss: () -> Unit,
    onSelectSpeed: (Float) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val speeds = listOf(0.25f, 0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f, 2.5f, 3.0f)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AmoledSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .testTag("speed_sheet")
        ) {
            Text(
                text = "Playback Speed",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            speeds.forEach { speed ->
                val isSelected = currentSpeed == speed
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) AmoledCard else Color.Transparent)
                        .clickable {
                            onSelectSpeed(speed)
                            onDismiss()
                        }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (speed == 1.0f) "1.0x (Normal)" else "${speed}x",
                        color = if (isSelected) NxvYellow else Color.White,
                        fontSize = 16.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )

                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = NxvYellow,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
