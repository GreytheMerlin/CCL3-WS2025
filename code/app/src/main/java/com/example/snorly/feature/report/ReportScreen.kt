package com.example.snorly.feature.report

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box // <--- Import Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape // <--- Import Shape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ReportScreen(viewModel: ReportViewModel) {
    val data = viewModel.weeklySleepData

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Weekly Sleep", style = MaterialTheme.typography.headlineMedium)
        Text("Last 7 Days", color = Color.Gray)

        Spacer(modifier = Modifier.height(32.dp))

        if (data.isNotEmpty()) {
            // Container for the Chart
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
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
            Text("Loading data...", modifier = Modifier.padding(top=20.dp))
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

        if (value > 0) {
            Text(
                text = String.format("%.1f", value),
                fontSize = 10.sp,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .width(20.dp)
                .height(150.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    // Safe check for 0 height to avoid crash or invisible bar
                    .fillMaxHeight(fraction = if(barHeightWeight == 0f) 0.01f else barHeightWeight)
                    .align(Alignment.BottomCenter)
                    .background(
                        color = if (value >= 7) Color(0xFF4CAF50) else Color(0xFFFF9800),
                        shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                    )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(text = dayName, fontWeight = FontWeight.Bold)
    }
}