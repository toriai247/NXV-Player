package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.VideoItem
import com.example.ui.theme.AmoledCard
import com.example.ui.theme.AmoledSurface
import com.example.ui.theme.NxvYellow
import com.example.utils.FileUtils
import com.example.utils.TimeUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun VideoDetailsDialog(
    video: VideoItem,
    onDismiss: () -> Unit
) {
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(18.dp),
        containerColor = AmoledSurface,
        title = {
            Text(
                text = "Video Details",
                color = Color.White,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                DetailRow("Title", video.title)
                DetailRow("Duration", TimeUtils.formatDuration(video.durationMs))
                DetailRow("Resolution", "${video.resolution} (${video.width}x${video.height})")
                DetailRow("File Size", FileUtils.formatFileSize(video.sizeBytes))
                DetailRow("Format", video.mimeType)
                DetailRow("Folder", video.folderName)
                DetailRow("Path", video.folderPath)
                if (video.dateAdded > 0) {
                    DetailRow("Date Added", dateFormatter.format(Date(video.dateAdded)))
                }
                if (video.lastPlayedTimestamp > 0) {
                    DetailRow("Last Played", dateFormatter.format(Date(video.lastPlayedTimestamp)))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = NxvYellow, contentColor = Color.Black)
            ) {
                Text("Close", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = label,
            color = NxvYellow,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = value,
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        HorizontalDivider(color = AmoledCard)
    }
}
