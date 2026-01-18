package com.example.snorly.feature.alarm.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun SoundWaveAnimation(
    color: androidx.compose.ui.graphics.Color,
    barCount: Int = 3,
    maxHeight: Dp = 18.dp,
    minHeight: Dp = 6.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sound_wave")

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier.height(24.dp) // Fixed container height
    ) {
        repeat(barCount) { index ->
            // Create a staggered animation for each bar
            val height by infiniteTransition.animateValue(
                initialValue = minHeight,
                targetValue = maxHeight,
                typeConverter = Dp.VectorConverter,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 300,
                        easing = FastOutSlowInEasing,
                        // Stagger the bars slightly based on index
                        delayMillis = index * 100
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bar_$index"
            )

            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(height) // Animated height
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}