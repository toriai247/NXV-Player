package com.example

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.VideoItem
import com.example.ui.MainViewModel
import com.example.ui.navigation.NxvApp
import com.example.ui.theme.NxvPlayerTheme
import com.example.utils.FileUtils

class MainActivity : ComponentActivity() {

  private val viewModel: MainViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    handleIncomingIntent(intent)

    setContent {
      val preferences by viewModel.preferences.collectAsStateWithLifecycle()

      NxvPlayerTheme(themeMode = preferences.themeMode) {
        NxvApp(
          viewModel = viewModel,
          modifier = Modifier.fillMaxSize()
        )
      }
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    handleIncomingIntent(intent)
  }

  private fun handleIncomingIntent(intent: Intent?) {
    if (intent == null) return
    val action = intent.action
    val data: Uri? = intent.data

    if (action == Intent.ACTION_VIEW && data != null) {
      val path = data.path ?: ""
      val fileName = data.lastPathSegment ?: "External Video"
      val title = FileUtils.getFileNameWithoutExtension(fileName)

      val videoItem = VideoItem(
        id = System.currentTimeMillis(),
        uri = data.toString(),
        title = title,
        displayName = fileName,
        durationMs = 0L,
        sizeBytes = 0L,
        width = 1920,
        height = 1080,
        resolution = "HD",
        mimeType = intent.type ?: "video/*",
        folderName = "External",
        folderPath = path,
        dateAdded = System.currentTimeMillis(),
        dateModified = System.currentTimeMillis()
      )

      viewModel.playVideo(videoItem, listOf(videoItem))
    }
  }

  override fun onPictureInPictureModeChanged(
    isInPictureInPictureMode: Boolean,
    newConfig: Configuration
  ) {
    super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
  }
}

