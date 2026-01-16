package com.example.snorly.feature.sleep.components

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

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
        animationSpec = tween(
            durationMillis = 2000,
            easing = LinearOutSlowInEasing
        ), // Slow, heavy transition
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

    var secondsElapsed by remember { mutableLongStateOf(0L) }
    LaunchedEffect(isTracking) {
        if (isTracking) {
            val startTime = System.currentTimeMillis()
            while (true) {
                secondsElapsed = (System.currentTimeMillis() - startTime) / 1000
                delay(500) // Update faster than 1s for smoother feel, calculation handles it
            }
        } else {
            secondsElapsed = 0L
        }
    }

    val timerText = remember(secondsElapsed) {
        val h = secondsElapsed / 3600
        val m = (secondsElapsed % 3600) / 60
        val s = secondsElapsed % 60
        String.format("%02d:%02d:%02d", h, m, s)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .scifiBackground(activeState, time) // <--- APPLY SHADER HERE
            .padding(24.dp)
    ) {
        // Content Container
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

                Icon(
                    imageVector = if (isTracking) Icons.Outlined.Timer else Icons.Outlined.Bedtime,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )

            Spacer(Modifier.height(20.dp))

            AnimatedContent(
                targetState = isTracking,
                transitionSpec = {
                    if (targetState) {
                        (slideInVertically { height -> height } + fadeIn()).togetherWith(
                            slideOutVertically { height -> -height } + fadeOut())
                    } else {
                        (slideInVertically { height -> -height } + fadeIn()).togetherWith(
                            slideOutVertically { height -> height } + fadeOut())
                    }.using(SizeTransform(clip = false))
                },
                label = "textMorph"
            ) { tracking ->
                if (tracking) {
                    Text(
                        text = timerText,
                        color = Color.White,
                        fontSize = 32.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = (-2).sp
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Ready for Sleep?",
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Initialize sleep tracking.",
                            color = Color.White.copy(0.7f),
                            fontSize = 14.sp
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            // High Contrast Button
            Button(
                onClick = onToggleTracking,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isTracking) Color(0xFFFF1744).copy(alpha = 0.8f) else Color.White.copy(
                        alpha = 0.2f
                    ),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
            ) {
                Text(
                    text = if (isTracking) "WAKE UP" else "Start Tracking",
                    fontSize = 16.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp
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