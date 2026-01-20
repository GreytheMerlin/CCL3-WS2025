package com.example.snorly.feature.alarm.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.snorly.core.common.components.BackTopBar
import com.example.snorly.core.ui.shader.ShaderLibrary
import com.example.snorly.feature.alarm.components.DeviceAlarmBanner
import com.example.snorly.feature.alarm.components.OrganicShaderCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RingtoneScreen(
    onBack: () -> Unit, onCategoryClick: (String) -> Unit = {}
) {
    Scaffold(
        topBar = { BackTopBar(title = "Ringtones", onBackClick = onBack) }

    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 16.dp,
                bottom = 16.dp,
                start = 16.dp,
                end = 16.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item(span = { GridItemSpan(2) }) { DeviceAlarmBanner(onClick = { onCategoryClick("device") }) }

            // 1. Spotify (Green/Black - Electric)
            item {
                OrganicShaderCard(
                    title = "Spotify",
                    subtitle = "Your music",
                    icon = Icons.Default.MusicNote,
                    color1 = Color(0xFF1DB954),
                    color2 = Color(0xFF000000),
                    shaderCode = ShaderLibrary.PULSE_PLASMA,
                    onClick = { onCategoryClick("spotify") })
            }

            // 2. Composer (Gold/Brown - Sharp)
            item {
                OrganicShaderCard(
                    title = "Composer",
                    subtitle = "Create custom",
                    icon = Icons.Default.Build,
                    color1 = Color(0xFFD4AF37),
                    color2 = Color(0xFF3E2723),
                    shaderCode = ShaderLibrary.SILK_FOG,
                    onClick = { onCategoryClick("composer") })
            }

            // 3. Classic (Grey/Blue - Grain)
            item {
                OrganicShaderCard(
                    title = "Classic",
                    subtitle = "Traditional",
                    icon = Icons.Default.Notifications,
                    color1 = Color(0xFF505050),
                    color2 = Color(0xFFF5F2E8), // Not used by this shader, but required by the component signature
                    shaderCode = ShaderLibrary.CLASSIC_PEARL,
                    count = "4 sounds",
                    onClick = { onCategoryClick("classic") })
            }

            // 4. Alarms (Red/Black - Deep Void)
            item {
                OrganicShaderCard(
                    title = "Alarms",
                    subtitle = "Attention",
                    icon = Icons.Outlined.ErrorOutline,
                    color1 = Color(0xFF000000),
                    color2 = Color(0xFFB71C1C),
                    shaderCode = ShaderLibrary.DEEP_VOID,
                    count = "4 sounds",
                    onClick = { onCategoryClick("alarms") })
            }

            // 5. Nature (Green/Teal - Aurora)
            item {
                OrganicShaderCard(
                    title = "Nature",
                    subtitle = "Peaceful",
                    icon = Icons.Default.Landscape,
                    // Color1: Bright Neon Green (The Glow)
                    color1 = Color(0xFF69F0AE),
                    // Color2: Deep Dark Green (The Atmosphere)
                    color2 = Color(0xFF003300),
                    shaderCode = ShaderLibrary.SOFT_BLOOM, // <--- New Shader
                    count = "5 sounds",
                    onClick = { onCategoryClick("nature") })
            }

            // 6. Animals (Orange/Dark - Heatmap)
            item {
                OrganicShaderCard(
                    title = "Animals",
                    subtitle = "Wild calls",
                    icon = Icons.Default.MusicNote,
                    color1 = Color(0xFFFF6F00),
                    color2 = Color(0xFFBF360C),
                    shaderCode = ShaderLibrary.HEATMAP,
                    count = "4 sounds",
                    onClick = { onCategoryClick("animals") })
            }

            // 7. Abstract (Purple/Pink - Liquid)
            item {
                OrganicShaderCard(
                    title = "Abstract",
                    subtitle = "Unique sounds",
                    icon = Icons.Default.Palette,
                    color1 = Color(0xFF7B1FA2),
                    color2 = Color(0xFFC2185B),
                    shaderCode = ShaderLibrary.LIQUID,
                    count = "6 sounds",
                    onClick = { onCategoryClick("abstract") })
            }

            // 8. Sleep (Blue/Indigo - Focus)
            item {
                OrganicShaderCard(
                    title = "Sleep",
                    subtitle = "Ambient noise",
                    icon = Icons.Default.Bedtime,
                    color1 = Color(0xFF304FFE),
                    color2 = Color(0xFF1A237E),
                    shaderCode = ShaderLibrary.FOCUS,
                    count = "8 sounds",
                    onClick = { onCategoryClick("sleep") })
            }
            item {
                OrganicShaderCard(
                    title = "Classic",
                    subtitle = "Traditional",
                    icon = Icons.Default.Notifications,
                    color1 = Color(0xFF70BFE5), // Light GreyBlue
                    color2 = Color(0xFF627885), // Dark Charcoal
                    shaderCode = ShaderLibrary.HEAVY_FILM_GRAIN, // <--- New Shader
                    count = "4 sounds",
                    onClick = { onCategoryClick("classic") })
            }

            // Example: Alarms using INVERTED_PULSE
            // High contrast works best here. Bright edge, dark center.
            item {
                OrganicShaderCard(
                    title = "Alarms",
                    subtitle = "Attention",
                    icon = Icons.Outlined.ErrorOutline,
                    color2 = Color(0xFF5A965A), // Bright Neon Red (Edge)
                    color1 = Color(0xFF253622), // Pure Black (Center)
                    shaderCode = ShaderLibrary.INVERTED_PULSE, // <--- New Shader
                    count = "4 sounds",
                    onClick = { onCategoryClick("alarms") })
            }
            item {
                OrganicShaderCard(
                    title = "Modern",
                    subtitle = "Data Grid",
                    icon = Icons.Default.GraphicEq,
                    // Color 1: Bright Cyan (Big Dots)
                    color1 = Color(0xFF00E5FF),
                    // Color 2: Deep Blue (Small/Medium Dots)
                    color2 = Color(0xFF2962FF),
                    shaderCode = ShaderLibrary.LED_MATRIX, // <--- The new halftone shader
                    count = "4 sounds",
                    onClick = { onCategoryClick("modern") })
            }
            item {
                OrganicShaderCard(
                    title = "Modern",
                    subtitle = "Data Grid",
                    icon = Icons.Default.GraphicEq,
                    // Color 1: Bright Cyan (Big Dots)
                    color1 = Color(0xFF2D628D),
                    // Color 2: Deep Blue (Small/Medium Dots)
                    color2 = Color(0xFF162F73),
                    shaderCode = ShaderLibrary.LED_MATRIX, // <--- The new halftone shader
                    count = "4 sounds",
                    onClick = { onCategoryClick("modern") })
            }
            // Example 2: "Modern" -> Cyan/Blue Data Flow
            // This gives a high-tech, flowing data vibe.
            item {
                OrganicShaderCard(
                    title = "Modern",
                    subtitle = "Contemporary",
                    icon = Icons.Default.GraphicEq,
                    // Bright Cyan
                    color1 = Color(0xFF00E5FF),
                    // Deep Midnight Blue
                    color2 = Color(0xFF001040),
                    shaderCode = ShaderLibrary.LIQUID_FLOW, // <--- The new shader
                    count = "4 sounds",
                    onClick = { onCategoryClick("modern") })
            }
        }
    }
}