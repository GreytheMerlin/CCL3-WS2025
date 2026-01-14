package com.example.snorly.feature.alarm.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.star
import androidx.graphics.shapes.toPath
import com.example.snorly.core.ui.theme.Space
import kotlinx.coroutines.launch

@Composable
fun SnorlyMorphSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    // 1. Haptics & Interaction
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // 2. Colors with "Flash" logic could go here, but let's keep it simple & classy
    val trackColor by animateColorAsState(
        targetValue = if (checked) Space else MaterialTheme.colorScheme.surfaceVariant,
        label = "trackColor",
        animationSpec = tween(300)
    )
    val thumbColor by animateColorAsState(
        targetValue = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "thumbColor"
    )

    // 3. Morph Shapes
    val morph = remember {
        val starShape = RoundedPolygon.star(
            numVerticesPerRadius = 5,
            innerRadius = 0.45f, // Slightly fatter for cuteness
            rounding = CornerRounding(radius = 0.15f)
        )
        val circleShape = RoundedPolygon(
            numVertices = 10,
            rounding = CornerRounding(radius = 1f)
        )
        Morph(start = circleShape, end = starShape)
    }

    // 4. PHYSICS ANIMATIONS ----------------------------------------------------

    // A. Morph: Smooth transition
    val morphProgress by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessLow),
        label = "morph"
    )

    // B. Position: Bouncy overshoot (The "Spring" feel)
    val thumbPositionProgress by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = spring(
            dampingRatio = 0.55f, // Low damping = High Bounce
            stiffness = Spring.StiffnessMedium
        ),
        label = "position"
    )

    // C. Rotation: Spin the icon as it travels! (0 to 360)
    val rotation by animateFloatAsState(
        targetValue = if (checked) 270f else 0f,
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = Spring.StiffnessLow
        ),
        label = "rotation"
    )

    // D. Scale Pulse: The thumb grows when active, shrinks when pressed
    val targetScale = if (isPressed) 0.85f else if (checked) 1.1f else 1.0f
    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(dampingRatio = 0.4f, stiffness = Spring.StiffnessMediumLow), // Very bouncy scale
        label = "scale"
    )

    // --------------------------------------------------------------------------

    // Layout Constants
    val switchWidth = 52.dp // Wider track for more travel fun
    val switchHeight = 32.dp
    val thumbSize = 30.dp
    val padding = 4.dp

    val density = LocalDensity.current
    val totalTravelDistance = with(density) { (switchWidth - thumbSize - (padding * 2)).toPx() }
    val currentTranslation = totalTravelDistance * thumbPositionProgress

    val pathizer = remember { android.graphics.Path() }

    // Haptic Trigger on change
    LaunchedEffect(checked) {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    Box(
        modifier = modifier
            .size(width = switchWidth, height = switchHeight)
            .clip(RoundedCornerShape(100))
            .background(trackColor)
            .clickable(
                indication = null,
                interactionSource = interactionSource
            ) {
                onCheckedChange(!checked)
            }
            .padding(padding),
        contentAlignment = Alignment.CenterStart
    ) {
        Canvas(
            modifier = Modifier
                .size(thumbSize)
                .graphicsLayer {
                    translationX = currentTranslation
                    rotationZ = rotation // <--- SPIN IT
                    scaleX = scale       // <--- PULSE IT
                    scaleY = scale
                    shadowElevation = 8.dp.toPx()
                    shape = CircleShape
                    clip = false
                }
        ) {
            morph.toPath(progress = morphProgress, path = pathizer)

            translate(left = size.width / 2, top = size.height / 2) {
                // We scale the path slightly down (0.5) so the rotation doesn't clip corners
                scale(scale = size.width / 2, pivot = Offset.Zero) {
                    drawPath(
                        path = pathizer.asComposePath(),
                        color = thumbColor,
                        style = Fill
                    )
                }
            }
        }
    }
}