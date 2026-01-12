package com.example.snorly.feature.alarm.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.snorly.feature.alarm.Alarm

@Composable
fun AlarmCard(
    alarm: Alarm,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.width(380.dp).height(100.dp),
        shape = RoundedCornerShape(16.dp), // High rounded corners like image
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface // 0xFF0F0F0F
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f) // Subtle border
        )
    ) {
        Row(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Side: Time, Info, Icons
            Column(modifier = Modifier.weight(1f)) {

                // 1. Time Display
                Text(
                    text = alarm.time,
                    lineHeight = 32.sp,
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Normal,
                        fontSize = 32.sp,
                        letterSpacing = (-1).sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )

                // 2. Details Row (Daily • In 7h...)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${alarm.pattern} • ${alarm.remaining} • ${alarm.label}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant // Muted Grey
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))


                // 3. Feature Icons (Sun, Brain, Vibrate)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FeatureIcon(Icons.Outlined.Build)
                    FeatureIcon(Icons.Outlined.DateRange)
                    FeatureIcon(Icons.Outlined.ThumbUp)
                }
            }

            // Right Side: Custom Switch
//            SnorlySwitch(
//                checked = alarm.isActive,
//                onCheckedChange = onToggle
//            )
            SnorlyMorphSwitch(
                checked = alarm.isActive,
                onCheckedChange = onToggle,
                // Optional: Make it slightly larger if you want to emphasize the shape
                modifier = Modifier.scale(1f)
            )
        }
    }
}
