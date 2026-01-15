package com.example.snorly.feature.sleep.model
import androidx.compose.ui.graphics.Color
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.SleepSessionRecord.Companion.STAGE_TYPE_AWAKE
import androidx.health.connect.client.records.SleepSessionRecord.Companion.STAGE_TYPE_AWAKE_IN_BED
import androidx.health.connect.client.records.SleepSessionRecord.Companion.STAGE_TYPE_DEEP
import androidx.health.connect.client.records.SleepSessionRecord.Companion.STAGE_TYPE_LIGHT
import androidx.health.connect.client.records.SleepSessionRecord.Companion.STAGE_TYPE_OUT_OF_BED
import androidx.health.connect.client.records.SleepSessionRecord.Companion.STAGE_TYPE_REM
import androidx.health.connect.client.records.SleepSessionRecord.Companion.STAGE_TYPE_SLEEPING
import java.time.Duration
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object SleepDataProcessor {

    fun calculateQuality(minutes: Long): Pair<String, Long> {
        return when {
            minutes >= 450 -> "Excellent" to 0xFF4CAF50 // > 7.5 hours
            minutes >= 360 -> "Fair" to 0xFFFFC107      // > 6.0 hours
            else -> "Poor" to 0xFFFF5252                // < 6.0 hours
        }
    }

    fun getStageLabel(stage: Int): String {
        return when (stage) {
            STAGE_TYPE_AWAKE -> "AWAKE"
            STAGE_TYPE_SLEEPING -> "SLEEPING"
            STAGE_TYPE_OUT_OF_BED -> "OUT_OF_BED"
            STAGE_TYPE_AWAKE_IN_BED -> "AWAKE_IN_BED"
            STAGE_TYPE_LIGHT -> "LIGHT"
            STAGE_TYPE_DEEP -> "DEEP"
            STAGE_TYPE_REM -> "REM"
            else -> "UNKNOWN"
        }
    }

    // 2. Map Stage -> Color (For the Detail Graph)
    fun getStageColor(stage: Int): Color {
        return when (stage) {
            STAGE_TYPE_DEEP -> Color(0xFF1A237E) // Dark Blue
            STAGE_TYPE_REM -> Color(0xFF7E57C2)  // Purple
            STAGE_TYPE_LIGHT -> Color(0xFF42A5F5) // Light Blue
            STAGE_TYPE_AWAKE, STAGE_TYPE_AWAKE_IN_BED -> Color(0xFFFFCA28) // Yellow
            else -> Color.Gray
        }
    }

    fun processHistory(records: List<SleepSessionRecord>): List<SleepDayUiModel> {
        // 1. Group by Date
        val grouped = records.groupBy {
            it.startTime.atZone(ZoneId.systemDefault()).toLocalDate()
        }

        // 2. Sort Descending (Newest first)
        val sortedDates = grouped.keys.sortedDescending()

        return sortedDates.map { date ->
            val sessions = grouped[date] ?: emptyList()

            val mainSessionId = sessions.maxByOrNull {
                Duration.between(it.startTime, it.endTime).toMinutes()
            }?.metadata?.id ?: ""

            // Calculate Total Minutes
            val totalMinutes = sessions.sumOf {
                Duration.between(it.startTime, it.endTime).toMinutes()
            }

            // Find Bedtime and Wakeup
            val earliestStart = sessions.minOf { it.startTime }
            val latestEnd = sessions.maxOf { it.endTime }

            val timeFmt = DateTimeFormatter.ofPattern("HH:mm")
            val dateFmt = DateTimeFormatter.ofPattern("EEE, MMM d", Locale.ENGLISH)

            // Calculate "Quality" % (Goal: 8 hours = 480 min)
            val qualityPercent = ((totalMinutes / 480.0) * 100).toInt().coerceIn(0, 100)

            // Color Logic (Returns actual Color object, not Long)
            val color = when {
                qualityPercent >= 85 -> Color(0xFF4CAF50) // Green
                qualityPercent >= 70 -> Color(0xFFFFC107) // Yellow
                else -> Color(0xFFFF5252) // Red
            }

            SleepDayUiModel(
                id = mainSessionId,
                dateLabel = date.format(dateFmt),
                durationFormatted = formatMinutes(totalMinutes),
                qualityLabel = "$qualityPercent% Quality",
                qualityColor = color,
                bedtime = earliestStart.atZone(ZoneId.systemDefault()).format(timeFmt),
                wakeup = latestEnd.atZone(ZoneId.systemDefault()).format(timeFmt)
            )
        }
    }

    fun calculateStats(history: List<SleepDayUiModel>, rawRecords: List<SleepSessionRecord>): SleepStats {
        if (rawRecords.isEmpty()) return SleepStats("-", "-")

        val totalMinutes = rawRecords.sumOf { Duration.between(it.startTime, it.endTime).toMinutes() }

        val daysCount = history.size
        if (daysCount == 0) return SleepStats("-", "-")

        val avgMin = totalMinutes / daysCount
        val avgQuality = ((avgMin / 480.0) * 100).toInt().coerceIn(0, 100)

        return SleepStats(
            avgDuration = formatMinutes(avgMin),
            avgQuality = "$avgQuality%"
        )
    }


    // stats for report screen
    fun calculateWeeklyStats(records: List<SleepSessionRecord>): WeeklyStats {
        if (records.isEmpty()) return WeeklyStats("-", 0, "-", "-")

        // 1. Avg Duration
        val totalMinutes = records.sumOf { Duration.between(it.startTime, it.endTime).toMinutes() }
        val avgMinutes = totalMinutes / records.size

        // 2. Avg Score
        val avgScore = ((avgMinutes / 480.0) * 100).toInt().coerceIn(0, 100)

        // 3. Avg Bedtime/Wakeup (Using seconds of day)
        // Note: For bedtime, we treat 11PM as 23, 1AM as 25 (next day) to average correctly
        val bedtimes = records.map {
            val zdt = it.startTime.atZone(ZoneId.systemDefault())
            var minuteOfDay = zdt.hour * 60 + zdt.minute
            if (zdt.hour < 12) minuteOfDay += 24 * 60 // Shift AM hours to next day for averaging
            minuteOfDay
        }
        val avgBedtimeMin = bedtimes.average().toInt() % (24 * 60)

        val wakeups = records.map {
            val zdt = it.endTime.atZone(ZoneId.systemDefault())
            zdt.hour * 60 + zdt.minute
        }
        val avgWakeupMin = wakeups.average().toInt()

        return WeeklyStats(
            avgDurationStr = formatMinutes(avgMinutes),
            avgScore = avgScore,
            avgBedtime = formatTimeFromMinutes(avgBedtimeMin),
            avgWakeup = formatTimeFromMinutes(avgWakeupMin)
        )
    }

    // Minutes Into to String
    private fun formatTimeFromMinutes(totalMinutes: Int): String {
        val h = (totalMinutes / 60) % 24
        val m = totalMinutes % 60
        return String.format(Locale.getDefault(), "%02d:%02d", h, m)
    }

    // Minutes Long to String
    private fun formatMinutes(minutes: Long): String {
        val h = minutes / 60
        val m = minutes % 60
        return "${h}h ${m}m"
    }
}