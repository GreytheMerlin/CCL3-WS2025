package com.example.snorly.feature.alarm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.snorly.core.database.AppDatabase
import com.example.snorly.core.database.entities.AlarmEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AlarmViewModel(application: Application) : AndroidViewModel(application) {

    private val alarmDao = AppDatabase.getDatabase(application).alarmDao()

    private val dayNames = listOf(
        "Monday",
        "Tuesday",
        "Wednesday",
        "Thursday",
        "Friday",
        "Saturday",
        "Sunday"
    )

    fun selectedDayNames(days: List<Int>): List<String> {
        return days.mapIndexedNotNull { index, value ->
            if (value == 1) dayNames.getOrNull(index) else null
        }
    }

    val alarms: StateFlow<List<Alarm>> =
        alarmDao.getAll()
            .map { entities -> entities.map { it.toAlarm() } }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000L),
                initialValue = emptyList()
            )

    fun insert(alarm: AlarmEntity) = viewModelScope.launch {
        alarmDao.addAlarm(alarm)
    }

    fun toggleAlarm(id: Long, newValue: Boolean) = viewModelScope.launch {
        alarmDao.updateActive(id = id, isActive = newValue)
    }
}

private fun AlarmEntity.toAlarm(): Alarm =
    Alarm(
        id = id,
        time = time,
        ringtone = ringtone,
        vibration = vibration,
        days = days,
        challenge = challenge,
        isActive = isActive
    )
