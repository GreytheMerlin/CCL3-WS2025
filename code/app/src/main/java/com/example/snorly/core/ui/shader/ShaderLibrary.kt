package com.example.snorly.core.ui.shader

object ShaderLibrary {

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

    const val LIQUID = """
        uniform float2 uResolution;
        uniform float uTime;
        layout(color) uniform half4 uColor1;
        layout(color) uniform half4 uColor2;

        half4 main(float2 fragCoord) {
            float2 uv = fragCoord.xy / uResolution.xy;
            float t = uTime * 0.2;
            
            // Warp
            float2 pos = uv;
            pos.x += 0.2 * sin(pos.y * 3.0 + t);
            pos.y += 0.2 * cos(pos.x * 2.5 - t);
            
            // Soft Blob shape
            float d = distance(pos, float2(0.5));
            float mask = 1.0 - smoothstep(0.2, 0.8, d);
            
            // Color Mix
            half4 col = mix(uColor1, uColor2, pos.x + pos.y);
            col *= mask * 1.2; // Boost brightness slightly
            
            return half4(col.rgb, 1.0);
        }
    """

    // 2. GRAIN_FOG (Textured)
    // Smoky with film grain. Great for "Classic" or "Lo-Fi".
    const val GRAIN_FOG = """
        uniform float2 uResolution;
        uniform float uTime;
        layout(color) uniform half4 uColor1;
        layout(color) uniform half4 uColor2;

        float random(float2 st) { return fract(sin(dot(st.xy, float2(12.9898,78.233))) * 43758.5453123); }

        half4 main(float2 fragCoord) {
            float2 uv = fragCoord.xy / uResolution.xy;
            float t = uTime * 0.15;
            
            // Slow drifting gradient
            float mixVal = sin(uv.x * 2.0 + t) * 0.5 + 0.5;
            half4 col = mix(uColor1, uColor2, mixVal);
            
            // Vignette
            col *= 1.0 - distance(uv, float2(0.5));
            
            // Heavy Grain
            float noise = random(uv * uTime) * 0.15;
            col.rgb += noise;
            
            return half4(col.rgb, 1.0);
        }
    """


    // 4. FOCUS (Bokeh)
    // Soft, out-of-focus blobs. Good for "Sleep" or "Ambient".
    const val FOCUS = """
        uniform float2 uResolution;
        uniform float uTime;
        layout(color) uniform half4 uColor1;
        layout(color) uniform half4 uColor2;

        half4 main(float2 fragCoord) {
            float2 uv = fragCoord.xy / uResolution.xy;
            float t = uTime * 0.2;
            
            // Two orbiting blobs
            float2 p1 = float2(0.5) + float2(sin(t), cos(t)) * 0.3;
            float2 p2 = float2(0.5) + float2(cos(t * 0.7), sin(t * 0.8)) * 0.3;
            
            float d1 = 1.0 - smoothstep(0.0, 0.6, distance(uv, p1));
            float d2 = 1.0 - smoothstep(0.0, 0.6, distance(uv, p2));
            
            half4 col = uColor1 * d1 + uColor2 * d2;
            col.rgb += 0.05; // Lift blacks slightly
            
            return half4(col.rgb, 1.0);
        }
    """

    // 5. ELECTRIC (Sharp Contrast)
    // Higher contrast, brighter. Good for "Spotify" or "Modern".
    const val ELECTRIC = """
        uniform float2 uResolution;
        uniform float uTime;
        layout(color) uniform half4 uColor1;
        layout(color) uniform half4 uColor2;

        half4 main(float2 fragCoord) {
            float2 uv = fragCoord.xy / uResolution.xy;
            float t = uTime * 0.5;
            
            float val = sin(uv.x * 10.0 + uv.y * 10.0 + t);
            val = smoothstep(0.4, 0.6, val); // Sharpen edges
            
            half4 col = mix(uColor1, uColor2, uv.y);
            col.rgb *= 0.5 + 0.5 * val;
            
            // Darken edges
            col.rgb *= smoothstep(1.0, 0.2, distance(uv, float2(0.5)));
            
            return half4(col.rgb, 1.0);
        }
    """

    // 7. HEATMAP (Blobby)
    // Looks like a thermal camera. Good for "Animals".
    const val HEATMAP = """
        uniform float2 uResolution;
        uniform float uTime;
        layout(color) uniform half4 uColor1;
        layout(color) uniform half4 uColor2;

        half4 main(float2 fragCoord) {
            float2 uv = fragCoord.xy / uResolution.xy;
            float t = uTime * 0.3;
            
            float n = sin(uv.x * 4.0 + t) * cos(uv.y * 4.0 + t);
            half4 col = mix(uColor1, uColor2, n * 0.5 + 0.5);
            
            // Soften significantly
            col.rgb *= 1.0 - distance(uv, float2(0.5));
            
            return half4(col.rgb, 1.0);
        }
    """

