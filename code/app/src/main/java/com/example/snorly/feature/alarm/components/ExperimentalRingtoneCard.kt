package com.example.snorly.feature.alarm.components

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight

@Composable
fun OrganicShaderCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color1: Color,
    color2: Color,
    shaderCode: String,
    count: String? = null,
    onClick: () -> Unit
) {
    // 1. Interaction State
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // 2. Animations
    // Scale: 100% -> 95% on hold
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMedium),
        label = "scale"
    )

    // Corner Radius: 20dp (Soft Square) -> 100dp (Circle/Pill)
    // This creates the "Squircle" morph without complex shape libraries
    val cornerRadius by animateDpAsState(
        targetValue = if (isPressed) 100.dp else 24.dp,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessLow),
        label = "corner"
    )

    // 3. Container
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .scale(scale)
            .clip(RoundedCornerShape(cornerRadius)) // Animated Shape
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
    ) {
        // 4. Background (Shader vs Gradient)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ShaderBackground(shaderCode, color1, color2)
        } else {
            // Fallback: Elegant Gradient
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.linearGradient(listOf(color1, color2)))
            )
        }

        // 5. Content Overlay (Text & Icon)
        CardContent(title, subtitle, icon, count)
    }
}

@Composable
fun CardContent(title: String, subtitle: String, icon: ImageVector, count: String?) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Glassy Icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color.White.copy(0.15f), androidx.compose.foundation.shape.CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = Color.White, modifier = Modifier.size(22.dp))
            }

            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                null,
                tint = Color.White.copy(0.3f)
            )
        }

        // Bottom Row
        Column {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(Modifier.height(4.dp))
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(0.7f))

            if (count != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    count.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(0.5f),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@androidx.annotation.RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun ShaderBackground(shaderCode: String, c1: Color, c2: Color) {
    // We run time linearly forever. The shaders use sin/cos to loop themselves.
    // This prevents the "reset jump" at the end of an animation cycle.
    val infiniteTransition = rememberInfiniteTransition(label = "time")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1000f,
        animationSpec = infiniteRepeatable(tween(1000000, easing = LinearEasing)),
        label = "time"
    )

    Spacer(
        modifier = Modifier
            .fillMaxSize()
            .drawWithCache {
                val shader = RuntimeShader(shaderCode)
                val brush = ShaderBrush(shader)
                onDrawBehind {
                    shader.setFloatUniform("uResolution", size.width, size.height)
                    shader.setFloatUniform("uTime", time)
                    shader.setColorUniform("uColor1", c1.toArgb())
                    shader.setColorUniform("uColor2", c2.toArgb())

                    // Draw black base first to ensure colors pop against darkness
                    drawRect(Color.Black)
                    drawRect(brush)
                }
            }
    )
}