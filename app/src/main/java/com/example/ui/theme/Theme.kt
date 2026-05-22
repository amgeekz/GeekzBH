package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

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

@Composable
fun NeobrutalistCard(
  modifier: Modifier = Modifier,
  containerColor: Color = NeoWhite,
  borderColor: Color = NeoDark,
  borderWidth: Dp = 2.5.dp,
  shadowOffset: Dp = 4.dp,
  onClick: (() -> Unit)? = null,
  content: @Composable () -> Unit
) {
  Box(
    modifier = modifier.then(
      if (onClick != null) Modifier.clickable { onClick() } else Modifier
    )
  ) {
    // Solid Shadow behind
    Box(
      modifier = Modifier
        .matchParentSize()
        .offset(x = shadowOffset, y = shadowOffset)
        .background(NeoDark, shape = RoundedCornerShape(12.dp))
    )
    // Main Panel Front with thick border
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(containerColor, shape = RoundedCornerShape(12.dp))
        .border(BorderStroke(borderWidth, borderColor), shape = RoundedCornerShape(12.dp))
    ) {
      content()
    }
  }
}
