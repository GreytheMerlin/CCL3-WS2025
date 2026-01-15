package com.example.snorly.feature.alarm.create

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.snorly.core.database.AppDatabase
import com.example.snorly.core.database.entities.AlarmEntity
import com.example.snorly.feature.alarm.wakeup.AlarmScheduler
import com.example.snorly.feature.alarm.wakeup.nextTriggerMillis
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AlarmCreateViewModel(application: Application) : AndroidViewModel(application) {

    private val alarmDao = AppDatabase.getDatabase(application).alarmDao()
    private val scheduler = AlarmScheduler(getApplication())

    private val _uiState = MutableStateFlow(AlarmCreateUiState())
    val uiState = _uiState.asStateFlow()

    // ---- setters ----
    fun setHour(v: Int) = _uiState.update { it.copy(hour = v, saved = false) }
    fun setMinute(v: Int) = _uiState.update { it.copy(minute = v, saved = false) }
    fun setLabel(v: String) = _uiState.update { it.copy(label = v, saved = false) }
    fun setRingtone(v: String) = _uiState.update { it.copy(ringtone = v, saved = false) }
    fun setVibration(v: String) = _uiState.update { it.copy(vibration = v, saved = false) }
    fun setRepeatDays(v: List<Int>) = _uiState.update { it.copy(repeatDays = v, saved = false) }
    fun setDynamicWake(v: Boolean) = _uiState.update { it.copy(dynamicWake = v, saved = false) }
    fun setWakeUpChecker(v: Boolean) = _uiState.update { it.copy(wakeUpChecker = v, saved = false) }
    fun setEnableSnooze(v: Boolean) = _uiState.update { it.copy(enableSnooze = v, saved = false) }
    fun setSnoozeMinutes(v: Int) = _uiState.update { it.copy(snoozeMinutes = v, saved = false) }
    fun setSelectedChallenges(v: List<String>) = _uiState.update { it.copy(selectedChallenges = v, saved = false) }

    /**
     * MVVM create:
     * - build AlarmEntity from uiState
     * - insert in DB
     * - schedule alarm
     * - set saved=true so UI can navigate back
     */
    suspend fun getAlarmById(id: Long): AlarmEntity = alarmDao.getById(id)

    fun save() = viewModelScope.launch {
        val state = _uiState.value
        _uiState.update { it.copy(isSaving = true, error = null, saved = false) }

        runCatching {
            val time = "%02d:%02d".format(state.hour, state.minute)

            val entity = AlarmEntity(
                id = state.id ?: 0L,              // ✅ if null -> create, else update
                time = time,
                ringtone = state.ringtone,
                vibration = state.vibration,
                days = state.repeatDays,
                challenge = state.selectedChallenges,
                isActive = true,                  // or keep existing if you store it in uiState
                snoozeMinutes = state.snoozeMinutes
            )

            val alarmId: Long =
                if (state.id == null) {
                    // CREATE
                    alarmDao.addAlarm(entity)
                } else {
                    // UPDATE
                    alarmDao.updateAlarm(entity)
                    state.id
                }!!

            // reschedule: cancel old then schedule new
            scheduler.cancel(alarmId)

            val triggerAt = nextTriggerMillis(
                hour = state.hour,
                minute = state.minute,
                days = state.repeatDays
            )
            scheduler.schedule(alarmId, triggerAt)
        }.onSuccess {
            _uiState.update { it.copy(isSaving = false, saved = true) }
        }.onFailure { ex ->
            _uiState.update { it.copy(isSaving = false, error = ex.message) }
        }
    }


    /**
     * Optional helper: allows your UI to pass an AlarmEntity directly
     * (useful if you still want onCreateAlarm(entity) style).
     */
    fun insert(alarm: AlarmEntity) = viewModelScope.launch {
        runCatching {
            val alarmId = alarmDao.addAlarm(alarm)
            val (hour, minute) = alarm.time.split(":").map { it.toInt() }

            val triggerAt = nextTriggerMillis(
                hour = hour,
                minute = minute,
                days = alarm.days
            )

            scheduler.schedule(alarmId, triggerAt)
        }.onFailure { ex ->
            _uiState.update { it.copy(error = ex.message) }
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
    fun loadForEdit(alarmId: Long) = viewModelScope.launch {
        _uiState.update { it.copy(error = null, saved = false) }

        runCatching {
            alarmDao.getById(alarmId)
        }.onSuccess { alarm ->
            val (h, m) = alarm.time.split(":").map { it.toInt() }

            _uiState.update {
                it.copy(
                    id = alarm.id,
                    hour = h,
                    minute = m,
                    ringtone = alarm.ringtone,
                    vibration = alarm.vibration,
                    repeatDays = alarm.days,
                    selectedChallenges = alarm.challenge,
                    snoozeMinutes = alarm.snoozeMinutes,
                    // keep isSaving/saved/error clean
                    isSaving = false,
                    saved = false,
                    error = null
                )
            }
        }.onFailure { ex ->
            _uiState.update { it.copy(error = ex.message) }
        }
    }
}
