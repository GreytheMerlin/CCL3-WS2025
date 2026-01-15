package com.example.snorly.core.database.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "sleep_sessions",
    // This index makes lookups fast and prevents adding the same Health Connect ID twice
    indices = [Index(value = ["healthConnectId"], unique = true)]
)
data class SleepSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // Core Data
    val startTime: Instant,
    val endTime: Instant,
    val timeZoneOffset: Int? = null, // Storing offset (in seconds) is helpful for accurate exports

    // Metadata
    val healthConnectId: String? = null, // The link to the outside world
    val sourcePackage: String? = null,   // e.g. "com.samsung.health"
    val hasStages: Boolean = false,      // Helps us decide if this is "high quality" data

    // store the Calculated "True" Score so we dont have to calculate it every time
    val sleepScore: Int? = null, // 0 to 100

    // Snorly Specific (User Input)
    val rating: Int? = null,
    val notes: String? = null,

    val createdAt: Instant = Instant.now()
)