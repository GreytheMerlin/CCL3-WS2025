package com.example.snorly.feature.report

import androidx.compose.ui.graphics.Color
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.snorly.core.database.UserProfileDao
import com.example.snorly.core.database.entities.UserProfileEntity
import com.example.snorly.core.health.HealthConnectManager
import com.example.snorly.feature.sleep.model.DailySleepData
import com.example.snorly.feature.sleep.model.SleepDataProcessor
import com.example.snorly.feature.sleep.util.SleepScoreUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.Duration
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.abs

data class ReportUiState(
    val isLoading: Boolean = true,
    val weeklyGraphData: List<DailySleepData> = emptyList(),
    val stats: WeeklyStats = WeeklyStats("-", 0, "-", "-"),
    val comparisonData: ComparisonResult? = null,
    val consistencyScore: ConsistencyResult? = null
)
class ReportViewModel(
    private val healthConnectManager: HealthConnectManager,
    private val userProfileDao: UserProfileDao
) : ViewModel() {

    private val refreshTrigger = MutableStateFlow(System.currentTimeMillis())
    private val userProfileFlow = userProfileDao.getUserProfile()

    // Single source of truth
    val uiState: StateFlow<ReportUiState> = combine(
        userProfileFlow,
        refreshTrigger
    ) { profile, _ ->
        calculateAllReportData(profile)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ReportUiState(isLoading = true)
    )

    fun refresh() {
        refreshTrigger.value = System.currentTimeMillis()
    }

    private suspend fun calculateAllReportData(profile: UserProfileEntity?): ReportUiState {
        val now = Instant.now()
        val fourteenDaysAgo = now.minus(14, ChronoUnit.DAYS)
        val splitPoint = now.minus(7, ChronoUnit.DAYS)

        // 1. Fetch data
        val allRecords = healthConnectManager.readSleepSessions(fourteenDaysAgo, now)
        val recentRecords = allRecords.filter { !it.startTime.isBefore(splitPoint) }

        // 2. Perform Calculations
        val stats = SleepDataProcessor.calculateWeeklyStats(recentRecords)
        val graphData = calculateGraphData(recentRecords)
        val comparison = calculateComparison(allRecords, now)

        val targetBed = parseTime(profile?.targetBedTime, LocalTime.of(23, 0))
        val targetWake = parseTime(profile?.targetWakeTime, LocalTime.of(7, 0))
        val consistency = calculateConsistency(recentRecords, targetBed, targetWake)

        return ReportUiState(
            isLoading = false,
            weeklyGraphData = graphData,
            stats = stats,
            comparisonData = comparison,
            consistencyScore = consistency
        )
    }

    // --- UPDATED HELPERS: NOW RETURNING VALUES ---

    private fun calculateGraphData(weeklyRecords: List<SleepSessionRecord>): List<DailySleepData> {
        val groupedByDay = weeklyRecords.groupBy {
            it.startTime.atZone(ZoneId.systemDefault()).toLocalDate()
        }
        return (0..6).map { i ->
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
    }

    private fun calculateComparison(records: List<SleepSessionRecord>, now: Instant): ComparisonResult? {
        if (records.isEmpty()) return null

        val splitPoint = now.minus(7, ChronoUnit.DAYS)
        val recentRecs = records.filter { !it.startTime.isBefore(splitPoint) }
        val olderRecs = records.filter { it.startTime.isBefore(splitPoint) }

        fun getAvgMinutes(list: List<SleepSessionRecord>) =
            if (list.isEmpty()) 0.0 else list.map { Duration.between(it.startTime, it.endTime).toMinutes() }.average()

        fun getAvgScore(list: List<SleepSessionRecord>) =
            if (list.isEmpty()) 0 else list.map { SleepScoreUtils.calculateScore(it) }.average().toInt()

        val recentAvgMin = getAvgMinutes(recentRecs)
        val olderAvgMin = getAvgMinutes(olderRecs)
        val recentScore = getAvgScore(recentRecs)
        val olderScore = getAvgScore(olderRecs)

        return ComparisonResult(
            recentAvgHours = recentAvgMin / 60,
            olderAvgHours = olderAvgMin / 60,
            diffMinutes = (recentAvgMin - olderAvgMin).toInt(),
            diffScore = recentScore - olderScore,
            recentAvgScore = recentScore,
            olderAvgScore = olderScore
        )
    }

    private fun calculateConsistency(
        records: List<SleepSessionRecord>,
        targetBed: LocalTime,
        targetWake: LocalTime
    ): ConsistencyResult? {
        if (records.isEmpty()) return null

        // 1. Group by date to handle multiple sessions per night (naps/fragmented sleep)
        val sessionsByDay = records.groupBy {
            it.startTime.atZone(ZoneId.systemDefault()).toLocalDate()
        }

        var totalBedDeviation = 0L
        var totalWakeDeviation = 0L
        val dayCount = sessionsByDay.size

        sessionsByDay.forEach { (_, dayRecords) ->
            // For bedtime, we only care about the EARLIEST session of that night
            val earliestStart = dayRecords.minBy { it.startTime }.startTime
                .atZone(ZoneId.systemDefault()).toLocalTime()

            // For wake up, we only care about the LATEST session of that morning
            val latestEnd = dayRecords.maxBy { it.endTime }.endTime
                .atZone(ZoneId.systemDefault()).toLocalTime()

            // Calculate Bedtime Deviation
            val bedDiff = abs(Duration.between(targetBed, earliestStart).toMinutes())
            val realBedDiff = if (bedDiff > 720) 1440 - bedDiff else bedDiff
            totalBedDeviation += realBedDiff

            // Calculate Wakeup Deviation
            val wakeDiff = abs(Duration.between(targetWake, latestEnd).toMinutes())
            val realWakeDiff = if (wakeDiff > 720) 1440 - wakeDiff else wakeDiff
            totalWakeDeviation += realWakeDiff
        }

        val avgBedDev = totalBedDeviation / dayCount
        val avgWakeDev = totalWakeDeviation / dayCount
        val overallScore = (100 - (((avgBedDev + avgWakeDev) / 2 - 15) * 0.95)).toInt().coerceIn(0, 100)

        val fmt = DateTimeFormatter.ofPattern("HH:mm")
        val scoreColor = if (overallScore >= 80) Color(0xFF4CAF50) else if (overallScore >= 50) Color(0xFFFFC107) else Color(0xFFFF5252)

        return ConsistencyResult(
            overallScore = overallScore,
            avgBedtimeOffsetMin = avgBedDev,
            avgWakeupOffsetMin = avgWakeDev,
            bedtimeColor = if (avgBedDev <= 30) Color(0xFF4CAF50) else Color(0xFFFFC107),
            wakeupColor = if (avgWakeDev <= 30) Color(0xFF4CAF50) else Color(0xFFFFC107),
            targetBedFormatted = targetBed.format(fmt),
            targetWakeFormatted = targetWake.format(fmt),
            label = if (overallScore >= 80) "Excellent" else if (overallScore >= 50) "Fair" else "Inconsistent",
            color = scoreColor
        )
    }

    private fun parseTime(timeStr: String?, default: LocalTime): LocalTime {
        return try { LocalTime.parse(timeStr) } catch (e: Exception) { default }
    }

    class Factory(private val manager: HealthConnectManager, private val userProfileDao: UserProfileDao) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T = ReportViewModel(manager, userProfileDao) as T
    }
}