package com.example.snorly.feature.alarm.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.snorly.feature.alarm.Alarm
import com.example.snorly.R
import com.example.snorly.feature.challenges.model.ChallengeDataSource

@Composable
fun AlarmCard(
    alarm: Alarm,
    dayText: String,
    selectionMode: Boolean,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onSelectToggle: () -> Unit,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val timeColor by animateColorAsState(
        targetValue = if (alarm.isActive) MaterialTheme.colorScheme.onBackground
        else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f), label = "timeColor"
    )

    val activeChallengeIcons = remember(alarm.challenge) {
        ChallengeDataSource.allChallenges.filter { it.id in alarm.challenge }
    }

    val borderColor by animateColorAsState(
        targetValue = if (alarm.isActive) MaterialTheme.colorScheme.outline
        else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), label = "borderColor"
    )

    val cardScale by animateFloatAsState(
        targetValue = if (alarm.isActive) 1.01f else 1f,
        animationSpec = spring(dampingRatio = 0.6f),
        label = "cardScale"
    )

    // calc Time
    val timeUntil = remember(alarm.time, alarm.isActive) {
        if (alarm.isActive) calculateTimeUntil(alarm.time) else ""
    }

    Card(
        modifier = modifier
            .scale(cardScale)
            .combinedClickable(
                onClick = onClick, onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(width = 1.dp, color = borderColor)
    ) {
        Row(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Middle content
            Column(
                modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = alarm.time,
                        lineHeight = 32.sp,
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Normal,
                            fontSize = 32.sp,
                            letterSpacing = (-1).sp
                        ),
                        color = timeColor
                    )
                    if (alarm.isActive && timeUntil.isNotEmpty()) {
                        Text(
                            text = "in $timeUntil",
                            modifier = Modifier.padding(start = 8.dp, bottom = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    val displayText = remember(dayText, alarm.label) {
                        buildString {
                            append(dayText.ifBlank { "Once" })
                            if (!alarm.label.isNullOrBlank()) {
                                append(" • ")
                                append(alarm.label)
                            }
                        }
                    }

                    Text(
                        text = displayText,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (activeChallengeIcons.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        activeChallengeIcons.forEachIndexed { index, challenge ->
                            CascadingIcon(
                                imageVector = challenge.icon,
                                isActive = alarm.isActive,
                                index = index // Determines the staggered pop delay
                            )
                        }
                    }
                }
            }

            // RIGHT: switch (hidden in selection mode)
            if (!selectionMode) {
                SnorlyMorphSwitch(
                    checked = alarm.isActive,
                    onCheckedChange = onToggle,
                    modifier = Modifier.scale(1f)
                )
            }

            // LEFT: rounded select button (only in selection mode)
            if (selectionMode) {
                Surface(
                    onClick = onSelectToggle,
                    shape = RoundedCornerShape(999.dp),
                    color = if (selected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .padding(end = 10.dp)
                        .size(32.dp)

                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (selected) {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Outlined.Check,
                                contentDescription = "Selected",
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
fun calculateTimeUntil(alarmTime: String): String {
    return try {
        val now = java.time.LocalTime.now()
        val parts = alarmTime.split(":")
        val alarm = java.time.LocalTime.of(parts[0].toInt(), parts[1].toInt())

        var duration = java.time.Duration.between(now, alarm)
        if (duration.isNegative) {
            duration = duration.plusDays(1)
        }

        val hours = duration.toHours()
        val minutes = duration.toMinutes() % 60

        if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
    } catch (e: Exception) {
        ""
    }
}

