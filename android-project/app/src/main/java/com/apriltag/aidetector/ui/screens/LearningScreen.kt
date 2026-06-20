package com.apriltag.aidetector.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Define static learning module data structure for Jetpack Compose UI layout
data class CourseModule(
    val id: String,
    val title: String,
    val subtitle: String,
    val rTime: String,
    val description: String,
    val category: String,
    val detailMarkdownPrerendered: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearningScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expandedModuleId by remember { mutableStateOf<String?>(null) }

    val modules = remember {
        listOf(
            CourseModule(
                id = "apriltag-fundamentals",
                title = "1. What are AprilTags?",
                subtitle = "Intro to visual fidicual tracking",
                rTime = "4 min",
                description = "Learn how block code matrices differ from standard consumer QR codes.",
                category = "Computer Vision",
                detailMarkdownPrerendered = """
                    An AprilTag is a visual fiducial system (similar to a highly specialized QR code) developed by researchers at the University of Michigan. 
                    
                    Features:
                    * High Hamming Distance: Designed to prevent sensor pixel corruption errors.
                    * High speed: Simple shapes enable 60+ FPS searches inside standard Android camera feeds.
                    * Robust detection: Works in dark, skewed, and motion-blurred scenarios safely.
                """.trimIndent()
            ),
            CourseModule(
                id = "pose-math",
                title = "2. Pose Estimation & Geometry",
                subtitle = "Deciphering 3D rotation angles",
                rTime = "7 min",
                description = "Learn the Perspective-n-Point solver mathematics converting pixels to 3D spaces.",
                category = "Mathematics",
                detailMarkdownPrerendered = """
                    The process of translating 2D picture positions into real 3D coordinates (X, Y, Z coordinates + Pitch, Roll, Yaw angles) is solved via Perspective-n-Point (PnP).
                    
                    Mathematics requirements:
                    * Camera calibration focal metrics: Lens distortion matrices (fx, fy, cx, cy) are loaded inside OpenCV.
                    * Rotational matrices: Reconstructed through Euler-Rodrigues conversion functions.
                """.trimIndent()
            ),
            CourseModule(
                id = "uav-dock",
                title = "3. Autonomous UAV Helipads",
                subtitle = "Visual Servo systems on drone landings",
                rTime = "6 min",
                description = "How micro UAV microcontrollers utilize nested targets to align descents.",
                category = "Robotics",
                detailMarkdownPrerendered = """
                    Standard GPS has a drift error profile of up to 5 meters. Autonomous industrial drones rely on downward cameras scanning AprilTags to align within 1cm accuracy.
                    
                    Nested layouts:
                    * Outer Tag: Picked up at elevation levels (5m to 20m off the ground).
                    * Inner Tags: Small high-density tags that align the drone of the final 20cm of prop clearance docking.
                """.trimIndent()
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fiducial Learning Section", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { onNavigateBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F171E))
            )
        },
        modifier = modifier.fillMaxSize()
    ) { pad ->
        LazyColumn(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize()
                .background(Color(0xFF0A0F14))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "AprilTag Academy",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Learn core robotic spatial calculation paradigms right inside Compose interfaces.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.LightGray,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )
            }

            items(modules) { module ->
                val isExpanded = expandedModuleId == module.id

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF121420)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandedModuleId = if (isExpanded) null else module.id }
                        .border(
                            1.dp,
                            if (isExpanded) MaterialTheme.colorScheme.primary else Color.Transparent,
                            RoundedCornerShape(14.dp)
                        )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = module.category,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = module.rTime,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                        }

                        Text(
                            text = module.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Text(
                            text = module.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.LightGray
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = module.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )

                        AnimatedVisibility(
                            visible = isExpanded,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column(modifier = Modifier.padding(top = 16.dp)) {
                                Divider(color = Color.Gray.copy(alpha = 0.3f), thickness = 1.dp)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = module.detailMarkdownPrerendered,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.LightGray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
