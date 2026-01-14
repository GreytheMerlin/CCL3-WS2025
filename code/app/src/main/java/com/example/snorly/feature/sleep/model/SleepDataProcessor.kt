package com.example.snorly.feature.sleep.model
import androidx.compose.ui.graphics.Color
import androidx.health.connect.client.records.SleepSessionRecord
import java.time.Duration
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object SleepDataProcessor {

    fun processHistory(records: List<SleepSessionRecord>): List<SleepDayUiModel> {
        // 1. Group by Date
        val grouped = records.groupBy {
            it.startTime.atZone(ZoneId.systemDefault()).toLocalDate()
        }

        // 2. Sort Descending (Newest first)
        val sortedDates = grouped.keys.sortedDescending()

        return sortedDates.map { date ->
            val sessions = grouped[date] ?: emptyList()

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

    private fun formatMinutes(minutes: Long): String {
        val h = minutes / 60
        val m = minutes % 60
        return "${h}h ${m}m"
    }
}