    // 8. SHARP_GRADIENT (The "Noah" Look)
    // Clean, linear gradient with a sharp horizon. Good for "Composer".
    const val SHARP_GRADIENT = """
        uniform float2 uResolution;
        uniform float uTime;
        layout(color) uniform half4 uColor1;
        layout(color) uniform half4 uColor2;

        half4 main(float2 fragCoord) {
            float2 uv = fragCoord.xy / uResolution.xy;
            float t = uTime * 0.1;
            
            // Tilt line
            float line = uv.y + sin(uv.x + t) * 0.2;
            
            // Sharp blending
            float blend = smoothstep(0.4, 0.6, line);
            
            half4 col = mix(uColor1, uColor2, blend);
            
            // Add subtle noise texture for realism
            float noise = fract(sin(dot(uv, float2(12.9, 78.2))) * 43758.5);
            col.rgb += noise * 0.05;
            
            return half4(col.rgb, 1.0);
        }
    """

    // 1. CLASSIC_PEARL (Clean, Modern, Off-White)
    // EFFECT: A slow-moving, liquid ceramic or pearl surface.
    // VIBE: Clean, premium, organic, and tactile.
    const val CLASSIC_PEARL = """
        uniform float2 uResolution;
        uniform float uTime;
        layout(color) uniform half4 uColor1; // Main Color
        layout(color) uniform half4 uColor2; // Shadow Color (Must be defined to prevent crash)

        // Simple pseudo-random noise
        float random(float2 st) {
            return fract(sin(dot(st.xy, float2(12.9898,78.233))) * 43758.5453123);
        }

        half4 main(float2 fragCoord) {
            float2 uv = fragCoord.xy / uResolution.xy;
            uv.x *= uResolution.x / uResolution.y;
            
            float t = uTime * 0.15;

            // 1. Subtle Domain Warping
            float2 pos = uv;
            pos.x += 0.03 * sin(pos.y * 4.0 + t);
            pos.y += 0.04 * cos(pos.x * 3.5 - t * 0.8);

            // 2. Base Shape & Depth
            float d = distance(pos, float2(0.5 * (uResolution.x / uResolution.y), 0.5));
            float depth = smoothstep(0.7, 0.1, d); 

            // 3. Highlight
            float2 highlightPos = float2(0.45, 0.45) + float2(sin(t * 0.5), cos(t * 0.4)) * 0.05;
            float highlight = smoothstep(0.4, 0.0, distance(pos, highlightPos));

            // 4. Texture
            float grain = random(uv * 200.0) * 0.025;

            // 5. Compose Final Color
            // FIX: We MUST mix uColor1 and uColor2 so the compiler sees uColor2 is used.
            // Since you pass the same color for both in Kotlin, the look remains consistent.
            half4 col = mix(uColor1, uColor2, depth * 0.3);
            
            // Add slight depth darkening
            col.rgb *= 0.95 + 0.05 * depth;
            
            // Add Pearl highlight
            col.rgb += highlight * 0.12;
            
            // Apply Grain
            col.rgb -= grain;
            
            // Clamp to keep it clean
            col.rgb = clamp(col.rgb, half3(0.0), half3(1.0));
            
            return half4(col.rgb, 1.0);
        }
    """

