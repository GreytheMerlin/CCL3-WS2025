package com.example.snorly.feature.sleep

import android.util.Log
import androidx.compose.runtime.derivedStateOf
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

    var rating by mutableIntStateOf(0) // 0 means unrated
    var notes by mutableStateOf("")

    var isEditable by mutableStateOf(true)
        private set

    var isLoading by mutableStateOf(false)

    // This combines all front-end logic errors into one reactive string
    val validationError: String? by derivedStateOf {
        val now = java.time.Instant.now()
        val startInstant = ZonedDateTime.of(startDate, startTime, ZoneId.systemDefault()).toInstant()
        val endInstant = ZonedDateTime.of(endDate, endTime, ZoneId.systemDefault()).toInstant()
        val duration = Duration.between(startInstant, endInstant)

        when {
            // 1. Future Check
            startInstant.isAfter(now) || endInstant.isAfter(now) ->
                "Time travel alert! Sleep cannot be in the future."

            // 2. Negative/Zero Duration
            !endInstant.isAfter(startInstant) ->
                "Wake up time must be after bedtime."

            // 3. Maximum Duration (24h proofing)
            duration.toHours() >= 24 ->
                "Sleep cannot exceed 24 hours. Please check your dates."

            // 4. Minimum Duration
            duration.toMinutes() < 1 ->
                "Sleep session is too short (min. 1 minute)."

            else -> null
        }
    }

    // We keep errorMessage only for BACKEND errors (like database conflicts)
    var errorMessage by mutableStateOf<String?>(null)

    // Combined property for the UI to display
    val activeErrorMessage: String?
        get() = validationError ?: errorMessage

    private var existingEntity: SleepSessionEntity? = null

    val sleepDuration: Duration
        get() = Duration.between(
            ZonedDateTime.of(startDate, startTime, ZoneId.systemDefault()).toInstant(),
            ZonedDateTime.of(endDate, endTime, ZoneId.systemDefault()).toInstant()
        )

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

                Log.d("SnorlyDebug", "Session ID: ${session.id}, Source: ${session.sourcePackage}")
                isEditable = session.sourcePackage == "com.example.snorly"
                Log.d("SnorlyDebug", "Is Editable: $isEditable")


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

        // If our reactive validation finds an error, we don't even try to save.
        val error = validationError
        if (error != null) {
            errorMessage = error // Sync the error message for the UI
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            val startInstant = ZonedDateTime.of(startDate, startTime, ZoneId.systemDefault()).toInstant()
            val endInstant = ZonedDateTime.of(endDate, endTime, ZoneId.systemDefault()).toInstant()



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