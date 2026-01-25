package com.example.snorly.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class AlarmEntity (
    @PrimaryKey(autoGenerate = true) val id: Long =0L,
    val label:String,
    val time:String,
    val challenge: List<String> = emptyList(),
    val ringtone: String,      // Display Name (e.g. "Morning Birds")
    val ringtoneUri: String,   // Playable URI (e.g. "content://media/internal/...")
    val vibration: String,
    val isActive: Boolean,
    val snoozeMinutes: Int,
    val days: List<Int> = emptyList()

)