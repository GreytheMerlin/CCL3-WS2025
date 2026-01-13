package com.example.snorly.feature.alarm.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.snorly.core.common.components.BackTopBar
import com.example.snorly.feature.alarm.components.DayToggleCircle
import com.example.snorly.feature.alarm.components.RepeatOptionCard

@Composable
fun RepeatScreen(
    initialDays: List<Int> = emptyList(), // e.g. [1, 2, 3] passed from DB
    onBack: () -> Unit,
    onSelectionChange: (List<Int>) -> Unit = {} // Return the new list
) {
    // 1. Manage State locally
    var selectedDays by remember { mutableStateOf(initialDays) }

    // 2. Constants for Logic
    val allDays = (1..7).toList()   // [1,2,3,4,5,6,7]
    val weekDays = (1..5).toList()  // [1,2,3,4,5]
    val weekendDays = listOf(6, 7)  // [6,7]

    // 3. Determine which card should look "Selected"
    fun getPresetType(days: List<Int>): String {
        return when {
            days.isEmpty() -> "Once"
            days.containsAll(allDays) && days.size == 7 -> "Daily"
            days.containsAll(weekDays) && days.size == 5 -> "MonToFri"
            days.containsAll(weekendDays) && days.size == 2 -> "Weekend"
            else -> "Custom"
        }
    }
    val currentPreset = getPresetType(selectedDays)

    Scaffold(
        topBar = {
            BackTopBar(title = "Repeat", onBackClick = onBack)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // --- Option 1: Once ---
            RepeatOptionCard(
                title = "Once",
                isSelected = currentPreset == "Once",
                onClick = {
                    selectedDays = emptyList()
                    onSelectionChange(selectedDays)
                }
            )

            // --- Option 2: Daily ---
            RepeatOptionCard(
                title = "Daily",
                isSelected = currentPreset == "Daily",
                onClick = {
                    selectedDays = allDays
                    onSelectionChange(selectedDays)
                }
            )

            // --- Option 3: Mon to Fri ---
            RepeatOptionCard(
                title = "Mon To Fri",
                subtitle = "Weekdays",
                isSelected = currentPreset == "MonToFri",
                onClick = {
                    selectedDays = weekDays
                    onSelectionChange(selectedDays)
                }
            )

            // --- Option 4: Weekend ---
            RepeatOptionCard(
                title = "Weekend",
                subtitle = "Saturday and Sunday",
                isSelected = currentPreset == "Weekend",
                onClick = {
                    selectedDays = weekendDays
                    onSelectionChange(selectedDays)
                }
            )

            // --- Option 5: Custom ---
            RepeatOptionCard(
                title = "Custom",
                // Show currently selected days as subtitle (e.g., "Mon, Wed")
                subtitle = if (currentPreset == "Custom") formatDays(selectedDays) else null,
                isSelected = currentPreset == "Custom",
                onClick = {
                    // If we click Custom, and list was empty, default to today or Monday
                    if (selectedDays.isEmpty()) {
                        selectedDays = listOf(1)
                        onSelectionChange(selectedDays)
                    }
                    // Otherwise keep existing Custom selection
                }
            )

            // --- The Expanding Day Selector ---
            AnimatedVisibility(
                visible = currentPreset == "Custom",
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Spacer(Modifier.height(24.dp))

                    // Row of 7 buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val daysLabels = listOf("M", "T", "W", "T", "F", "S", "S")

                        daysLabels.forEachIndexed { index, label ->
                            val dayValue = index + 1 // Convert index 0 -> Day 1
                            val isDaySelected = selectedDays.contains(dayValue)

                            DayToggleCircle(
                                label = label,
                                isSelected = isDaySelected,
                                onClick = {
                                    // Logic: Toggle the day in the list
                                    val newSelection = if (isDaySelected) {
                                        selectedDays - dayValue
                                    } else {
                                        selectedDays + dayValue
                                    }.sorted() // Keep list sorted 1..7

                                    selectedDays = newSelection
                                    onSelectionChange(newSelection)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// Helper to print "Mon, Tue, Fri"
private fun formatDays(days: List<Int>): String {
    if (days.isEmpty()) return ""
    val names = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    // Safe check to avoid crashes if index is out of bounds
    return days.filter { it in 1..7 }
        .sorted()
        .joinToString(", ") { names[it - 1] }
}