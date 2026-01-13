package com.example.snorly.feature.alarm.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp

@Composable
fun CascadingIcon(
    imageVector: androidx.compose.ui.graphics.vector.ImageVector,
    isActive: Boolean,
    index: Int // 0, 1, 2... determines delay
) {
    // 1. Color Animation (Grey -> Primary)
    val color by animateColorAsState(
        targetValue = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        animationSpec = tween(durationMillis = 300, delayMillis = index * 100), // Staggered delay
        label = "iconColor"
    )

    // 2. Scale Animation (Pop effect)
    // When becoming active, it scales 0.5 -> 1.2 -> 1.0
    val scale by animateFloatAsState(
        targetValue = if (isActive) 1f else 0.9f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "iconScale"
    )

    Icon(
        imageVector = imageVector,
        contentDescription = null,
        tint = color,
        modifier = Modifier
            .size(20.dp)
            .scale(scale)
    )
}