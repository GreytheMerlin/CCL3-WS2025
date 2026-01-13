package com.example.snorly.feature.sleep

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.snorly.core.health.HealthConnectManager
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.temporal.ChronoUnit

class SleepViewModel(
    private val healthConnectManager: HealthConnectManager
): ViewModel() {
    // Expose permissions so the UI Button can use them
    val requiredPermissions = healthConnectManager.permissions

    // state for ui to know what to show
    var hasPermission by mutableStateOf(false)
    private set


    //  Holds the result string "7h 30m"
    var totalSleepDuration by mutableStateOf("Loading...")
        private set



    private fun loadSleepData() {
        viewModelScope.launch {
            val now = Instant.now()
            val yesterday = now.minus(24, ChronoUnit.HOURS)

            // get list form manager
            val sessions = healthConnectManager.readSleepSessions(start = yesterday, end = now)

            // sum up  duration of all sessions
            val totalDuration = sessions.sumOf { record ->
                java.time.Duration.between(record.startTime, record.endTime).toMinutes()
            }

            // Convert minutes to "8h 30m"
            val hours = totalDuration / 60
            val minutes = totalDuration % 60
            totalSleepDuration = "${hours}h ${minutes}m"
        }
    }

    fun checkPermissions(){
        viewModelScope.launch {
            hasPermission = healthConnectManager.hasAllPermissions()
            if (hasPermission) {
                loadSleepData()
            } else {
                totalSleepDuration = "No Permission"
            }
        }
    }

    //Check permission immediately when VM starts

    init {
        checkPermissions()
    }

    // Because our ViewModel needs an argument (Manager), we need a custom Factory.
    // This is "boilerplate" you will see often in Android without Hilt.
    class Factory(private val manager: HealthConnectManager) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SleepViewModel(manager) as T
        }
    }
}