package com.example.snorly.feature.alarm.overview


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.snorly.core.database.AppDatabase
import com.example.snorly.core.database.entities.AlarmEntity
import com.example.snorly.feature.alarm.Alarm
import com.example.snorly.feature.alarm.wakeup.AlarmScheduler
import com.example.snorly.feature.alarm.wakeup.nextTriggerMillis
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch



class AlarmScreenViewModel(application: Application) : AndroidViewModel(application) {

    private val alarmDao = AppDatabase.getDatabase(application).alarmDao()
    private val scheduler = AlarmScheduler(getApplication())

    private val dayNames = listOf(
        "Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday"
    )

    fun selectedDayNames(days: List<Int>): String {
        if (days.size != 7) return ""
        if (days.all { it == 1 }) return "Daily"
        if (days.take(5).all { it == 1 } && days.drop(5).all { it == 0 }) return "Weekdays"
        if (days.take(5).all { it == 0 } && days.drop(5).all { it == 1 }) return "Weekend"

        val selected = days.mapIndexedNotNull { index, value -> if (value == 1) index else null }
        if (selected.isEmpty()) return "Once"

        val ranges = mutableListOf<Pair<Int, Int>>()
        var start = selected.first()
        var prev = start
        for (i in 1 until selected.size) {
            val cur = selected[i]
            if (cur == prev + 1) prev = cur
            else {
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

    val alarms: StateFlow<List<Alarm>> =
        alarmDao.getAll()
            .map { entities ->
                entities
                    .map { it.toAlarm() }
                    .sortedWith(compareBy<Alarm> { it.time.toMinutes() }.thenBy { it.id })
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), emptyList())

    private fun String.toMinutes(): Int {
        val (h, m) = split(":").map { it.toInt() }
        return h * 60 + m
    }

    // ---- Selection mode state ----
    private val _selectionUiState = MutableStateFlow(AlarmSelectionUiState())
    val selectionUiState = _selectionUiState.asStateFlow()

    fun onAlarmLongPress(id: Long) {
        _selectionUiState.update { state ->
            if (state.selectionMode) state.copy(selectedIds = state.selectedIds.toggle(id))
            else state.copy(selectionMode = true, selectedIds = setOf(id))
        }
    }

    fun onAlarmClick(id: Long, onEdit: (Long) -> Unit) {
        val state = _selectionUiState.value
        if (state.selectionMode) toggleSelected(id) else onEdit(id)
    }

    fun toggleSelected(id: Long) {
        _selectionUiState.update { state ->
            val newSet = state.selectedIds.toggle(id)
            if (newSet.isEmpty()) AlarmSelectionUiState()
            else state.copy(selectedIds = newSet)
        }
    }

    fun exitSelectionMode() {
        _selectionUiState.value = AlarmSelectionUiState()
    }

    fun deleteSelected() = viewModelScope.launch {
        val ids = _selectionUiState.value.selectedIds.toList()
        if (ids.isEmpty()) return@launch

        ids.forEach { scheduler.cancel(it) }
        alarmDao.deleteByIds(ids)

        exitSelectionMode()
    }

    fun toggleAlarm(id: Long, newValue: Boolean) = viewModelScope.launch {
        alarmDao.updateActive(id = id, isActive = newValue)
        if (!newValue) {
            scheduler.cancel(id)
        } else {
            val alarm = alarmDao.getById(id)
            val (h, m) = alarm.time.split(":").map { it.toInt() }
            scheduler.schedule(id, nextTriggerMillis(h, m, alarm.days))
        }
    }

    private fun Set<Long>.toggle(id: Long): Set<Long> =
        if (contains(id)) minus(id) else plus(id)
}

private fun AlarmEntity.toAlarm(): Alarm =
    Alarm(
        id = id,
        label = label,
        time = time,
        ringtone = ringtone,
        vibration = vibration,
        days = days,
        challenge = challenge,
        isActive = isActive
    )