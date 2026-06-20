package com.apriltag.aidetector.ui.screens

import android.content.Context
import android.os.Environment
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.apriltag.aidetector.data.ScanHistoryDatabase
import com.apriltag.aidetector.data.ScanHistoryEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val database = remember { ScanHistoryDatabase.getDatabase(context) }
    
    // Scan histories state collected as reactive flows
    var historyList by remember { mutableStateOf<List<ScanHistoryEntity>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(searchQuery) {
        // Simple reactive local query
        database.scanHistoryDao().getAllScans().collect { scans ->
            historyList = if (searchQuery.trim().isEmpty()) {
                scans
            } else {
                scans.filter { 
                    it.tagId.toString().contains(searchQuery) || 
                    it.scenario.lowercase().contains(searchQuery.lowercase())
                }
            }
        }
    }

    val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan Logging History", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { onNavigateBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    // Export scans as csv file
                    IconButton(onClick = { 
                        coroutineScope.launch {
                            val activeList = database.scanHistoryDao().getAllScans().first()
                            if (activeList.isEmpty()) {
                                Toast.makeText(context, "No scanned tags available to export", Toast.LENGTH_SHORT).show()
                            } else {
                                val state = exportDatabaseToCSV(context, activeList)
                                Toast.makeText(context, state, Toast.LENGTH_LONG).show()
                            }
                        }
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Export CSV", tint = MaterialTheme.colorScheme.primary)
                    }

                    // Clear database
                    IconButton(onClick = {
                        coroutineScope.launch {
                            database.scanHistoryDao().clearAll()
                            Toast.makeText(context, "Database cleared", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Clear All", tint = Color.Red)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F171E))
            )
        },
        modifier = modifier.fillMaxSize()
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize()
                .background(Color(0xFF0A0F14))
                .padding(16.dp)
        ) {
            // High Tech search matrix input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Filter scans by ID or Scenario...", color = Color.LightGray) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.LightGray) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (historyList.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.FolderOpen, 
                            contentDescription = null, 
                            modifier = Modifier.size(64.dp), 
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No recorded frame detections located", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(historyList) { scan ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF121A21)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            "Tag #${scan.tagId}",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            scan.scenario,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.LightGray
                                        )
                                    }
                                    IconButton(onClick = {
                                        coroutineScope.launch {
                                            database.scanHistoryDao().deleteScan(scan)
                                            Toast.makeText(context, "Scan removed", Toast.LENGTH_SHORT).show()
                                        }
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f))
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(10.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Distance", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                        Text("${scan.distance}m", style = MaterialTheme.typography.bodyMedium, color = Color.White)
                                    }
                                    Column {
                                        Text("Pitch / Yaw", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                        Text("${scan.pitch}° / ${scan.yaw}°", style = MaterialTheme.typography.bodyMedium, color = Color.White)
                                    }
                                    Column {
                                        Text("Timestamp", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                        Text(
                                            dateFormatter.format(Date(scan.timestamp)),
                                            style = MaterialTheme.typography.bodySmall,
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
    }
}

/**
 * Encodes recorded tag scans list into a secure download ready local CSV file.
 */
private fun exportDatabaseToCSV(context: Context, scans: List<ScanHistoryEntity>): String {
    val df = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
    val fileName = "AprilTag_Scans_${df.format(Date())}.csv"
    val docDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: return "Unable to locate directories"
    val csvFile = File(docDir, fileName)

    return try {
        val writer = FileWriter(csvFile)
        writer.append("ID,TagFamily,Timestamp,DistanceM,PitchDeg,RollDeg,YawDeg,Scenario\n")
        scans.forEach { scan ->
            writer.append("${scan.tagId},${scan.family},${scan.timestamp},${scan.distance},${scan.pitch},${scan.roll},${scan.yaw},${scan.scenario}\n")
        }
        writer.flush()
        writer.close()
        "Saved successfully: ${csvFile.absolutePath}"
    } catch (e: Exception) {
        "Failure exporting scans CSV: ${e.message}"
    }
}
