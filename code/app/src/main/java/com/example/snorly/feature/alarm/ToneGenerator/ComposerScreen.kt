package com.example.snorly.feature.alarm.ToneGenerator

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposerScreen(
    onBack: () -> Unit,
    viewModel: ComposerViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val noteKeys = ToneGenerator.NOTES.keys.sorted() // Simple Sort

    // Save Dialog State
    var showSaveDialog by remember { mutableStateOf(false) }
    var songName by remember { mutableStateOf("") }

    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Name your masterpiece") },
            text = {
                OutlinedTextField(
                    value = songName,
                    onValueChange = { songName = it },
                    label = { Text("Song Name") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.saveRingtone(songName.ifBlank { "My Song" })
                    showSaveDialog = false
                    onBack() // Go back after save
                }) { Text("Save") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Composer Studio", color = Color.White) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // 1. TIMELINE / VISUALIZER
            // Shows a 10s progress bar and dots for notes
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Color(0xFF1A1A1A), RoundedCornerShape(16.dp))
                    .border(1.dp, Color(0xFF333333), RoundedCornerShape(16.dp))
            ) {
                // Progress Bar
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(state.progress)
                        .background(Color(0xFF1677FF).copy(alpha = 0.2f))
                )

                // Notes Visualization
                state.recordedNotes.forEach { note ->
                    // Position based on time (0-10000ms)
                    val percent = note.timeOffset / 10000f
                    Box(
                        modifier = Modifier
                            .absoluteOffset(x = (300.dp * percent)) // Approximate width math, ideally use BoxWithConstraints
                            .padding(top = 40.dp) // Just center vertically-ish
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(getInstrumentColor(note.instrument))
                    )
                }

                // Status Text
                Text(
                    text = when(state.recordingState) {
                        RecordingState.RECORDING -> "RECORDING..."
                        RecordingState.PLAYING -> "PREVIEWING..."
                        else -> "READY"
                    },
                    color = Color.White,
                    modifier = Modifier.align(Alignment.TopCenter).padding(8.dp),
                    style = MaterialTheme.typography.labelSmall
                )
            }

            Spacer(Modifier.height(24.dp))

            // 2. CONTROLS (Record / Play / Save)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Record Button
                ControlButton(
                    icon = Icons.Default.FiberManualRecord,
                    label = "Rec",
                    color = if (state.recordingState == RecordingState.RECORDING) Color.Red else Color.White,
                    onClick = {
                        if (state.recordingState == RecordingState.RECORDING) viewModel.stopRecording()
                        else viewModel.startRecording()
                    }
                )

                // Play Button
                ControlButton(
                    icon = Icons.Default.PlayArrow,
                    label = "Play",
                    color = Color.Green,
                    enabled = state.recordedNotes.isNotEmpty() && state.recordingState == RecordingState.IDLE,
                    onClick = { viewModel.playFullSequence() }
                )

                // Save Button
                ControlButton(
                    icon = Icons.Default.Save,
                    label = "Save",
                    color = Color(0xFF1677FF),
                    enabled = state.recordedNotes.isNotEmpty() && state.recordingState == RecordingState.IDLE,
                    onClick = { showSaveDialog = true }
                )
            }

            Spacer(Modifier.height(24.dp))

            // 3. INSTRUMENT SELECTOR
            Text("Instrument", color = Color.Gray, style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Instrument.entries.forEach { inst ->
                    InstrumentChip(
                        label = inst.name.take(4),
                        selected = state.selectedInstrument == inst,
                        color = getInstrumentColor(inst),
                        onClick = { viewModel.selectInstrument(inst) }
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            // 4. PIANO ROLL
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(noteKeys) { note ->
                    ComposerKey(note) { viewModel.onNoteClick(note) }
                }
            }
        }
    }
}

@Composable
fun ControlButton(icon: ImageVector, label: String, color: Color, enabled: Boolean = true, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Button(
            onClick = onClick,
            enabled = enabled,
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2E)),
            modifier = Modifier.size(60.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(icon, null, tint = if (enabled) color else Color.Gray, modifier = Modifier.size(28.dp))
        }
        Spacer(Modifier.height(4.dp))
        Text(label, color = Color.Gray, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun InstrumentChip(label: String, selected: Boolean, color: Color, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = color.copy(alpha = 0.3f),
            selectedLabelColor = color,
            labelColor = Color.Gray
        ),
        border = FilterChipDefaults.filterChipBorder(
            borderColor = if (selected) color else Color.Gray,
            borderWidth = 1.dp,
            enabled = true,
            selected = selected
        )
    )
}

fun getInstrumentColor(inst: Instrument): Color {
    return when(inst) {
        Instrument.SINE -> Color(0xFF00E5FF) // Cyan
        Instrument.SQUARE -> Color(0xFFFF4081) // Pink
        Instrument.SAWTOOTH -> Color(0xFFFFEB3B) // Yellow
        Instrument.TRIANGLE -> Color(0xFF76FF03) // Green
    }
}

@Composable
fun ComposerKey(note: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF2C2C2E), Color(0xFF1F1F1F))
                )
            )
            .clickable { onClick() }
            .border(1.dp, Color(0xFF333333), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = note,
            color = Color.White,
            style = MaterialTheme.typography.titleMedium
        )
    }
}