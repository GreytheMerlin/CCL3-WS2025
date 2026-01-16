package com.example.snorly.feature.sleep.components

import android.content.Context
import android.graphics.RuntimeShader
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.FiberManualRecord
import androidx.compose.material.icons.rounded.StopCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

// -----------------------------------------------------------------------------
// 1. THE MEGA-SHADER (AGSL)
// Includes: Domain Warping, Data Motes, Parallax inputs, Touch inputs
// -----------------------------------------------------------------------------
const val ULTIMATE_SLEEP_SHADER = """
    uniform float2 uResolution;
    uniform float uTime;
    uniform float uActive; // 0.0 (Idle) to 1.0 (Tracking)
    uniform float2 uTilt;  // x, y tilt from sensors relative to center
    uniform float2 uTouch; // normalized x, y touch position
    uniform float uTouchActive; // 0.0 or 1.0 if touching

    // -- Noise Helpers --
    float hash(float2 p) {
        p = fract(p * float2(123.34, 456.21));
        p += dot(p, p + 45.32);
        return fract(p.x * p.y);
    }

    float noise(float2 p) {
        float2 i = floor(p);
        float2 f = fract(p);
        f = f * f * (3.0 - 2.0 * f);
        float res = mix(mix(hash(i), hash(i + float2(1.0, 0.0)), f.x),
                        mix(hash(i + float2(0.0, 1.0)), hash(i + float2(1.0, 1.0)), f.x), f.y);
        return res;
    }

    // Domain Warping FBM
    float fbm(float2 p) {
        float v = 0.0;
        float a = 0.5;
        float2 shift = float2(100.0);
        // Rotate to reduce axial bias
        float2x2 rot = float2x2(cos(0.5), sin(0.5), -sin(0.5), cos(0.50));
        for (int i = 0; i < 3; ++i) {
            v += a * noise(p);
            p = rot * p * 2.0 + shift;
            a *= 0.5;
        }
        return v;
    }

    half4 main(float2 fragCoord) {
        // Normalize coordinates and correct aspect ratio
        float2 uv = fragCoord.xy / uResolution.xy;
        float2 pos = uv;
        pos.x *= uResolution.x / uResolution.y;

        // --- PARALLAX & TOUCH DISTORTION ---
        // Background moves slowly opposite to tilt
        float2 bgTilt = uTilt * 0.1; 
        
        // Calculate touch influence
        float touchDist = distance(uv, uTouch);
        // Create a localized warp effect around the finger
        float touchWarp = smoothstep(0.3, 0.0, touchDist) * uTouchActive * 0.05;
        
        float2 distortedPos = pos + bgTilt + touchWarp;

        // --- LAYER 1: LIQUID DOMAIN WARPING BACKGROUND ---
        float t = uTime * 0.15;
        
        float2 q = float2(0.0);
        q.x = fbm(distortedPos + 0.00 * t);
        q.y = fbm(distortedPos + float2(1.0));

        float2 r = float2(0.0);
        r.x = fbm(distortedPos + 1.0 * q + float2(1.7, 9.2) + 0.15 * t);
        r.y = fbm(distortedPos + 1.0 * q + float2(8.3, 2.8) + 0.126 * t);

        float f = fbm(distortedPos + r);

        // Define Palettes
        // Idle: Deep Cyan/Blue/Purple
        half3 idleCol = mix(half3(0.0, 0.05, 0.2), half3(0.0, 0.5, 0.6), clamp(f*f*3.0, 0.0, 1.0));
        idleCol = mix(idleCol, half3(0.2, 0.1, 0.4), r.x); // Add some purple swirls

        // Active: Deep Red/Orange/Void
        half3 activeCol = mix(half3(0.05, 0.0, 0.02), half3(0.7, 0.1, 0.1), clamp(f*f*3.5, 0.0, 1.0));
        activeCol = mix(activeCol, half3(0.8, 0.4, 0.0), r.y); // Add magma swirls
        
        // Blend based on state
        half3 finalColor = mix(idleCol, activeCol, uActive);
        
        // Brighten slightly under touch
        finalColor += smoothstep(0.2, 0.0, touchDist) * uTouchActive * 0.1;

        // Vignette
        float vig = 1.0 - length(uv - 0.5) * 0.6;
        finalColor *= vig;

        // --- LAYER 2: DATA MOTES (FIREFLIES) ---
        float moteLayer = 0.0;
        for(float i=0.0; i<12.0; i++){
             // Random start positions
            float2 start = float2(hash(float2(i, 1.0)), hash(float2(i, 2.0)));
            
            // Speed increases with activity
            float speed = 0.1 + (0.8 * uActive); 
            
            // Movement logic
            float yOffset = fract(uTime * speed * (0.05 + 0.05*hash(float2(i,3.0))) + start.y);
            float xWiggle = sin(uTime + i * 1.5) * 0.03;
            
            // Motes move faster with tilt for depth effect
            float2 moteTilt = uTilt * 0.25;
            float2 motePos = float2(start.x + xWiggle, yOffset) + moteTilt;
            
            float dist = length(pos - motePos);
            // Sharp small glow
            float glow = 0.001 / (dist * dist);
            moteLayer += smoothstep(0.0, 1.0, glow);
        }

        // Mote color: Cyan/White (Idle) -> Gold/Orange (Active)
        half3 moteColor = mix(half3(0.6, 0.9, 1.0), half3(1.0, 0.7, 0.3), uActive);
        // Motes are brighter when active
        finalColor += moteLayer * moteColor * (0.3 + 0.5 * uActive);

        return half4(finalColor, 1.0);
    }
"""

