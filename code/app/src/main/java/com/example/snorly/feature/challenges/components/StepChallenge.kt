package com.example.snorly.feature.challenges.components

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import kotlin.math.max

@Composable
fun StepChallengeScreen(
    requiredSteps: Int,
    onSolved: () -> Unit
) {
    val context = LocalContext.current
    val sensorManager = remember { context.getSystemService(SensorManager::class.java) }
    val stepSensor = remember { sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) }

    LaunchedEffect(Unit) {
        Log.d("StepChallenge", "stepSensor = $stepSensor")
    }

    // TYPE_STEP_COUNTER is cumulative since boot; we store a baseline at start
    var baseline by remember { mutableStateOf<Float?>(null) }
    var currentSteps by remember { mutableIntStateOf(0) }
    var sensorMissing by remember { mutableStateOf(stepSensor == null) }

    DisposableEffect(Unit) {
        if (stepSensor == null) {
            sensorMissing = true
            onDispose { }
        } else {
            sensorMissing = false

            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    val totalSinceBoot = event.values.firstOrNull() ?: return
                    val base = baseline
                    if (base == null) {
                        baseline = totalSinceBoot
                        currentSteps = 0
                        return
                    }
                    val delta = (totalSinceBoot - base).toInt()
                    currentSteps = max(0, delta)

                    if (currentSteps >= requiredSteps) {
                        onSolved()
                    }
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
            }

            sensorManager.registerListener(listener, stepSensor, SensorManager.SENSOR_DELAY_NORMAL)
            onDispose { sensorManager.unregisterListener(listener) }
        }
    }

    val done = currentSteps.coerceAtMost(requiredSteps.coerceAtLeast(1))
    val remaining = (requiredSteps - done).coerceAtLeast(0)
    val progress = if (requiredSteps <= 0) 0f else done.toFloat() / requiredSteps.toFloat()

    val bg = Brush.verticalGradient(listOf(Color(0xFF0B0F1A), Color(0xFF060812)))
    val accent = Color(0xFF1DBB9A) // greenish like screenshot

    Surface(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(bg)
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Walk to Dismiss",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(90.dp))

            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(accent.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Text("👣", style = MaterialTheme.typography.displaySmall, color = accent)
            }

            Spacer(Modifier.height(36.dp))

            Text(
                text = remaining.toString(),
                style = MaterialTheme.typography.displayLarge,
                color = accent
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "steps remaining",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFFB0B0B0)
            )

            Spacer(Modifier.height(26.dp))

            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(50.dp)),
            )

            Spacer(Modifier.height(10.dp))
            Text(
                text = "$done / $requiredSteps\nsteps",
                color = Color(0xFFB0B0B0),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(18.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = accent.copy(alpha = 0.18f))
            ) {
                Text(
                    text = if (sensorMissing)
                        "⚠️ No step counter sensor found on this device.\n(Use a different challenge or implement a fallback using accelerometer.)"
                    else
                        "🚶 Walk around to count steps and dismiss the alarm",
                    modifier = Modifier.padding(16.dp),
                    color = Color.White
                )
            }

            Spacer(Modifier.weight(1f))
        }
    }
}