    // 2. PULSE PLASMA (Electric Glow) - For "Spotify"
    // EFFECT: Smooth, glowing green energy waves.
    // LOGIC: Sine waves mixing with a radial glow.
    const val PULSE_PLASMA = """
        uniform float2 uResolution;
        uniform float uTime;
        layout(color) uniform half4 uColor1; // Deep Black/Green
        layout(color) uniform half4 uColor2; // Bright Neon Green

        half4 main(float2 fragCoord) {
            float2 uv = fragCoord.xy / uResolution.xy;
            
            // Center the coordinates (-0.5 to 0.5)
            float2 centerUV = uv - 0.5;
            
            // 1. Orbiting Plasma Blobs (Perfect Loop)
            float t = uTime * 0.3;
            float2 p1 = float2(sin(t), cos(t)) * 0.3;
            float2 p2 = float2(sin(t + 3.14), cos(t + 3.14)) * 0.3; // Opposite side
            
            // Calculate distances
            float d1 = length(centerUV - p1);
            float d2 = length(centerUV - p2);
            
            // 2. Combine fields (Metaball-ish)
            float field = 0.1 / d1 + 0.1 / d2;
            
            // 3. Add Ripple Waves
            float ripple = sin(length(centerUV) * 10.0 - t * 4.0) * 0.05;
            field += ripple;
            
            // 4. Color Mapping
            // Smoothstep makes the glow distinct but soft
            float intensity = smoothstep(0.2, 0.8, field);
            
            half4 col = mix(uColor1, uColor2, intensity);
            
            return col;
        }
    """
    // 3. SILK FOG (The Elegant Default) - For "Abstract/Modern"
    // EFFECT: Slow moving mist.
    const val SILK_FOG = """
        uniform float2 uResolution;
        uniform float uTime;
        layout(color) uniform half4 uColor1;
        layout(color) uniform half4 uColor2;

        half4 main(float2 fragCoord) {
            float2 uv = fragCoord.xy / uResolution.xy;
            float t = uTime * 0.2; // Slow
            
            // Warp
            float2 pos = uv;
            pos.x += 0.1 * sin(pos.y * 3.0 + t);
            pos.y += 0.1 * cos(pos.x * 2.0 - t);
            
            float fog = 0.5 + 0.5 * sin(pos.x * 4.0 + pos.y * 4.0);
            
            half4 col = mix(uColor1, uColor2, fog);
            
            // Vignette
            col.rgb *= smoothstep(1.2, 0.2, distance(uv, float2(0.5)));
            return col;
        }
    """

    // 4. DEEP VOID (Ominous) - For "Alarms"
    // EFFECT: Breathing darkness with red edges.
    const val DEEP_VOID = """
        uniform float2 uResolution;
        uniform float uTime;
        layout(color) uniform half4 uColor1;
        layout(color) uniform half4 uColor2;

        half4 main(float2 fragCoord) {
            float2 uv = fragCoord.xy / uResolution.xy;
            
            // Breathing radius
            float breath = 0.5 + 0.1 * sin(uTime * 1.5);
            
            float d = distance(uv, float2(0.5));
            
            // Inverted glow: Center is dark, edges are bright
            float glow = smoothstep(0.2, breath + 0.4, d);
            
            half4 col = mix(uColor1, uColor2, glow);
            return col;
        }
    """

    const val SOFT_BLOOM = """
        uniform float2 uResolution;
        uniform float uTime;
        layout(color) uniform half4 uColor1; // Bright/Center Color (e.g. Neon Green)
        layout(color) uniform half4 uColor2; // Deep/Outer Color (e.g. Dark Jungle Green)

        // Soft noise for texture
        float random(float2 st) { return fract(sin(dot(st.xy, float2(12.9898,78.233))) * 43758.5453123); }

        half4 main(float2 fragCoord) {
            float2 uv = fragCoord.xy / uResolution.xy;
            float ratio = uResolution.x / uResolution.y;
            
            // Center coordinates
            float2 pos = uv;
            pos.x *= ratio;
            float2 center = float2(0.5 * ratio, 0.5);

            float t = uTime * 0.2; // Slow, calm movement

            // 1. BACKGROUND: A drifting vertical gradient
            // It waves slowly to feel like underwater or aurora
            float bgWave = uv.y + 0.1 * sin(uv.x * 2.0 + t);
            half4 bg = mix(uColor2, uColor1 * 0.4, bgWave * 0.6);

            // 2. THE BLOOM: Abstract Organic Shape
            float dist = distance(pos, center);
            
            // Get angle for petal-like distortion (5 lobes)
            float angle = atan(pos.y - center.y, pos.x - center.x);
            // Very subtle distortion so it's not a perfect circle
            float distortion = cos(angle * 5.0 + t) * 0.03; 
            
            // Breathing radius
            float radius = 0.45 + 0.05 * sin(t * 1.5);
            
            // 3. GLOW CALCULATION
            // Soft, wide falloff. No hard edges.
            float glow = 1.0 - smoothstep(0.0, radius + distortion, dist);
            
            // 4. COMPOSITE
            // Mix background with the bright center color
            half4 finalCol = mix(bg, uColor1, glow * 0.5); // 50% opacity blend for softness
            
            // 5. TEXTURE
            // Add subtle grain to match the "Material" feel of other cards
            float grain = random(uv * 1.5 + t * 0.1) * 0.04;
            finalCol.rgb += grain;

            return half4(finalCol.rgb, 1.0);
        }
    """

