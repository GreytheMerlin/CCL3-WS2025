package com.example.snorly.feature.sleep.components

import android.R.attr.onClick
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.snorly.feature.sleep.model.SleepDayUiModel

@Composable
fun SleepHistoryItem(data: SleepDayUiModel, onClick: () -> Unit = {}) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Date and Quality Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(data.dateLabel, color = Color.White, fontWeight = FontWeight.Medium)

                // Quality Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(data.qualityColor.copy(alpha = 0.2f)) // Dim background
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        data.qualityLabel,
                        color = data.qualityColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Divider(color = Color(0xFF2C2C2E), thickness = 1.dp)
            Spacer(Modifier.height(16.dp))

            // Times Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Bedtime
                SleepTimeColumn(
                    icon = Icons.Outlined.Bedtime,
                    time = data.bedtime,
                    label = "Bedtime"
                )

                // Wakeup
                SleepTimeColumn(
                    icon = Icons.Filled.WbSunny,
                    time = data.wakeup,
                    label = "Wake up"
                )

                // Duration
                SleepTimeColumn(
                    icon = Icons.Filled.Timer,
                    time = data.wakeup,
                    label = "Duration"
                )
            }
        }
    }
}