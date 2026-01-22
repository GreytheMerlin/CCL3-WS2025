package com.example.snorly.feature.sleep

import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.health.connect.client.records.SleepSessionRecord
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

    var sleepStages by mutableStateOf<List<SleepSessionRecord.Stage>>(emptyList())
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

    fun loadRecord() {
        viewModelScope.launch {
            isLoading = true

            // 1. Fetch Local Summary
            val entity = repository.getSessionById(sleepId)
            sleepRecord = entity

            if (entity != null) {
                // Formatting
                val dateFmt = DateTimeFormatter.ofPattern("EEEE, MMMM d")
                formattedDate = entity.startTime.atZone(ZoneId.systemDefault()).format(dateFmt)

                val duration = Duration.between(entity.startTime, entity.endTime)
                val totalMin = duration.toMinutes()
                val h = totalMin / 60
                val m = totalMin % 60
                formattedDuration = "${h}h ${m}m"
                sleepQuality = SleepDataProcessor.calculateQuality(totalMin).first

                // 2. FETCH DETAILED STAGES (If linked to Health Connect)
                if (entity.healthConnectId != null && healthConnectManager.isHealthConnectAvailable()) {
                    val hcRecord = healthConnectManager.readRecordById(entity.healthConnectId)

                    if (hcRecord != null) {
                        // CAPTURE STAGES
                        sleepStages = hcRecord.stages

                        // Determine Source App
                        val packageName = hcRecord.metadata.dataOrigin.packageName
                        if (packageName != context.packageName) {
                            sourceAppName = getAppNameFromPackage(context, packageName)
                            isEditable = false
                        } else {
                            sourceAppName = "Snorly"
                            isEditable = true
                        }
                    } else {
                        // HC ID exists but record missing (deleted remotely?)
                        sourceAppName = "Imported (Missing)"
                        isEditable = false
                        sleepStages = emptyList()
                    }
                } else {
                    // Local only
                    sourceAppName = "Snorly"
                    isEditable = true
                    sleepStages = emptyList()
                }
            }
            isLoading = false
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

fun getAppNameFromPackage(
    context: Context,
    packageName: String
): String {
    return try {
        val pm = context.packageManager
        val appInfo = pm.getApplicationInfo(packageName, 0)
        pm.getApplicationLabel(appInfo).toString()
    } catch (e: PackageManager.NameNotFoundException) {
        // Fallback if the app is no longer installed
        packageName
    }
}
