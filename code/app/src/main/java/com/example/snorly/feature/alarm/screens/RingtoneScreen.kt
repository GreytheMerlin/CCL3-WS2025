package com.example.snorly.feature.alarm.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.snorly.core.common.components.BackTopBar
import com.example.snorly.feature.alarm.components.DeviceAlarmBanner
import com.example.snorly.feature.alarm.components.RingtoneCategoryCard
import com.example.snorly.feature.alarm.model.RingtoneData
import com.example.snorly.feature.alarm.model.RingtoneItem

@Composable
fun RingtoneScreen(
    onBack: () -> Unit,
    onCategoryClick: (String) -> Unit = {} // Navigate to details
) {
    Scaffold(
        topBar = {
            BackTopBar(
                title = "Ringtones",
                onBackClick = onBack
            )
        }
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
            // 1. Device Alarms Banner (Spans 2 columns)
            item(span = { GridItemSpan(2) }) {
                DeviceAlarmBanner(onClick = { onCategoryClick("device") })
            }

            // 2. The Rest of the Items
            items(RingtoneData.items) { item ->
                when (item) {
                    is RingtoneItem.Spotify -> {
                        RingtoneCategoryCard(
                            title = "Spotify",
                            subtitle = "Your music",
                            icon = Icons.Default.MusicNote, // Replace with Spotify Logo drawable if you have it
                            iconTint = Color(0xFF1DB954), // Spotify Green
                            // Subtle gradient simulating album art feel
                            backgroundBrush = Brush.verticalGradient(
                                colors = listOf(Color(0xFF191414), Color(0xFF1DB954).copy(alpha = 0.2f))
                            ),
                            onClick = { onCategoryClick("spotify") }
                        )
                    }
                    is RingtoneItem.Composer -> {
                        RingtoneCategoryCard(
                            title = "Composer",
                            subtitle = "Create custom",
                            icon = Icons.Default.Build,
                            // Gold/Brown Gradient
                            backgroundBrush = Brush.linearGradient(
                                colors = listOf(Color(0xFF5A4A3A), Color(0xFF2E241E))
                            ),
                            onClick = { onCategoryClick("composer") }
                        )
                    }
                    is RingtoneItem.Category -> {
                        RingtoneCategoryCard(
                            title = item.title,
                            subtitle = item.subtitle,
                            count = item.countText,
                            icon = item.icon,
                            // Diagonal gradient for category cards
                            backgroundBrush = Brush.linearGradient(
                                colors = listOf(item.colorStart, item.colorEnd)
                            ),
                            onClick = { onCategoryClick(item.id) }
                        )
                    }
                    // 'Device' handled above manually to force span
                    else -> {}
                }
            }
        }
    }
}