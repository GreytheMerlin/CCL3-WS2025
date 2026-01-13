package com.example.snorly.feature.report

// A simple holder for one bar in the graph
data class DailySleepData(
    val dayName: String, // e.g., "Mon", "Tue"
    val hours: Float     // e.g., 7.5
)