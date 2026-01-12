package com.example.snorly.feature.alarm.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
    // 1. Subtle Color States
    // When inactive, the time text dims slightly to visual priority to active alarms
    val timeColor by animateColorAsState(
        targetValue = if (alarm.isActive) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
        label = "timeColor"
    )

    // Border glows slightly when active
    val borderColor by animateColorAsState(
        targetValue = if (alarm.isActive) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
        label = "borderColor"
    )

    val cardScale by animateFloatAsState(
        targetValue = if (alarm.isActive) 1.01f else 1f,
        animationSpec = spring(dampingRatio = 0.6f),
        label = "cardScale"
    )

    Card(
        modifier = modifier.scale(cardScale)
//            .width(380.dp) // Kept your original size
//            .height(100.dp)
        ,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(width = 1.dp, color = borderColor)
    ) {
        Row(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Side: Time, Info, Icons
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {

                // 1. Time Display
                Text(
                    text = alarm.time,
                    lineHeight = 32.sp,
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Normal,
                        fontSize = 32.sp,
                        letterSpacing = (-1).sp
                    ),
                    color = timeColor // Animated color
                )

                Spacer(modifier = Modifier.height(4.dp)) // Slight breathing room

                // 2. Details Row
                Text(
                    text = "${alarm.pattern} • ${alarm.remaining}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // I moved the icons down slightly or you can keep them inline if you prefer.
                // Based on your original code, you had them below the text.
                Spacer(modifier = Modifier.height(2.dp))

                // 3. Feature Icons (Cascading Animation)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CascadingIcon(Icons.Outlined.DateRange, alarm.isActive, 0)
                    CascadingIcon(Icons.Outlined.Info, alarm.isActive, 1) // Representing "Smart Wake"
                    CascadingIcon(Icons.Filled.Notifications, alarm.isActive, 2)
                }
            }

            // Right Side: Custom Switch
            SnorlyMorphSwitch(
                checked = alarm.isActive,
                onCheckedChange = onToggle,
                modifier = Modifier.scale(1f)
            )
        }
    }
}
