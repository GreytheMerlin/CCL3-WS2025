package com.example.snorly.feature.alarm.wakeup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.snorly.core.database.AlarmDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class AlarmRingingViewModel(
    private val alarmDao: AlarmDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlarmRingingUiState())
    val uiState: StateFlow<AlarmRingingUiState> = _uiState.asStateFlow()

    private val formatter = DateTimeFormatter.ofPattern("HH:mm")

    fun init(alarmId: Long) {
        startClock()
        loadAlarm(alarmId)
    }

    private fun startClock() {
        // prevent starting twice if recomposed / re-init
        if (!_uiState.value.loading && _uiState.value.timeText != "--:--") return

        viewModelScope.launch {
            while (true) {
                val now = LocalTime.now().format(formatter)
                _uiState.update { it.copy(timeText = now) }
                delay(1_000) // use 60_000 if you only want minute updates
            }
        }
    }

    private fun loadAlarm(alarmId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(loading = true, error = null) }

            runCatching { alarmDao.getById(alarmId) }
                .onSuccess { alarm ->
                    if (alarm == null) {
                        _uiState.update {
                            it.copy(
                                loading = false,
                                error = "Alarm not found",
                                snoozeMinutes = 0,
                                hasChallenge = false
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                loading = false,
                                error = null,
                                snoozeMinutes = alarm.snoozeMinutes,
                                hasChallenge = alarm.challenge.isNotEmpty()
                            )
                        }
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(loading = false, error = e.message ?: "Error")
                    }
                }
        }
    }
}
