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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.PI

// -----------------------------------------------------------------------------
// 1. FIXED SEAMLESS SHADER
// -----------------------------------------------------------------------------
const val SCI_FI_SHADER = """
    uniform float2 uResolution;
    uniform float uTime; // Expects 0 to 6.283 (2*PI)
    uniform float uActive;

    float random(float2 st) {
        return fract(sin(dot(st.xy, float2(12.9898,78.233))) * 43758.5453123);
    }

    half4 main(float2 fragCoord) {
        float2 uv = fragCoord.xy / uResolution.xy;
        
        half3 idleBg = half3(0.05, 0.05, 0.2); 
        half3 idleBlob1 = half3(0.2, 0.6, 0.9);
        half3 idleBlob2 = half3(0.6, 0.2, 0.8);
        
        half3 activeBg = half3(0.02, 0.0, 0.05);
        half3 activeBlob1 = half3(0.8, 0.1, 0.1);
        half3 activeBlob2 = half3(0.9, 0.4, 0.1);
        
        half3 bgCol = mix(idleBg, activeBg, uActive);
        half3 blob1Col = mix(idleBlob1, activeBlob1, uActive);
        half3 blob2Col = mix(idleBlob2, activeBlob2, uActive);

        // ---  LOOPING MATH ---
        // We use simple sin/cos of uTime directly.
        // Since uTime goes 0 -> 2*PI, sin(uTime) starts at 0 and ends at 0 seamlessly.
        
        // Blob 1: Moves in a circle
        float2 pos1 = float2(0.3 + 0.2 * sin(uTime), 0.5 + 0.2 * cos(uTime));
        float d1 = distance(uv, pos1);
        float blob1 = smoothstep(0.6, 0.0, d1);

        // Blob 2: Moves in a counter-circle at different offset
        // We add PI to offset it, but keep the frequency (1.0) strictly consistent
        float2 pos2 = float2(0.7 + 0.2 * cos(uTime + 3.14), 0.4 + 0.2 * sin(uTime + 3.14));
        float d2 = distance(uv, pos2);
        float blob2 = smoothstep(0.7, 0.0, d2); 

        half3 color = bgCol;
        color += blob1 * blob1Col * 0.6;
        color += blob2 * blob2Col * 0.5;

        // --- TECH OVERLAY ---
//        float scale = 40.0;
//        float2 grid = fract(uv * scale);
//        float dots = distance(grid, float2(0.5));
//        float mask = smoothstep(0.4, 0.5, dots); 
//        
//        // Scanline also needs to loop. 
//        // sin(uv.y * 4.0 - uTime) will loop perfectly over 2*PI
//        float scanline = smoothstep(0.4, 0.6, sin(uv.y * 4.0 - uTime));

//        color = mix(color, color * 0.7, mask);
//        float noise = random(uv * uTime) * 0.05;
//        color += noise;

        return half4(color, 1.0);
    }
"""

// -----------------------------------------------------------------------------
// 2. THE COMPONENT
// -----------------------------------------------------------------------------
@Composable
fun UpgradedSleepCard(
    isTracking: Boolean,
    startTime: Long,
    onToggleTracking: () -> Unit
) {
    val activeState by animateFloatAsState(
        targetValue = if (isTracking) 1f else 0f,
        animationSpec = tween(2000, easing = LinearOutSlowInEasing),
        label = "shaderState"
    )

    // FIX 1: PERFECT LOOP TIME
    // 2 * PI is approx 6.28318f. We animate exactly to this value so sin(time) resets perfectly.
    val transition = rememberInfiniteTransition(label = "shaderTime")
    val time by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            // 15 seconds for one full graceful rotation
            tween(15000, easing = LinearEasing)
        ),
        label = "time"
    )

    var secondsElapsed by remember { mutableLongStateOf(0L) }
    LaunchedEffect(isTracking, startTime) {
        // 1. Check if we are tracking AND have a valid startTime from the ViewModel
        if (isTracking && startTime > 0) {
            while (true) {
                val now = System.currentTimeMillis()

                // 2. Use the 'startTime' parameter passed into the Composable,
                // NOT a new local variable.
                secondsElapsed = (now - startTime) / 1000

                delay(500)
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
            .scifiBackground(activeState, time)
            .padding(24.dp)
    ) {
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
                // FIX 2: ALIGNMENT
                // This anchors the content to the center, preventing the "side jump"
                contentAlignment = Alignment.Center,
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
                        letterSpacing = (-2).sp,
                        textAlign = TextAlign.Center
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Ready for Sleep?",
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Initialize sleep tracking.",
                            color = Color.White.copy(0.7f),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))

            Button(
                onClick = onToggleTracking,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isTracking) Color(0xFFFF1744).copy(alpha = 0.8f) else Color.White.copy(alpha = 0.2f),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().height(44.dp)
            ) {
                Text(
                    text = if (isTracking) "WAKE UP" else "Start Tracking",
                    fontSize = 16.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp
                )
            }
        }
    }
}

// Modifier remains mostly the same, just ensuring fallback is clean
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
        Modifier.background(Color(0xFF1A237E)) // Simple fallback
    }
)