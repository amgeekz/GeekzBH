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

private val DarkColorScheme =
  darkColorScheme(
    primary = NeonGreen,
    secondary = DarkCharcoal,
    tertiary = LightBlue,
    background = AmoledBlack,
    surface = DarkCharcoal,
    onPrimary = AmoledBlack,
    onSecondary = TextPrimary,
    onTertiary = TextPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary
  )

private val LightColorScheme =
  lightColorScheme(
    primary = NeonGreen,
    secondary = Color(0xFFAFAFAF),
    tertiary = LightBlue,
    background = Color(0xFFFFFFFF),
    surface = Color(0xFFF2F2F7),
    onPrimary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force dark theme to match screenshots
  dynamicColor: Boolean = false, // Disable dynamic colors to preserve charging styles
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
