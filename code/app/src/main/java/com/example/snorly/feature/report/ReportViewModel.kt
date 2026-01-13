package com.example.snorly.feature.report

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.snorly.core.health.HealthConnectManager
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

class ReportViewModel(
    private val healthConnectManager: HealthConnectManager
) : ViewModel() {

    // State: A list of bars for our graph
    var weeklySleepData by mutableStateOf<List<DailySleepData>>(emptyList())
        private set

    init {
        loadWeeklyData()
    }

    private fun loadWeeklyData() {
        viewModelScope.launch {
            // 1. Define Range: Last 7 Days
            val end = java.time.Instant.now()
            val start = end.minus(7, ChronoUnit.DAYS)

            // 2. Fetch Raw Data
            val rawRecords = healthConnectManager.readSleepSessions(start, end)

            // 3. The "Bucketing" Logic (The hard part!)
            // We group records by which day of the month they started in.
            val groupedByDay = rawRecords.groupBy { record ->
                // Convert timestamp to a local Date (LocalDate)
                record.startTime.atZone(ZoneId.systemDefault()).toLocalDate()
            }

            // 4. Transform into our UI Model
            // We iterate over the last 7 days to ensure even days with 0 sleep show up
            val graphData = (0..6).map { i ->
                val targetDate = java.time.LocalDate.now().minusDays(6 - i.toLong())

                // Get records for this specific date (or empty list if none)
                val dayRecords = groupedByDay[targetDate] ?: emptyList()

                // Sum hours
                val totalMinutes = dayRecords.sumOf {
                    java.time.Duration.between(it.startTime, it.endTime).toMinutes()
                }
                val totalHours = totalMinutes / 60f

                // Format Name: "Mon", "Tue"
                val dayName = targetDate.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())

                DailySleepData(dayName, totalHours)
            }

            weeklySleepData = graphData
        }
    }

    // Standard Factory Boilerplate
    class Factory(private val manager: HealthConnectManager) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ReportViewModel(manager) as T
        }
    }
}