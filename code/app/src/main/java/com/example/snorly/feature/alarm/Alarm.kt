package com.example.snorly.feature.alarm

data class Alarm(
    val id: Long,
    val label: String,
    val time:String,
    val challenge: List<String> = emptyList(),
    val ringtone: String,
    val vibration: String,
    val isActive: Boolean,
    val days: List<Int> = emptyList()
)