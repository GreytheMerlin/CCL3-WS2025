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
import com.example.snorly.feature.sleep.model.SleepDataProcessor
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class SleepDetailViewModel(
    private val healthConnectManager: HealthConnectManager,
    private val sleepId: String,
    private val appPackageName: String
) : ViewModel() {

    // State: The full record from Google
    var sleepRecord by mutableStateOf<SleepSessionRecord?>(null)
        private set

    // State: Formatted strings for the UI
    var formattedDate by mutableStateOf("")
    var formattedDuration by mutableStateOf("")
    var sleepQuality by mutableStateOf("")

    // State for Ownership
    var isEditable by mutableStateOf(false)
    var sourceAppName by mutableStateOf("")

    // State: Loading/Error
    var isLoading by mutableStateOf(true)

    init {
        loadRecord()
    }

    private fun loadRecord() {
        viewModelScope.launch {
            isLoading = true
            // 1. Fetch from Manager (We added readRecordById earlier)
            val record = healthConnectManager.readRecordById(sleepId)
            sleepRecord = record

            if (record != null) {
                // 2. Format Basic Info
                val dateFmt = DateTimeFormatter.ofPattern("EEEE, MMMM d")
                formattedDate = record.startTime.atZone(ZoneId.systemDefault()).format(dateFmt)

                val duration = Duration.between(record.startTime, record.endTime)
                val totalMin = duration.toMinutes()
                val h = totalMin / 60
                val m = totalMin % 60
                formattedDuration = "${h}h ${m}m"

                // Calc Quality
                sleepQuality = SleepDataProcessor.calculateQuality(totalMin).first

                // 2. OWNERSHIP CHECK
                // We check if the record's creator matches our app package
                val originPackage = record.metadata.dataOrigin.packageName
                isEditable = (originPackage == appPackageName)
                // Set a readable source name (e.g., "com.google.android.apps.fitness" -> "Google Fit")
                // In a real app, you'd use PackageManager to get the readable label.
                sourceAppName = if (isEditable) "Snorly" else originPackage
            }
            isLoading = false
        }
    }

    fun deleteSession(onSuccess: () -> Unit) {

        // If Sleep data was not created by Snorly, Return
        if (!isEditable) return

        viewModelScope.launch {
            if (sleepRecord != null) {
                healthConnectManager.deleteSleepSession(sleepRecord!!.metadata.id)
                onSuccess()
            }
        }
    }

    // Factory to pass the ID
    class Factory(
        private val manager: HealthConnectManager,
        private val id: String,
        private val context: Context
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SleepDetailViewModel(manager, id, context.packageName) as T
        }
    }
}