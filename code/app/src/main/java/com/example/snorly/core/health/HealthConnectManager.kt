package com.example.snorly.core.health

import android.content.Context
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant

class HealthConnectManager(private val context: Context) {

    // This is the actual entry point to the Google API.
    // We get it efficiently using getOrCreate.
    private val healthConnectClient by lazy { HealthConnectClient.getOrCreate(context) }

    // Define Permissions
    // We need to tell the system exactly what data types we want to touch.
    val permissions = setOf(
        HealthPermission.getReadPermission(SleepSessionRecord::class),
        HealthPermission.getWritePermission(SleepSessionRecord::class)
    )

    // Check if we have permission
    // We call this before trying to read data.
    suspend fun hasAllPermissions(): Boolean {
        return healthConnectClient.permissionController.getGrantedPermissions()
            .containsAll(permissions)
    }

    //ddd Read Sleep Data
    // "suspend" means this runs in the background (Coroutines) so the UI doesn't freeze.
    // "start" and "end" define the time window we want to look at.
    suspend fun readSleepSessions(start: Instant, end: Instant): List<SleepSessionRecord> {
        return try {
            // A. Create the request
            // We ask for records of type 'SleepSessionRecord' within the time range.
            val request = ReadRecordsRequest(
                recordType = SleepSessionRecord::class,
                timeRangeFilter = TimeRangeFilter.between(start, end)
            )

            // B. Execute the request
            val response = healthConnectClient.readRecords(request)

            // C. Return the list of records found
            response.records
        } catch (e: Exception) {
            // Log errors (e.g., user revoked permission, Health Connect not installed)
            Log.e("HealthConnectManager", "Error reading sleep: ${e.message}")
            emptyList()
        }
    }
}