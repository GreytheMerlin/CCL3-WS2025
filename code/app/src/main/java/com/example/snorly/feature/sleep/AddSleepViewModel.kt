package com.example.snorly.feature.sleep

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.snorly.core.health.HealthConnectManager
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class AddSleepViewModel(
    private val healthConnectManager: HealthConnectManager,
    private val editId: String? // Null = Add Mode
) : ViewModel() {

    // STATE: We store Date and Time separately for easier UI handling
    var startDate by mutableStateOf(LocalDate.now().minusDays(1)) // Default: Yesterday
    var startTime by mutableStateOf(LocalTime.of(23, 0))          // Default: 11:00 PM

    var endDate by mutableStateOf(LocalDate.now())                // Default: Today
    var endTime by mutableStateOf(LocalTime.of(7, 0))             // Default: 7:00 AM

    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    private var originalRecord: SleepSessionRecord? = null

    init {
        if (editId != null) {
            loadRecord(editId)
        }
    }

    private fun loadRecord(id: String) {
        viewModelScope.launch {
            isLoading = true
            val record = healthConnectManager.readRecordById(id)
            if (record != null) {
                originalRecord = record

                // Convert Instant to Local Zone for UI
                val startZone = record.startTime.atZone(ZoneId.systemDefault())
                val endZone = record.endTime.atZone(ZoneId.systemDefault())

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

            // Combine Date + Time back to Instant
            val startInstant = ZonedDateTime.of(startDate, startTime, ZoneId.systemDefault()).toInstant()
            val endInstant = ZonedDateTime.of(endDate, endTime, ZoneId.systemDefault()).toInstant()

            // Valdiate entry

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
            if (duration.toHours() > 24) {
                errorMessage = "Sleep cannot be longer than 24 hours."
                isLoading = false
                return@launch
            }

            // 3. OVERLAP CHECK (Fixes "Messy Data")
            // Query for any sleep records in this exact window
            val existingRecords = healthConnectManager.readSleepSessions(startInstant, endInstant)

            // If we are editing, we expect to find OURSELVES in the list. That's fine.
            // But if we find *other* records, that's an overlap.
            val hasOverlap = existingRecords.any {
                // If in edit mode, ignore the record with our own ID
                it.metadata.id != (originalRecord?.metadata?.id ?: "")
            }

            if (hasOverlap) {
                errorMessage = "You already have a sleep entry during this time."
                isLoading = false
                return@launch
            }

            if (editId != null && originalRecord != null) {
                // EDIT MODE: Create copy of original with new times
                val updatedRecord = SleepSessionRecord(
                    startTime = startInstant,
                    startZoneOffset = ZoneId.systemDefault().rules.getOffset(startInstant),
                    endTime = endInstant,
                    endZoneOffset = ZoneId.systemDefault().rules.getOffset(endInstant),
                    stages = originalRecord!!.stages, // Keep existing stages if any
                    metadata = originalRecord!!.metadata // Keep ID!!
                )
                healthConnectManager.updateSleepSession(updatedRecord)
            } else {
                // ADD MODE
                healthConnectManager.writeSleepSession(startInstant, endInstant)
            }
            onSuccess()
        }
    }

    class Factory(
        private val manager: HealthConnectManager,
        private val editId: String?
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AddSleepViewModel(manager, editId) as T
        }
    }
}