package com.example.snorly.core.data

import android.util.Log
import com.example.snorly.core.database.dao.SleepSessionDao
import com.example.snorly.core.database.entities.SleepSessionEntity
import com.example.snorly.core.health.HealthConnectManager
import androidx.health.connect.client.records.SleepSessionRecord
import com.example.snorly.feature.sleep.util.SleepScoreUtils.calculateScore
import kotlinx.coroutines.flow.Flow
import java.time.Duration
import java.time.ZoneId
import java.time.ZoneOffset

class SleepRepository(
    private val dao: SleepSessionDao,
    private val healthConnectManager: HealthConnectManager
) {

    val allSleepSessions: Flow<List<SleepSessionEntity>> = dao.getAllSleepSessions()

    suspend fun getSessionById(id: Long) = dao.getSleepSessionById(id)

    suspend fun saveSleepSession(entity: SleepSessionEntity, isEdit: Boolean) {
        val localId = if (isEdit) {
            dao.updateSleepSession(entity)
            entity.id
        } else {
            dao.insertSleepSession(entity)
        }

        if (healthConnectManager.isHealthConnectAvailable() && healthConnectManager.hasAllPermissions()) {
            try {
                if (isEdit && entity.healthConnectId != null) {
                    val record = entity.toRecord()
                    healthConnectManager.updateSleepSession(record)
                } else {
                    val hcId = healthConnectManager.writeSleepSessionReturningId(entity.startTime, entity.endTime)
                    if (hcId != null) {
                        dao.updateSleepSession(entity.copy(id = localId, healthConnectId = hcId))
                    }
                }
            } catch (e: Exception) {
                Log.e("SleepRepo", "Sync Up Failed: ${e.message}")
            }
        }
    }

    // --- THE SMART SYNC LOGIC ---
    suspend fun syncWithHealthConnect(importedRecords: List<SleepSessionRecord>) {

        importedRecords.forEach { record ->
            val hcId = record.metadata.id
            val packageName = record.metadata.dataOrigin.packageName
            val hasStages = record.stages.isNotEmpty()
            val duration = Duration.between(record.startTime, record.endTime).toMinutes()

            // 1. Is this record ALREADY in our DB?
            val existingLinked = dao.getByHealthConnectId(hcId)

            if (existingLinked != null) {
                // Yes, we already have it. Just update times if they changed remotely.
                // We keep our local ID, ratings, and notes.
                val updated = existingLinked.copy(
                    startTime = record.startTime,
                    endTime = record.endTime,
                    hasStages = hasStages
                )
                if (updated != existingLinked) dao.updateSleepSession(updated)
            } else {
                // 2. It's NEW to us. Does it conflict with any OTHER session?
                val overlaps = dao.getSessionsOverlapping(record.startTime, record.endTime)

                if (overlaps.isEmpty()) {
                    // No conflicts? Great, insert it!
                    insertNewImport(record)
                } else {
                    // CONFLICT DETECTED!
                    // We check if the new record is "better" than what we currently have.

                    var shouldReplace = true

                    for (conflict in overlaps) {
                        // Rule A: Never overwrite Snorly Manual Entries (User knows best)
                        if (conflict.sourcePackage == "com.example.snorly" || conflict.sourcePackage == null) {
                            shouldReplace = false
                            break
                        }

                        // Rule B: Prefer Detailed Stages over Simple Duration
                        if (conflict.hasStages && !hasStages) {
                            // Existing has stages, new one doesn't. Keep existing.
                            shouldReplace = false
                            break
                        }

                        // Rule C: If both have (or don't have) stages, prefer the LONGER one
                        // (Assuming longer = more complete tracking)
                        val existingDuration = Duration.between(conflict.startTime, conflict.endTime).toMinutes()
                        if (hasStages == conflict.hasStages && existingDuration >= duration) {
                            shouldReplace = false
                            break
                        }
                    }

                    if (shouldReplace) {
                        // The new record is better!
                        // Delete the inferior conflicting records and insert the new one.
                        overlaps.forEach { dao.deleteSleepSession(it) }
                        insertNewImport(record)
                        Log.d("SleepRepo", "Replaced inferior sleep record with ${packageName}")
                    } else {
                        Log.d("SleepRepo", "Ignored inferior import from ${packageName}")
                    }
                }
            }
        }
    }

    private suspend fun insertNewImport(record: SleepSessionRecord) {
        // CALCULATE SCORE HERE
        val calculatedScore = calculateScore(record)

        val newEntity = SleepSessionEntity(
            startTime = record.startTime,
            endTime = record.endTime,
            timeZoneOffset = record.startZoneOffset?.totalSeconds,
            healthConnectId = record.metadata.id,
            sourcePackage = record.metadata.dataOrigin.packageName,
            hasStages = record.stages.isNotEmpty(),
            sleepScore = calculatedScore,
            rating = null,
            notes = null
        )
        dao.insertSleepSession(newEntity)
    }

    suspend fun deleteSession(entity: SleepSessionEntity) {
        dao.deleteSleepSession(entity)
        if (entity.healthConnectId != null && healthConnectManager.isHealthConnectAvailable()) {
            healthConnectManager.deleteSleepSession(entity.healthConnectId)
        }
    }

    private fun SleepSessionEntity.toRecord(): SleepSessionRecord {
        return SleepSessionRecord(
            startTime = this.startTime,
            startZoneOffset = ZoneOffset.ofTotalSeconds(this.timeZoneOffset ?: 0),
            endTime = this.endTime,
            endZoneOffset = ZoneOffset.ofTotalSeconds(this.timeZoneOffset ?: 0),
            metadata = androidx.health.connect.client.records.metadata.Metadata(id = this.healthConnectId ?: "")
        )
    }
}