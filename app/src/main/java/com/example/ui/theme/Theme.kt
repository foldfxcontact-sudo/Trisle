package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val TrisleColorScheme = darkColorScheme(
  primary = PlatinumWhite,
  onPrimary = ObsidianBlack,
  primaryContainer = GraphiteSurfaceVariant,
  onPrimaryContainer = TextHighContrast,
  secondary = TitaniumSilver,
  onSecondary = ObsidianBlack,
  secondaryContainer = GraphiteSurface,
  onSecondaryContainer = TextHighContrast,
  tertiary = TextMediumContrast,
  onTertiary = ObsidianBlack,
  background = ObsidianBlack,
  onBackground = TextHighContrast,
  surface = CarbonBackground,
  onSurface = TextHighContrast,
  surfaceVariant = GraphiteSurface,
  onSurfaceVariant = TextMediumContrast,
  outline = SteelBorder,
  outlineVariant = SteelBorderSubtle
)

@Composable
fun TrisleTheme(
  content: @Composable () -> Unit
) {
  // Trisle is an intentional OLED black & precision grey aesthetic
  MaterialTheme(
    colorScheme = TrisleColorScheme,
    typography = Typography,
    content = content
  )
}
