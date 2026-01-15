package com.example.snorly.feature.alarm.overview

data class AlarmSelectionUiState(
    val selectionMode: Boolean = false,
    val selectedIds: Set<Long> = emptySet()
)