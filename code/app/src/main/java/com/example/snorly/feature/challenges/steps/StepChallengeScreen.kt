package com.example.snorly.feature.challenges.steps

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun StepChallengeScreen(
    state: StepChallengeUiState,
    modifier: Modifier = Modifier
) {
    val bg = Brush.verticalGradient(listOf(Color(0xFF0B0F1A), Color(0xFF060812)))
    val accent = Color(0xFF1DBB9A)

    Surface(modifier.fillMaxSize()) {
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
                text = state.remaining.toString(),
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
                progress = { state.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(50.dp)),
            )

            Spacer(Modifier.height(10.dp))
            Text(
                text = "${state.done} / ${state.requiredSteps}\nsteps",
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
                    text = if (state.sensorMissing)
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
