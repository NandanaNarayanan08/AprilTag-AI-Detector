package com.apriltag.aidetector.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Define premium neon dark cyber-tech color variables
val NeonGreen = Color(0xFF00FF40)
val CyberDarkBase = Color(0xFF0A0F14)
val CyberGreySurface = Color(0xFF121A21)
val TealAccent = Color(0xFF00F1FF)
val OrangeFlame = Color(0xFFFF5E00)
val PureMutedWhite = Color(0xFFE2EAF1)

private val DarkColorScheme = darkColorScheme(
    primary = NeonGreen,
    secondary = TealAccent,
    tertiary = OrangeFlame,
    background = CyberDarkBase,
    surface = CyberGreySurface,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = PureMutedWhite,
    onSurface = PureMutedWhite
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF00A22C),
    secondary = Color(0xFF007C85),
    tertiary = OrangeFlame,
    background = Color(0xFFF4F7F9),
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF121A21),
    onSurface = Color(0xFF121A21)
)

@Composable
fun AprilTagAIDetectorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Hardforce cyber green theme by default
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            // Standard dynamic theme is overridden here to protect cyber aesthetic
            DarkColorScheme
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(), // Standard default typography sets auto-falls to Roboto
        content = content
    )
}
