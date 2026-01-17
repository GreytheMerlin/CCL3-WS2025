package com.example.snorly.core.ui.shader

object ShaderLibrary {

    // -------------------------------------------------------------------------
    // 1. GRAINY NEBULA (Liquid, Smoke, Organic)
    // -------------------------------------------------------------------------
    const val NEBULA = """
        uniform float2 uResolution;
        uniform float uTime;
        layout(color) uniform half4 uColorStart;
        layout(color) uniform half4 uColorEnd;
        
        float random(float2 st) {
            return fract(sin(dot(st.xy, float2(12.9898,78.233))) * 43758.5453123);
        }
        
        float noise(float2 st) {
            float2 i = floor(st);
            float2 f = fract(st);
            float a = random(i);
            float b = random(i + float2(1.0, 0.0));
            float c = random(i + float2(0.0, 1.0));
            float d = random(i + float2(1.0, 1.0));
            float2 u = f * f * (3.0 - 2.0 * f);
            return mix(a, b, u.x) + (c - a) * u.y * (1.0 - u.x) + (d - b) * u.x * u.y;
        }

        half4 main(float2 fragCoord) {
            float2 uv = fragCoord.xy / uResolution.xy;
            float t = uTime * 0.4;
            
            // Warp
            float2 pos = uv;
            pos.x += 0.2 * sin(pos.y * 3.0 + t);
            pos.y += 0.2 * cos(pos.x * 3.0 + t * 0.8);
            float n = noise(pos * 2.5 + t);
            
            // Mix
            half4 col = mix(uColorStart, uColorEnd, n);
            
            // Grain & Vignette
            float vig = 1.0 - length(uv - 0.5) * 1.2;
            col.rgb *= smoothstep(0.0, 1.5, vig + 0.5);
            col.rgb += random(uv * uTime) * 0.12; // Film grain
            
            return col;
        }
    """

    // -------------------------------------------------------------------------
    // 2. DIGITAL GRID (Tech, Blueprint, Constructed)
    // -------------------------------------------------------------------------
    const val GRID = """
        uniform float2 uResolution;
        uniform float uTime;
        layout(color) uniform half4 uColorStart;
        layout(color) uniform half4 uColorEnd;

        float random(float2 st) { return fract(sin(dot(st.xy, float2(12.9898,78.233))) * 43758.5453123); }

        half4 main(float2 fragCoord) {
            float2 uv = fragCoord.xy / uResolution.xy;
            
            // Perspective / Movement
            float2 gridUV = uv * 6.0; // Grid Scale
            gridUV.x += sin(gridUV.y * 0.5 + uTime * 0.5) * 0.5; // Wave distortion
            gridUV.y += uTime * 0.2; // Scroll down
            
            // Create Grid Lines
            float2 grid = fract(gridUV);
            float lineThickness = 0.08;
            float lines = step(1.0 - lineThickness, grid.x) + step(1.0 - lineThickness, grid.y);
            
            // Base Gradient
            half4 col = mix(uColorStart, uColorEnd, uv.y);
            
            // Apply Grid (Lighter lines)
            col.rgb += lines * 0.15;
            
            // Tech Noise overlay
            float staticNoise = random(floor(uv * 50.0) + floor(uTime * 10.0));
            if (staticNoise > 0.98) col.rgb += 0.3; // Random bright pixels
            
            // Vignette
            col.rgb *= 1.0 - length(uv - 0.5) * 0.5;
            
            return col;
        }
    """

    // -------------------------------------------------------------------------
    // 3. SOUND WAVES (Flowing, Audio, Smooth)
    // -------------------------------------------------------------------------
    const val WAVES = """
        uniform float2 uResolution;
        uniform float uTime;
        layout(color) uniform half4 uColorStart;
        layout(color) uniform half4 uColorEnd;

        half4 main(float2 fragCoord) {
            float2 uv = fragCoord.xy / uResolution.xy;
            
            // Base Background
            half4 col = mix(uColorStart, uColorEnd, uv.x + uv.y * 0.5);
            
            // Generate multiple sine waves
            float t = uTime * 1.5;
            float waveSum = 0.0;
            
            for(float i = 1.0; i <= 3.0; i++){
                // Offset waves
                float wave = sin(uv.x * (3.0 + i) + t * (0.5 * i));
                // Make them thin lines
                float line = 0.02 / abs(uv.y - 0.5 - wave * 0.15);
                // Fade edges
                line *= smoothstep(0.0, 1.0, 1.0 - abs(uv.x - 0.5) * 1.5);
                waveSum += line;
            }
            
            // Add waves to color (Additive blending)
            col.rgb += waveSum * 0.3 * uColorStart.rgb;
            
            return col;
        }
    """

    const val RETRO_NOISE = """
    uniform float2 uResolution;
    uniform float uTime;
    layout(color) uniform half4 uColorStart;
    layout(color) uniform half4 uColorEnd;

    float random(float2 st) {
        return fract(sin(dot(st.xy, float2(12.9898,78.233))) * 43758.5453123);
    }

    half4 main(float2 fragCoord) {
        float2 uv = fragCoord.xy / uResolution.xy;
        
        // Base Gradient
        half4 col = mix(uColorStart, uColorEnd, uv.y);
        
        // Heavy Grain
        float noise = random(uv * uTime * 2.0);
        col.rgb += (noise - 0.5) * 0.15;
        
        // Scanlines
        float scanline = sin(uv.y * 100.0 + uTime * 5.0);
        col.rgb -= scanline * 0.03;
        
        return col;
    }
"""
    const val AURORA = """
    uniform float2 uResolution;
    uniform float uTime;
    layout(color) uniform half4 uColorStart;
    layout(color) uniform half4 uColorEnd;

    half4 main(float2 fragCoord) {
        float2 uv = fragCoord.xy / uResolution.xy;
        float t = uTime * 0.5;
        
        // Wavy bands
        float wave1 = sin(uv.x * 5.0 + t) * 0.1;
        float wave2 = sin(uv.x * 8.0 - t * 1.5) * 0.05;
        
        float intensity = 0.02 / abs(uv.y - 0.5 + wave1 + wave2);
        intensity = pow(intensity, 1.2); // Sharpen
        
        // Base color + glowing bands
        half4 col = mix(uColorStart, uColorEnd, uv.y);
        col.rgb += intensity * half3(0.4, 0.8, 0.6); // Greenish tint
        
        return col;
    }
"""

    const val CYBER_GLITCH = """
    uniform float2 uResolution;
    uniform float uTime;
    layout(color) uniform half4 uColorStart;
    layout(color) uniform half4 uColorEnd;

    float random(float2 st) { return fract(sin(dot(st.xy, float2(12.9898,78.233))) * 43758.5453123); }

    half4 main(float2 fragCoord) {
        float2 uv = fragCoord.xy / uResolution.xy;
        
        // Glitch Offset
        float block = floor(uv.y * 10.0);
        float noise = random(float2(block, floor(uTime * 10.0)));
        
        if (noise > 0.9) {
            uv.x += 0.05 * sin(uTime * 20.0);
        }
        
        half4 col = mix(uColorStart, uColorEnd, uv.x);
        
        // Chromatic Abberation on edges
        if (noise > 0.8) {
            col.r = mix(uColorStart.r, uColorEnd.r, uv.x + 0.02);
            col.b = mix(uColorStart.b, uColorEnd.b, uv.x - 0.02);
        }
        
        return col;
    }
"""

}