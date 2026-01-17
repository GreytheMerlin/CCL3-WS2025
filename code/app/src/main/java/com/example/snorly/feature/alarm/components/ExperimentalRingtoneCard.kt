package com.example.snorly.feature.alarm.components

import android.graphics.Matrix
import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.graphics.shapes.*
import com.example.snorly.core.ui.shader.ShaderLibrary
import com.example.snorly.core.ui.shapes.rememberRoundedRect
import com.example.snorly.core.ui.shapes.rememberRoundedStar

// -----------------------------------------------------------------------------
// MAIN COMPONENT
// -----------------------------------------------------------------------------
@Composable
fun ExperimentalRingtoneCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    colorStart: Color,
    colorEnd: Color,
    count: String? = null,
    shaderCode: String = ShaderLibrary.NEBULA,
    // 1. CONTROL SHAPES: Defaults to Square -> Circle
    shapeStart: RoundedPolygon = rememberRoundedRect(),
    shapeEnd: RoundedPolygon = rememberRoundedStar(),
    onClick: () -> Unit
) {
    // Interaction State
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // 2. ANIMATIONS
    // Morph Progress: 0f (Start Shape) -> 1f (End Shape)
    val morphProgress by animateFloatAsState(
        targetValue = if (isPressed) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessLow),
        label = "morph"
    )

    // Scale: Shrink slightly on press to keep content visible during morph
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.4f, stiffness = Spring.StiffnessMedium),
        label = "scale"
    )

    // Shader Time Loop
    val infiniteTransition = rememberInfiniteTransition(label = "time")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 100f,
        animationSpec = infiniteRepeatable(tween(20000, easing = LinearEasing)),
        label = "time"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
            .drawWithCache {
                // 3. MORPH LOGIC (Works on all APIs)
                val morph = Morph(shapeStart, shapeEnd)
                val path = morph.toComposePath(progress = morphProgress, size = size)

                // 4. BACKGROUND LOGIC (Shader on API 33+, Gradient on older)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val shader = RuntimeShader(shaderCode)
                    val brush = ShaderBrush(shader)
                    onDrawBehind {
                        shader.setFloatUniform("uResolution", size.width, size.height)
                        shader.setFloatUniform("uTime", time)
                        shader.setSafeColorUniform("uColorStart", colorStart.toArgb())
                        shader.setSafeColorUniform("uColorEnd", colorEnd.toArgb())

                        clipPath(path) { drawRect(brush) }
                    }
                } else {
                    // Fallback: Standard Gradient
                    val brush = Brush.linearGradient(listOf(colorStart, colorEnd))
                    onDrawBehind {
                        clipPath(path) { drawRect(brush) }
                    }
                }
            }
    ) {
        // 5. CONTENT FITTING
        // We add extra padding when pressed (morphed) so text doesn't get cut by corners
        val contentPadding by animateDpAsState(targetValue = if (isPressed) 32.dp else 20.dp)

        CardContentOverlay(
            title = title,
            subtitle = subtitle,
            icon = icon,
            count = count,
            isPressed = isPressed,
            padding = contentPadding
        )
    }
}

// -----------------------------------------------------------------------------
// CONTENT OVERLAY
// -----------------------------------------------------------------------------
@Composable
private fun CardContentOverlay(
    title: String,
    subtitle: String,
    icon: ImageVector,
    count: String?,
    isPressed: Boolean,
    padding: androidx.compose.ui.unit.Dp
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding), // Dynamic padding ensures fit
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color.White.copy(0.15f), androidx.compose.foundation.shape.CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            AnimatedVisibility(visible = !isPressed) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color.White.copy(0.4f)
                )
            }
        }

        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                letterSpacing = 0.5.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f),
                maxLines = 1 // Ensure text doesn't overflow vertically
            )

            if (count != null) {
                Spacer(Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .background(
                            Color.Black.copy(0.2f),
                            androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = count.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// UTILS
// -----------------------------------------------------------------------------
// Helper to fix the API 33 Crash
fun RuntimeShader.setSafeColorUniform(name: String, colorArgb: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this.setColorUniform(name, colorArgb)
    }
}

// Extension to map Shape to Path
fun Morph.toComposePath(progress: Float, size: Size): Path {
    val androidPath = android.graphics.Path()
    this.toPath(progress, androidPath)

    val bounds = android.graphics.RectF()
    androidPath.computeBounds(bounds, true)

    val matrix = Matrix()
    matrix.setRectToRect(bounds, android.graphics.RectF(0f, 0f, size.width, size.height), Matrix.ScaleToFit.FILL)
    androidPath.transform(matrix)

    return androidPath.asComposePath()
}