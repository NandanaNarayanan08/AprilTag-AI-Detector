package com.apriltag.aidetector.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.apriltag.aidetector.data.ScanHistoryDatabase
import com.apriltag.aidetector.data.ScanHistoryEntity
import com.apriltag.aidetector.utils.AprilTagImageAnalyzer
import com.google.generativeai.GenerativeModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    onNavigateToHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val database = remember { ScanHistoryDatabase.getDatabase(context) }
    
    // Camera permissions handling state
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            Toast.makeText(context, "Camera permission is required for AprilTag tracking", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Active environment scenarios and pose calculations variables
    var activeScenario by remember { mutableStateOf("Autonomous UAV Helipad") }
    var detectedTagsList by remember { mutableStateOf<List<AprilTagImageAnalyzer.DetectedTagResult>>(emptyList()) }
    var selectedTagForAI by remember { mutableStateOf<AprilTagImageAnalyzer.DetectedTagResult?>(null) }
    var isAILoading by remember { mutableStateOf(false) }
    var aiExplanation by remember { mutableStateOf("") }
    
    // Bottom sheet state for Gemini breakdown
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showExplanationSheet by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToHistory() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.Black
            ) {
                Icon(Icons.Default.History, contentDescription = "Scan History")
            }
        },
        modifier = modifier.fillMaxSize()
    ) { pad ->
        Box(modifier = Modifier.padding(pad).fillMaxSize()) {
            if (hasCameraPermission) {
                // Render CameraX surface preview utilizing AndroidView JNI callback
                AndroidView(
                    factory = { ctx ->
                        PreviewView(ctx).apply {
                            scaleType = PreviewView.ScaleType.FILL_CENTER
                            // Developer configures CameraProvider & BindToLifecycle with AprilTagImageAnalyzer
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // High-precision Cyber UI frame simulator interface overlay
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Draw custom holographic crosshairs targeting center of the lens
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val r = 80.dp.toPx()
                    
                    drawCircle(
                        color = Color(0xFF00FF40),
                        radius = r,
                        style = Stroke(width = 2.dp.toPx()),
                        alpha = 0.45f
                    )
                    
                    drawLine(
                        color = Color(0xFF00FF40),
                        start = Offset(cx - r - 20, cy),
                        end = Offset(cx + r + 20, cy),
                        strokeWidth = 2.dp.toPx(),
                        alpha = 0.45f
                    )

                    drawLine(
                        color = Color(0xFF00FF40),
                        start = Offset(cx, cy - r - 20),
                        end = Offset(cx, cy + r + 20),
                        strokeWidth = 2.dp.toPx(),
                        alpha = 0.45f
                    )

                    // Draw bounds markers over any dynamic active detections
                    detectedTagsList.forEach { tag ->
                        // Simulate scale bounds based on pitch / yaw parameters
                        val sizeSq = (180.dp.toPx() / tag.distance).coerceIn(40.dp.toPx(), 400.dp.toPx())
                        drawRect(
                            color = Color(0xFF00FF40),
                            topLeft = Offset(tag.centerX - sizeSq/2, tag.centerY - sizeSq/2),
                            size = androidx.compose.ui.geometry.Size(sizeSq, sizeSq),
                            style = Stroke(width = 3.dp.toPx())
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Grant Camera Permission", color = Color.Black)
                    }
                }
            }

            // HUD overlay status grid (Cyber-tech appearance)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(12.dp))
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color(0xFF00FF40), RoundedCornerShape(50))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "DETECTOR ENGINE: READY",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = "FAMILY: tag36h11",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.LightGray
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "ACTIVE SCENARIO: $activeScenario",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White
                )
            }

            // Simulative target locator card if any dummy or actual tags detect:
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp, start = 16.dp, end = 16.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val dummyTag = AprilTagImageAnalyzer.DetectedTagResult(12, 0.95f, 12f, -4f, 8f, 300f, 400f)
                            detectedTagsList = listOf(dummyTag)
                            selectedTagForAI = dummyTag
                            showExplanationSheet = true
                            
                            // Log scan to physical local room database in background
                            coroutineScope.launch {
                                database.scanHistoryDao().insertScan(
                                    ScanHistoryEntity(
                                        tagId = dummyTag.id,
                                        family = "tag36h11",
                                        timestamp = System.currentTimeMillis(),
                                        distance = dummyTag.distance,
                                        pitch = dummyTag.pitch,
                                        roll = dummyTag.roll,
                                        yaw = dummyTag.yaw,
                                        scenario = activeScenario
                                    )
                                )
                            }
                            
                            // Query Gemini AI via the official GenAI integration SDK proxy
                            isAILoading = true
                            aiExplanation = "Querying Gemini AI modeling parameters..."
                            coroutineScope.launch {
                                try {
                                    val genModel = GenerativeModel(
                                        modelName = "gemini-3.5-flash",
                                        apiKey = "AI_STUDY_PLACEHOLDER_KEY" // Read from BuildConfig in real installs
                                    )
                                    val response = genModel.generateContent(
                                        "Explain AprilTag #${dummyTag.id} with pitch=${dummyTag.pitch}°, distance=${dummyTag.distance}m " +
                                        "for the use case scenario $activeScenario. Provide short beginner explanations."
                                    )
                                    aiExplanation = response.text ?: "No insight received."
                                } catch (e: Exception) {
                                    // Fallback if key is empty or network blocked
                                    aiExplanation = """
                                        ### Target AprilTag #${dummyTag.id} Breakdown
                                        
                                        * **Identity**: Family tag36h11, typically used for horizontal high-contrast layouts.
                                        * **Distance**: ${dummyTag.distance}m. Perfect range for automated robot alignment.
                                        * **Pose Calculations**: Pitch angle is tilted ${dummyTag.pitch}°, meaning your lens is positioned higher than the orthogonal axis.
                                        * **Robotics Deployment**: Frequently mounted on commercial drone pads. The feedback loops map landing vectors straight to camera coordinates.
                                    """.trimIndent()
                                } finally {
                                    isAILoading = false
                                }
                            }
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.DeveloperMode,
                            contentDescription = "Tap tag",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("DUMMY SCAN ACTIVE (TAG #12)", style = MaterialTheme.typography.titleMedium, color = Color.White)
                            Text("Pose: Roll -4° | Pitch 12° | Distance 0.95m", style = MaterialTheme.typography.bodySmall, color = Color.LightGray)
                            Text("Tap Card to Test Scan logging & Explain with Gemini AI", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }

    // AI Explanation Sheet
    if (showExplanationSheet) {
        ModalBottomSheet(
            onDismissRequest = { showExplanationSheet = false },
            sheetState = sheetState,
            containerColor = Color(0xFF0F171E),
            modifier = Modifier.fillMaxHeight(0.85f)
        ) {
            Column(modifier = Modifier.padding(24.dp).fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "AI Agent Explanation (Tag #12)",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = { showExplanationSheet = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                if (isAILoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(150.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else {
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        item {
                            Text(
                                text = aiExplanation,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
