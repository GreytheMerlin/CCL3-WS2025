package com.example.snorly.feature.sleep.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SleepTrackingCard(
    isTracking: Boolean,
    onToggleTracking: () -> Unit
) {
    // Dynamic Colors based on state
    val color1 = if (isTracking) Color(0xFF311B92) else Color(0xFF2979FF) // Deep Purple vs Blue
    val color2 = if (isTracking) Color(0xFF4527A0) else Color(0xFF1565C0)

    val buttonText = if (isTracking) "Wake Up / Stop" else "Start Tracking"
    val titleText = if (isTracking) "Good Night" else "Ready for sleep?"
    val subText = if (isTracking) "Sleep tracking is active. Sweet dreams!" else "Track your sleep to get insights and improve your rest"
    val icon = if (isTracking) Icons.Outlined.Timer else Icons.Outlined.Bedtime

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(color1, color2)
                )
            )
            .padding(20.dp)
    ) {
        Column(modifier = Modifier.align(Alignment.Center)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(40.dp).align(Alignment.CenterHorizontally)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = titleText,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Text(
                text = subText,
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 13.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Button(
                onClick = onToggleTracking,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isTracking) Color(0xFFFF5252) else Color.White
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = buttonText,
                    color = if (isTracking) Color.White else Color(0xFF1565C0),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}