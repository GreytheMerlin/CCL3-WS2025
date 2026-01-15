package com.example.snorly.feature.settings

import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    // Initialize state. If null, use empty string/defaults
    var age by remember(state) { mutableStateOf(state.age?.toString() ?: "") }
    var sex by remember(state) { mutableStateOf(state.sex ?: "") }
    var chronotype by remember(state) { mutableStateOf(state.chronotype ?: "") }
    var sleepNeed by remember(state) { mutableStateOf(state.sleepNeedCategory ?: "") }

    // For times, we keep them null internally if not set, or use a placeholder for display
    var bedTime by remember(state) { mutableStateOf(state.targetBedTime) }
    var wakeTime by remember(state) { mutableStateOf(state.targetWakeTime) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Optimization Profile", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.saveProfile(
                            age = age.toIntOrNull(),
                            sex = sex.ifBlank { null },
                            chronotype = chronotype.ifBlank { null },
                            sleepNeed = sleepNeed.ifBlank { null },
                            bedTime = bedTime,
                            wakeTime = wakeTime
                        )
                        onBack()
                    }) {
                        Icon(Icons.Default.Check, contentDescription = "Save", tint = Color(0xFF4CAF50))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        containerColor = Color.Black
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 1. AGE & SEX
            item {
                Text("Bio", color = Color.Gray, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = age,
                        onValueChange = { if(it.length <= 2) age = it.filter { char -> char.isDigit() } },
                        label = { Text("Age") },
                        placeholder = { Text("e.g. 25") }, // Hint if empty
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF4A90E2),
                            unfocusedBorderColor = Color.Gray
                        )
                    )
                    ProfileDropdown(
                        label = "Sex",
                        current = sex,
                        options = listOf("Male", "Female"),
                        onSelect = { sex = it },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 2. CHRONOTYPE
            item {
                Text("Sleep Type", color = Color.Gray, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))

                ProfileOptionCard(
                    title = "Chronotype",
                    currentValue = chronotype,
                    options = listOf("Early Bird", "Intermediate", "Night Owl"),
                    onSelect = { chronotype = it }
                )

                Spacer(Modifier.height(16.dp))

                ProfileOptionCard(
                    title = "Sleep Need",
                    currentValue = sleepNeed,
                    options = listOf("Low (<7h)", "Average (7-8h)", "High (>9h)"),
                    onSelect = { sleepNeed = it.split(" ")[0] }
                )
            }

            // 3. TARGET SCHEDULE
            item {
                Text("Target Schedule", color = Color.Gray, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    // Display "--:--" if null
                    TimePickerButton("Bedtime", bedTime ?: "--:--") {
                        showTimePicker(context, bedTime ?: "23:00") { newTime -> bedTime = newTime }
                    }
                    TimePickerButton("Wake Up", wakeTime ?: "--:--") {
                        showTimePicker(context, wakeTime ?: "07:00") { newTime -> wakeTime = newTime }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileDropdown(
    label: String,
    current: String,
    options: List<String>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        OutlinedTextField(
            value = current,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                IconButton(onClick = { expanded = true }) { /* Icon */ }
            },
            modifier = Modifier.fillMaxWidth().clickable { expanded = true },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun ProfileOptionCard(
    title: String,
    currentValue: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    Column {
        Text(title, color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { option ->
                val isSelected = currentValue == option || (title == "Sleep Need" && option.startsWith(currentValue))
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelect(option) },
                    label = { Text(option) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF4A90E2),
                        selectedLabelColor = Color.White
                    )
                )
            }
        }
    }
}

@Composable
fun TimePickerButton(label: String, time: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C1C1E)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.width(160.dp).height(80.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, color = Color.Gray, fontSize = 12.sp)
            Text(time, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
    }
}

fun showTimePicker(context: android.content.Context, current: String, onTimeSelected: (String) -> Unit) {
    val parts = current.split(":")
    val h = parts.getOrElse(0) { "00" }.toInt()
    val m = parts.getOrElse(1) { "00" }.toInt()

    TimePickerDialog(context, { _, hour, minute ->
        onTimeSelected(String.format("%02d:%02d", hour, minute))
    }, h, m, true).show()
}