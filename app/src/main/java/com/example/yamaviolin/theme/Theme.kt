package com.example.yamaviolin.theme

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
    primary = Color(0xFFFFB58D), // Lighter warm orange-brown for dark mode contrast
    onPrimary = Color(0xFF472106),
    primaryContainer = Color(0xFF5F3517),
    onPrimaryContainer = Color(0xFFFFDBC8),
    secondary = Color(0xFFFFB77F),
    onSecondary = Color(0xFF4C2700),
    secondaryContainer = Color(0xFF6C3A0A),
    onSecondaryContainer = Color(0xFFFFDCC4),
    tertiary = Color(0xFFFFB2C2),
    onTertiary = Color(0xFF5F1129),
    tertiaryContainer = Color(0xFF7D2940),
    onTertiaryContainer = Color(0xFFFFD9E0),
    background = Color(0xFF1E1A17), // Charcoal warm brown
    onBackground = Color(0xFFECE0DA),
    surface = Color(0xFF26201C),
    onSurface = Color(0xFFECE0DA),
    surfaceVariant = Color(0xFF52443C),
    onSurfaceVariant = Color(0xFFD7C3B7),
    outline = Color(0xFFA08D82)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = ViolinWoodBrown,
    onPrimary = OnViolinWoodBrown,
    primaryContainer = ViolinWoodBrownContainer,
    onPrimaryContainer = OnViolinWoodBrownContainer,
    secondary = WarmAmber,
    onSecondary = OnWarmAmber,
    secondaryContainer = WarmAmberContainer,
    onSecondaryContainer = OnWarmAmberContainer,
    tertiary = DeepBurgundy,
    onTertiary = OnDeepBurgundy,
    tertiaryContainer = DeepBurgundyContainer,
    onTertiaryContainer = OnDeepBurgundyContainer,
    background = SoftIvory,
    onBackground = OnSoftIvory,
    surface = NearWhiteWarm,
    onSurface = OnNearWhiteWarm,
    surfaceVariant = Color(0xFFF5ECE5),
    onSurfaceVariant = SoftGraphite,
    outline = WarmBorder
  )

@Composable
fun YAMAViolinTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Set to false by default to ensure the user's custom violin aesthetic is always shown
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }
      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

