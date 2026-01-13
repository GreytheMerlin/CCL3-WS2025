package com.example.snorly.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class AlarmEntity (
    @PrimaryKey(autoGenerate = true) val id: Long =0L,
    val time:String,
    val challenge: String,
    val ringtone: String,
    val vibration: String,
    val isActive: Boolean,
    val snoozeMinutes: Int,
    val days: List<Int> = emptyList()

)