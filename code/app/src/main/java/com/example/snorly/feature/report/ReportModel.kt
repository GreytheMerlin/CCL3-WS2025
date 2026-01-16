package com.example.snorly.feature.report

import androidx.compose.ui.graphics.Color

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