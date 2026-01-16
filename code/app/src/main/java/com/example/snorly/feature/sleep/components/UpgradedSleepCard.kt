package com.example.snorly.feature.sleep.components

import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// -----------------------------------------------------------------------------
// 1. THE SCI-FI SHADER (AGSL)
// -----------------------------------------------------------------------------
// This shader combines 3 layers:
// 1. A deep background color
// 2. Two drifting "blobs" of color (Nebula effect)
// 3. A Dot-Matrix pattern (Halftone) overlay for the tech look
const val SCI_FI_SHADER = """
    uniform float2 uResolution;
    uniform float uTime;
    uniform float uActive; // 0.0 = Idle, 1.0 = Tracking

    // Simple pseudo-random for grain
    float random(float2 st) {
        return fract(sin(dot(st.xy, float2(12.9898,78.233))) * 43758.5453123);
    }

    half4 main(float2 fragCoord) {
        float2 uv = fragCoord.xy / uResolution.xy;
        
        // --- PALETTE CONFIGURATION ---
        // Idle: Deep Blue background with Cyan/Purple blobs
        half3 idleBg = half3(0.05, 0.05, 0.2); 
        half3 idleBlob1 = half3(0.2, 0.6, 0.9); // Cyan
        half3 idleBlob2 = half3(0.6, 0.2, 0.8); // Purple
        
        // Active: Deep Void background with Red/Orange blobs
        half3 activeBg = half3(0.02, 0.0, 0.05);
        half3 activeBlob1 = half3(0.8, 0.1, 0.1); // Red
        half3 activeBlob2 = half3(0.9, 0.4, 0.1); // Orange
        
        // Mix palettes based on uActive state
        half3 bgCol = mix(idleBg, activeBg, uActive);
        half3 blob1Col = mix(idleBlob1, activeBlob1, uActive);
        half3 blob2Col = mix(idleBlob2, activeBlob2, uActive);

        // --- ORGANIC MOVEMENT (The "Nebula") ---
        // Blob 1 Movement
        float t1 = uTime * 0.5;
        float2 pos1 = float2(0.3 + 0.2*sin(t1), 0.5 + 0.2*cos(t1 * 1.3));
        float d1 = distance(uv, pos1);
        float blob1 = smoothstep(0.6, 0.0, d1); // Soft edge

        // Blob 2 Movement
        float t2 = uTime * 0.3 + 2.0;
        float2 pos2 = float2(0.7 + 0.2*cos(t2 * 0.8), 0.4 + 0.2*sin(t2));
        float d2 = distance(uv, pos2);
        float blob2 = smoothstep(0.7, 0.0, d2); 

        // Combine Background + Blobs
        half3 color = bgCol;
        color += blob1 * blob1Col * 0.6;
        color += blob2 * blob2Col * 0.5;

        // --- TECH OVERLAY (The "Halftone") ---
        // Create a grid of dots
        float scale = 40.0; // How many dots
        float2 grid = fract(uv * scale); // 0..1 in each grid cell
        float dots = distance(grid, float2(0.5));
        
        // Make dots fade in/out based on position (vignette style)
        float mask = smoothstep(0.4, 0.5, dots); 
        
        // Subtle scanline moving down
        float scanline = smoothstep(0.4, 0.6, sin(uv.y * 4.0 - uTime * 2.0));

        // Apply dark overlay for the grid lines
        color = mix(color, color * 0.7, mask);
        
        // Add subtle grain
        float noise = random(uv * uTime) * 0.05;
        color += noise;

        return half4(color, 1.0);
    }
"""

// -----------------------------------------------------------------------------
// 2. THE COMPONENT
// -----------------------------------------------------------------------------
@Composable
fun UpgradedSleepCard(
    isTracking: Boolean,
    onToggleTracking: () -> Unit
) {
    // Animation Bridge: Smoothly interpolate the "Tracking" state (0f -> 1f)
    val activeState by animateFloatAsState(
        targetValue = if (isTracking) 1f else 0f,
        animationSpec = tween(durationMillis = 2000, easing = LinearOutSlowInEasing), // Slow, heavy transition
        label = "shaderState"
    )

    // Time loop for movement
    val transition = rememberInfiniteTransition(label = "shaderTime")
    val time by transition.animateFloat(
        initialValue = 0f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(tween(20000, easing = LinearEasing)),
        label = "time"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp) // Taller for more immersive visual area
            .clip(RoundedCornerShape(32.dp))
            .scifiBackground(activeState, time) // <--- APPLY SHADER HERE
            .padding(24.dp)
    ) {
        // Content Container
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon Container (Glassy Look)
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isTracking) Icons.Outlined.Timer else Icons.Outlined.Bedtime,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(Modifier.height(20.dp))

            // Text with specific typography
            Text(
                text = if (isTracking) "Tracking Sleep" else "Ready to Sleep?",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp
            )

            Text(
                text = if (isTracking) "Wake me up when I'm refreshed." else "Initialize sleep tracking sequence.",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
            )

            // High Contrast Button
            Button(
                onClick = onToggleTracking,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = if (isTracking) Color(0xFFB71C1C) else Color(0xFF311B92)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = if (isTracking) "TERMINATE" else "INITIATE",
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
// 3. THE MODIFIER (AGSL ENGINE)
// -----------------------------------------------------------------------------
fun Modifier.scifiBackground(activeState: Float, time: Float): Modifier = this.then(
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Modifier.drawWithCache {
            val shader = RuntimeShader(SCI_FI_SHADER)
            val brush = ShaderBrush(shader)

            onDrawBehind {
                shader.setFloatUniform("uResolution", size.width, size.height)
                shader.setFloatUniform("uTime", time)
                shader.setFloatUniform("uActive", activeState)
                drawRect(brush)
            }
        }
    } else {
        // Fallback for API < 33
        Modifier.background(
            androidx.compose.ui.graphics.Brush.verticalGradient(
                colors = if (activeState > 0.5f)
                    listOf(Color(0xFF212121), Color(0xFFB71C1C))
                else
                    listOf(Color(0xFF1A237E), Color(0xFF0D47A1))
            )
        )
    }
)