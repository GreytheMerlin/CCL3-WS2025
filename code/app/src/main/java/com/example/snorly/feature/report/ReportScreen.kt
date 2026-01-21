package com.example.snorly.feature.report

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Rule
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.example.snorly.core.common.components.MainTopBar
import com.example.snorly.feature.sleep.util.SleepScoreUtils.getScoreColor
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

    Scaffold(
        containerColor = Color.Black, topBar = {
            MainTopBar(
                title = "Sleep Report"
                // No actionIcon needed here based on your code
            )
        }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(bg)
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState)
        ) {


            // 2. Sleep Score Card (Existing)
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
                        Text("Avg Sleep Score", color = Color.Gray, fontSize = 14.sp)
                        Text(
                            text = "${stats.avgScore}",
                            color = getScoreColor(stats.avgScore),
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            getScoreLabel(stats.avgScore),
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(getScoreColor(stats.avgScore).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.WbSunny,
                            null,
                            tint = getScoreColor(stats.avgScore),
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. The Graph (Existing)
            Text(
                "Weekly Trends", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold
            )
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
                                BarItem(dayName = day.dayName, value = day.hours, max = safeMax)
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .height(150.dp)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No data for graph", color = Color.Gray)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 4. Comparison (UPDATED: 7 vs 7 Days, Quality + Duration)
            if (comparison != null) {
                Text(
                    "Weekly Comparison",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {

                        // --- DURATION COMPARISON ROW ---
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left: Trend Icon
                            val isMoreTime = comparison.diffMinutes >= 0
                            Icon(
                                imageVector = if (isMoreTime) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                contentDescription = null,
                                tint = if (isMoreTime) Color(0xFF4CAF50) else Color(0xFFFF5252),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(8.dp))

                            // Center: Description
                            Column(Modifier.weight(1f)) {
                                Text("Sleep Duration", color = Color.Gray, fontSize = 12.sp)
                                Text(
                                    text = "${abs(comparison.diffMinutes)}m ${if (isMoreTime) "more" else "less"}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            // Right: Values
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "This Week: ${
                                        String.format(
                                            Locale.US, "%.1fh", comparison.recentAvgHours
                                        )
                                    }", color = Color.White, fontSize = 12.sp
                                )
                                Text(
                                    "Last Week: ${
                                        String.format(
                                            Locale.US, "%.1fh", comparison.olderAvgHours
                                        )
                                    }", color = Color.Gray, fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                        Divider(color = Color.White.copy(alpha = 0.1f))
                        Spacer(Modifier.height(16.dp))

                        // --- QUALITY COMPARISON ROW ---
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left: Trend Icon
                            val isBetterScore = comparison.diffScore >= 0
                            Icon(
                                imageVector = if (isBetterScore) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                contentDescription = null,
                                tint = if (isBetterScore) Color(0xFF4CAF50) else Color(0xFFFF5252),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(8.dp))

                            // Center: Description
                            Column(Modifier.weight(1f)) {
                                Text("Sleep Quality", color = Color.Gray, fontSize = 12.sp)
                                Text(
                                    text = "${abs(comparison.diffScore)} pts ${if (isBetterScore) "better" else "worse"}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            // Right: Values
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "This Week: ${comparison.recentAvgScore}",
                                    color = Color.White,
                                    fontSize = 12.sp
                                )
                                Text(
                                    "Last Week: ${comparison.olderAvgScore}",
                                    color = Color.Gray,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // 5. Consistency Score
            if (consistency != null) {
                Text(
                    "Sleep Consistency",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
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
                            Text(
                                consistency.label,
                                color = consistency.color,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        // --- BEDTIME ---
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Bedtime", color = Color.White, fontSize = 12.sp)
                            // Show Offset Minutes
                            Text(
                                "${consistency.avgBedtimeOffsetMin}m avg offset",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF2C2C2E))
                        ) {
                            // Max bad deviation is 180min. Closer to 0 deviation = Fuller Bar.
                            val bedProgress =
                                (1f - (consistency.avgBedtimeOffsetMin / 180f)).coerceIn(0.05f, 1f)

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(bedProgress)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(consistency.bedtimeColor) // Individual Color
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        // --- WAKE UP ---
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Wake Up", color = Color.White, fontSize = 12.sp)
                            // Show Offset Minutes
                            Text(
                                "${consistency.avgWakeupOffsetMin}m avg offset",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF2C2C2E))
                        ) {
                            // Max bad deviation is 180min. Closer to 0 deviation = Fuller Bar.
                            val wakeProgress =
                                (1f - (consistency.avgWakeupOffsetMin / 180f)).coerceIn(0.05f, 1f)

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(wakeProgress)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(consistency.wakeupColor) // Individual Color
                            )
                        }

                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "Target: ${consistency.targetBedFormatted} Bed / ${consistency.targetWakeFormatted} Wake",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // 6. Averages Grid (Existing)
            Text(
                "Weekly Averages",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    "Sleep",
                    stats.avgDuration,
                    Icons.Default.Timer,
                    Color(0xFF42A5F5),
                    Modifier.weight(1f)
                )
                StatCard(
                    "Bedtime",
                    stats.avgBedtime,
                    Icons.Default.Bedtime,
                    Color(0xFF7E57C2),
                    Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                StatCard(
                    "Wake Up",
                    stats.avgWakeup,
                    Icons.Default.AccessTime,
                    Color(0xFFFFA726),
                    Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                StatCard(
                    title = "Consistency",
                    value = if (consistency != null) "${consistency.overallScore}/100" else "-",
                    icon = Icons.AutoMirrored.Filled.Rule,
                    color = if (consistency != null) consistency.color else Color.Gray,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

// --- KEEPING YOUR EXISTING COMPONENTS ---
@Composable
fun StatCard(
    title: String, value: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(110.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
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
            modifier = Modifier
                .width(12.dp)
                .weight(1f), contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(fraction = if (barHeightWeight == 0f) 0.02f else barHeightWeight)
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
        Text(
            text = dayName.take(1),
            color = Color.Gray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
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