package com.example.snorly.feature.sleep

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.snorly.core.data.SleepRepository
import com.example.snorly.core.database.entities.SleepSessionEntity
import com.example.snorly.core.health.HealthConnectManager
import com.example.snorly.feature.sleep.model.SleepDayUiModel
import com.example.snorly.feature.sleep.model.SleepStats
import com.example.snorly.feature.sleep.util.SleepScoreUtils
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class SleepViewModel(
    private val repository: SleepRepository,
    private val healthConnectManager: HealthConnectManager,
    private val context: Context
) : ViewModel() {

    var isTracking by mutableStateOf(false)
        private set

    var trackingStartTime by mutableStateOf(-1L) // Add this
        private set

    // Use SharedPreferences to persist state "overnight"
    private val prefs: SharedPreferences = context.getSharedPreferences("sleep_tracker_prefs", Context.MODE_PRIVATE)

    val requiredPermissions = healthConnectManager.permissions

    // UI States
    var isHealthConnectAvailable by mutableStateOf(false)
        private set

    var hasPermission by mutableStateOf(false)
        private set

    var isSyncing by mutableStateOf(false)
        private set

    var sleepHistory by mutableStateOf<List<SleepDayUiModel>>(emptyList())
        private set

    var latestSleepDuration by mutableStateOf("--")
        private set
    var latestSleepScore by mutableStateOf("--")
        private set

    init {
        // 1. OBSERVE DATABASE (Single Source of Truth)
        // This runs automatically whenever the DB changes (e.g., after a Sync or Manual Add)
        repository.allSleepSessions.onEach { sessions ->

            val timeFormatter = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault())
            val dateFormatter = DateTimeFormatter.ofPattern("EEE, MMM d").withZone(ZoneId.systemDefault())

            // Map Database Entities to UI Models
            sleepHistory = sessions.map { entity ->
                // Determine a friendly label for the source
                val score = entity.sleepScore ?: 0
                val qualityLabel = if (score > 0) "$score Sleep Score" else "No Data"
                val qualityColor = SleepScoreUtils.getScoreColor(score)

                SleepDayUiModel(
                    id = entity.id.toString(),
                    dateLabel = dateFormatter.format(entity.startTime),
                    bedtime = timeFormatter.format(entity.startTime),
                    wakeup = timeFormatter.format(entity.endTime),
                    durationFormatted = formatDuration(entity.startTime, entity.endTime),
                    qualityLabel = qualityLabel,
                    qualityColor = qualityColor
                )
            }

            // HEADER LOGIC: Show Latest Night Stats (Not Average)
            val latest = sessions.firstOrNull()
            if (latest != null) {
                latestSleepDuration = formatDuration(latest.startTime, latest.endTime)
                latestSleepScore = (latest.sleepScore ?: "--").toString()
            } else {
                latestSleepDuration = "--"
                latestSleepScore = "--"
            }

        }.launchIn(viewModelScope)

        // CHECK IF WE ARE ALREADY TRACKING (Restore state after app kill)
        restoreTrackingState()

        // Check Permissions & Trigger Sync
        checkPermissionsAndSync()
    }

    fun checkPermissionsAndSync() {
        viewModelScope.launch {
            // Check Availability
            isHealthConnectAvailable = healthConnectManager.isHealthConnectAvailable()

            if (!isHealthConnectAvailable) {
                hasPermission = false
                return@launch
            }

            // Check Permissions
            hasPermission = healthConnectManager.hasAllPermissions()

            // IF GRANTED -> SYNC!
            if (hasPermission) {
                syncSleepData()
            } else {
            }
        }
    }


    // --- THE SYNC TRIGGER ---
    fun syncSleepData() {
        viewModelScope.launch {
            if (isSyncing) return@launch
            isSyncing = true

            try {
                // 1. Define Range: Last 30 Days
                val now = Instant.now()
                val start = now.minus(30, ChronoUnit.DAYS)

                // 2. Fetch raw data from Google Health Connect
                val externalRecords = healthConnectManager.readSleepSessions(start, now)

                // 3. Push to Repository to run "Smart Merge" logic
                if (externalRecords.isNotEmpty()) {
                    repository.syncWithHealthConnect(externalRecords)
                }

            } catch (e: Exception) {
                android.util.Log.e("SleepViewModel", "Sync failed: ${e.message}")
            } finally {
                isSyncing = false
            }
        }
    }

    private fun formatDuration(start: Instant, end: Instant): String {
        val duration = Duration.between(start, end)
        val hours = duration.toHours()
        val minutes = duration.toMinutes() % 60
        return "${hours}h ${minutes}m"
    }

    private fun restoreTrackingState() {
        val startTime = prefs.getLong("tracking_start_time", -1L)
        isTracking = (startTime != -1L)
        trackingStartTime = startTime
    }

    fun toggleTracking() {
        if (isTracking) {
            stopTracking()
        } else {
            startTracking()
        }
    }

    private fun startTracking() {
        val now = Instant.now().toEpochMilli()
        // Save to disk immediately
        prefs.edit().putLong("tracking_start_time", now).apply()
        trackingStartTime = now
        isTracking = true

        ContextCompat.startForegroundService(
            context,
            Intent(context, SleepTimerService::class.java).apply {
                action = SleepTimerService.ACTION_START
            }
        )
    }

    fun stopTracking() {
        viewModelScope.launch {
            val startMillis = prefs.getLong("tracking_start_time", -1L)

            if (startMillis != -1L) {
                val startInstant = Instant.ofEpochMilli(startMillis)
                val endInstant = Instant.now()

                // Create the entity
                val newSession = SleepSessionEntity(
                    startTime = startInstant,
                    endTime = endInstant,
                    sourcePackage = context.packageName, // Mark as Manual/App
                    notes = "Tracked Session"
                )

                // Save via Repository (Gatekeeper will handle validation)
                repository.saveSleepSession(newSession, isEdit = false)
            }

            // Clear the preference
            prefs.edit().remove("tracking_start_time").apply()
            trackingStartTime = -1L
            isTracking = false

            // Trigger a sync/refresh to show the new item

            context.startService(
                Intent(context, SleepTimerService::class.java).apply {
                    action = SleepTimerService.ACTION_STOP
                }
            )

            syncSleepData()
        }
    }

    class Factory(
        private val repository: SleepRepository,
        private val manager: HealthConnectManager,
        private val context: Context
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SleepViewModel(repository, manager, context) as T
        }
    }
}