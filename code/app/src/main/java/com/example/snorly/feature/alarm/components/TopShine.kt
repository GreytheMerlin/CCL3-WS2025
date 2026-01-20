package com.example.snorly.feature.alarm.components

import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.toArgb
import kotlin.math.PI

// --- COLORS ---
val PremiumCyanColor = Color(0xFF082B36) // Deep Cyan
val PremiumSlateColor = Color(0xFF192135) // Deep Violet/Slate

fun Modifier.premiumBackground(): Modifier = composed {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        FallbackBackground(PremiumCyanColor, PremiumSlateColor)
    } else {
        FallbackBackground(PremiumCyanColor, PremiumSlateColor)
    }
}

// --- 1. MODERN SHADER (Android 13+) ---
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
private fun Modifier.ShaderBackground(color1: Color, color2: Color): Modifier {
    val infiniteTransition = rememberInfiniteTransition(label = "bg_anim")

    // EXACT 2*PI LOOP
    // We animate from 0 to 6.28318 (2*PI).
    // The shader strictly uses sin(time) or sin(time + offset) to ensure
    // the start and end values match perfectly.
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing), // 20s for very slow fluid motion
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    val shader = remember { RuntimeShader(FLUID_AURORA_SHADER) }

    return this.drawWithCache {
        val brush = ShaderBrush(shader)

        shader.setFloatUniform("resolution", size.width, size.height)
        shader.setFloatUniform("time", time)

        shader.setColorUniform("color1", color1.toArgb())
        shader.setColorUniform("color2", color2.toArgb())
        shader.setColorUniform("colorBg", Color.Black.toArgb())

        onDrawBehind {
            drawRect(brush)
        }
    }
}

// THE "FLUID AURORA" SHADER (Fixed for Perfect Looping)
private const val FLUID_AURORA_SHADER = """
    uniform float2 resolution;
    uniform float time;
    layout(color) uniform float4 color1;
    layout(color) uniform float4 color2;
    layout(color) uniform float4 colorBg;

    half4 main(float2 fragCoord) {
        float2 uv = fragCoord.xy / resolution.xy;

        // 1. TOP MASK (Fades out by 50% height)
        float verticalFade = 1.0 - smoothstep(0.0, 0.5, uv.y);

        // 2. COORDINATE WARPING
        // We removed fractional multipliers on 'time' (e.g. time * 0.5) to fix the loop.
        // Instead, we use offsets (time + 1.0) or integer multipliers (time * 1.0).
        float2 warpedUV = uv;
        warpedUV.x += sin(uv.y * 2.5 + time) * 0.1;
        warpedUV.y += cos(uv.x * 2.5 + time) * 0.1;

        // 3. LIGHT 1 (Cyan)
        // Y is kept negative (-0.1 to -0.2) to stay above the screen edge.
        // X sways between 0.1 and 0.5.
        float2 pos1 = float2(0.3 + sin(time) * 0.2, -0.15 + cos(time) * 0.05);

        // Squash factor 2.0 (Wide/Flat)
        float dist1 = distance(warpedUV * float2(1.0, 1.5), pos1 * float2(1.0, 2.0));
        float glow1 = smoothstep(0.6, 0.0, dist1);
        glow1 = pow(glow1, 2.2); 

        // 4. LIGHT 2 (Slate)
        // X sways between 0.5 and 0.9.
        // We use sin(time + 2.0) to de-sync it from Light 1 while keeping the period correct.
        float2 pos2 = float2(0.7 + sin(time + 2.0) * 0.2, -0.15 + cos(time + 1.5) * 0.05);

        float dist2 = distance(warpedUV * float2(1.0, 1.5), pos2 * float2(1.0, 2.0));
        float glow2 = smoothstep(0.6, 0.0, dist2);
        glow2 = pow(glow2, 2.2);

        // 5. BLEND
        half4 finalColor = colorBg;
        
        finalColor += color1 * glow1 * 2.0;
        finalColor += color2 * glow2 * 2.0;

        finalColor *= verticalFade;
        finalColor.a = 1.0;

        return finalColor;
    }
"""

// --- 2. FALLBACK (Old Android) ---
@Composable
private fun Modifier.FallbackBackground(color1: Color, color2: Color): Modifier {
    return this.drawWithCache {
        // Light 1: Cyan (Left Side) - Mimics shader position 1
        val light1 = Brush.radialGradient(
            colors = listOf(color1.copy(alpha = 0.6f), Color.Transparent),
            center = Offset(size.width * 0.2f, -100f), // Slightly above top edge
            radius = size.width * .8f // Large soft glow
        )

        // Light 2: Slate (Right Side) - Mimics shader position 2
        val light2 = Brush.radialGradient(
            colors = listOf(color2.copy(alpha = 0.5f), Color.Transparent),
            center = Offset(size.width * 0.8f, -100f),
            radius = size.width * 1.2f
        )

        // Safety Scrim: Ensures the bottom is perfectly black
        val scrim = Brush.verticalGradient(
            0.0f to Color.Transparent,
            0.7f to Color.Transparent,
            1.0f to Color.Black
        )

        onDrawBehind {
            drawRect(Color.Black) // Dark Base
            drawRect(light1)      // Add Cyan Blob
            drawRect(light2)      // Add Slate Blob
            drawRect(scrim)       // Clean up bottom edge
        }
    }
}