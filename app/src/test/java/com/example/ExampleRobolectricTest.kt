package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.db.VideoEntity
import com.example.domain.model.VideoItem
import com.example.utils.FileUtils
import com.example.utils.TimeUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("NXV Player", appName)
  }

  @Test
  fun `time formatting formats mm ss and hh mm ss correctly`() {
    assertEquals("00:00", TimeUtils.formatDuration(0L))
    assertEquals("01:15", TimeUtils.formatDuration(75_000L))
    assertEquals("1:01:05", TimeUtils.formatDuration(3665_000L))
  }

  @Test
  fun `file size formatting formats B, KB, MB, and GB correctly`() {
    assertEquals("0 B", FileUtils.formatFileSize(0L))
    assertEquals("100 KB", FileUtils.formatFileSize(100 * 1024L))
    assertTrue(FileUtils.formatFileSize(50 * 1024 * 1024L).contains("MB"))
    assertTrue(FileUtils.formatFileSize(2 * 1024L * 1024L * 1024L).contains("GB"))
  }

  @Test
  fun `video item entity domain conversion preserves properties`() {
    val domainItem = VideoItem(
      id = 42L,
      uri = "content://media/external/video/media/42",
      title = "Trailer",
      displayName = "Trailer.mp4",
      durationMs = 120_000L,
      sizeBytes = 45_000_000L,
      width = 1920,
      height = 1080,
      resolution = "1080p",
      mimeType = "video/mp4",
      folderName = "Movies",
      folderPath = "/storage/emulated/0/Movies",
      dateAdded = 1000L,
      dateModified = 2000L,
      isFavorite = true,
      lastPlayedPositionMs = 30_000L
    )

    val entity = VideoEntity.fromDomain(domainItem)
    assertEquals(domainItem.id, entity.id)
    assertEquals(domainItem.uri, entity.uri)
    assertEquals(domainItem.isFavorite, entity.isFavorite)

    val convertedBack = entity.toDomain()
    assertEquals(domainItem.title, convertedBack.title)
    assertEquals(domainItem.lastPlayedPositionMs, convertedBack.lastPlayedPositionMs)
    assertEquals(0.25f, convertedBack.progressPercentage, 0.01f)
  }
}

