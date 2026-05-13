package com.example.camscanner.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val AppShapes = Shapes(
    // Thumbnail, small chip
    extraSmall = RoundedCornerShape(4.dp),
    // Page thumbnail, small card
    small = RoundedCornerShape(8.dp),
    // Document card, button, filter chip
    medium = RoundedCornerShape(12.dp),
    // Dialog, bottom sheet top corners, large card
    large = RoundedCornerShape(16.dp),
    // Bottom sheet, full-screen overlay
    extraLarge = RoundedCornerShape(24.dp)
)
