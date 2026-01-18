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
import com.example.snorly.feature.alarm.components.AuroraRingtoneCard
import com.example.snorly.feature.alarm.components.DeviceAlarmBanner

@Composable
fun RingtoneScreen(
    onBack: () -> Unit,
    onCategoryClick: (String) -> Unit = {}
) {
    Scaffold(
        topBar = { BackTopBar(title = "Ringtones", onBackClick = onBack) }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 16.dp,
                bottom = 16.dp, start = 16.dp, end = 16.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. Device Banner
            item(span = { GridItemSpan(2) }) {
                DeviceAlarmBanner(onClick = { onCategoryClick("device") })
            }

            // 2. Spotify (Vibrant Green Glow)
            item {
                AuroraRingtoneCard(
                    title = "Spotify", subtitle = "Your music",
                    icon = Icons.Default.MusicNote,
                    accentColor = Color(0xFF1DB954),
                    onClick = { onCategoryClick("spotify") }
                )
            }

            // 3. Composer (Warm Gold Glow)
            item {
                AuroraRingtoneCard(
                    title = "Composer", subtitle = "Create custom",
                    icon = Icons.Default.Build,
                    accentColor = Color(0xFFFFC107),
                    onClick = { onCategoryClick("composer") }
                )
            }

            // 4. Classic (Cool Blue/Grey Glow)
            item {
                AuroraRingtoneCard(
                    title = "Classic", subtitle = "Traditional",
                    icon = Icons.Default.Notifications,
                    accentColor = Color(0xFF90CAF9),
                    count = "4 sounds",
                    onClick = { onCategoryClick("classic") }
                )
            }

            // 5. Alarms (Intense Red Glow)
            item {
                AuroraRingtoneCard(
                    title = "Alarms", subtitle = "Attention",
                    icon = Icons.Outlined.ErrorOutline,
                    accentColor = Color(0xFFFF5252),
                    count = "4 sounds",
                    onClick = { onCategoryClick("alarms") }
                )
            }

            // 6. Nature (Soft Teal Glow)
            item {
                AuroraRingtoneCard(
                    title = "Nature", subtitle = "Peaceful",
                    icon = Icons.Default.Landscape,
                    accentColor = Color(0xFF4DB6AC),
                    count = "5 sounds",
                    onClick = { onCategoryClick("nature") }
                )
            }

            // 7. Animals (Sunset Orange Glow)
            item {
                AuroraRingtoneCard(
                    title = "Animals", subtitle = "Wild calls",
                    icon = Icons.Default.MusicNote,
                    accentColor = Color(0xFFFFB74D),
                    count = "4 sounds",
                    onClick = { onCategoryClick("animals") }
                )
            }
        }
    }
}