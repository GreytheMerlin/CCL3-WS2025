package com.example.snorly.feature.sleep.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RowScope.DailyBar(
    dayName: String,
    value: Float,    // Hours (e.g., 7.5)
    maxValue: Float  // Max graph height (e.g., 10.0)
) {
    // Calculate height percentage (cap at 100%)
    val barHeightWeight = (value / maxValue).coerceIn(0f, 1f)

    Column(
        modifier = Modifier
            .weight(1f) // Distribute width evenly in the Row
            .fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. The Empty Space ABOVE the bar (pushes bar down)
        Spacer(modifier = Modifier.weight(1f - barHeightWeight))

        // 2. The Colored Bar
        Box(
            modifier = Modifier
                .fillMaxWidth(0.6f) // Make bars slightly thinner than full slot width
                .weight(if (barHeightWeight == 0f) 0.01f else barHeightWeight) // Ensure 0-height bars are invisible
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = if (value >= 7f) {
                            // Good Sleep (Green Gradient)
                            listOf(Color(0xFF66BB6A), Color(0xFF43A047))
                        } else {
                            // Poor Sleep (Orange/Red Gradient)
                            listOf(Color(0xFFFF7043), Color(0xFFD84315))
                        }
                    )
                )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 3. Day Label (e.g. "Mon")
        Text(
            text = dayName.take(1), // Show only first letter (M, T, W...)
            color = Color.Gray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}