    const val HEAVY_FILM_GRAIN = """
        uniform float2 uResolution;
        uniform float uTime;
        layout(color) uniform half4 uColor1; // Lighter Color
        layout(color) uniform half4 uColor2; // Darker Color

        // High-frequency noise for grain
        float random(float2 st) {
            return fract(sin(dot(st.xy, float2(12.9898,78.233))) * 43758.5453123);
        }

        half4 main(float2 fragCoord) {
            float2 uv = fragCoord.xy / uResolution.xy;
            float t = uTime * 0.1; // Very slow base movement

            // 1. Base Organic Gradient (Warped diagonal)
            float2 pos = uv;
            pos.x += 0.1 * sin(pos.y * 2.0 + t);
            float mixVal = smoothstep(0.0, 1.0, pos.x + pos.y * 0.5);
            half4 baseCol = mix(uColor2, uColor1, mixVal);

            // 2. Heavy Cinematic Grain
            // We use time to make the grain dance rapidly
            float noise = random(uv + uTime * 10.0);
            
            // "Overlay" blend mode simulation for gritty contrast
            // Darkens darks, brightens brights based on the noise value.
            float grainStrength = 0.12; // Adjust for more/less grit
            baseCol.rgb += (noise - 0.5) * grainStrength;

            // 3. Strong Vignette (Focus attention center)
            float dist = distance(uv, float2(0.5));
            baseCol.rgb *= smoothstep(1.0, 0.2, dist);

            return half4(baseCol.rgb, 1.0);
        }
    """

    // 7. INVERTED_PULSE (Outside-In Glow)
    // EFFECT: The edges glow brightly and pulse inwards towards a dark center.
    // VIBE: Ominous, containing energy, dramatic.
    const val INVERTED_PULSE = """
        uniform float2 uResolution;
        uniform float uTime;
        layout(color) uniform half4 uColor1; // Edge/Glow Color (Bright)
        layout(color) uniform half4 uColor2; // Center/Void Color (Dark)

        // Simple noise for texture
        float random(float2 st) { return fract(sin(dot(st.xy, float2(12.9898,78.233))) * 43758.5453123); }

        half4 main(float2 fragCoord) {
            float2 uv = fragCoord.xy / uResolution.xy;
            
            // Center coordinates
            float2 pos = uv - 0.5;
            // Correct aspect ratio for a circular pulse, remove for a boxy pulse
            pos.x *= uResolution.x / uResolution.y;

            // 1. Calculate Distance from Center
            // Add subtle warping so it's not a perfect geometric circle
            float warp = 0.05 * sin(atan(pos.y, pos.x) * 5.0 + uTime * 0.5);
            float dist = length(pos) + warp;

            // 2. Breathing Animation
            // The threshold moves out and in, controlling how much darkness fills the box.
            // A slow, deep breath.
            float breathState = sin(uTime * 0.8) * 0.5 + 0.5; // 0.0 to 1.0
            // Threshold oscillates between 0.2 (mostly glowing) and 0.5 (mostly dark center)
            float threshold = mix(0.2, 0.5, breathState);

            // 3. Inverted Glow Calculation
            // smoothstep(edge, center, dist) -> 1.0 at edge, 0.0 at center
            // We want a wide, soft transition.
            float glow = smoothstep(threshold - 0.2, threshold + 0.3, dist);

            // 4. Mix Colors
            // 0.0 (Center) gets uColor2, 1.0 (Edge) gets uColor1
            half4 col = mix(uColor2, uColor1, glow);

            // 5. Subtle Texture
            float grain = random(uv * 2.0 + uTime * 0.1) * 0.03;
            col.rgb += grain;

            return half4(col.rgb, 1.0);
        }
    """

