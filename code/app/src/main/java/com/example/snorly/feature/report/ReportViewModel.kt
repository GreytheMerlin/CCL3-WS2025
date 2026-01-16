package com.example.snorly.feature.report

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.snorly.core.database.UserProfileDao
import com.example.snorly.core.health.HealthConnectManager
import com.example.snorly.feature.sleep.model.ComparisonResult
import com.example.snorly.feature.sleep.model.ConsistencyResult
import com.example.snorly.feature.sleep.model.DailySleepData
import com.example.snorly.feature.sleep.model.SleepDataProcessor
import com.example.snorly.feature.sleep.model.WeeklyStats
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.abs

class ReportViewModel(
    private val healthConnectManager: HealthConnectManager,
    private val userProfileDao: UserProfileDao
) : ViewModel() {

    // Existing State
    var weeklyGraphData by mutableStateOf<List<DailySleepData>>(emptyList())
        private set
    var stats by mutableStateOf(WeeklyStats("-", 0, "-", "-"))
        private set

    // Comparison & Consistency
    var comparisonData by mutableStateOf<ComparisonResult?>(null)
        private set
    var consistencyScore by mutableStateOf<ConsistencyResult?>(null)
        private set

    fun loadReportData() {
        viewModelScope.launch {
            val now = Instant.now()

            // 1. Existing Weekly Logic (Last 7 Days)
            val sevenDaysAgo = now.minus(7, ChronoUnit.DAYS)
            val weeklyRecords = healthConnectManager.readSleepSessions(sevenDaysAgo, now)

            stats = SleepDataProcessor.calculateWeeklyStats(weeklyRecords)

            val groupedByDay = weeklyRecords.groupBy {
                it.startTime.atZone(ZoneId.systemDefault()).toLocalDate()
            }
            weeklyGraphData = (0..6).map { i ->
                val targetDate = java.time.LocalDate.now().minusDays(6 - i.toLong())
                val dayRecords = groupedByDay[targetDate] ?: emptyList()
                val totalMinutes = dayRecords.sumOf {
                    Duration.between(it.startTime, it.endTime).toMinutes()
                }
                DailySleepData(
                    dayName = targetDate.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                    hours = totalMinutes / 60f
                )
            }

            // Last 30 Days for Comparison & Consistency
            val thirtyDaysAgo = now.minus(30, ChronoUnit.DAYS)
            val monthlyRecords = healthConnectManager.readSleepSessions(thirtyDaysAgo, now)

            if (monthlyRecords.isNotEmpty()) {
                calculateComparison(monthlyRecords, now)

                // Fetch User Targets from Database
                val profile = userProfileDao.getUserProfileSnapshot()
                val targetBed = parseTime(profile?.targetBedTime, LocalTime.of(23, 0))
                val targetWake = parseTime(profile?.targetWakeTime, LocalTime.of(7, 0))

                // For consistency, we need target times.
                // Since userProfileDao isn't injected here yet, we'll assume defaults or
                // calculate based on their average if you prefer dynamic targets.
                // Using defaults 23:00 / 07:00 for robust "ideal" comparison.
                calculateConsistency(monthlyRecords, targetBed, targetWake)
            }
        }
    }

    private fun parseTime(timeStr: String?, default: LocalTime): LocalTime {
        if (timeStr.isNullOrBlank()) return default
        return try { LocalTime.parse(timeStr) } catch (e: Exception) { default }
    }

    private fun calculateComparison(records: List<androidx.health.connect.client.records.SleepSessionRecord>, now: Instant) {
        val splitPoint = now.minus(15, ChronoUnit.DAYS)

        // Split records into "Recent Half" (Last 15 days) and "Older Half" (Days 16-30)
        val recentRecs = records.filter { !it.startTime.isBefore(splitPoint) }
        val olderRecs = records.filter { it.startTime.isBefore(splitPoint) }

        // Function to calc avg duration in minutes
        fun getAvgMinutes(list: List<androidx.health.connect.client.records.SleepSessionRecord>): Double {
            if (list.isEmpty()) return 0.0
            return list.map { Duration.between(it.startTime, it.endTime).toMinutes() }.average()
        }

        val recentAvg = getAvgMinutes(recentRecs)
        val olderAvg = getAvgMinutes(olderRecs)

        // Compare
        val diff = recentAvg - olderAvg // Positive means we sleep MORE now
        val percentChange = if (olderAvg > 0) ((diff / olderAvg) * 100).toInt() else 0

        comparisonData = ComparisonResult(
            recentAvgHours = recentAvg / 60,
            olderAvgHours = olderAvg / 60,
            percentChange = percentChange
        )
    }

    private fun calculateConsistency(
        records: List<androidx.health.connect.client.records.SleepSessionRecord>,
        targetBed: LocalTime,
        targetWake: LocalTime
    ) {
        var totalBedDeviation = 0L
        var totalWakeDeviation = 0L
        var count = 0

        records.forEach { record ->
            val startLocal = record.startTime.atZone(ZoneId.systemDefault()).toLocalTime()
            val endLocal = record.endTime.atZone(ZoneId.systemDefault()).toLocalTime()

            // Bedtime Deviation (handle midnight wrap)
            val bedDiff = abs(Duration.between(targetBed, startLocal).toMinutes())
            val realBedDiff = if (bedDiff > 720) 1440 - bedDiff else bedDiff

            // Wakeup Deviation
            val wakeDiff = abs(Duration.between(targetWake, endLocal).toMinutes())
            val realWakeDiff = if (wakeDiff > 720) 1440 - wakeDiff else wakeDiff

            totalBedDeviation += realBedDiff
            totalWakeDeviation += realWakeDiff
            count++
        }

        if (count == 0) return

        val avgBedDev = totalBedDeviation / count
        val avgWakeDev = totalWakeDeviation / count
        val overallAvgDev = (avgBedDev + avgWakeDev) / 2

        // Helper to calculate score (0-100) based on minutes deviation
        fun calcScore(devMinutes: Long): Int {
            return when {
                devMinutes <= 15 -> 100 // Strict: 15 mins window is perfect
                devMinutes >= 120 -> 0  // 2 hours off is bad
                else -> (100 - ((devMinutes - 15) * 0.95)).toInt().coerceIn(0, 100)
            }
        }

        val bedScore = calcScore(avgBedDev)
        val wakeScore = calcScore(avgWakeDev)
        val overallScore = calcScore(overallAvgDev)

        val (label, color) = when {
            overallScore >= 80 -> "Excellent" to Color(0xFF4CAF50)
            overallScore >= 60 -> "Fair" to Color(0xFFFFC107)
            else -> "Inconsistent" to Color(0xFFFF5252)
        }

        // Format Targets for UI
        val fmt = DateTimeFormatter.ofPattern("HH:mm")

        consistencyScore = ConsistencyResult(
            overallScore = overallScore,
            bedtimeScore = bedScore,
            wakeupScore = wakeScore,
            targetBedFormatted = targetBed.format(fmt),
            targetWakeFormatted = targetWake.format(fmt),
            label = label,
            color = color
        )
    }

    init {
        loadReportData()
    }

    class Factory(
        private val manager: HealthConnectManager,
        private val userProfileDao: UserProfileDao
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ReportViewModel(manager, userProfileDao) as T
        }
    }
}

