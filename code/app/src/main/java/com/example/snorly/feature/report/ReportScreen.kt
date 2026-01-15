package com.example.snorly.feature.report

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ReportScreen(viewModel: ReportViewModel) {
    val data = viewModel.weeklyGraphData
    val stats = viewModel.stats
    val scrollState = rememberScrollState()

    // Background Color
    val bg = Color.Black
    val cardBg = Color(0xFF1C1C1E)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .padding(16.dp)
            .verticalScroll(scrollState) // Allow scrolling for small screens
    ) {
        // 1. Header
        Text(
            "Weekly Report",
            color = Color.White,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text("Last 7 Days", color = Color.Gray, modifier = Modifier.padding(bottom = 24.dp))

        // 2. Sleep Score Card (Big Header)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg)
        ) {
            Row(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Sleep Score", color = Color.Gray, fontSize = 14.sp)
                    Text(
                        text = "${stats.avgScore}",
                        color = getScoreColor(stats.avgScore),
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = getScoreLabel(stats.avgScore),
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Visual Circle (Placeholder for a Ring Chart)
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(getScoreColor(stats.avgScore).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WbSunny,
                        contentDescription = null,
                        tint = getScoreColor(stats.avgScore),
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 3. The Graph
        Text("Trends", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                if (data.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        val maxHours = data.maxOfOrNull { it.hours } ?: 8f
                        val safeMax = if (maxHours == 0f) 8f else maxHours

                        data.forEach { day ->
                            BarItem(
                                dayName = day.dayName,
                                value = day.hours,
                                max = safeMax
                            )
                        }
                    }
                } else {
                    Box(modifier = Modifier.height(150.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No data for graph", color = Color.Gray)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 4. Averages Grid
        Text("Averages", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // Avg Duration
            StatCard(
                title = "Sleep",
                value = stats.avgDurationStr,
                icon = Icons.Default.Timer,
                color = Color(0xFF42A5F5), // Blue
                modifier = Modifier.weight(1f)
            )
            // Avg Bedtime
            StatCard(
                title = "Bedtime",
                value = stats.avgBedtime,
                icon = Icons.Default.Bedtime,
                color = Color(0xFF7E57C2), // Purple
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            // Avg Wakeup (Full Width or half)
            StatCard(
                title = "Wake Up",
                value = stats.avgWakeup,
                icon = Icons.Default.AccessTime,
                color = Color(0xFFFFA726), // Orange
                modifier = Modifier.weight(1f)
            )
            // Empty spacer for grid alignment or add another stat here later
            Spacer(modifier = Modifier.width(12.dp))
            Box(Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(80.dp)) // Bottom padding
    }
}

// --- COMPONENTS ---

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(110.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(icon, contentDescription = null, tint = color)
            Column {
                Text(value, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(title, color = Color.Gray, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun BarItem(dayName: String, value: Float, max: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
        modifier = Modifier.fillMaxHeight()
    ) {
        // Calculate height fraction
        val barHeightWeight = (value / max).coerceIn(0f, 1f)

        // Value Label (Optional: only show if relevant)
        // if (value > 0) Text(String.format("%.0f", value), fontSize = 10.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(8.dp))

        // Bar Container
        Box(
            modifier = Modifier
                .width(12.dp) // Thinner bars look more modern
                .weight(1f), // Take available height
            contentAlignment = Alignment.BottomCenter
        ) {
            // The Actual Colored Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(fraction = if(barHeightWeight == 0f) 0.02f else barHeightWeight)
                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = if (value >= 7)
                                listOf(Color(0xFF66BB6A), Color(0xFF43A047)) // Green Gradient
                            else
                                listOf(Color(0xFFFFA726), Color(0xFFFB8C00)) // Orange Gradient
                        )
                    )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Axis Label
        Text(text = dayName.take(1), color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

// --- HELPERS ---

fun getScoreColor(score: Int): Color {
    return when {
        score >= 80 -> Color(0xFF4CAF50) // Green
        score >= 60 -> Color(0xFFFFC107) // Yellow
        else -> Color(0xFFFF5252)        // Red
    }
}

fun getScoreLabel(score: Int): String {
    return when {
        score >= 80 -> "Excellent"
        score >= 60 -> "Fair"
        else -> "Needs Work"
    }
}