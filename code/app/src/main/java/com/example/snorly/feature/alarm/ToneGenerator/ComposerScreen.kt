package com.example.snorly.feature.alarm.ToneGenerator

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.rounded.FiberManualRecord
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposerScreen(
    onBack: () -> Unit,
    onListClick: () -> Unit = {},
    viewModel: ComposerViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val notes = ToneGenerator.SCALE_NOTES
    val haptic = LocalHapticFeedback.current

    // Save Dialog State
    var showSaveDialog by remember { mutableStateOf(false) }
    var songName by remember { mutableStateOf("") }

    val arePadsEnabled = state.recordingState != RecordingState.PLAYING

    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Save Recording") },
            text = {
                OutlinedTextField(
                    value = songName,
                    onValueChange = { songName = it },
                    label = { Text("Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveRingtone(songName.ifBlank { "My Song" })
                        showSaveDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1677FF))
                ) { Text("Save") }
            },
            containerColor = Color(0xFF1E1E1E),
            titleContentColor = Color.White,
            textContentColor = Color.White
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Composer") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            null,
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onListClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.List,
                            "Saved Songs",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black
                ),
            )
        },
        containerColor = Color.Black
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // 1. INSTRUMENT DROPDOWN
            InstrumentDropdown(
                selected = state.selectedInstrument,
                onSelect = { viewModel.selectInstrument(it) }
            )

            Spacer(Modifier.height(16.dp))

            // 2. VISUALIZER
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp) // Fixed, tight height
                    .background(Color(0xFF111111), RoundedCornerShape(24.dp))
                    .border(1.dp, Color(0xFF222222), RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
            ) {
                // Get exact dimensions to calculate dot positions
                val containerWidth = maxWidth
                val containerHeight = maxHeight

                // Playhead (Canvas is better for continuous lines)
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val playheadX = size.width * state.progress
                    drawLine(
                        color = Color(0xFF1677FF).copy(alpha = 0.5f),
                        start = Offset(playheadX, 0f),
                        end = Offset(playheadX, size.height),
                        strokeWidth = 2.dp.toPx()
                    )
                }

                // Render Recorded Notes
                state.recordedNotes.forEach { note ->
                    val timePercent = note.timeOffset / 10000f
                    val minFreq = ToneGenerator.SCALE_NOTES.first().second
                    val maxFreq = ToneGenerator.SCALE_NOTES.last().second
                    val pitchPercent = ((note.frequency - minFreq) / (maxFreq - minFreq)).toFloat().coerceIn(0f, 1f)

                    // Invert Y so high pitch is at top.
                    // Add 16dp padding to Top/Bottom so dots don't touch the edges.
                    val verticalPadding = 16.dp
                    val usableHeight = containerHeight - (verticalPadding * 2)

                    // Calculate Y position: Top Padding + (Height * inverted pitch)
                    val yPos = verticalPadding + (usableHeight * (1f - pitchPercent))

                    // Calculate X position
                    val xPos = containerWidth * timePercent

                    Box(
                        modifier = Modifier
                            // Center the 10dp dot on the calculated point
                            .offset(x = xPos - 5.dp, y = yPos - 5.dp)
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(getInstrumentColor(note.instrument))
                            .border(1.dp, Color.White.copy(0.5f), CircleShape)
                    )
                }

                // Overlay Status Text
                Box(modifier = Modifier.padding(12.dp)) {
                    val statusText = when (state.recordingState) {
                        RecordingState.RECORDING -> "REC"
                        RecordingState.PLAYING -> "PLAY"
                        else -> "10s"
                    }
                    val statusColor = when(state.recordingState) {
                        RecordingState.RECORDING -> Color.Red
                        RecordingState.PLAYING -> Color.Green
                        else -> Color.Gray
                    }
                    Text(
                        text = statusText,
                        color = statusColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(Color.Black.copy(0.5f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(Modifier.weight(1f))

            // 3. CONTROLS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Record
                val isRecording = state.recordingState == RecordingState.RECORDING
                CompactControlButton(
                    icon = if (isRecording) Icons.Rounded.Stop else Icons.Rounded.FiberManualRecord,
                    isActive = isRecording,
                    activeColor = Color.Red,
                    defaultColor = Color.White,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (isRecording) viewModel.stopRecording() else viewModel.startRecording()
                    }
                )

                // Play
                val isPlaying = state.recordingState == RecordingState.PLAYING
                CompactControlButton(
                    icon = if (isPlaying) Icons.Rounded.Stop else Icons.Rounded.PlayArrow,
                    isActive = isPlaying,
                    activeColor = Color.Green,
                    defaultColor = Color.White,
                    enabled = state.recordedNotes.isNotEmpty(),
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        if (isPlaying) viewModel.stopRecording() else viewModel.playFullSequence()
                    }
                )

                // Save
                CompactControlButton(
                    icon = Icons.Rounded.Save,
                    isActive = false,
                    activeColor = Color(0xFF1677FF),
                    defaultColor = Color.White,
                    enabled = state.recordedNotes.isNotEmpty() && state.recordingState == RecordingState.IDLE,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showSaveDialog = true
                    }
                )
            }

            // 4. INSTANT RESPONSE PADS
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                itemsIndexed(notes) { index, (noteName, _) ->
                    val isCircle = index % 2 == 0

                    ComposerPad(
                        isCircle = isCircle,
                        activeColor = getInstrumentColor(state.selectedInstrument),
                        enabled = arePadsEnabled,
                        onPlayNote = {
                            viewModel.onNoteClick(noteName)
                        }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// --- UPDATED COMPONENT: INSTANT COMPOSER PAD ---
@Composable
fun ComposerPad(
    isCircle: Boolean,
    activeColor: Color,
    enabled: Boolean = true,
    onPlayNote: () -> Unit
) {
    // 1. We manually track state so we can react instantly to "Down" events
    var isPressed by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    // Visuals based on Enabled/Pressed state
    // If disabled, dim the whole pad
    val contentAlpha = if (enabled) 1f else 0.3f

    // 2. Instant Color Change (No Animation on Press)
    val backgroundColor = if (isPressed) activeColor.copy(alpha = 0.8f) else Color(0xFF252525)
    val borderColor = if (isPressed) Color.White.copy(alpha = 0.9f) else Color(0xFF333333)
    val shape = if (isCircle) CircleShape else RoundedCornerShape(20.dp)

    // 3. Fast Scale Animation
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        label = "scale",
        animationSpec = tween(durationMillis = 50)
    )

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .scale(scale)
            .clip(shape)
            .background(backgroundColor)
            .border(1.dp, borderColor, shape)
            // 4. CRITICAL FIX: Use pointerInput instead of clickable
            // This detects the "Press" (finger down) instantly.
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput // Ignore touches if disabled

                detectTapGestures(
                    onPress = {
                        isPressed = true
                        haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                        onPlayNote() // Play sound IMMEDIATELY

                        tryAwaitRelease() // Wait for finger up

                        isPressed = false
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Simple Center Dot
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(
                    (if (isPressed) Color.White else Color.Gray.copy(alpha = 0.5f))
                        .copy(alpha = contentAlpha) // Apply dimming
                )
        )
    }
}

// ... (InstrumentDropdown, CompactControlButton, getInstrumentColor remain the same) ...
@Composable
fun InstrumentDropdown(
    selected: Instrument,
    onSelect: (Instrument) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .wrapContentSize(Alignment.TopCenter)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(Color(0xFF222222))
                .clickable { expanded = true }
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Box(
                modifier = Modifier.size(8.dp).clip(CircleShape).background(getInstrumentColor(selected))
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = selected.name,
                color = Color.White,
                style = MaterialTheme.typography.labelLarge
            )
            Spacer(Modifier.width(4.dp))
            Icon(Icons.Default.ArrowDropDown, null, tint = Color.Gray)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color(0xFF2C2C2E))
        ) {
            Instrument.entries.forEach { inst ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(getInstrumentColor(inst)))
                            Spacer(Modifier.width(12.dp))
                            Text(inst.name, color = Color.White)
                        }
                    },
                    onClick = {
                        onSelect(inst)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun CompactControlButton(
    icon: ImageVector,
    isActive: Boolean,
    activeColor: Color,
    defaultColor: Color,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val backgroundColor = if (isActive) activeColor.copy(alpha = 0.2f) else Color(0xFF222222)
    val iconColor = if (isActive) activeColor else defaultColor.copy(alpha = if (enabled) 1f else 0.3f)

    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(28.dp)
        )
    }
}

fun getInstrumentColor(inst: Instrument): Color {
    return when(inst) {
        Instrument.SINE -> Color(0xFF01579B) // Cyan
        Instrument.SQUARE -> Color(0xFF880E4F) // Pink
        Instrument.SAWTOOTH -> Color(0xFFF57F17) // Yellow
        Instrument.TRIANGLE -> Color(0xFF33691E) // Green
    }
}