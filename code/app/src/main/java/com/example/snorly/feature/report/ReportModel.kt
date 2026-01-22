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

data class MetricExplainer(
    val title: String,
    val scoreLogic: String,
    val medicalInsight: String,
    val recommendations: List<String>,
    val resources: List<Pair<String, String>> // Label to URL
)

object ExplainerProvider {
    val sleepScore = MetricExplainer(
        title = "Sleep Score",
        scoreLogic = "Snorly calculates this by blending total duration with sleep cycles (if Stages are available). We look for the 'Golden 8' hours and penalize frequent interruptions.",
        medicalInsight = "High sleep quality is linked to better cognitive function and emotional regulation. Deep sleep is when your brain 'washes' away toxins.",
        recommendations = listOf("Maintain a cool room (18°C)", "Avoid caffeine 6-8h before bed", "Limit blue light exposure 1-3h before bed"),
        resources = listOf("Sleep Foundation - Quality" to "https://www.sleepfoundation.org")
    )

    val consistency = MetricExplainer(
        title = "Consistency Score",
        scoreLogic = "This is your 'Social Jetlag' meter. We compare your actual bedtime and wakeup times against your target schedule. Every 15m of deviation reduces the score.",
        medicalInsight = "Irregular sleep schedules disrupt your Circadian Rhythm, leading to daytime fatigue even if you sleep enough hours.",
        recommendations = listOf("Stick to the same wakeup time on weekends", "Use Snorly's smart alarms to bridge the gap"),
        resources = listOf("Harvard Health - Circadian" to "https://health.harvard.edu")
    )
}