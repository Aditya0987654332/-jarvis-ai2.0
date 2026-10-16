package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val JarvisColorScheme = darkColorScheme(
  primary = NeonCyan,
  onPrimary = VoidBlack,
  primaryContainer = CyberPlate,
  onPrimaryContainer = NeonCyanBright,
  secondary = NeonBlue,
  onSecondary = VoidBlack,
  secondaryContainer = CyberSlate,
  onSecondaryContainer = NeonCyanDim,
  tertiary = ElectricBlue,
  onTertiary = TextWhite,
  background = VoidBlack,
  onBackground = TextCyan,
  surface = CyberDark,
  onSurface = TextCyan,
  surfaceVariant = CyberSlate,
  onSurfaceVariant = TextCyanDim,
  outline = CyberBorderBright,
  outlineVariant = CyberBorder
)

@Composable
fun MyApplicationTheme(
  content: @Composable () -> Unit
) {
  MaterialTheme(
    colorScheme = JarvisColorScheme,
    typography = Typography,
    content = content
  )
}
