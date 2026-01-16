package com.example.snorly.feature.report

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
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
import java.util.Locale

@Composable
fun ReportScreen(viewModel: ReportViewModel) {
    val data = viewModel.weeklyGraphData
    val stats = viewModel.stats
    val comparison = viewModel.comparisonData
    val consistency = viewModel.consistencyScore

    val scrollState = rememberScrollState()

    val bg = Color.Black
    val cardBg = Color(0xFF1C1C1E)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        // 1. Header
        Text(
            "Sleep Report",
            color = Color.White,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text("Insights & Trends", color = Color.Gray, modifier = Modifier.padding(bottom = 24.dp))

        // 2. Sleep Score Card (Existing)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg)
        ) {
            Row(
                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Avg Sleep Score", color = Color.Gray, fontSize = 14.sp)
                    Text(
                        text = "${stats.avgScore}",
                        color = getScoreColor(stats.avgScore),
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(getScoreLabel(stats.avgScore), color = Color.White, fontWeight = FontWeight.Medium)
                }
                Box(
                    modifier = Modifier.size(80.dp).clip(CircleShape).background(getScoreColor(stats.avgScore).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.WbSunny, null, tint = getScoreColor(stats.avgScore), modifier = Modifier.size(40.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 3. The Graph (Existing)
        Text("Weekly Trends", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                if (data.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().height(180.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        val maxHours = data.maxOfOrNull { it.hours } ?: 8f
                        val safeMax = if (maxHours == 0f) 8f else maxHours
                        data.forEach { day ->
                            BarItem(dayName = day.dayName, value = day.hours, max = safeMax)
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

        // 30-Day Comparison
        if (comparison != null) {
            Text("Monthly Comparison", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg)
            ) {
                Row(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Vs. Previous 15 Days", color = Color.Gray, fontSize = 12.sp)
                        Spacer(Modifier.height(4.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (comparison.percentChange >= 0) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                contentDescription = null,
                                tint = if (comparison.percentChange >= 0) Color(0xFF4CAF50) else Color(0xFFFF5252),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "${abs(comparison.percentChange)}%",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Text(
                            text = if (comparison.percentChange >= 0) "More sleep on average" else "Less sleep on average",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }

                    // Small Divider
                    Box(Modifier.width(1.dp).height(40.dp).background(Color.Gray.copy(alpha=0.3f)))
                    Spacer(Modifier.width(16.dp))

                    Column(horizontalAlignment = Alignment.End) {
                        Text("Recent Avg", color = Color.Gray, fontSize = 12.sp)
                        Text(String.format(Locale.US, "%.1fh", comparison.recentAvgHours), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Text("Older Avg", color = Color.Gray, fontSize = 12.sp)
                        Text(String.format(Locale.US, "%.1fh", comparison.olderAvgHours), color = Color.Gray, fontSize = 16.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // 5. NEW: Consistency Score
        if (consistency != null) {
            Text("Sleep Consistency", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Schedule Adherence", color = Color.Gray, fontSize = 14.sp)
                        Text(consistency.label, color = consistency.color, fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.height(12.dp))

                    // Custom Progress Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF2C2C2E))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(consistency.score / 100f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(6.dp))
                                .background(consistency.color)
                        )
                    }

                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Measures how close you are to your 23:00 / 07:00 target times.",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // 6. Averages Grid (Existing)
        Text("Weekly Averages", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Sleep", stats.avgDuration, Icons.Default.Timer, Color(0xFF42A5F5), Modifier.weight(1f))
            StatCard("Bedtime", stats.avgBedtime, Icons.Default.Bedtime, Color(0xFF7E57C2), Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            StatCard("Wake Up", stats.avgWakeup, Icons.Default.AccessTime, Color(0xFFFFA726), Modifier.weight(1f))
            Spacer(modifier = Modifier.width(12.dp))
            Box(Modifier.weight(1f)) // Placeholder
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

// --- KEEPING YOUR EXISTING COMPONENTS ---
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
        val barHeightWeight = (value / max).coerceIn(0f, 1f)
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier.width(12.dp).weight(1f),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(fraction = if(barHeightWeight == 0f) 0.02f else barHeightWeight)
                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = if (value >= 7) listOf(Color(0xFF66BB6A), Color(0xFF43A047))
                            else listOf(Color(0xFFFFA726), Color(0xFFFB8C00))
                        )
                    )
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = dayName.take(1), color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

fun getScoreColor(score: Int): Color {
    return when {
        score >= 80 -> Color(0xFF4CAF50)
        score >= 60 -> Color(0xFFFFC107)
        else -> Color(0xFFFF5252)
    }
}

fun getScoreLabel(score: Int): String {
    return when {
        score >= 80 -> "Excellent"
        score >= 60 -> "Fair"
        else -> "Needs Work"
    }
}
// Helper for Absolute value used in Screen Logic (moved logic to ViewModel but just in case)
private fun abs(n: Int) = if (n < 0) -n else n