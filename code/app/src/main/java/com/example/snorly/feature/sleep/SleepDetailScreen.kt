package com.example.snorly.feature.sleep

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.health.connect.client.records.SleepSessionRecord
import java.time.Duration
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepDetailScreen(
    viewModel: SleepDetailViewModel,
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    onDeleteSuccess: () -> Unit
) {
    val record = viewModel.sleepRecord
    var showDeleteDialog by remember { mutableStateOf(false) }

    val bg = Color.Black
    val cardColor = Color(0xFF1C1C1E)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { }, navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White
                    )
                }
            }, actions = {
                // Only allow edits if Snorly owns the data
                IconButton(onClick = {
                    // FIXED: Use local 'id' converted to String, not 'metadata.id'
                    if (record != null) onEdit(record.id.toString())
                }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White)
                }
                if (viewModel.isEditable) {

                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.Red
                        )
                    }
                }
            }, colors = TopAppBarDefaults.topAppBarColors(containerColor = bg)
            )
        }, containerColor = bg
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
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(viewModel.formattedDate, color = Color.Gray, fontSize = 14.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            viewModel.formattedDuration,
                            color = Color.White,
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(16.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Quality Badge
                            Surface(
                                color = Color(0xFF2C2C2E),
                                shape = RoundedCornerShape(100),
                            ) {
                                Text(
                                    text = "Quality: ${viewModel.sleepQuality}",
                                    color = Color.White,
                                    modifier = Modifier.padding(
                                        horizontal = 16.dp, vertical = 8.dp
                                    ),
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp
                                )
                            }

                            Spacer(Modifier.width(8.dp))

                            // Source App Badge
                            Surface(
                                color = if (viewModel.isEditable) Color(0xFF0F3D64) else Color(
                                    0xFF3E2723
                                ), // Blue for Snorly, Brown/Red for others
                                shape = RoundedCornerShape(100),
                            ) {
                                Row(
                                    modifier = Modifier.padding(
                                        horizontal = 12.dp, vertical = 8.dp
                                    ), verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (!viewModel.isEditable) {
                                        Icon(
                                            Icons.Default.Info,
                                            contentDescription = null,
                                            tint = Color(0xFFFFAB91),
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                    }
                                    Text(
                                        text = "Source: ${viewModel.sourceAppName}",
                                        color = if (viewModel.isEditable) Color(0xFF90CAF9) else Color(
                                            0xFFFFAB91
                                        ),
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 12.sp
                                    )
                                }
                            }
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
                            Text(
                                startStr,
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Wake up", color = Color.Gray, fontSize = 12.sp)
                            Text(
                                endStr,
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // 3. Info Text if not editable
                if (!viewModel.isEditable) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2E)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(16.dp)) {
                                Icon(
                                    Icons.Default.Info, contentDescription = null, tint = Color.Gray
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    "This sleep session was recorded by ${viewModel.sourceAppName}. Please use that app to edit details.",
                                    color = Color.Gray,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }

                item {
                    Text(
                        "Sleep Stages",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(12.dp))

                    if (viewModel.sleepStages.isNotEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = cardColor),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                val totalDurationMin =
                                    Duration.between(record.startTime, record.endTime).toMinutes()

                                // Group stages by type
                                val stagesByType = viewModel.sleepStages.groupBy { it.stage }

                                StageRow(
                                    "Deep",
                                    stagesByType[SleepSessionRecord.STAGE_TYPE_DEEP],
                                    totalDurationMin,
                                    Color(0xFF3F51B5)
                                )
                                Spacer(Modifier.height(12.dp))
                                StageRow(
                                    "Light",
                                    stagesByType[SleepSessionRecord.STAGE_TYPE_LIGHT],
                                    totalDurationMin,
                                    Color(0xFF03A9F4)
                                )
                                Spacer(Modifier.height(12.dp))
                                StageRow(
                                    "REM",
                                    stagesByType[SleepSessionRecord.STAGE_TYPE_REM],
                                    totalDurationMin,
                                    Color(0xFF9C27B0)
                                )
                                Spacer(Modifier.height(12.dp))
                                StageRow(
                                    "Awake",
                                    stagesByType[SleepSessionRecord.STAGE_TYPE_AWAKE],
                                    totalDurationMin,
                                    Color(0xFFFF9800)
                                )
                            }
                        }
                    } else {
                        // Empty State for stages
                        Card(
                            colors = CardDefaults.cardColors(containerColor = cardColor),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Info, contentDescription = null, tint = Color.Gray
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    "No detailed stages available.",
                                    color = Color.Gray,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }

                // disaply star rating
                item {
                    Text(
                        "Rating",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val infiniteTransition = rememberInfiniteTransition(label = "shimmer")

                        repeat(5) { index ->
                            val starIndex = index + 1
                            // If rating is null or 0, isFilled is always false
                            val isFilled = (record.rating ?: 0) >= starIndex

                            // Delicate scale animation (1.0 to 1.08 is very subtle)
                            val scale by infiniteTransition.animateFloat(
                                initialValue = 1f,
                                targetValue = 1.1f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(
                                        1200, delayMillis = index * 150, easing = EaseInOutSine
                                    ), repeatMode = RepeatMode.Reverse
                                ),
                                label = "scale"
                            )

                            // Delicate alpha shimmer
                            val alpha by infiniteTransition.animateFloat(
                                initialValue = 0.5f,
                                targetValue = 1f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(
                                        1200, delayMillis = index * 150, easing = EaseInOutSine
                                    ), repeatMode = RepeatMode.Reverse
                                ),
                                label = "alpha"
                            )

                            Icon(
                                imageVector = if (isFilled) Icons.Filled.Star else Icons.Outlined.Star,
                                contentDescription = null,
                                // Gold if filled, Grey if empty
                                tint = if (isFilled) Color(0xFFFFC107) else Color.Gray.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .size(38.dp)
                                    .padding(horizontal = 6.dp)
                                    .graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                        this.alpha = alpha
                                    })
                        }
                    }
                }
// 5. Notes Section
                item {
                    Text(
                        "Notes",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(8.dp))

                    if (!record.notes.isNullOrBlank()) {
                        Text(
                            text = record.notes,
                            color = Color(0xFFE0E0E0),
                            fontSize = 16.sp,
                            lineHeight = 24.sp
                        )
                    } else {
                        // Subtle empty state
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "No note for this session",
                                color = Color.Gray.copy(alpha = 0.6f),
                                fontSize = 14.sp,
                                style = androidx.compose.ui.text.TextStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(80.dp))

                }
            }
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Sleep?") },
                text = { Text("This will remove the record from Snorly and Health Connect.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteSession(onSuccess = onDeleteSuccess)
                            showDeleteDialog = false
                        }) {
                        Text("Delete", color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel")
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface,
            )
        }
    }
}

@Composable
fun StageRow(
    label: String, stages: List<SleepSessionRecord.Stage>?, totalMin: Long, color: Color
) {
    val durationMin = stages?.sumOf { Duration.between(it.startTime, it.endTime).toMinutes() } ?: 0
    val percentage = if (totalMin > 0) durationMin.toFloat() / totalMin else 0f

    val hours = durationMin / 60
    val mins = durationMin % 60
    val timeStr = if (hours > 0) "${hours}h ${mins}m" else "${mins}m"

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(timeStr, color = Color.Gray, fontSize = 14.sp)
        }
        Spacer(Modifier.height(6.dp))
        // Progress Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color(0xFF2C2C2E)) // Track color
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(percentage) // Fill based on percentage
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(3.dp))
                    .background(color)
            )
        }
    }
}