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
            // Combine Date + Time back to Instant
            val startInstant = ZonedDateTime.of(startDate, startTime, ZoneId.systemDefault()).toInstant()
            val endInstant = ZonedDateTime.of(endDate, endTime, ZoneId.systemDefault()).toInstant()

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