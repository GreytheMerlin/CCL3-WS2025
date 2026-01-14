package com.example.snorly.feature.report

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.snorly.core.health.HealthConnectManager
import com.example.snorly.feature.sleep.model.DailySleepData
import com.example.snorly.feature.sleep.model.SleepDataProcessor
import com.example.snorly.feature.sleep.model.SleepDayUiModel
import kotlinx.coroutines.launch
import java.time.Instant
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

    var sleepHistoryList by mutableStateOf<List<SleepDayUiModel>>(emptyList())
        private set

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            // 1. Define Range: Last 30 Days (for timeline)
            val now = Instant.now()
            val thirtyDaysAgo = now.minus(30, ChronoUnit.DAYS)

            // 2. Fetch Raw Data once
            val allRecords = healthConnectManager.readSleepSessions(thirtyDaysAgo, now)

            // 3. Process for Timeline (Newest 30 days history)
            // We delegate the heavy logic to our new Helper Object
            sleepHistoryList = SleepDataProcessor.processHistory(allRecords)

            // 4. Process for Graph (Last 7 days only)


            val groupedByDay = allRecords.groupBy { record ->
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