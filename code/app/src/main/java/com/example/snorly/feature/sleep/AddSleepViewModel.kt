package com.example.snorly.feature.sleep

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.snorly.core.data.SleepRepository
import com.example.snorly.core.database.entities.SleepSessionEntity
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class AddSleepViewModel(
    private val repository: SleepRepository,
    private val editId: Long?
) : ViewModel() {

    var startDate by mutableStateOf(LocalDate.now().minusDays(1))
    var startTime by mutableStateOf(LocalTime.of(23, 0))
    var endDate by mutableStateOf(LocalDate.now())
    var endTime by mutableStateOf(LocalTime.of(7, 0))

    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    // We store the full entity if we are in Edit Mode
    private var existingEntity: SleepSessionEntity? = null

    init {
        if (editId != null && editId != -1L) {
            loadRecord(editId)
        }
    }

    private fun loadRecord(id: Long) {
        viewModelScope.launch {
            isLoading = true
            val session = repository.getSessionById(id)
            if (session != null) {
                existingEntity = session

                val startZone = session.startTime.atZone(ZoneId.systemDefault())
                val endZone = session.endTime.atZone(ZoneId.systemDefault())

                startDate = startZone.toLocalDate()
                startTime = startZone.toLocalTime()
                endDate = endZone.toLocalDate()
                endTime = endZone.toLocalTime()
            }
            isLoading = false
        }
    }

    fun saveSleep(onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            val startInstant = ZonedDateTime.of(startDate, startTime, ZoneId.systemDefault()).toInstant()
            val endInstant = ZonedDateTime.of(endDate, endTime, ZoneId.systemDefault()).toInstant()

            // --- Validation ---
            if (!endInstant.isAfter(startInstant)) {
                errorMessage = "Wake up time must be after bedtime."
                isLoading = false
                return@launch
            }
            val duration = Duration.between(startInstant, endInstant)
            if (duration.toMinutes() < 1) {
                errorMessage = "Sleep must be at least 1 minute."
                isLoading = false
                return@launch
            }

            // --- Construct Entity ---
            // If editing, preserve ID and HealthConnectID. If new, ID is 0 (auto-gen).
            val entityToSave = existingEntity?.copy(
                startTime = startInstant,
                endTime = endInstant
                // ratings/notes would go here
            ) ?: SleepSessionEntity(
                startTime = startInstant,
                endTime = endInstant
            )

            // --- Save via Repository ---
            repository.saveSleepSession(
                entity = entityToSave,
                isEdit = existingEntity != null
            )

            isLoading = false
            onSuccess()
        }
    }

    class Factory(
        private val repository: SleepRepository,
        private val editId: Long?
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AddSleepViewModel(repository, editId) as T
        }
    }
}