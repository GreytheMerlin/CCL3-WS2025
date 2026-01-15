package com.example.snorly.feature.sleep

import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.snorly.core.data.SleepRepository
import com.example.snorly.core.database.entities.SleepSessionEntity
import com.example.snorly.core.health.HealthConnectManager
import com.example.snorly.feature.sleep.model.SleepDataProcessor
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class SleepDetailViewModel(
    private val repository: SleepRepository,
    private val healthConnectManager: HealthConnectManager, // Added this dependency back
    private val sleepId: Long,
    private val context: Context // To look up app names
) : ViewModel() {

    // State: The full local record
    var sleepRecord by mutableStateOf<SleepSessionEntity?>(null)
        private set

    // State: Formatted strings for the UI
    var formattedDate by mutableStateOf("")
    var formattedDuration by mutableStateOf("")
    var sleepQuality by mutableStateOf("")

    // NEW: Source App Display
    var sourceAppName by mutableStateOf("Snorly")
    var isEditable by mutableStateOf(true)

    var isLoading by mutableStateOf(true)

    init {
        loadRecord()
    }

    private fun loadRecord() {
        viewModelScope.launch {
            isLoading = true
            // Fetch from Local DB
            val entity = repository.getSessionById(sleepId)
            sleepRecord = entity

            if (entity != null) {
                // Format Basic Info
                val dateFmt = DateTimeFormatter.ofPattern("EEEE, MMMM d")
                formattedDate = entity.startTime.atZone(ZoneId.systemDefault()).format(dateFmt)

                val duration = Duration.between(entity.startTime, entity.endTime)
                val totalMin = duration.toMinutes()
                val h = totalMin / 60
                val m = totalMin % 60
                formattedDuration = "${h}h ${m}m"
                sleepQuality = SleepDataProcessor.calculateQuality(totalMin).first

                // 2. DETERMINE SOURCE
                // If it has a Health Connect ID, we try to find out who created it.
                if (entity.healthConnectId != null && healthConnectManager.isHealthConnectAvailable()) {
                    val hcRecord = healthConnectManager.readRecordById(entity.healthConnectId)
                    if (hcRecord != null) {
                        val packageName = hcRecord.metadata.dataOrigin.packageName
                        // If the package is NOT us, it's imported
                        if (packageName != context.packageName) {
                            sourceAppName = getAppNameFromPackage(packageName)
                            isEditable = false // Prevent editing external data
                        } else {
                            sourceAppName = "Snorly"
                            isEditable = true
                        }
                    } else {
                        // HC ID exists but we couldn't find it (maybe deleted remotely?)
                        sourceAppName = "Imported"
                        isEditable = false
                    }
                } else {
                    // Local only
                    sourceAppName = "Snorly"
                    isEditable = true
                }
            }
            isLoading = false
        }
    }

    private fun getAppNameFromPackage(packageName: String): String {
        return try {
            val pm = context.packageManager
            val info = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(info).toString()
        } catch (e: Exception) {
            packageName // Fallback to "com.google.android..." if name lookup fails
        }
    }

    fun deleteSession(onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (sleepRecord != null) {
                repository.deleteSession(sleepRecord!!)
                onSuccess()
            }
        }
    }

    class Factory(
        private val repository: SleepRepository,
        private val manager: HealthConnectManager,
        private val id: Long,
        private val context: Context
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SleepDetailViewModel(repository, manager, id, context) as T
        }
    }
}