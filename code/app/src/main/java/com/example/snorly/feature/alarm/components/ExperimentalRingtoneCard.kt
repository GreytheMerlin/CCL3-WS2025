package com.example.snorly.feature.alarm.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AuroraRingtoneCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    count: String? = null,
    onClick: () -> Unit
) {
    // 1. Interaction State (Scale on Press)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.96f else 1f, label = "scale")

    // 2. The "Breathing" Animation Loop
    val infiniteTransition = rememberInfiniteTransition(label = "breathing")

    // Animate the "Soul" position (A figure-8 movement)
    val t by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(tween(8000, easing = LinearEasing)),
        label = "movement"
    )

    // Animate the Glow Intensity (Pulse)
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.6f, targetValue = 0.8f,
        animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .scale(scale)
            .clip(RoundedCornerShape(24.dp))
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
            .drawBehind {
                // A. DARK BACKGROUND BASE
                drawRect(Color(0xFF151517))

                // B. THE "SOUL" (Moving Glow)
                // Calculate position based on time `t`
                val centerX = size.width / 2
                val centerY = size.height / 2
                val offsetX = cos(t) * (size.width * 0.3f) // Move 30% of width
                val offsetY = sin(t * 0.7f) * (size.height * 0.2f) // Move 20% of height

                // Draw the glow blob
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            accentColor.copy(alpha = if(isPressed) 0.8f else 0.5f), // Brighter when pressed
                            Color.Transparent
                        ),
                        center = Offset(centerX + offsetX, centerY + offsetY),
                        radius = size.width * if(isPressed) 0.9f else pulse // Expands when pressed
                    ),
                    center = Offset(centerX + offsetX, centerY + offsetY),
                    radius = size.width * 0.8f
                )

                // C. THE GLASS LAYER (Frosted Overlay)
                // We draw a semi-transparent dark layer on top to make the glow look "deep"
                drawRect(Color(0xFF101012).copy(alpha = 0.65f))
            }
            // Add a subtle border for definition
            .border(1.dp, Color.White.copy(0.08f), RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // --- Header ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Icon in a Glass Bubble
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.White.copy(0.08f), androidx.compose.foundation.shape.CircleShape)
                        .border(1.dp, Color.White.copy(0.1f), androidx.compose.foundation.shape.CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor, // Icon glows with the category color
                        modifier = Modifier.size(22.dp)
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color.White.copy(0.2f)
                )
            }

            // --- Footer ---
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 0.5.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(0.6f),
                    maxLines = 1
                )

                if (count != null) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = count.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor.copy(alpha = 0.8f), // Colored text for count
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}