package com.example.snorly.feature.sleep.components

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.WbSunny
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
import com.example.snorly.feature.sleep.SleepTrackingMode
import kotlinx.coroutines.delay
import kotlin.math.PI

// -----------------------------------------------------------------------------
// FIXED SEAMLESS SHADER
// -----------------------------------------------------------------------------
// In UpgradedSleepCard.kt

const val SCI_FI_SHADER = """
    uniform float2 uResolution;
    uniform float uTime; 
    uniform float uActive;
    uniform float uMorning;
    uniform float uError;

    half4 main(float2 fragCoord) {
        float2 uv = fragCoord.xy / uResolution.xy;
        
        // Original Colors
        half3 idleBg = half3(0.05, 0.05, 0.2); 
        half3 activeBg = half3(0.02, 0.0, 0.05);
        half3 morningBg = half3(0.1, 0.4, 0.8);
        half3 errorBg = half3(.15, -0.5, -0.5);
        
        half3 idleBlob1 = half3(0.2, 0.6, 0.9);
        half3 activeBlob1 = half3(0.8, 0.1, 0.1);
        half3 morningBlob1 = half3(0.0, 0.0, 0.0); // Sun Glow
        
        // Mix Idle and Active exactly like before
        half3 baseBg = mix(idleBg, activeBg, uActive);
        half3 baseBlob = mix(idleBlob1, activeBlob1, uActive);
        
        // Now layer the Morning state on top smoothly
        half3 finalBg = mix(baseBg, morningBg, uMorning);
        finalBg = mix(finalBg, errorBg, uError);
        half3 finalBlob = mix(baseBlob, morningBlob1, uMorning);

        // --- YOUR ORIGINAL PERFECT LOOP MATH ---
        float2 pos1 = float2(0.3 + 0.2 * sin(uTime), 0.5 + 0.2 * cos(uTime));
        float d1 = distance(uv, pos1);
        float blob1 = smoothstep(0.6, 0.0, d1);

        float2 pos2 = float2(0.7 + 0.2 * cos(uTime + 3.14), 0.4 + 0.2 * sin(uTime + 3.14));
        float d2 = distance(uv, pos2);
        float blob2 = smoothstep(0.7, 0.0, d2); 

        half3 color = finalBg;
        color += blob1 * finalBlob * 0.6;
        color += blob2 * mix(half3(0.6, 0.2, 0.8), half3(1.0, 1.0, 1.0), uMorning) * 0.5;

        return half4(color, 1.0);
    }
"""

// -----------------------------------------------------------------------------
// 2. THE COMPONENT
// -----------------------------------------------------------------------------
@Composable
fun UpgradedSleepCard(
    mode: SleepTrackingMode,
    startTime: Long,
    errorMessage: String?,
    onStart: () -> Unit,
    onWakeUp: () -> Unit,
    onLogSleep: () -> Unit,
    onErrorDismiss: () -> Unit
) {

    val activeState by animateFloatAsState(
        targetValue = if (mode == SleepTrackingMode.TRACKING) 1f else 0f,
        animationSpec = tween(1500),
        label = "activeState"
    )

    // Mode float for shader
    val morningState by animateFloatAsState(
        targetValue = if (mode == SleepTrackingMode.GOOD_MORNING) 1f else 0f,
        animationSpec = tween(1500),
        label = "morningState"
    )

    val errorState by animateFloatAsState(
        targetValue = if (mode == SleepTrackingMode.ERROR) 1f else 0f, label = "error"
    )


    // PERFECT LOOP TIME
    // 2 * PI is approx 6.28318f. We animate exactly to this value so sin(time) resets perfectly.
    val transition = rememberInfiniteTransition(label = "shaderTime")
    val time by transition.animateFloat(
        initialValue = 0f, targetValue = (2 * PI).toFloat(), animationSpec = infiniteRepeatable(
            // 15 seconds for one full graceful rotation
            tween(15000, easing = LinearEasing)
        ), label = "time"
    )

    var secondsElapsed by remember { mutableLongStateOf(0L) }

    LaunchedEffect(mode, startTime) {
        if (mode == SleepTrackingMode.TRACKING && startTime > 0) {
            while (true) {
                secondsElapsed = (System.currentTimeMillis() - startTime) / 1000
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
            .scifiBackground(activeState, morningState, errorState, time)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Icon(
                imageVector = when (mode) {
                    SleepTrackingMode.IDLE -> Icons.Outlined.Bedtime
                    SleepTrackingMode.TRACKING -> Icons.Outlined.Timer
                    SleepTrackingMode.GOOD_MORNING -> Icons.Default.WbSunny
                    SleepTrackingMode.ERROR -> Icons.Default.Error
                }, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp)
            )

            Spacer(Modifier.height(20.dp))

            AnimatedContent(
                targetState = mode, contentAlignment = Alignment.Center, transitionSpec = {
                    (slideInVertically { it } + fadeIn()).togetherWith(slideOutVertically { -it } + fadeOut())
                }) { targetMode ->
                when (targetMode) {
                    SleepTrackingMode.IDLE -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Ready for Sleep?",
                                color = Color.White,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Initialize sleep tracking.",
                                color = Color.White.copy(0.7f),
                                fontSize = 14.sp
                            )
                        }
                    }

                    SleepTrackingMode.TRACKING -> {
                        Text(
                            text = timerText,
                            color = Color.White,
                            fontSize = 36.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    SleepTrackingMode.GOOD_MORNING -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Good Morning!",
                                color = Color.White,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Sleep session recorded.",
                                color = Color.White.copy(0.8f),
                                fontSize = 14.sp
                            )
                        }
                    }

                    SleepTrackingMode.ERROR -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Oops!",
                                color = Color.White,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = errorMessage ?: "Something went wrong",
                                color = Color.White.copy(0.9f),
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))

            Button(
                onClick = {
                    when (mode) {
                        SleepTrackingMode.IDLE -> onStart()
                        SleepTrackingMode.TRACKING -> onWakeUp()
                        SleepTrackingMode.GOOD_MORNING -> onLogSleep()
                        SleepTrackingMode.ERROR -> onErrorDismiss()
                    }
                }, colors = ButtonDefaults.buttonColors(
                    containerColor = if (mode == SleepTrackingMode.GOOD_MORNING) Color.White else Color.White.copy(
                        0.2f
                    ),
                    contentColor = if (mode == SleepTrackingMode.GOOD_MORNING) Color.Black else Color.White

                ), shape = RoundedCornerShape(16.dp), modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = when (mode) {
                        SleepTrackingMode.IDLE -> "START TRACKING"
                        SleepTrackingMode.TRACKING -> "WAKE UP"
                        SleepTrackingMode.GOOD_MORNING -> "REFINE LOG"
                        SleepTrackingMode.ERROR -> "DISMISS"
                    }, fontWeight = FontWeight.Bold
                )
            }
        }

    }
}


fun Modifier.scifiBackground(active: Float, morning: Float, error: Float, time: Float): Modifier =
    this.then(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Modifier.drawWithCache {
                val shader = RuntimeShader(SCI_FI_SHADER)
                onDrawBehind {
                    shader.setFloatUniform("uResolution", size.width, size.height)
                    shader.setFloatUniform("uTime", time)
                    shader.setFloatUniform("uActive", active)
                    shader.setFloatUniform("uMorning", morning)
                    shader.setFloatUniform("uError", error) // Set error uniform
                    drawRect(ShaderBrush(shader))
                }
            }
        } else {
            Modifier.background(Color(0xFF0D1B2A))
        })