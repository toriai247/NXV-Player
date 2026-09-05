package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

val AmoledDarkColorScheme = darkColorScheme(
  primary = NxvYellow,
  onPrimary = Color.Black,
  primaryContainer = NxvYellowContainer,
  onPrimaryContainer = NxvOnYellowContainer,
  secondary = NxvYellowLight,
  onSecondary = Color.Black,
  secondaryContainer = AmoledCard,
  onSecondaryContainer = TextPrimaryDark,
  tertiary = NxvYellowDark,
  onTertiary = Color.White,
  background = AmoledBackground,
  onBackground = TextPrimaryDark,
  surface = AmoledSurface,
  onSurface = TextPrimaryDark,
  surfaceVariant = AmoledCard,
  onSurfaceVariant = TextSecondaryDark,
  outline = AmoledBorder,
  outlineVariant = AmoledDivider
)

val NxvLightColorScheme = lightColorScheme(
  primary = NxvYellowDark,
  onPrimary = Color.White,
  primaryContainer = NxvYellowLight,
  onPrimaryContainer = Color.Black,
  secondary = NxvYellow,
  onSecondary = Color.Black,
  secondaryContainer = LightCard,
  onSecondaryContainer = TextPrimaryLight,
  tertiary = Color(0xFF8A6D00),
  onTertiary = Color.White,
  background = LightBackground,
  onBackground = TextPrimaryLight,
  surface = LightSurface,
  onSurface = TextPrimaryLight,
  surfaceVariant = LightCard,
  onSurfaceVariant = TextSecondaryLight,
  outline = LightBorder,
  outlineVariant = Color(0xFFECEFF1)
)

@Composable
fun NxvPlayerTheme(
  themeMode: String = "AMOLED", // "AMOLED", "DARK", "LIGHT", "SYSTEM"
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val isSystemDark = isSystemInDarkTheme()
  val context = LocalContext.current

  val colorScheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
      val isDark = when (themeMode) {
        "LIGHT" -> false
        "DARK", "AMOLED" -> true
        else -> isSystemDark
      }
      if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    themeMode == "LIGHT" -> NxvLightColorScheme
    themeMode == "SYSTEM" && !isSystemDark -> NxvLightColorScheme
    else -> AmoledDarkColorScheme // Default AMOLED Dark
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  NxvPlayerTheme(
    themeMode = if (darkTheme) "AMOLED" else "LIGHT",
    dynamicColor = dynamicColor,
    content = content
  )
}

