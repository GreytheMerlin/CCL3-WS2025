package com.example.snorly.feature.alarm.wakeup

data class AlarmRingingUiState(
    val loading: Boolean = true,
    val timeText: String = "--:--",

    val snoozeMinutes: Int = 0,
    val hasChallenge: Boolean = false,

    val error: String? = null
)
