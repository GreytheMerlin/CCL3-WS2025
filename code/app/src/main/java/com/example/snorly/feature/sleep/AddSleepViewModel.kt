package com.example.snorly.feature.sleep

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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

    // Time States
    var startDate by mutableStateOf(LocalDate.now().minusDays(1))
    var startTime by mutableStateOf(LocalTime.of(23, 0))
    var endDate by mutableStateOf(LocalDate.now())
    var endTime by mutableStateOf(LocalTime.of(7, 0))

    // New Fields
    var rating by mutableIntStateOf(0) // 0 means unrated
    var notes by mutableStateOf("")

    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

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

                // Load optional fields
                rating = session.rating ?: 0
                notes = session.notes ?: ""
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

            // 1. Logic Validation
            if (!endInstant.isAfter(startInstant)) {
                errorMessage = "Wake up time must be after bedtime."
                isLoading = false
                return@launch
            }
            if (Duration.between(startInstant, endInstant).toMinutes() < 1) {
                errorMessage = "Sleep must be at least 1 minute."
                isLoading = false
                return@launch
            }

            // 2. Prepare Entity
            val entityToSave = existingEntity?.copy(
                startTime = startInstant,
                endTime = endInstant,
                rating = if (rating > 0) rating else null,
                notes = if (notes.isNotBlank()) notes else null,
                sourcePackage = "com.example.snorly" // Mark as OURS
            ) ?: SleepSessionEntity(
                startTime = startInstant,
                endTime = endInstant,
                rating = if (rating > 0) rating else null,
                notes = if (notes.isNotBlank()) notes else null,
                sourcePackage = "com.example.snorly" // Mark as OURS
            )

            // 3. Save with Result Check
            val result = repository.saveSleepSession(
                entity = entityToSave,
                isEdit = existingEntity != null
            )

            result.fold(
                onSuccess = {
                    isLoading = false
                    onSuccess()
                },
                onFailure = { exception ->
                    isLoading = false
                    // Show the specific overlap message from Repository
                    errorMessage = exception.message ?: "Failed to save sleep."
                }
            )
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