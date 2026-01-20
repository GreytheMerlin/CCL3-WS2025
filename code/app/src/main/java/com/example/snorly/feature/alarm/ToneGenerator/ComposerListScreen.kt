package com.example.snorly.feature.alarm.ToneGenerator

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.snorly.core.database.entities.ComposedRingtoneEntity
import com.example.snorly.feature.alarm.components.SoundWaveAnimation
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposerListScreen(
    onBack: () -> Unit,
    onSelect: (ComposedRingtoneEntity) -> Unit,
    viewModel: ComposerListViewModel = viewModel()
) {
    val ringtones by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Recordings", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        containerColor = Color.Black
    ) { innerPadding ->
        if (ringtones.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("No recordings yet.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                contentPadding = innerPadding,
                modifier = Modifier.fillMaxSize()
            ) {
                items(ringtones, key = { it.data.id }) { item ->
                    ComposerListItem(
                        item = item,
                        onPlay = { viewModel.togglePlay(item.data) },
                        onSelect = { onSelect(item.data) },
                        onDelete = { viewModel.deleteRingtone(item.data) }
                    )
                    Divider(color = Color(0xFF1F1F1F), thickness = 1.dp)
                }
            }
        }
    }
}

@Composable
fun ComposerListItem(
    item: ComposedRingtoneUi,
    onPlay: () -> Unit,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    val dateString = dateFormat.format(Date(item.data.createdAt))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPlay() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // PLAY ICON / ANIMATION
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color(0xFF222222), androidx.compose.foundation.shape.CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (item.isPlaying) {
                SoundWaveAnimation(color = Color(0xFF1677FF))
            } else {
                Icon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = "Play",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(Modifier.width(16.dp))

        // INFO
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.data.name,
                color = if (item.isPlaying) Color(0xFF1677FF) else Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = dateString,
                color = Color.Gray,
                style = MaterialTheme.typography.bodySmall
            )
        }

        // DELETE
// ACTIONS ROW
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Select Button
            IconButton(onClick = onSelect) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Select",
                    tint = Color(0xFF1677FF) // Blue to indicate action
                )
            }

            // Delete Button
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.Gray.copy(alpha = 0.5f)
                )
            }
        }
    }
}