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
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale
import com.example.snorly.feature.sleep.model.WeeklyStats

class ReportViewModel(
    private val healthConnectManager: HealthConnectManager
) : ViewModel() {

    // Graph Data
    var weeklyGraphData by mutableStateOf<List<DailySleepData>>(emptyList())
        private set

    // Stats Data
    var stats by mutableStateOf(
        WeeklyStats("-", 0, "-", "-")
    )
        private set

    fun loadReportData() { // Renamed to public so we can call it from UI refresh
        viewModelScope.launch {
            val now = Instant.now()
            val sevenDaysAgo = now.minus(7, ChronoUnit.DAYS)

            val records = healthConnectManager.readSleepSessions(sevenDaysAgo, now)

            // 1. Calculate Stats
            stats = SleepDataProcessor.calculateWeeklyStats(records)

            // 2. Prepare Graph Data (Group by Day)
            val groupedByDay = records.groupBy {
                it.startTime.atZone(ZoneId.systemDefault()).toLocalDate()
            }

            weeklyGraphData = (0..6).map { i ->
                val targetDate = java.time.LocalDate.now().minusDays(6 - i.toLong())
                val dayRecords = groupedByDay[targetDate] ?: emptyList()

                val totalMinutes = dayRecords.sumOf {
                    java.time.Duration.between(it.startTime, it.endTime).toMinutes()
                }

                DailySleepData(
                    dayName = targetDate.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                    hours = totalMinutes / 60f
                )
            }
        }
    }

    init {
        loadReportData()
    }

    class Factory(private val manager: HealthConnectManager) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ReportViewModel(manager) as T
        }
    }
}