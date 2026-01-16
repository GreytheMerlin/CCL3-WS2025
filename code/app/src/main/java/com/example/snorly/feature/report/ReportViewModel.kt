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
import com.example.snorly.feature.sleep.util.SleepScoreUtils
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

            // COMPARISON LOGIC: THIS WEEK (0-7) vs LAST WEEK (8-14)
            // We need 14 days of data total
            val fourteenDaysAgo = now.minus(14, ChronoUnit.DAYS)
            val allRecords = healthConnectManager.readSleepSessions(fourteenDaysAgo, now)

            if (allRecords.isNotEmpty()) {
                calculateComparison(allRecords, now)

                // 3. CONSISTENCY LOGIC: LAST 30 DAYS (For better statistical relevance)
                val thirtyDaysAgo = now.minus(30, ChronoUnit.DAYS)
                val monthlyRecords = healthConnectManager.readSleepSessions(thirtyDaysAgo, now)

                val profile = userProfileDao.getUserProfileSnapshot()
                val targetBed = parseTime(profile?.targetBedTime, LocalTime.of(23, 0))
                val targetWake = parseTime(profile?.targetWakeTime, LocalTime.of(7, 0))

                calculateConsistency(monthlyRecords, targetBed, targetWake)
            }
        }
    }

    private fun parseTime(timeStr: String?, default: LocalTime): LocalTime {
        if (timeStr.isNullOrBlank()) return default
        return try { LocalTime.parse(timeStr) } catch (e: Exception) { default }
    }

    private fun calculateComparison(records: List<androidx.health.connect.client.records.SleepSessionRecord>, now: Instant) {
        val splitPoint = now.minus(7, ChronoUnit.DAYS)

        // Split: "Recent" = Last 7 days. "Older" = Days 8-14.
        val recentRecs = records.filter { !it.startTime.isBefore(splitPoint) }
        val olderRecs = records.filter { it.startTime.isBefore(splitPoint) }

        // --- DURATION ---
        fun getAvgMinutes(list: List<androidx.health.connect.client.records.SleepSessionRecord>): Double {
            if (list.isEmpty()) return 0.0
            return list.map { Duration.between(it.startTime, it.endTime).toMinutes() }.average()
        }

        val recentAvgMin = getAvgMinutes(recentRecs)
        val olderAvgMin = getAvgMinutes(olderRecs)
        val diffMin = (recentAvgMin - olderAvgMin).toInt()

        // --- QUALITY SCORE ---
        fun getAvgScore(list: List<androidx.health.connect.client.records.SleepSessionRecord>): Int {
            if (list.isEmpty()) return 0
            // Calculate score for each record and average it
            return list.map { SleepScoreUtils.calculateScore(it) }.average().toInt()
        }

        val recentScore = getAvgScore(recentRecs)
        val olderScore = getAvgScore(olderRecs)
        val diffScore = recentScore - olderScore

        comparisonData = ComparisonResult(
            recentAvgHours = recentAvgMin / 60,
            olderAvgHours = olderAvgMin / 60,
            diffMinutes = diffMin,
            diffScore = diffScore,
            recentAvgScore = recentScore,
            olderAvgScore = olderScore
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

        // Helper: Calculate Score (0-100) for the overall badge
        fun calcScore(devMinutes: Long): Int {
            return when {
                devMinutes <= 15 -> 100
                devMinutes >= 180 -> 0
                else -> (100 - ((devMinutes - 15) * 0.95)).toInt().coerceIn(0, 100)
            }
        }

        // Helper: Determine Color based on deviation
        fun getColorForOffset(minutes: Long): Color {
            return when {
                minutes <= 30 -> Color(0xFF4CAF50) // Green
                minutes <= 90 -> Color(0xFFFFC107) // Orange
                else -> Color(0xFFFF5252)          // Red
            }
        }


        val overallScore = calcScore(overallAvgDev)
        val bedColor = getColorForOffset(avgBedDev)
        val wakeColor = getColorForOffset(avgWakeDev)

        val label = when {
            overallScore >= 80 -> "Excellent"
            overallScore >= 50 -> "Fair"
            else -> "Inconsistent"
        }

        // Overall Color based on overallScore
        val overallColor = when {
            overallScore >= 80 -> Color(0xFF4CAF50)
            overallScore >= 50 -> Color(0xFFFFC107)
            else -> Color(0xFFFF5252)
        }

        // Format Targets for UI
        val fmt = DateTimeFormatter.ofPattern("HH:mm")

        consistencyScore = ConsistencyResult(
            overallScore = overallScore,
            avgBedtimeOffsetMin = avgBedDev,
            avgWakeupOffsetMin = avgWakeDev,
            bedtimeColor = bedColor,
            wakeupColor = wakeColor,
            targetBedFormatted = targetBed.format(fmt),
            targetWakeFormatted = targetWake.format(fmt),
            label = label,
            color = overallColor
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

