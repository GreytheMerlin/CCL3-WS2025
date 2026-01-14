package com.example.snorly.feature.sleep

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val context = LocalContext.current
    val bg = Color.Black
    val cardColor = Color(0xFF1C1C1E)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Log Sleep", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.saveSleep(onSuccess = onSaveSuccess) }) {
                        Icon(Icons.Default.Check, contentDescription = "Save", tint = Color(0xFF4CAF50))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bg)
            )
        },
        containerColor = bg
    ) { innerPadding ->
        if (viewModel.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // START SECTION
                Text("Bedtime", color = Color.Gray, fontSize = 14.sp)
                TimeInputRow(
                    date = viewModel.startDate,
                    time = viewModel.startTime,
                    onDateClick = { showDatePicker(context, viewModel.startDate) { viewModel.startDate = it } },
                    onTimeClick = { showTimePicker(context, viewModel.startTime) { viewModel.startTime = it } }
                )

                Divider(color = Color(0xFF2C2C2E))

                // END SECTION
                Text("Wake up", color = Color.Gray, fontSize = 14.sp)
                TimeInputRow(
                    date = viewModel.endDate,
                    time = viewModel.endTime,
                    onDateClick = { showDatePicker(context, viewModel.endDate) { viewModel.endDate = it } },
                    onTimeClick = { showTimePicker(context, viewModel.endTime) { viewModel.endTime = it } }
                )
            }
        }
    }
}

@Composable
fun TimeInputRow(
    date: LocalDate,
    time: LocalTime,
    onDateClick: () -> Unit,
    onTimeClick: () -> Unit
) {
    val dateFmt = DateTimeFormatter.ofPattern("EEE, MMM d")
    val timeFmt = DateTimeFormatter.ofPattern("hh:mm a")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Date Button
        Box(
            modifier = Modifier
                .background(Color(0xFF2C2C2E), RoundedCornerShape(8.dp))
                .clickable(onClick = onDateClick)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(date.format(dateFmt), color = Color.White, fontWeight = FontWeight.SemiBold)
        }

        // Time Button
        Box(
            modifier = Modifier
                .background(Color(0xFF2C2C2E), RoundedCornerShape(8.dp))
                .clickable(onClick = onTimeClick)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(time.format(timeFmt), color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// --- Native Dialog Helpers ---
fun showDatePicker(context: Context, current: LocalDate, onDateSelected: (LocalDate) -> Unit) {
    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            onDateSelected(LocalDate.of(year, month + 1, dayOfMonth))
        },
        current.year, current.monthValue - 1, current.dayOfMonth
    ).show()
}

fun showTimePicker(context: Context, current: LocalTime, onTimeSelected: (LocalTime) -> Unit) {
    TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            onTimeSelected(LocalTime.of(hourOfDay, minute))
        },
        current.hour, current.minute, false // false = AM/PM mode
    ).show()
}