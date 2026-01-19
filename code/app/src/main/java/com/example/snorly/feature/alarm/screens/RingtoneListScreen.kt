package com.example.snorly.feature.alarm.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.snorly.feature.alarm.components.SoundWaveAnimation
import com.example.snorly.feature.alarm.model.Ringtone
import com.example.snorly.feature.alarm.viewmodel.RingtoneListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RingtoneListScreen(
    categoryId: String,
    onBack: () -> Unit,
    onRingtoneSelected: (String, String) -> Unit,
    viewModel: RingtoneListViewModel = viewModel()
) {
    val ringtones by viewModel.uiState.collectAsState()
    // We track the currently selected item to return it when "Save" is clicked
    val selectedRingtone = ringtones.find { it.isSelected }
    val context = LocalContext.current

    // 1. File Picker Launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            // Persist permission so we can read it later (Crucial for alarms!)
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                // Ignore if not supported by provider, but usually needed
            }

            // Get display name
            var name = "Custom Sound"
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (index >= 0) name = cursor.getString(index)
                }
            }

            // Select it immediately
            onRingtoneSelected(name, uri.toString())
        }
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.stopPreview() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(categoryId.replaceFirstChar { it.uppercase() }, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        // Standard Back Icon
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                },
                actions = {
                    // CONFIRM BUTTON: Only visible if something is selected
                    if (selectedRingtone != null) {
                        IconButton(onClick = {
                            // HERE: We pass Name first, then URI. Order matters!
                            onRingtoneSelected(selectedRingtone.title, selectedRingtone.uri)
                        }) {
                            Icon(Icons.Default.Check, contentDescription = "Save", tint = Color(0xFF1677FF))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        containerColor = Color.Black
    ) { innerPadding ->
        LazyColumn(
            contentPadding = innerPadding,
            modifier = Modifier.fillMaxSize()
        ) {

            // 2. Add "Pick from Storage" button ONLY for Device category
            if (categoryId == "device") {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { filePickerLauncher.launch("audio/*") } // Launch Picker
                            .padding(vertical = 16.dp, horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFF2C2C2E), androidx.compose.foundation.shape.CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = "Folder",
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Select from files...",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Divider(color = Color(0xFF1F1F1F), thickness = 1.dp)
                }
            }

            // 3. Existing List
            items(ringtones) { ringtone ->
                RingtoneItemRow(
                    item = ringtone,
                    onClick = {
                        viewModel.onRingtoneClick(ringtone)
                    },
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
//
//    SideEffect {
//        Log.d("UI_DEBUG", "Row '${item.title}': isSelected=${item.isSelected}, isPlaying=${item.isPlaying}")
//    }

    // Define Colors based on state
    val activeColor = Color(0xFF1677FF)
    val inactiveColor = Color.White
    val contentColor = if (item.isSelected) activeColor else inactiveColor
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick) // Click plays/pauses
            .padding(vertical = 16.dp, horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier.size(24.dp),
            contentAlignment = Alignment.Center
        ) {
            if (item.isPlaying) {
                // The new Animation
                SoundWaveAnimation(color = activeColor)
            } else {
                // Static Icon
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = if (item.isSelected) activeColor else Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = item.title,
            color = contentColor,
            fontWeight = if (item.isSelected) FontWeight.Bold else FontWeight.Normal,
            style = MaterialTheme.typography.bodyLarge
        )

    }
}