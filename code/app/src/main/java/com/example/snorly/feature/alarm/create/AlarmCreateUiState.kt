package com.example.snorly.feature.alarm.create

data class AlarmCreateUiState(
    val id: Long? = null,

    val hour: Int = 7,
    val minute: Int = 30,
    val label: String = "Alarm Name",
    val ringtone: String = "Repeater",
    val vibration: String = "Zig Zag",
    val repeatDays: List<Int> = List(7) { 0 },

    val dynamicWake: Boolean = false,
    val wakeUpChecker: Boolean = false,

    val enableSnooze: Boolean = true,
    val snoozeMinutes: Int = 5,

    val selectedChallenges: List<String> = emptyList(),

    val isSaving: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false
)
