package com.example.snorly.feature.alarm

data class Alarm(
    val id: String,
    val time: String,      // "07:00"
    val label: String,     // "Daily Alarm"
    val pattern: String,   // "Daily"
    val remaining: String, // "In 7h 26min"
    val isActive: Boolean
)