    // 8. LIQUID_FLOW (Complex Domain Warping)
    // EFFECT: Deep, swirling, molten liquid that folds onto itself.
    // VIBE: Extremely premium, organic, mesmerizing. High computation aesthetic.
    const val LIQUID_FLOW = """
        uniform float2 uResolution;
        uniform float uTime;
        layout(color) uniform half4 uColor1; // Highlight Color (Bright liquid)
        layout(color) uniform half4 uColor2; // Deep Color (Background flow)

        // --- Noise Helper Functions ---
        float random(float2 st) {
            return fract(sin(dot(st.xy, float2(12.9898,78.233))) * 43758.5453123);
        }

        float noise(float2 st) {
            float2 i = floor(st);
            float2 f = fract(st);
            // Cubic Hermite Interpolation
            float2 u = f*f*(3.0-2.0*f);
            return mix( mix( random( i + float2(0.0,0.0) ), random( i + float2(1.0,0.0) ), u.x),
                        mix( random( i + float2(0.0,1.0) ), random( i + float2(1.0,1.0) ), u.x), u.y);
        }

        // Fractal Brownian Motion (Layered Noise)
        // FIX: Removed #define OCTAVES 4. Replaced loop limit with literal '4'.
        float fbm(float2 st) {
            float value = 0.0;
            float amplitude = 0.5;
            // Iterate 4 times manually
            for (int i = 0; i < 4; i++) {
                value += amplitude * noise(st);
                st *= 2.0;
                amplitude *= 0.5;
            }
            return value;
        }
        // ---------------------------

        half4 main(float2 fragCoord) {
            float2 uv = fragCoord.xy / uResolution.xy;
            // Correct aspect ratio
            uv.x *= uResolution.x / uResolution.y;

            // Slow time down for majestic movement
            float t = uTime * 0.15;

            // --- Domain Warping Logic ---
            
            // Layer 1: Base movement vector field
            float2 q = float2(0.0);
            q.x = fbm( uv + 0.00*t );
            q.y = fbm( uv + float2(1.0) );

            // Layer 2: Warp the coordinates based on Layer 1
            float2 r = float2(0.0);
            r.x = fbm( uv + 1.0*q + float2(1.7,9.2)+0.15*t );
            r.y = fbm( uv + 1.0*q + float2(8.3,2.8)+0.126*t );

            // Layer 3: Final noise sample using the heavily warped coordinates 'r'
            float f = fbm(uv + r);

            // --- Color Grading ---
            
            // Sharpen the contrast of the noise pattern
            // This makes the "flow lines" more distinct.
            float mixVal = f * f * f + 0.6 * f * f + 0.5 * f;
            mixVal = clamp(mixVal, 0.0, 1.0);
            
            // Mix the two defining colors based on the warped pattern
            half4 col = mix(uColor2, uColor1, mixVal);

            // Add subtle vignette to darken edges and focus on the center flow
            float vig = 1.0 - distance(fragCoord.xy / uResolution.xy, float2(0.5)) * 0.6;
            col.rgb *= vig;

            // Subtle grain texture for realism
            float grain = random(uv * uTime) * 0.03;
            col.rgb -= grain;

            return half4(col.rgb, 1.0);
        }
    """
    // 8. LED_MATRIX (Halftone / Dot Grid)
    // EFFECT: A grid of dots where dot size = brightness of the underlying flow.
    // VIBE: Digital, retro-futuristic, high-tech display.
    // Matches your reference images of "bright big LED, dark small black".
    const val LED_MATRIX = """
        uniform float2 uResolution;
        uniform float uTime;
        layout(color) uniform half4 uColor1; // Bright/Active Color
        layout(color) uniform half4 uColor2; // Darker/Background Color

        half4 main(float2 fragCoord) {
            float2 uv = fragCoord.xy / uResolution.xy;
            // Correct aspect ratio so LEDs are perfect circles
            uv.x *= uResolution.x / uResolution.y;

            // 1. Grid Setup
            // 'density' determines how many LEDs fit on screen. Higher = smaller dots.
            float density = 40.0; 
            
            // Calculate which "Cell" (LED) we are in
            float2 gridUV = uv * density;
            float2 cellID = floor(gridUV);       // Integer ID of the cell
            float2 cellPos = fract(gridUV) - 0.5; // Coordinate inside the cell (-0.5 to 0.5)

            // 2. Sample the Animation
            // We sample the movement at the *center* of the cell (cellID)
            // so the whole dot reacts uniformly.
            float2 samplePos = cellID / density;
            float t = uTime * 0.5;

            // Generate a flowing plasma value (0.0 to 1.0)
            float flow = sin(samplePos.x * 4.0 + t);
            flow += sin(samplePos.y * 4.0 + t * 0.8);
            flow += sin((samplePos.x + samplePos.y) * 3.0 - t * 1.5);
            // Remap from approx -3..3 to 0..1
            float intensity = flow * 0.16 + 0.5;
            intensity = clamp(intensity, 0.0, 1.0);

            // 3. Halftone Logic (Dot Size)
            // Bright areas = Large Radius (up to 0.45, nearly touching)
            // Dark areas = Tiny Radius (down to 0.0)
            float radius = intensity * 0.45;

            // Draw the circle for this cell
            float dist = length(cellPos);
            // Smoothstep for anti-aliased edges
            float dotMask = smoothstep(radius, radius - 0.1, dist);

            // 4. Color & Composition
            // Mix colors based on intensity (brighter dots are uColor1)
            half4 dotColor = mix(uColor2, uColor1, intensity);
            
            // Apply the circle mask. Outside the circle is pure black.
            half4 finalCol = half4(0.0); // Black background
            finalCol = mix(finalCol, dotColor, dotMask);

            return finalCol;
        }
    """
}

