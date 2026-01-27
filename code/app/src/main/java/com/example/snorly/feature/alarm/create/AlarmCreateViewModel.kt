package com.example.snorly.feature.alarm.create

import android.app.Application
import android.util.Log
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

    // Prevent re-loading same alarm repeatedly
    private var loadedForAlarmId: Long? = null

    fun init(alarmId: Long?) {
        if (loadedForAlarmId == alarmId) return
        loadedForAlarmId = alarmId

        if (alarmId == null) {
            _uiState.value = AlarmCreateUiState()


        } else {
            loadForEdit(alarmId)
        }
    }

    // ---- setters ----
    fun setHour(v: Int) = _uiState.update { it.copy(hour = v, saved = false) }
    fun setMinute(v: Int) = _uiState.update { it.copy(minute = v, saved = false) }
    fun setLabel(v: String) = _uiState.update { it.copy(label = v, saved = false) }
    fun setRingtone(name: String, uri: String) = _uiState.update { it.copy(ringtone = name,ringtoneUri = uri, saved = false) }
    fun setVibration(v: String) = _uiState.update { it.copy(vibration = v, saved = false) }
    fun setRepeatDays(v: List<Int>) = _uiState.update { it.copy(repeatDays = v, saved = false) }
    fun setDynamicWake(v: Boolean) = _uiState.update { it.copy(dynamicWake = v, saved = false) }
    fun setWakeUpChecker(v: Boolean) = _uiState.update { it.copy(wakeUpChecker = v, saved = false) }
    fun setEnableSnooze(v: Boolean) = _uiState.update { it.copy(enableSnooze = v, saved = false) }
    fun setSnoozeMinutes(v: Int) = _uiState.update { it.copy(snoozeMinutes = v, saved = false) }
    fun setSelectedChallenges(v: List<String>) = _uiState.update { it.copy(selectedChallenges = v, saved = false) }
    fun setChallengeEnabled(v: Boolean) = _uiState.update { it.copy(challengeEnabled = v, saved = false) }

    fun save() = viewModelScope.launch {
        val state = _uiState.value
        _uiState.update { it.copy(isSaving = true, error = null, saved = false) }

        runCatching {
            val time = "%02d:%02d".format(state.hour, state.minute)

            val entity = AlarmEntity(
                id = state.id ?: 0L,
                label = state.label,
                time = time,
                ringtone = state.ringtone,
                ringtoneUri = state.ringtoneUri,
                vibration = state.vibration,
                days = state.repeatDays,
                challenge = state.selectedChallenges,
                challengeEnabled = state.challengeEnabled,
                isActive = true,
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

    private fun loadForEdit(alarmId: Long) = viewModelScope.launch {
        _uiState.update { it.copy(isSaving = true, error = null, saved = false) }

        runCatching { alarmDao.getById(alarmId) }
            .onSuccess { alarm ->
                val (h, m) = alarm.time.split(":").map { it.toInt() }

                _uiState.update {
                    it.copy(
                        id = alarm.id,
                        hour = h,
                        minute = m,
                        label = alarm.label,
                        ringtone = alarm.ringtone,
                        ringtoneUri = alarm.ringtoneUri,
                        vibration = alarm.vibration,
                        repeatDays = alarm.days,
                        selectedChallenges = alarm.challenge,
                        challengeEnabled = alarm.challengeEnabled,
                        snoozeMinutes = alarm.snoozeMinutes,
                        // dynamicWake / wakeUpChecker are not in DB currently -> keep defaults
                        // enableSnooze is in UI state, but not in entity -> keep defaults

                        isSaving = false,
                        saved = false,
                        error = null
                    )
                }
            }
            .onFailure { ex ->
                _uiState.update { it.copy(isSaving = false, error = ex.message) }
            }
    }

    fun formatDays(days: List<Int>): String {
        if (days.size != 7) return "Once"
        if (days.all { it == 0 }) return "Once"
        if (days.all { it == 1 }) return "Daily"

        val isWeekdays = days.subList(0, 5).all { it == 1 } && days.subList(5, 7).all { it == 0 }
        if (isWeekdays) return "Weekdays"

        val isWeekend = days.subList(0, 5).all { it == 0 } && days.subList(5, 7).all { it == 1 }
        if (isWeekend) return "Weekend"

        val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val activeLabels = days.mapIndexedNotNull { index, value ->
            if (value == 1) dayLabels[index] else null
        }
        return activeLabels.joinToString(", ")
    }
}
