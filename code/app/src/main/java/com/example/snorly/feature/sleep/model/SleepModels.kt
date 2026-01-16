package com.example.snorly.feature.sleep.model

import androidx.compose.ui.graphics.Color

data class DailySleepData(
    val dayName: String, // "Mon"
    val hours: Float     // 7.5
)

data class SleepStats(
    val avgDuration: String,     // "7h 39m"
    val avgQuality: String       // "80%"
)

data class SleepDayUiModel(
    val id: String,
    val dateLabel: String,       // "Thu, Jan 8"
    val durationFormatted: String, // "7h 15m"
    val qualityLabel: String,    // "85% Quality"
    val qualityColor: Color,     // Green/Yellow color for the tag
    val bedtime: String,         // "23:00"
    val wakeup: String           // "06:45"
)

data class WeeklyStats(
    val avgDuration: String, // "7h 30m"
    val avgScore: Int,          // 85
    val avgBedtime: String,     // "23:15"
    val avgWakeup: String,       // "07:10"
)

// Updated Data Class
data class ComparisonResult(
    val recentAvgHours: Double,
    val olderAvgHours: Double,
    val diffMinutes: Int, // e.g. +30 or -15
    val diffScore: Int = 0, // Placeholder for quality score diff
    val recentAvgScore: Int,
    val olderAvgScore: Int
)

data class ConsistencyResult(
    val overallScore: Int,
    val avgBedtimeOffsetMin: Long,
    val avgWakeupOffsetMin: Long,
    val bedtimeColor: Color,
    val wakeupColor: Color,
    val targetBedFormatted: String,
    val targetWakeFormatted: String,
    val label: String,
    val color: Color
)