package com.example.camscanner

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.example.camscanner.presentation.navigation.NavGraph
import com.example.camscanner.presentation.theme.CamScannerTheme

@Composable
fun CamScannerApp() {
    val navController = rememberNavController()

    CamScannerTheme {
        NavGraph(navController = navController)
    }
}
