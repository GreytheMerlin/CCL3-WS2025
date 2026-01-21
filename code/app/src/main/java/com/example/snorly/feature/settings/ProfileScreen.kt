package com.example.snorly.feature.settings

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.snorly.feature.settings.components.CircularSleepPicker
import org.checkerframework.checker.units.qual.h
import org.checkerframework.checker.units.qual.m
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    // Parse existing or Default (23:00 - 07:00)
    var bedTime by remember(state) {
        mutableStateOf(parseLocalTime(state.targetBedTime, 23, 0))
    }
    var wakeTime by remember(state) {
        mutableStateOf(parseLocalTime(state.targetWakeTime, 7, 0))
    }

    // Calculate Duration Live
    val totalMinutes = calculateDuration(bedTime, wakeTime)
    val displayHours = totalMinutes / 60
    val displayMinutes = totalMinutes % 60

    val feedbackHours = totalMinutes / 60.0
    val feedback = getMedicalFeedback(feedbackHours)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Schedule", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val fmt = DateTimeFormatter.ofPattern("HH:mm")
                        viewModel.saveProfile(
                            bedTime = bedTime.format(fmt),
                            wakeTime = wakeTime.format(fmt)
                        )
                        onBack()
                    }) {
                        Icon(Icons.Default.Check, contentDescription = "Save", tint = Color(0xFF4CAF50))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black),
            )
        },
        containerColor = Color.Black
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // 1. Digital Display (Top)
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Bedtime, null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("BEDTIME", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = bedTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.WbSunny, null, tint = Color(0xFFFFD54F), modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("WAKE UP", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = wakeTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // 2. THE CIRCULAR PICKER
            Box(
                modifier = Modifier
                    .weight(1f) // Fill available center space
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                // Background Center Text (Duration)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${displayHours}hr ${displayMinutes}min",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text("Duration", color = Color.Gray, fontSize = 12.sp)
                }

                // The Wheel
                CircularSleepPicker(
                    bedTime = bedTime,
                    wakeTime = wakeTime,
                    onSelectionChange = { newBed, newWake ->
                        bedTime = newBed
                        wakeTime = newWake
                    },
                    modifier = Modifier.fillMaxWidth(0.9f)
                )
            }

            Spacer(Modifier.height(24.dp))

            // 3. MEDICAL FEEDBACK (Bottom)
            Card(
                colors = CardDefaults.cardColors(containerColor = feedback.color.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = feedback.icon,
                        contentDescription = null,
                        tint = feedback.color,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = feedback.message,
                        color = Color.LightGray,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

// --- HELPERS (Make sure these are included at the bottom of the file) ---

data class MedicalFeedback(
    val message: String,
    val color: Color,
    val icon: ImageVector
)

fun parseLocalTime(s: String?, defH: Int, defM: Int): LocalTime {
    return if (s.isNullOrBlank()) LocalTime.of(defH, defM)
    else try { LocalTime.parse(s) } catch (e: Exception) { LocalTime.of(defH, defM) }
}

fun calculateDuration(start: LocalTime, end: LocalTime): Int {
    var diff = ChronoUnit.MINUTES.between(start, end)
    Log.e("Duration", "$diff $start - $end")
    if (diff < 0) diff += 1440 // Handle midnight wrap
    return diff.toInt()
}

fun getMedicalFeedback(hours: Double): MedicalFeedback {
    return when {
        hours < 6.0 -> MedicalFeedback(
            "This schedule is quite short. Most adults need 7-9 hours to function optimally.",
            Color(0xFFFF5252), // Red
            Icons.Default.Warning
        )
        hours < 7.0 -> MedicalFeedback(
            "You are slightly under the recommended range (7-9h). Aim for 7+ hours for better recovery.",
            Color(0xFFFFC107), // Orange
            Icons.Default.Info
        )
        hours in 7.0..9.0 -> MedicalFeedback(
            "Great schedule! 7-9 hours is the medically recommended range for optimal health.",
            Color(0xFF4CAF50), // Green
            Icons.Default.Check
        )
        else -> MedicalFeedback(
            "Long sleep duration. If you consistently need >9 hours, ensure quality is high.",
            Color(0xFF64B5F6), // Blue
            Icons.Default.Info
        )
    }
}