package com.example.snorly.feature.alarm.wakeup

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.snorly.core.database.AppDatabase
import com.example.snorly.core.database.entities.AlarmEntity
import com.example.snorly.feature.alarm.Alarm
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

    fun selectedDayNames(days: List<Int>): String {


        // Guard: wrong size
        if (days.size != 7) return ""

        // Special cases
        if (days.all { it == 1 }) return "Daily"
        if (days.take(5).all { it == 1 } && days.drop(5).all { it == 0 }) return "Weekdays"
        if (days.take(5).all { it == 0 } && days.drop(5).all { it == 1 }) return "Weekend"

        val selected = days.mapIndexedNotNull { index, value -> if (value == 1) index else null }
        if (selected.isEmpty()) return "No days"

        // Build consecutive ranges (non-wrapping)
        val ranges = mutableListOf<Pair<Int, Int>>()
        var start = selected.first()
        var prev = start

        for (i in 1 until selected.size) {
            val cur = selected[i]
            if (cur == prev + 1) {
                prev = cur
            } else {
                ranges += start to prev
                start = cur
                prev = cur
            }
        }
        ranges += start to prev

        return ranges.joinToString(", ") { (s, e) ->
            if (s == e) dayNames[s] else "${dayNames[s]}–${dayNames[e]}"
        }
    }

    fun formatDays(days: List<Int>): String {
        // Safety check: if list is missing or wrong size, treat as "Once"
        if (days.size != 7) return "Once"

        // 1. Check for "Once" (All zeros)
        if (days.all { it == 0 }) return "Once"

        // 2. Check for "Daily" (All ones)
        if (days.all { it == 1 }) return "Daily"

        // 3. Check for "Weekdays" (Mon(0)-Fri(4) are 1, Sat(5)-Sun(6) are 0)
        // We slice the list to check specific ranges
        val isWeekdays = days.subList(0, 5).all { it == 1 } && days.subList(5, 7).all { it == 0 }
        if (isWeekdays) return "Weekdays"

        // 4. Check for "Weekend" (Mon(0)-Fri(4) are 0, Sat(5)-Sun(6) are 1)
        val isWeekend = days.subList(0, 5).all { it == 0 } && days.subList(5, 7).all { it == 1 }
        if (isWeekend) return "Weekend"

        // 5. Custom Formatting (e.g., "Mon, Wed")
        val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val activeLabels = days.mapIndexedNotNull { index, value ->
            if (value == 1) dayLabels[index] else null
        }
        return activeLabels.joinToString(", ")
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
        // 1) Insert into DB and get ID
        val alarmId = alarmDao.addAlarm(alarm)

        // 2) Parse HH:mm
        val (hour, minute) = alarm.time.split(":").map { it.toInt() }

        // 3) Compute next trigger
        val triggerAt = nextTriggerMillis(
            hour = hour,
            minute = minute,
            days = alarm.days
        )

        // 4) Schedule alarm
        AlarmScheduler(getApplication())
            .schedule(alarmId, triggerAt)
    }

    fun toggleAlarm(id: Long, newValue: Boolean) = viewModelScope.launch {
        alarmDao.updateActive(id = id, isActive = newValue)

        val scheduler = AlarmScheduler(getApplication())

        if (!newValue) {
            scheduler.cancel(id)
        } else {
            val alarm = alarmDao.getById(id) // add DAO query
            val (h, m) = alarm.time.split(":").map { it.toInt() }
            scheduler.schedule(
                id,
                nextTriggerMillis(h, m, alarm.days)
            )
        }
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
