package com.example.snorly.feature.alarm.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.snorly.core.common.components.BackTopBar
import com.example.snorly.feature.alarm.model.Ringtone
import com.example.snorly.feature.alarm.viewmodel.RingtoneListViewModel

@Composable
fun RingtoneListScreen(
    categoryId: String,
    onBack: () -> Unit,
    onRingtoneSelected: (String, String) -> Unit, // (Name, Uri)
    viewModel: RingtoneListViewModel = viewModel()
) {
    val ringtones by viewModel.uiState.collectAsState()

    // Stop audio when leaving the screen
    DisposableEffect(Unit) {
        onDispose { viewModel.stopPreview() }
    }

    Scaffold(
        topBar = {
            BackTopBar(
                title = categoryId.replaceFirstChar { it.uppercase() },
                onBackClick = onBack
            )
        },
        containerColor = Color.Black
    ) { innerPadding ->
        LazyColumn(
            contentPadding = innerPadding,
            modifier = Modifier.fillMaxSize()
        ) {
            items(ringtones) { ringtone ->
                RingtoneItemRow(
                    item = ringtone,
                    onClick = {
                        // 1. Play & update UI
                        viewModel.onRingtoneClick(ringtone)

                        // 2. Pass data back to AlarmCreateScreen immediately
                        onRingtoneSelected(ringtone.title, ringtone.uri)
                    }
                )
                Divider(color = Color(0xFF1F1F1F), thickness = 1.dp)
            }
        }
    }
}

@Composable
fun RingtoneItemRow(
    item: Ringtone,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp, horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Play/Stop Indicator
            Icon(
                imageVector = if (item.isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                contentDescription = null,
                tint = if (item.isSelected) Color(0xFF1677FF) else Color.Gray,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = item.title,
                color = if (item.isSelected) Color(0xFF1677FF) else Color.White,
                fontWeight = if (item.isSelected) FontWeight.Bold else FontWeight.Normal,
                style = MaterialTheme.typography.bodyLarge
            )
        }

        if (item.isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = Color(0xFF1677FF)
            )
        }
    }
}