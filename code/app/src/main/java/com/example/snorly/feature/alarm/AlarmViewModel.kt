package com.example.snorly.feature.alarm

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AlarmViewModel : ViewModel() {

    // 1. The Source of Truth
    // In a real app, this would come from a Room Database
    private val _alarms = MutableStateFlow(
        listOf(
            Alarm(
                id = "1",
                time = "07:00",
                label = "Work",
                pattern = "Mon-Fri",
                remaining = "In 7h 26min",
                isActive = true
            ),
            Alarm(
                id = "2",
                time = "08:30",
                label = "Weekend",
                pattern = "Sat-Sun",
                remaining = "In 1d 9h",
                isActive = false
            ),
            Alarm(
                id = "3",
                time = "09:00",
                label = "Gym",
                pattern = "Daily",
                remaining = "In 9h 00min",
                isActive = true
            )
        )
    )
    // Expose as immutable flow for the UI to observe
    val alarms: StateFlow<List<Alarm>> = _alarms.asStateFlow()

    // 2. Logic to handle the toggle
    fun toggleAlarm(id: String, newValue: Boolean) {
        _alarms.update { currentList ->
            currentList.map { alarm ->
                if (alarm.id == id) {
                    // Create a copy of the alarm with the new status
                    alarm.copy(isActive = newValue)
                } else {
                    alarm
                }
            }
        }
    }
}