package com.example.snorly.feature.alarm.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.snorly.core.common.components.BackTopBar
import com.example.snorly.core.ui.shader.ShaderLibrary
import com.example.snorly.feature.alarm.components.DeviceAlarmBanner
import com.example.snorly.feature.alarm.components.OrganicShaderCard
import com.example.snorly.feature.alarm.model.RingtoneData


@Composable
fun RingtoneScreen(
    onBack: () -> Unit,
    onCategoryClick: (String) -> Unit = {}
) {
    Scaffold(topBar = { BackTopBar(title = "Ringtones", onBackClick = onBack) }) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(top = innerPadding.calculateTopPadding() + 16.dp, bottom = 16.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item(span = { GridItemSpan(2) }) { DeviceAlarmBanner(onClick = { onCategoryClick("device") }) }
/*
            // 1. Spotify (Green/Black - Electric)
            item {
                OrganicShaderCard(
                    title = "Spotify", subtitle = "Your music",
                    icon = Icons.Default.MusicNote,
                    color1 = Color(0xFF1DB954), color2 = Color(0xFF000000),
                    shaderCode = ShaderLibrary.PULSE_PLASMA,
                    onClick = { onCategoryClick("spotify") }
                )
            }*/

            // 2. Composer (Gold/Brown - Sharp)
            item {
                OrganicShaderCard(
                    title = "Composer", subtitle = "Create custom",
                    icon = Icons.Default.Build,
                    color1 = Color(0xFFA88F00), color2 = Color(0xFF0A4949),
                    shaderCode = ShaderLibrary.SHARP_GRADIENT,
                    onClick = { onCategoryClick("composer") }
                )
            }

            // 3. Classic (Grey/Blue - Grain)
            item {
                OrganicShaderCard(
                    title = "Classic", subtitle = "Traditional",
                    icon = Icons.Default.Notifications,
                    color1 = Color(0xFF505050),
                    color2 = Color(0xFFF5F2E8), // Not used by this shader, but required by the component signature
                    shaderCode = ShaderLibrary.CLASSIC_PEARL,
                    count = "${ RingtoneData.getSoundsForCategory("classic").size} sounds",
                    onClick = { onCategoryClick("classic") }
                )
            }

            // 4. Alarms (Red/Black - Deep Void)
            item {
                OrganicShaderCard(
                    title = "Alarms", subtitle = "Attention",
                    icon = Icons.Outlined.ErrorOutline,
                    color1 = Color(0xFF000000), color2 = Color(0xFFB71C1C),
                    shaderCode = ShaderLibrary.DEEP_VOID,
                    count = "${ RingtoneData.getSoundsForCategory("alarms").size} sounds", onClick = { onCategoryClick("alarms") }
                )
            }

            // 5. Nature (Green/Teal - Aurora)
            item {
                OrganicShaderCard(
                    title = "Nature", subtitle = "Peaceful",
                    icon = Icons.Default.Landscape,
                    // Color1: Bright Neon Green (The Glow)
                    color1 = Color(0xFF69F0AE),
                    // Color2: Deep Dark Green (The Atmosphere)
                    color2 = Color(0xFF003300),
                    shaderCode = ShaderLibrary.SOFT_BLOOM, // <--- New Shader
                    count = "${ RingtoneData.getSoundsForCategory("nature").size} sounds",
                    onClick = { onCategoryClick("nature") }
                )
            }

            // 6. Animals (Orange/Dark - Heatmap)
            item {
                OrganicShaderCard(
                    title = "Animals", subtitle = "Wild calls",
                    icon = Icons.Default.MusicNote,
                    color1 = Color(0xFFFF6F00), color2 = Color(0xFF330F04),
                    shaderCode = ShaderLibrary.LIQUID_FLOW,
                    count = "${ RingtoneData.getSoundsForCategory("animals").size} sounds", onClick = { onCategoryClick("animals") }
                )
            }

            // 7. Abstract (Purple/Pink - Liquid)
            item {
                OrganicShaderCard(
                    title = "Abstract", subtitle = "Unique sounds",
                    icon = Icons.Default.Palette,
                    color1 = Color(0xFF7B1FA2), color2 = Color(0xFFC2185B),
                    shaderCode = ShaderLibrary.LIQUID,
                    count = "${ RingtoneData.getSoundsForCategory("abstract").size} sounds", onClick = { onCategoryClick("abstract") }
                )
            }

            // 8. Sleep (Blue/Indigo - Focus)
            item {
                OrganicShaderCard(
                    title = "Funny", subtitle = "Ambient noise",
                    icon = Icons.Default.Bedtime,
                    color1 = Color(0xFF304FFE), color2 = Color(0xFF131959),
                    shaderCode = ShaderLibrary.FOCUS,
                    count = "${ RingtoneData.getSoundsForCategory("funny").size} sounds", onClick = { onCategoryClick("funny") }
                )
            }/*
            item {
                OrganicShaderCard(
                    title = "Classic", subtitle = "Traditional",
                    icon = Icons.Default.Notifications,
                    color1 = Color(0xFF70BFE5), // Light GreyBlue
                    color2 = Color(0xFF627885), // Dark Charcoal
                    shaderCode = ShaderLibrary.HEAVY_FILM_GRAIN, // <--- New Shader
                    count = "${ RingtoneData.getSoundsForCategory("classic").size} sounds",
                    onClick = { onCategoryClick("classic") }
                )
            }*/

            // Example: Alarms using INVERTED_PULSE
            // High contrast works best here. Bright edge, dark center.
            item {
                OrganicShaderCard(
                    title = "Motivational", subtitle = "Motivational Speech",
                    icon = Icons.Outlined.ErrorOutline,
                    color2 = Color(0xFF2D628D), // Bright Neon Red (Edge)
                    color1 = Color(0xFF060C1E), // Pure Black (Center)
                    shaderCode = ShaderLibrary.SILK_FOG, // <--- New Shader
                    count = "${ RingtoneData.getSoundsForCategory("motivational").size} sounds",
                    onClick = { onCategoryClick("motivational") }
                )
            }
/*
            item {
                OrganicShaderCard(
                    title = "Modern", subtitle = "Data Grid",
                    icon = Icons.Default.GraphicEq,
                    // Color 1: Bright Cyan (Big Dots)
                    color1 = Color(0xFF2D628D),
                    // Color 2: Deep Blue (Small/Medium Dots)
                    color2 = Color(0xFF162F73),
                    shaderCode = ShaderLibrary.LED_MATRIX, // <--- The new halftone shader
                    count = "${ RingtoneData.getSoundsForCategory("modern").size} sounds",
                    onClick = { onCategoryClick("modern") }
                )
            }
            // Example 2: "Modern" -> Cyan/Blue Data Flow
            // This gives a high-tech, flowing data vibe.
            item {
                OrganicShaderCard(
                    title = "Modern", subtitle = "Contemporary",
                    icon = Icons.Default.GraphicEq,
                    // Bright Cyan
                    color1 = Color(0xFF00E5FF),
                    // Deep Midnight Blue
                    color2 = Color(0xFF001040),
                    shaderCode = ShaderLibrary.LIQUID_FLOW, // <--- The new shader
                    count = "${ RingtoneData.getSoundsForCategory("modern").size} sounds",
                    onClick = { onCategoryClick("modern") }
                )
            }*/
        }
    }
}