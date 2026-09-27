package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Force light theme as specified:
private val LightColorScheme = lightColorScheme(
  primary = PrimaryGreen,
  onPrimary = Color.White,
  primaryContainer = PrimaryGreenLight,
  onPrimaryContainer = PrimaryGreenDark,
  secondary = PrimaryOrange,
  onSecondary = Color.White,
  secondaryContainer = PrimaryOrangeLight,
  onSecondaryContainer = PrimaryOrangeDark,
  background = AppBackground,
  onBackground = TextDark,
  surface = CardBackground,
  onSurface = TextDark,
  surfaceVariant = CardBackground,
  onSurfaceVariant = TextMedium,
  outline = BorderLight,
  error = Color(0xFFEF4444),
  onError = Color.White
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = false, // Force light mode
  content: @Composable () -> Unit
) {
  MaterialTheme(
    colorScheme = LightColorScheme,
    typography = Typography,
    content = content
  )
}
