package com.example.camscanner

import androidx.compose.runtime.Composable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.compose.rememberNavController
import com.example.camscanner.presentation.navigation.NavGraph

@Composable
fun CamScannerApp() {
    val navController = rememberNavController()
    
    MaterialTheme {
        Surface {
            NavGraph(navController = navController)
        }
    }
}
