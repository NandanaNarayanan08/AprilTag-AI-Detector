package com.apriltag.aidetector

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*
import com.apriltag.aidetector.ui.screens.HistoryScreen
import com.apriltag.aidetector.ui.screens.LearningScreen
import com.apriltag.aidetector.ui.screens.ScannerScreen
import com.apriltag.aidetector.ui.theme.AprilTagAIDetectorTheme

/**
 * Main entrance activity for AprilTag AI Detector.
 * Manages full system navigation loops & request overlays.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            AprilTagAIDetectorTheme {
                val navController = rememberNavController()
                
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = "scanner"
                    ) {
                        composable("scanner") {
                            ScannerScreen(
                                onNavigateToHistory = { navController.navigate("history") }
                            )
                        }
                        
                        composable("history") {
                            HistoryScreen(
                                onNavigateBack = { navController.navigateUp() }
                            )
                        }
                        
                        composable("learning") {
                            LearningScreen(
                                onNavigateBack = { navController.navigateUp() }
                            )
                        }
                    }
                }
            }
        }
    }
}
