package com.example.snorly.core.ui.shader

object ShaderLibrary {
    // A clean, looped, misty background. No glitch, no noise.
    // Pure "Apple Dark Mode" aesthetic.
    const val GRAINY_GRADIENT_SHADER = """
    uniform float2 uResolution;
    uniform float uTime;
    layout(color) uniform half4 uColorStart; // Explicitly marked as color
    layout(color) uniform half4 uColorEnd;   // Explicitly marked as color
    
    // Pseudo-random noise
    float random(float2 st) {
        return fract(sin(dot(st.xy, float2(12.9898,78.233))) * 43758.5453123);
    }
    
    // 2D Noise
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
        
        // --- FLUID MOVEMENT ---
        float t = uTime * 0.5;
        
        // Distort UVs for liquid feel
        float2 pos = uv;
        pos.x += 0.2 * sin(pos.y * 3.0 + t);
        pos.y += 0.2 * cos(pos.x * 3.0 + t * 0.8);
        
        float n = noise(pos * 2.0 + t);
        
        // --- COLOR MIXING ---
        half4 col = mix(uColorStart, uColorEnd, n);
        
        // Add a "deep void" darkness to corners (Vignette)
        float vig = 1.0 - length(uv - 0.5) * 1.2;
        col.rgb *= smoothstep(0.0, 1.5, vig + 0.5);

        // --- FILM GRAIN (Texture) ---
        float grain = random(uv * uTime) * 0.15;
        col.rgb += grain;
        
        return col;
    }
"""
    // A soft, flowing gradient that feels like slow-moving ink or aurora.
    // Matches the "Noisy Dark" and "Blur" aesthetic of your references.
    const val ORGANIC_GRADIENT = """
        uniform float2 uResolution;
        uniform float uTime;
        layout(color) uniform half4 uColor1; // Primary Color (e.g., Green)
        layout(color) uniform half4 uColor2; // Secondary Color (e.g., Blue)

        // Simple noise for that "premium grain" texture
        float random(float2 st) {
            return fract(sin(dot(st.xy, float2(12.9898,78.233))) * 43758.5453123);
        }

        half4 main(float2 fragCoord) {
            float2 uv = fragCoord.xy / uResolution.xy;
            
            // 1. Slow Down Time for a "Calm" effect
            float t = uTime * 0.25;

            // 2. Create Organic Movement (Warping)
            // We distort the UV coordinates with large, slow sine waves
            float2 pos = uv;
            pos.x += 0.2 * sin(uv.y * 2.5 + t);
            pos.y += 0.2 * cos(uv.x * 2.0 - t * 0.8);

            // 3. Define the two "Light Blobs"
            // Blob A: Moves based on distorted position
            float dist1 = distance(pos, float2(0.5, 0.3));
            float spot1 = smoothstep(0.8, 0.0, dist1); // Soft blur

            // Blob B: Moves in opposition
            float dist2 = distance(pos, float2(0.4, 0.7));
            float spot2 = smoothstep(0.8, 0.0, dist2);

            // 4. Mix the Colors
            // Start with a Deep Dark Void background
            half4 finalCol = half4(0.05, 0.05, 0.07, 1.0);
            
            // Add Color 1 (Primary)
            finalCol = mix(finalCol, uColor1, spot1 * 0.6);
            
            // Add Color 2 (Secondary)
            finalCol = mix(finalCol, uColor2, spot2 * 0.5);

            // 5. Add Subtle Texture (Grain)
            // This stops it from looking like a cheap CSS gradient
            float noise = random(uv + t);
            finalCol.rgb += (noise - 0.5) * 0.06;

            return finalCol;
        }
    """
}