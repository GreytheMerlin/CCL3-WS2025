package com.example.snorly.feature.alarm.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color

@Composable
fun SnorlySwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = Modifier.scale(1.1f), // Make it slightly chunkier
        colors = SwitchDefaults.colors(
            // Active State (ON)
            checkedThumbColor = MaterialTheme.colorScheme.background, // Black thumb
            checkedTrackColor = MaterialTheme.colorScheme.primary,    // MoonYellow track
            checkedBorderColor = Color.Transparent,

            // Inactive State (OFF)
            uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant, // Grey thumb
            uncheckedTrackColor = MaterialTheme.colorScheme.surface,          // Dark track
            uncheckedBorderColor = MaterialTheme.colorScheme.outline
        )
    )
}