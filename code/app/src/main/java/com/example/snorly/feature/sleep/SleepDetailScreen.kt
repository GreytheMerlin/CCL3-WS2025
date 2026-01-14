package com.example.snorly.feature.sleep

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.snorly.feature.sleep.model.SleepDataProcessor
import java.time.Duration
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepDetailScreen(
    viewModel: SleepDetailViewModel,
    onBack: () -> Unit,
    onEdit: (String) -> Unit // Pass ID to edit screen
) {
    val record = viewModel.sleepRecord
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Colors
    val bg = Color.Black
    val cardColor = Color(0xFF1C1C1E)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {"asdf" }, // Clean look
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    // ONLY SHOW IF EDITABLE (Owned by Snorly)
                    if (viewModel.isEditable) {
                        IconButton(onClick = { if (record != null) onEdit(record.metadata.id) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White)
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bg)
            )
        },
        containerColor = bg
    ) { innerPadding ->

        if (viewModel.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (record == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Sleep session not found.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // 1. Header (Big Duration)
                item {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(viewModel.formattedDate, color = Color.Gray, fontSize = 14.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            viewModel.formattedDuration,
                            color = Color.White,
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(16.dp))

                        // Quality Badge
                        Surface(
                            color = Color(0xFF2C2C2E),
                            shape = RoundedCornerShape(100),
                        ) {
                            Text(
                                text = "Quality: ${viewModel.sleepQuality}",
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // 2. Time Range Card
                item {
                    val timeFmt = DateTimeFormatter.ofPattern("HH:mm")
                    val startStr = record.startTime.atZone(ZoneId.systemDefault()).format(timeFmt)
                    val endStr = record.endTime.atZone(ZoneId.systemDefault()).format(timeFmt)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(cardColor, RoundedCornerShape(16.dp))
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Bedtime", color = Color.Gray, fontSize = 12.sp)
                            Text(startStr, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Wake up", color = Color.Gray, fontSize = 12.sp)
                            Text(endStr, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // 3. Detailed Stages (Smart Section)
                if (record.stages.isNotEmpty()) {
                    item {
                        Text("Sleep Stages", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }

                    // Sort stages by time to show chronological order
                    items(record.stages.sortedBy { it.startTime }) { stage ->
                        val duration = Duration.between(stage.startTime, stage.endTime).toMinutes()
                        val stageName = SleepDataProcessor.getStageLabel(stage.stage)
                        val stageColor = SleepDataProcessor.getStageColor(stage.stage)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Color Dot
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(stageColor)
                            )
                            Spacer(Modifier.width(16.dp))

                            // Stage Name
                            Text(stageName, color = Color.White, modifier = Modifier.weight(1f))

                            // Duration
                            Text("${duration}m", color = Color.Gray)
                        }
                    }
                } else {
                    // Fallback for Manual Entries
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF2C2C2E), RoundedCornerShape(16.dp))
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No detailed stages available for this session.",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        // Delete Dialog
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Sleep?") },
                text = { Text("This will permanently remove this sleep record from Health Connect.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteSession(onSuccess = onBack)
                            showDeleteDialog = false
                        }
                    ) {
                        Text("Delete", color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}