// -----------------------------------------------------------------------------
// 2. SENSOR & UI HELPERS
// -----------------------------------------------------------------------------

/**
 * Helper to connect to device accelerometer and return tilt as State.
 * Returns a normalized Offset roughly between -1.0 and 1.0 depending on tilt.
 */
@Composable
fun rememberDeviceTilt(): State<Offset> {
    val context = LocalContext.current
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val accelerometer = remember { sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }

    val tilt = remember { mutableStateOf(Offset.Zero) }

    DisposableEffect(Unit) {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    // Simple low-pass filter to smooth out jitters
                    val alpha = 0.1f
                    val x = it.values[0] * alpha + tilt.value.x * (1.0f - alpha)
                    val y = it.values[1] * alpha + tilt.value.y * (1.0f - alpha)
                    // Normalize roughly. X acts on screen Y, Y acts on screen X due to orientation
                    tilt.value = Offset(
                        (y / 10f).coerceIn(-1f, 1f),
                        (x / 10f).coerceIn(-1f, 1f)
                    )

                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }
    return tilt
}

// -----------------------------------------------------------------------------
// 3. THE MAIN COMPONENT
// -----------------------------------------------------------------------------
@Composable
fun UltimateSleepCard(
    isTracking: Boolean,
    onToggleTracking: () -> Unit
) {
    // --- STATE MANAGEMENT ---

    // 1. Animation State (Smooth transition 0f -> 1f)
    val activeState by animateFloatAsState(
        targetValue = if (isTracking) 1f else 0f,
        animationSpec = tween(2000, easing = FastOutSlowInEasing),
        label = "active"
    )

    // 2. Shader Time Loop
    val infiniteTransition = rememberInfiniteTransition(label = "time")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 100f,
        // Slower animation for a more "majestic" space feel
        animationSpec = infiniteRepeatable(tween(80000, easing = LinearEasing)),
        label = "time"
    )

    // 3. Sensor Tilt State (3D Parallax)
    val deviceTilt by rememberDeviceTilt()

    // 4. Touch Interaction State
    var touchPositionNormalized by remember { mutableStateOf(Offset(0.5f, 0.5f)) }
    var isTouching by remember { mutableStateOf(false) }
    val touchStrength by animateFloatAsState(if (isTouching) 1f else 0f, label = "touch")

    // 5. Stopwatch Logic
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

    // --- UI COMPOSITION ---
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp)
            .clip(RoundedCornerShape(32.dp))
            // Handle Touch Input for the shader
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { isTouching = true },
                    onDragEnd = { isTouching = false },
                    onDragCancel = { isTouching = false },
                    onDrag = { change, _ ->
                        // Normalize touch position to 0.0 - 1.0 range
                        touchPositionNormalized = Offset(
                            change.position.x / size.width,
                            change.position.y / size.height
                        )
                    }
                )
            }
            // Apply the Ultimate Shader
            .ultimateSciFiBackground(
                activeState = activeState,
                time = time,
                tilt = deviceTilt,
                touchPos = touchPositionNormalized,
                touchStrength = touchStrength
            )
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                AnimatedVisibility(visible = isTracking, enter = fadeIn(), exit = fadeOut()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.FiberManualRecord, null, tint = Color(0xFFFF1744), modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("LIVE TRACKING", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    }
                }
            }

            // Center Content (Icon + Text/Timer)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Morphing Icon
                AnimatedContent(
                    targetState = isTracking,
                    transitionSpec = {
                        (scaleIn() + fadeIn()).togetherWith(scaleOut() + fadeOut())
                    },
                    label = "iconMorph"
                ) { tracking ->
                    Box(modifier = Modifier
                        .size(72.dp)
                        .background(Color.White.copy(0.1f), RoundedCornerShape(50))
                        .padding(12.dp)
                    ) {
                        Icon(
                            imageVector = if (tracking) Icons.Rounded.StopCircle else Icons.Rounded.Bedtime,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Text to Timer Transition
                AnimatedContent(
                    targetState = isTracking,
                    transitionSpec = {
                        if (targetState) {
                            (slideInVertically { height -> height } + fadeIn()).togetherWith(slideOutVertically { height -> -height } + fadeOut())
                        } else {
                            (slideInVertically { height -> -height } + fadeIn()).togetherWith(slideOutVertically { height -> height } + fadeOut())
                        }.using(SizeTransform(clip = false))
                    },
                    label = "textMorph"
                ) { tracking ->
                    if (tracking) {
                        Text(
                            text = timerText,
                            color = Color.White,
                            fontSize = 48.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = (-2).sp
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Ready for Sleep?", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(4.dp))
                            Text("Enter the void.", color = Color.White.copy(0.7f), fontSize = 14.sp)
                        }
                    }
                }
            }

            // Bottom Button
            Button(
                onClick = onToggleTracking,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if(isTracking) Color(0xFFFF1744).copy(alpha = 0.8f) else Color.White.copy(alpha = 0.2f),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().height(60.dp)
            ) {
                Text(
                    text = if (isTracking) "WAKE UP" else "INITIATE SLEEP",
                    fontSize = 16.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
// 4. THE MODIFIER BRIDGE
// -----------------------------------------------------------------------------
fun Modifier.ultimateSciFiBackground(
    activeState: Float,
    time: Float,
    tilt: Offset,
    touchPos: Offset,
    touchStrength: Float
): Modifier = this.then(
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Modifier.drawWithCache {
            // Compile shader once
            val shader = RuntimeShader(ULTIMATE_SLEEP_SHADER)
            val brush = ShaderBrush(shader)
            onDrawBehind {
                // Update uniforms every frame
                shader.setFloatUniform("uResolution", size.width, size.height)
                shader.setFloatUniform("uTime", time)
                shader.setFloatUniform("uActive", activeState)
                // Pass sensor tilt (negated one axis for natural feel)
                shader.setFloatUniform("uTilt", -tilt.x, tilt.y)
                // Pass touch data
                shader.setFloatUniform("uTouch", touchPos.x, touchPos.y)
                shader.setFloatUniform("uTouchActive", touchStrength)

                drawRect(brush)
            }
        }
    } else {
        // Fallback for older APIs
        Modifier.background(Color.DarkGray)
    }
)