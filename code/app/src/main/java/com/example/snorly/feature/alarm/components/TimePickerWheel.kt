package com.example.snorly.feature.alarm.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun TimePickerWheel(
    hour: Int,
    minute: Int,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        // 1. The Highlight Capsule (Background)
        Box(
            modifier = Modifier
                .width(220.dp)
                .height(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF222222)) // Your Card2 color
        )

        // 2. The Wheels
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Hours
            InfiniteWheelPicker(
                width = 70.dp,
                itemHeight = 42.dp,
                items = (0..23).toList(),
                initialValue = hour,
                onItemSelected = onHourChange
            )

            // Colon Separator
            Text(
                text = ":",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // Minutes
            InfiniteWheelPicker(
                width = 70.dp,
                itemHeight = 42.dp,
                items = (0..59).toList(),
                initialValue = minute,
                onItemSelected = onMinuteChange
            )
        }
    }
}
