package com.example.snorly.feature.alarm

data class Alarm(
    val id: Long,
    val time:String,
    val challenge: String,
    val ringtone: String,
    val vibration: String,
    val isActive: Boolean,
    val days: List<Int> = emptyList()
)