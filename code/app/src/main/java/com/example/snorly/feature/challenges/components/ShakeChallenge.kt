package com.example.snorly.feature.challenges.components

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
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
import kotlin.math.sqrt

@Composable
fun ShakeChallengeScreen(
    requiredShakes: Int,
    onSolved: () -> Unit
) {
    val context = LocalContext.current
    val sensorManager = remember { context.getSystemService(SensorManager::class.java) }
    val accel = remember { sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }

    // tweak difficulty
    val shakeThresholdG = 2.2f
    val shakeCooldownMs = 350L

    var lastShakeTime by remember { mutableLongStateOf(0L) }
    var done by remember { mutableIntStateOf(0) }

    DisposableEffect(Unit) {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

                val gX = event.values[0] / SensorManager.GRAVITY_EARTH
                val gY = event.values[1] / SensorManager.GRAVITY_EARTH
                val gZ = event.values[2] / SensorManager.GRAVITY_EARTH

                val gForce = sqrt(gX * gX + gY * gY + gZ * gZ)

                if (gForce > shakeThresholdG) {
                    val now = System.currentTimeMillis()
                    if (now - lastShakeTime > shakeCooldownMs) {
                        lastShakeTime = now
                        done = (done + 1).coerceAtMost(requiredShakes)
                        if (done >= requiredShakes) onSolved()
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        accel?.let { sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_GAME) }
        onDispose { sensorManager.unregisterListener(listener) }
    }

    // --- UI (your existing look) ---
    val remaining = (requiredShakes - done).coerceAtLeast(0)

    val bg = Brush.verticalGradient(
        listOf(Color(0xFF0B0F1A), Color(0xFF060812))
    )

    Surface(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(bg)
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Shake to Dismiss",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(90.dp))

            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color(0x220B4AA2)),
                contentAlignment = Alignment.Center
            ) {
                Text("📱", style = MaterialTheme.typography.displaySmall, color = Color.White)
            }

            Spacer(Modifier.height(36.dp))

            Text(
                text = remaining.toString(),
                style = MaterialTheme.typography.displayLarge,
                color = Color.White
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "shakes remaining",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFFB0B0B0)
            )

            Spacer(Modifier.height(26.dp))

            val progress = if (requiredShakes == 0) 0f else done.toFloat() / requiredShakes.toFloat()
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(50.dp)),
            )

            Spacer(Modifier.height(10.dp))
            Text(
                text = "$done / $requiredShakes",
                color = Color(0xFFB0B0B0),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(18.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0x221E88E5))
            ) {
                Text(
                    text = "💪 Shake your phone vigorously to dismiss the alarm",
                    modifier = Modifier.padding(16.dp),
                    color = Color.White
                )
            }

            Spacer(Modifier.weight(1f))
        }
    }
}
