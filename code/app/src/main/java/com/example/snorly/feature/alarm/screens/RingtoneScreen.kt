package com.example.snorly.feature.alarm.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.snorly.core.common.components.BackTopBar
import com.example.snorly.core.ui.shader.ShaderLibrary
import com.example.snorly.feature.alarm.components.*
import com.example.snorly.feature.alarm.model.RingtoneData
import com.example.snorly.feature.alarm.model.RingtoneItem
import com.example.snorly.feature.alarm.model.ShaderType

@Composable
fun RingtoneScreen(
    onBack: () -> Unit,
    onCategoryClick: (String) -> Unit = {}
) {
    Scaffold(
        topBar = {
            BackTopBar(title = "Ringtones", onBackClick = onBack)
        }
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

            // 2. All Cards (Unified)
            items(RingtoneData.items) { item ->
                when (item) {
                    is RingtoneItem.Device -> { /* Already handled above */ }

                    is RingtoneItem.Card -> {
                        // Map Shader Enum to String
                        val shaderCode = when (item.shader) {
                            ShaderType.NEBULA -> ShaderLibrary.NEBULA
                            ShaderType.GRID -> ShaderLibrary.GRID
                            ShaderType.WAVES -> ShaderLibrary.WAVES
                            ShaderType.RETRO_NOISE -> ShaderLibrary.RETRO_NOISE
                            ShaderType.CYBER_GLITCH -> ShaderLibrary.CYBER_GLITCH
                            ShaderType.AURORA -> ShaderLibrary.AURORA
                        }

                        ExperimentalRingtoneCard(
                            title = item.title,
                            subtitle = item.subtitle,
                            icon = item.icon,
                            count = item.countText.takeIf { it.isNotEmpty() },
                            colorStart = item.colorStart,
                            colorEnd = item.colorEnd,
                            shaderCode = shaderCode,
                            shapeStart = getShapeForType(item.shapeStart),
                            shapeEnd = getShapeForType(item.shapeEnd),
                            onClick = { onCategoryClick(item.id) }
                        )
                    }
                }
            }
        }
    }
}