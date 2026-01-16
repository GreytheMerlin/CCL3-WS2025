package com.example.snorly.feature.settings.components

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import java.time.LocalTime
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun CircularSleepPicker(
    bedTime: LocalTime,
    wakeTime: LocalTime,
    onSelectionChange: (LocalTime, LocalTime) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val view = LocalView.current

    // Convert Time to Angle (0 degrees = 12:00 / 00:00)
    // 24 hours = 360 degrees.
    fun timeToAngle(time: LocalTime): Float {
        val totalMinutes = time.hour * 60 + time.minute
        return (totalMinutes / 1440f) * 360f
    }

    // Convert Angle to Time (Snap to 5 minutes)
    fun angleToTime(angle: Float): LocalTime {
        // Normalize angle 0..360
        var norm = angle % 360
        if (norm < 0) norm += 360

        val totalMinutes = ((norm / 360f) * 1440).roundToInt()
        // Snap to nearest 5 min
        val snapped = ((totalMinutes + 2.5) / 5).toInt() * 5

        val h = (snapped / 60) % 24
        val m = snapped % 60
        return LocalTime.of(h, m)
    }

    // State for drag
    var bedAngle by remember(bedTime) { mutableStateOf(timeToAngle(bedTime)) }
    var wakeAngle by remember(wakeTime) { mutableStateOf(timeToAngle(wakeTime)) }

    // Interaction State
    var isDraggingBed by remember { mutableStateOf(false) }
    var isDraggingWake by remember { mutableStateOf(false) }

    Box(modifier = modifier.aspectRatio(1f)) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val center = Offset((size.width / 2).toFloat(), (size.height / 2).toFloat())
                            val touchAngle = getAngle(center, offset)

                            // Check which knob is closer (Threshold ~30 degrees)
                            val distBed = minAngleDiff(touchAngle, bedAngle)
                            val distWake = minAngleDiff(touchAngle, wakeAngle)

                            if (distBed < 25f && distBed < distWake) {
                                isDraggingBed = true
                            } else if (distWake < 25f) {
                                isDraggingWake = true
                            }
                        },
                        onDragEnd = {
                            isDraggingBed = false
                            isDraggingWake = false
                        },
                        onDragCancel = {
                            isDraggingBed = false
                            isDraggingWake = false
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            val center = Offset((size.width / 2).toFloat(),
                                (size.height / 2).toFloat()
                            )
                            val newAngle = getAngle(center, change.position)

                            // Haptic & Update logic
                            if (isDraggingBed) {
                                val newTime = angleToTime(newAngle)
                                if (newTime != angleToTime(bedAngle)) {
                                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                }
                                bedAngle = newAngle
                                onSelectionChange(newTime, angleToTime(wakeAngle))
                            } else if (isDraggingWake) {
                                val newTime = angleToTime(newAngle)
                                if (newTime != angleToTime(wakeAngle)) {
                                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                }
                                wakeAngle = newAngle
                                onSelectionChange(angleToTime(bedAngle), newTime)
                            }
                        }
                    )
                }
        ) {
            val w = size.width
            val h = size.height
            val center = Offset(w / 2, h / 2)
            val radius = w / 2

            // 1. Draw Track Background
            drawCircle(
                color = Color(0xFF2C2C2E),
                radius = radius,
                style = Stroke(width = 40.dp.toPx())
            )

            // 2. Draw Ticks (24h)
            for (i in 0 until 24) {
                val angleRad = Math.toRadians((i * 15 - 90).toDouble())
                val startX = center.x + (radius - 50) * cos(angleRad).toFloat()
                val startY = center.y + (radius - 50) * sin(angleRad).toFloat()
                val endX = center.x + (radius - 20) * cos(angleRad).toFloat()
                val endY = center.y + (radius - 20) * sin(angleRad).toFloat()

                // Highlight 0, 6, 12, 18
                val isMajor = i % 6 == 0
                drawLine(
                    color = if (isMajor) Color.Gray else Color.DarkGray,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = if (isMajor) 4f else 2f,
                    cap = StrokeCap.Round
                )
            }

            // 3. Draw Icons (Numbers or Moon/Sun)
            // Ideally use TextMeasurer, simpler for now:
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.GRAY
                    textSize = 32f
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                // Draw '12' at top
                drawText("00", center.x, center.y - radius + 80, paint)
                // Draw '06' at bottom
                drawText("12", center.x, center.y + radius - 60, paint)
            }

            // 4. Draw Active Arc (Sleep Duration)
            // Determine sweep angle handling midnight crossover
            // Compose Angles: 0 is 3 o'clock (Right). Our logic: 0 is 12 o'clock (Top).
            // Correction: Compose = Logic - 90.

            val startAngle = bedAngle - 90
            var sweep = wakeAngle - bedAngle
            if (sweep < 0) sweep += 360

            // Gradient Colors (Deep Indigo -> Bright Yellow)
            val colorBed = Color(0xFF03305E)
            val colorWake = Color(0xFF4976CB)

            // Construct Gradient: Starts at 0, ends at 'sweep' degrees (mapped to 0..1 range)
            val gradient = Brush.sweepGradient(
                colorStops = arrayOf(
                    0.0f to colorBed,
                    (sweep / 360f).coerceIn(0.001f, 1f) to colorWake
                ),
                center = center
            )

            // Don't draw if full circle overlap causes issues, clamp usually handled
            rotate(degrees = startAngle, pivot = center) {
                drawArc(
                    brush = gradient,
                    startAngle = 0f, // Start at 0 relative to rotation
                    sweepAngle = sweep,
                    useCenter = false,
                    style = Stroke(width = 40.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // 5. Draw Knobs
            // Bed Knob (Moon)
            val bedRad = Math.toRadians((bedAngle - 90).toDouble())
            val bedX = center.x + radius * cos(bedRad).toFloat()
            val bedY = center.y + radius * sin(bedRad).toFloat()

            drawCircle(Color.Black, radius = 22.dp.toPx(), center = Offset(bedX, bedY))
            drawCircle(Color.White, radius = 18.dp.toPx(), center = Offset(bedX, bedY))
            // Optional: Draw Moon Icon using drawImage or Path here

            // Wake Knob (Sun)
            val wakeRad = Math.toRadians((wakeAngle - 90).toDouble())
            val wakeX = center.x + radius * cos(wakeRad).toFloat()
            val wakeY = center.y + radius * sin(wakeRad).toFloat()

            drawCircle(Color.Black, radius = 22.dp.toPx(), center = Offset(wakeX, wakeY))
            drawCircle(Color(0xFFFFD54F), radius = 18.dp.toPx(), center = Offset(wakeX, wakeY))
        }
    }
}

// --- Helpers ---

fun getAngle(center: Offset, target: Offset): Float {
    val angle = Math.toDegrees(atan2((target.y - center.y).toDouble(), (target.x - center.x).toDouble())).toFloat() + 90
    return if (angle < 0) angle + 360 else angle
}

fun minAngleDiff(a1: Float, a2: Float): Float {
    val diff = kotlin.math.abs(a1 - a2)
    return kotlin.math.min(diff, 360 - diff)
}