package com.example.camscanner.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    // Primary — teal accent
    primary = TealPrimary,
    onPrimary = TextOnPrimary,
    primaryContainer = TealContainer,
    onPrimaryContainer = TealPrimary,

    // Secondary
    secondary = TealContainerLight,
    onSecondary = TextPrimary,
    secondaryContainer = TealContainer,
    onSecondaryContainer = TealPrimary,

    // Tertiary
    tertiary = TealPrimary,
    onTertiary = TextOnPrimary,

    // Background
    background = BackgroundDark,
    onBackground = TextPrimary,

    // Surface
    surface = BackgroundMedium,
    onSurface = TextPrimary,
    surfaceVariant = BackgroundElevated,
    onSurfaceVariant = TextSecondary,

    // Error
    error = StatusError,
    onError = TextPrimary,
    errorContainer = Color(0xFF4A1515),
    onErrorContainer = StatusError,

    // Outline
    outline = BorderSubtle,
    outlineVariant = BorderMedium,

    // Scrim
    scrim = Scrim,

    // Inverse
    inverseSurface = TextPrimary,
    inverseOnSurface = BackgroundDark,
    inversePrimary = TealPrimaryDark,
)

@Composable
fun CamScannerTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
