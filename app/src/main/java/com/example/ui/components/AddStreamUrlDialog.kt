package com.example.ui.components

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLink
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.domain.model.VideoItem
import com.example.ui.theme.AmoledCard
import com.example.ui.theme.NxvYellow
import com.example.utils.StreamUrlHelper

@Composable
fun AddStreamUrlDialog(
    onDismiss: () -> Unit,
    onPlayUrl: (url: String, title: String, saveToLibrary: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var urlInput by remember { mutableStateOf("") }
    var titleInput by remember { mutableStateOf("") }
    var clipboardUrl by remember { mutableStateOf<String?>(null) }
    var isSaveChecked by remember { mutableStateOf(true) }

    // Check clipboard on dialog open
    LaunchedEffect(Unit) {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            val clipData = clipboard?.primaryClip
            if (clipData != null && clipData.itemCount > 0) {
                val text = clipData.getItemAt(0).text?.toString()?.trim() ?: ""
                if (text.startsWith("http://", ignoreCase = true) || text.startsWith("https://", ignoreCase = true) ||
                    text.contains("youtube.com") || text.contains("youtu.be") || text.contains("tiktok.com")
                ) {
                    clipboardUrl = text
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val detectedPlatform = remember(urlInput) {
        if (urlInput.isBlank()) null else StreamUrlHelper.detectPlatform(urlInput)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) +
                    scaleIn(initialScale = 0.88f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)),
            exit = fadeOut() + scaleOut(targetScale = 0.88f)
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = AmoledCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFFFFF)),
                modifier = modifier
                    .fillMaxWidth(0.92f)
                    .padding(vertical = 20.dp)
                    .testTag("add_stream_url_dialog")
            ) {
                Column(
                    modifier = Modifier.padding(22.dp)
                ) {
                    // Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(NxvYellow.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddLink,
                                    contentDescription = null,
                                    tint = NxvYellow,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Play Online Stream",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "YouTube, TikTok, HLS or Direct Video URL",
                                    color = Color.Gray,
                                    fontSize = 11.5.sp
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Close",
                                tint = Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Clipboard Quick-Paste Chip
                    if (!clipboardUrl.isNullOrBlank() && urlInput.isBlank()) {
                        Surface(
                            onClick = {
                                urlInput = clipboardUrl ?: ""
                                if (titleInput.isBlank()) {
                                    titleInput = StreamUrlHelper.suggestTitle(clipboardUrl ?: "")
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0x22FFD600),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NxvYellow.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentPaste,
                                    contentDescription = null,
                                    tint = NxvYellow,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Paste from Clipboard",
                                        color = NxvYellow,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = clipboardUrl ?: "",
                                        color = Color.LightGray,
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    // Supported Platform Badges
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PlatformBadge(name = "YouTube", color = Color(0xFFFF3D00), isSelected = detectedPlatform == "YouTube")
                        PlatformBadge(name = "TikTok", color = Color(0xFF00E5FF), isSelected = detectedPlatform == "TikTok")
                        PlatformBadge(name = "HLS (.m3u8)", color = Color(0xFF00E676), isSelected = detectedPlatform == "HLS Stream")
                        PlatformBadge(name = "Direct MP4", color = NxvYellow, isSelected = detectedPlatform == "Direct Video")
                    }

                    // URL Input Field
                    OutlinedTextField(
                        value = urlInput,
                        onValueChange = {
                            urlInput = it
                            if (titleInput.isBlank() && it.isNotBlank()) {
                                titleInput = StreamUrlHelper.suggestTitle(it)
                            }
                        },
                        label = { Text("Video Stream URL", fontSize = 13.sp) },
                        placeholder = { Text("Paste YouTube, TikTok or video link...", color = Color.Gray, fontSize = 12.5.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = when (detectedPlatform) {
                                    "YouTube" -> Icons.Default.SmartDisplay
                                    "TikTok" -> Icons.Default.Videocam
                                    "HLS Stream" -> Icons.Default.LiveTv
                                    else -> Icons.Default.Language
                                },
                                contentDescription = null,
                                tint = if (detectedPlatform != null) NxvYellow else Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            if (urlInput.isNotEmpty()) {
                                IconButton(onClick = { urlInput = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.Gray, modifier = Modifier.size(18.dp))
                                }
                            }
                        },
                        singleLine = false,
                        maxLines = 3,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = NxvYellow,
                            unfocusedBorderColor = Color(0x44FFFFFF),
                            focusedContainerColor = Color(0xFF141418),
                            unfocusedContainerColor = Color(0xFF141418)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("stream_url_text_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Optional Title Input Field
                    OutlinedTextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        label = { Text("Video Title (Optional)", fontSize = 13.sp) },
                        placeholder = { Text("Enter a friendly title...", color = Color.Gray, fontSize = 12.5.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = NxvYellow,
                            unfocusedBorderColor = Color(0x33FFFFFF),
                            focusedContainerColor = Color(0xFF141418),
                            unfocusedContainerColor = Color(0xFF141418)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("stream_title_text_input")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Save to Library Toggle
                    Surface(
                        onClick = { isSaveChecked = !isSaveChecked },
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0x18FFFFFF),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isSaveChecked) NxvYellow else Color.Transparent)
                                    .border(1.5.dp, if (isSaveChecked) NxvYellow else Color.Gray, RoundedCornerShape(4.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSaveChecked) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Save URL in Streams Library for quick access",
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x44FFFFFF)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                        ) {
                            Text("Cancel", fontSize = 13.sp)
                        }

                        Button(
                            onClick = {
                                if (urlInput.isNotBlank()) {
                                    val cleanUrl = StreamUrlHelper.sanitizeUrl(urlInput)
                                    val finalTitle = if (titleInput.isNotBlank()) titleInput else StreamUrlHelper.suggestTitle(cleanUrl)
                                    onPlayUrl(cleanUrl, finalTitle, isSaveChecked)
                                    onDismiss()
                                }
                            },
                            enabled = urlInput.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NxvYellow,
                                contentColor = Color.Black,
                                disabledContainerColor = Color(0x33FFD600),
                                disabledContentColor = Color.Gray
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1.3f)
                                .height(46.dp)
                                .testTag("play_stream_url_submit_button")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Play Stream", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlatformBadge(name: String, color: Color, isSelected: Boolean) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) color.copy(alpha = 0.25f) else Color(0x18FFFFFF),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, color) else null,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Text(
            text = name,
            color = if (isSelected) color else Color.Gray,
            fontSize = 10.5.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
