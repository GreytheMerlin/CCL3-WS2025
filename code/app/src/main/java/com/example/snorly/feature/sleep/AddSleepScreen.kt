package com.example.snorly.feature.sleep

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.snorly.feature.sleep.components.CustomDateTimePicker
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSleepScreen(
    viewModel: AddSleepViewModel,
    onBack: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    val bg = Color.Black

    // UI State for our custom sheet
    var showTimePickerSheet by remember { mutableStateOf(false) }
    var activePickerType by remember { mutableStateOf<PickerType>(PickerType.Start) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Log Sleep", color = Color.White, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.saveSleep(onSuccess = onSaveSuccess) }) {
                        Icon(Icons.Default.Check, contentDescription = "Save", tint = Color(0xFF4CAF50))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = bg)
            )
        },
        containerColor = bg
    ) { innerPadding ->

        Box(modifier = Modifier.fillMaxSize()) {

            // --- MAIN CONTENT ---
            if (viewModel.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .padding(24.dp)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // TIME SECTION
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Time", color = Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.Bold)

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        TimeCard(
                            label = "Bedtime",
                            date = viewModel.startDate,
                            time = viewModel.startTime,
                            onClick = {
                                activePickerType = PickerType.Start
                                showTimePickerSheet = true
                            }
                        )
                        TimeCard(
                            label = "Wake up",
                            date = viewModel.endDate,
                            time = viewModel.endTime,
                            onClick = {
                                activePickerType = PickerType.End
                                showTimePickerSheet = true
                            }
                        )
                    }}

                    // RATING SECTION
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Quality Rating", color = Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    RatingInput(
                        rating = viewModel.rating,
                        onRatingChanged = { viewModel.rating = it }
                    )}

                    // NOTES SECTION
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Notes", color = Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = viewModel.notes,
                        onValueChange = { viewModel.notes = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        placeholder = { Text("How did you sleep?", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF1C1C1E),
                            unfocusedContainerColor = Color(0xFF1C1C1E),
                            focusedBorderColor = Color(0xFF2C2C2E),
                            unfocusedBorderColor = Color(0xFF2C2C2E),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )}

                    if (viewModel.errorMessage != null) {
                        Text(
                            text = viewModel.errorMessage!!,
                            color = Color(0xFFFF5252),
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            // --- CUSTOM LOCKED BOTTOM SHEET ---
            LockedBottomSheet(
                isVisible = showTimePickerSheet,
                onDismiss = { }
            ) {
                val isStart = activePickerType == PickerType.Start
                CustomDateTimePicker(
                    title = if (isStart) "Bedtime" else "Wake Up",
                    initialDate = if (isStart) viewModel.startDate else viewModel.endDate,
                    initialTime = if (isStart) viewModel.startTime else viewModel.endTime,
                    onCancel = { showTimePickerSheet = false },
                    onSave = { newDate, newTime ->
                        if (isStart) {
                            viewModel.startDate = newDate
                            viewModel.startTime = newTime
                        } else {
                            viewModel.endDate = newDate
                            viewModel.endTime = newTime
                        }
                        showTimePickerSheet = false
                    }
                )
            }
        }
    }
}

enum class PickerType { Start, End }

// --- UI COMPONENTS ---

@Composable
fun LockedBottomSheet(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onDismiss() }
        )
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically { it } + fadeIn(),
        exit = slideOutVertically { it } + fadeOut(),
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1C1C1E), RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .clickable(enabled = false) {}
                    .padding(bottom = 32.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun TimeCard(label: String, date: LocalDate, time: LocalTime, onClick: () -> Unit) {
    val dateFmt = DateTimeFormatter.ofPattern("EEE, MMM d")
    val timeFmt = DateTimeFormatter.ofPattern("HH:mm")

    Column(
        modifier = Modifier
            .width(160.dp)
            .background(Color(0xFF1C1C1E), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Text(label, color = Color.Gray, fontSize = 12.sp)
        Spacer(Modifier.height(8.dp))
        Text(
            text = time.format(timeFmt),
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = date.format(dateFmt),
            color = Color(0xFF4A90E2),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun RatingInput(rating: Int, onRatingChanged: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1C1C1E), RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        (1..5).forEach { star ->
            Icon(
                imageVector = if (star <= rating) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = "$star Stars",
                tint = if (star <= rating) Color(0xFFFFC107) else Color.Gray,
                modifier = Modifier
                    .size(32.dp)
                    .clickable { onRatingChanged(star) }
            )
        }
    }
}
