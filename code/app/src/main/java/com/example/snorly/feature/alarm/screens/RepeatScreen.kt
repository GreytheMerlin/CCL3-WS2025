package com.example.snorly.feature.alarm.screens

import androidx.activity.compose.BackHandler
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
import androidx.navigation.NavController
import com.example.snorly.core.common.components.BackTopBar
import com.example.snorly.feature.alarm.components.DayToggleCircle
import com.example.snorly.feature.alarm.components.RepeatOptionCard

@Composable
fun RepeatScreen(
    initialDays: List<Int> = emptyList(), // e.g. [1, 1, 0] passed from DB
    navController: NavController,
) {
    // Manage State locally
    var selectedDays by remember {
        mutableStateOf(if (initialDays.size == 7) initialDays else List(7) { 0 })
    }
    fun returnResult() {
        navController.previousBackStackEntry
            ?.savedStateHandle
            ?.set("selected_days_result", selectedDays)
        navController.popBackStack()
    }

    BackHandler(enabled = true) {
        returnResult()
    }
    // Binary Templates for comparison
    val dailyTemplate = List(7) { 1 }           // [1, 1, 1, 1, 1, 1, 1]
    val weekdaysTemplate = List(7) { i -> if (i < 5) 1 else 0 } // [1, 1, 1, 1, 1, 0, 0]
    val weekendTemplate = List(7) { i -> if (i >= 5) 1 else 0 } // [0, 0, 0, 0, 0, 1, 1]
    val onceTemplate = List(7) { 0 }            // [0, 0, 0, 0, 0, 0, 0]

    // Determine current preset based on list content
    var isCustomExpanded by remember {
        mutableStateOf(
            selectedDays != onceTemplate &&
                    selectedDays != dailyTemplate &&
                    selectedDays != weekdaysTemplate &&
                    selectedDays != weekendTemplate
        )
    }

    // Local helper for subtitle (simplified version of VM logic)
    fun getCustomSubtitle(): String {
        val names = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        return selectedDays.mapIndexedNotNull { index, value ->
            if (value == 1) names[index] else null
        }.joinToString(", ")
    }

    Scaffold(
        topBar = {
            BackTopBar(title = "Repeat", onBackClick = {
                navController.previousBackStackEntry?.savedStateHandle?.set(
                    "selected_days_result", selectedDays
                )
                navController.popBackStack()
            })
        }) { innerPadding ->
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
                isSelected = !isCustomExpanded && selectedDays == onceTemplate,
                onClick = { selectedDays = onceTemplate })
            RepeatOptionCard(
                title = "Daily",
                isSelected = !isCustomExpanded && selectedDays == dailyTemplate,
                onClick = { selectedDays = dailyTemplate })
            RepeatOptionCard(
                title = "Mon To Fri",
                subtitle = "Weekdays",
                isSelected = !isCustomExpanded && selectedDays == weekdaysTemplate,
                onClick = { selectedDays = weekdaysTemplate })
            RepeatOptionCard(
                title = "Weekend",
                subtitle = "Saturday and Sunday",
                isSelected = !isCustomExpanded && selectedDays == weekendTemplate,
                onClick = { selectedDays = weekendTemplate })

            // --- Option 5: Custom ---
            RepeatOptionCard(
                title = "Custom", subtitle = if (isCustomExpanded) getCustomSubtitle() else null,
                // Selected whenever explicitly expanded
                isSelected = isCustomExpanded, onClick = {
                    isCustomExpanded = true // FORCE Expansion

                    // User Convenience: If list was empty (Once), default to Monday
                    // But if it was already "Weekend", keep "Weekend" so they can edit it
                    if (selectedDays.all { it == 0 }) {
                        val newDays = onceTemplate.toMutableList()
                        newDays[0] = 1 // Default to Monday
                        selectedDays = newDays
                    }
                })

            // --- The Expanding Day Selector ---
            AnimatedVisibility(
                visible = isCustomExpanded,
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
                            val isDaySelected = selectedDays[index] == 1

                            DayToggleCircle(
                                label = label, isSelected = isDaySelected, onClick = {
                                    val newDays = selectedDays.toMutableList()
                                    newDays[index] = if (isDaySelected) 0 else 1
                                    selectedDays = newDays
                                })
                        }
                    }
                }
            }
        }
    }
}
